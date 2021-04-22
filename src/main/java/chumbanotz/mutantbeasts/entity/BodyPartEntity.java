package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.item.MBItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class BodyPartEntity extends Entity {
	private static final DataParameter<Byte> PART = EntityDataManager.createKey(BodyPartEntity.class, DataSerializers.BYTE);
	private final boolean yawPositive;
	private final boolean pitchPositive;
	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private int despawnTimer;

	public BodyPartEntity(EntityType<? extends BodyPartEntity> type, World world) {
		super(type, world);
		this.prevRotationYaw = this.rotationYaw = this.rand.nextFloat() * 360.0F;
		this.prevRotationPitch = this.rotationPitch = this.rand.nextFloat() * 360.0F;
		this.yawPositive = this.rand.nextBoolean();
		this.pitchPositive = this.rand.nextBoolean();
	}

	public BodyPartEntity(World world, MobEntity owner, int part) {
		this(MBEntityType.BODY_PART, world);
		this.setPart(part);
		this.setPosition(owner.posX, owner.posY + (double)(3.2F * (0.25F + this.rand.nextFloat() * 0.5F)), owner.posZ);
		this.setFireTimer(owner.getFireTimer());
	}

	public BodyPartEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.BODY_PART, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(PART, (byte)0);
	}

	public int getPart() {
		return this.dataManager.get(PART);
	}

	private void setPart(int part) {
		this.dataManager.set(PART, (byte)part);
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return new ItemStack(this.getItemByPart());
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive();
	}

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		this.setPosition(x, y, z);
		this.setMotion(this.velocityX, this.velocityY, this.velocityZ);
	}

	@Override
	public void setVelocity(double x, double y, double z) {
		this.velocityX = x;
		this.velocityY = y;
		this.velocityZ = z;
		this.setMotion(this.velocityX, this.velocityY, this.velocityZ);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.hasNoGravity()) {
			this.setMotion(this.getMotion().subtract(0.0D, 0.045D, 0.0D));
		}

		this.move(MoverType.SELF, this.getMotion());
		this.setMotion(this.getMotion().scale(0.96D));

		if (this.onGround) {
			this.setMotion(this.getMotion().scale(0.7D));
		}

		if (!this.onGround && this.motionMultiplier == Vec3d.ZERO) {
			this.rotationYaw += 10.0F * (float)(this.yawPositive ? 1 : -1);
			this.rotationPitch += 15.0F * (float)(this.pitchPositive ? 1 : -1);
			for (Entity entity : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), this::canHarm)) {
				entity.setFire(this.getFireTimer() / 20);
				entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this), 4.0F + (float)this.rand.nextInt(4));
			}
		}

		if (!this.world.isRemote && ++this.despawnTimer >= 6000) {
			this.remove();
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (this.isAlive()) {
			if (!this.world.isRemote) {
				if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
					this.entityDropItem(this.getItemByPart()).setNoPickupDelay();
				}

				this.remove();
			}

			player.swingArm(hand);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	private boolean canHarm(Entity entity) {
		return entity.canBeCollidedWith() && entity.getType() != MBEntityType.MUTANT_SKELETON;
	}

	@Override
	public ITextComponent getName() {
		return this.hasCustomName() ? super.getName() : new TranslationTextComponent(this.getItemByPart().getTranslationKey());
	}

	public Item getItemByPart() {
		int part = this.getPart();
		if (part == 0) {
			return MBItems.MUTANT_SKELETON_PELVIS;
		}

		if (part >= 1 && part < 19) {
			return MBItems.MUTANT_SKELETON_RIB;
		}

		if (part == 19) {
			return MBItems.MUTANT_SKELETON_SKULL;
		}

		if (part >= 21 && part < 29) {
			return MBItems.MUTANT_SKELETON_LIMB;
		}

		if (part == 29 || part == 30) {
			return MBItems.MUTANT_SKELETON_SHOULDER_PAD;
		}

		return MBItems.MUTANT_SKELETON_PELVIS;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putByte("Part", (byte)this.getPart());
		compound.putShort("DespawnTimer", (short)this.despawnTimer);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.setPart(compound.getByte("Part"));
		this.despawnTimer = compound.getShort("DespawnTimer");
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}