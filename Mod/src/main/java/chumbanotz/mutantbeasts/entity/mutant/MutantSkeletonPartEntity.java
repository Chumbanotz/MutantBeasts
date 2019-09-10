package chumbanotz.mutantbeasts.entity.mutant;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class MutantSkeletonPartEntity extends Entity {
	private static final DataParameter<Byte> PART = EntityDataManager.createKey(MutantSkeletonPartEntity.class, DataSerializers.BYTE);
	public float prevRotationYaw;
	public float prevRotationPitch;
	public float rotationYaw;
	public float rotationPitch;
	private boolean yawPositive;
	private boolean pitchPositive;
	private MutantSkeletonEntity owner;
	@OnlyIn(Dist.CLIENT)
	private double velocityX;
	@OnlyIn(Dist.CLIENT)
	private double velocityY;
	@OnlyIn(Dist.CLIENT)
	private double velocityZ;

	public MutantSkeletonPartEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		super(MBEntityType.MUTANT_SKELETON_PART, world);
	}

	public MutantSkeletonPartEntity(World world, MutantSkeletonEntity skeleton, int bodyPart) {
		super(MBEntityType.MUTANT_SKELETON_PART, world);
		this.prevRotationYaw = this.rotationYaw = this.rand.nextFloat() * 360.0F;
		this.prevRotationPitch = this.rotationPitch = this.rand.nextFloat() * 360.0F;
		this.ignoreFrustumCheck = true;
		this.yawPositive = this.rand.nextBoolean();
		this.pitchPositive = this.rand.nextBoolean();
		this.owner = skeleton;
		this.setPosition(skeleton.posX, skeleton.posY + (double)(skeleton.getHeight() * (0.25F + this.rand.nextFloat() * 0.5F)), skeleton.posZ);
		this.setBodyPart(bodyPart);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(PART, (byte)1);
	}

	public int getBodyPart() {
		return this.dataManager.get(PART);
	}

	public void setBodyPart(int i) {
		this.dataManager.set(PART, (byte)i);
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
	public boolean canBePushed() {
		return true;
	}

	@Override
	public double getYOffset() {
		return 0.16D;
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
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
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
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.move();

		if (!this.onGround && this.motionMultiplier == Vec3d.ZERO) {
			this.rotationYaw += 10.0F * (float)(this.yawPositive ? 1 : -1);
			this.rotationPitch += 15.0F * (float)(this.pitchPositive ? 1 : -1);

			for (Entity entity : EntityUtil.getCollidingEntities(this, this.world, this.getBoundingBox())) {
				if (entity instanceof LivingEntity && !(entity instanceof MutantSkeletonEntity)) {
					DamageSource source = DamageSource.GENERIC;

					if (this.owner != null) {
						source = DamageSource.causeMobDamage(this.owner);
					}

					entity.attackEntityFrom(source.setDifficultyScaled(), 4.0F + (float)this.rand.nextInt(4));
				}
			}
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (!this.world.isRemote) {
			int bodyPart = this.getBodyPart();
			// ItemStack stack = null;

			if (bodyPart == 0) {
				// stack = new ItemStack(MutantCreatures.skeletonPart, 1, 2);
			}

			if (bodyPart >= 1 && bodyPart < 19) {
				// stack = new ItemStack(MutantCreatures.skeletonPart, 1, 1);
			}

			if (bodyPart == 19) {
				// stack = new ItemStack(MutantCreatures.skeleArmorHead);
			}

			if (bodyPart >= 21 && bodyPart < 29) {
				// stack = new ItemStack(MutantCreatures.skeletonPart, 1, 0);
			}

			if (bodyPart == 29 || bodyPart == 30) {
				// stack = new ItemStack(MutantCreatures.skeletonPart, 1, 3);
			}

			// ItemEntity item = new ItemEntity(this.world, this.posX, this.posY, this.posZ,
			// stack);
			// item.setNoPickupDelay();
			// this.world.addEntity(item);
		}

		this.remove();
		return true;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.putShort("BodyPart", (short)this.getBodyPart());

		if (compound.contains("Rotation")) {
			compound.put("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		this.setBodyPart(compound.getShort("BodyPart"));
		ListNBT listnbt = compound.getList("Rotation", 5);
		this.rotationYaw = listnbt.getFloat(0);
		this.rotationPitch = listnbt.getFloat(1);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}