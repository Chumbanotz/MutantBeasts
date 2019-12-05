package chumbanotz.mutantbeasts.entity;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.item.MBItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class BodyPartEntity extends Entity {
	private static final DataParameter<String> OWNER_TYPE = EntityDataManager.createKey(BodyPartEntity.class, DataSerializers.STRING);
	private static final DataParameter<Byte> PART = EntityDataManager.createKey(BodyPartEntity.class, DataSerializers.BYTE);
	private boolean yawPositive;
	private boolean pitchPositive;
	@Nullable
	private MobEntity owner;
	@OnlyIn(Dist.CLIENT)
	private double velocityX;
	@OnlyIn(Dist.CLIENT)
	private double velocityY;
	@OnlyIn(Dist.CLIENT)
	private double velocityZ;
	private boolean hurtsEntities = true;

	public BodyPartEntity(EntityType<? extends BodyPartEntity> type, World world) {
		super(type, world);
		this.prevRotationYaw = this.rotationYaw = this.rand.nextFloat() * 360.0F;
		this.prevRotationPitch = this.rotationPitch = this.rand.nextFloat() * 360.0F;
		this.yawPositive = this.rand.nextBoolean();
		this.pitchPositive = this.rand.nextBoolean();
	}

	public BodyPartEntity(World world, MobEntity owner, int bodyPart) {
		this(MBEntityType.BODY_PART, world);
		this.owner = owner;
		this.setOwnerType(owner.getType().getRegistryName().getPath());
		this.setPosition(owner.posX, owner.posY + (double)(3.2F * (0.25F + this.rand.nextFloat() * 0.5F)), owner.posZ);
		this.setPart(bodyPart);
	}

	public BodyPartEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.BODY_PART, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(OWNER_TYPE, "mutant_skeleton");
		this.dataManager.register(PART, (byte)0);
	}

	public String getOwnerType() {
		return this.dataManager.get(OWNER_TYPE);
	}

	private void setOwnerType(String ownerType) {
		if (ownerType.isEmpty()) {
			ownerType = "mutant_skeleton";
		}

		this.dataManager.set(OWNER_TYPE, ownerType);
	}

	public int getPart() {
		return this.dataManager.get(PART);
	}

	private void setPart(int partId) {
		this.dataManager.set(PART, (byte)partId);
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return this.getItemStackByPart();
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
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		this.setPosition(x, y, z);
		this.setMotion(this.velocityX, this.velocityY, this.velocityZ);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.velocityX = x;
		this.velocityY = y;
		this.velocityZ = z;
		this.setMotion(this.velocityX, this.velocityY, this.velocityZ);
	}

	protected void move() {
		if (this.getRidingEntity() == null) {
			this.setMotion(this.getMotion().subtract(0.0D, 0.045D, 0.0D));
			this.move(MoverType.SELF, this.getMotion());
			Vec3d vec3d = this.getMotion();
			this.setMotion(vec3d.scale(0.96D));

			if (this.onGround) {
				this.setMotion(vec3d.scale(0.7D));
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.move();

		if (!this.onGround && this.motionMultiplier.length() == 0.0D) {
			this.markVelocityChanged();
			this.rotationYaw += 10.0F * (float)(this.yawPositive ? 1 : -1);
			this.rotationPitch += 15.0F * (float)(this.pitchPositive ? 1 : -1);

			if (this.hurtsEntities) {
				this.damageEntities();
			}
		}
	}

	private void damageEntities() {
		for (LivingEntity entity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(4.0D), entity -> entity.getEntityString() != this.getOwnerType())) {
			DamageSource source = DamageSource.DRYOUT;

			if (this.owner != null) {
				source = DamageSource.causeMobDamage(this.owner);
			}

			if (this.isBurning()) {
				entity.setFireTimer(this.getFireTimer());
			}

			entity.attackEntityFrom(source.setDifficultyScaled(), 4.0F + (float)this.rand.nextInt(4));
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (!this.getItemStackByPart().isEmpty()) {
			player.swingArm(hand);
			if (!this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
				ItemEntity item = new ItemEntity(this.world, this.posX, this.posY, this.posZ, this.getItemStackByPart());
				item.setNoPickupDelay();
				this.world.addEntity(item);
			}

			this.remove();
			return true;
		}

		return false;
	}

	@Override
	public boolean hitByEntity(Entity entityIn) {
		return true;
	}

	private ItemStack getItemStackByPart() {
		int part = this.getPart();
		switch (this.getOwnerType()) {
		case "mutant_skeleton":
			if (part == 0) {
				return new ItemStack(MBItems.MUTANT_SKELETON_PELVIS);
			}

			if (part >= 1 && part < 19) {
				return new ItemStack(MBItems.MUTANT_SKELETON_RIB);
			}

			if (part == 19) {
				return new ItemStack(MBItems.MUTANT_SKELETON_SKULL);
			}

			if (part >= 21 && part < 29) {
				return new ItemStack(MBItems.MUTANT_SKELETON_LIMB);
			}

			if (part == 29 || part == 30) {
				return new ItemStack(MBItems.MUTANT_SKELETON_SHOULDER_PAD);
			}
		default:
			return ItemStack.EMPTY;
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putString("OwnerType", this.getOwnerType());
		compound.putByte("Part", (byte)this.getPart());
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.setOwnerType(compound.getString("OwnerType"));
		this.setPart(compound.getByte("Part"));
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}