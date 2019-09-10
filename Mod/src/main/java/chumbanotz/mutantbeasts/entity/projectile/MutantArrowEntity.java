package chumbanotz.mutantbeasts.entity.projectile;

import java.util.ArrayList;
import java.util.List;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class MutantArrowEntity extends AbstractArrowEntity {
	private static final DataParameter<Integer> TARGET_X = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TARGET_Y = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TARGET_Z = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Float> SPEED = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> CLONES = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private int damage;
	private final List<Entity> pointedEntities = new ArrayList<>();
	private EffectInstance potionEffect;

	public MutantArrowEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		super(MBEntityType.MUTANT_SKELETON_ARROW, world);
	}

	public MutantArrowEntity(World world, LivingEntity shooter, LivingEntity target) {
		super(MBEntityType.MUTANT_SKELETON_ARROW, world);
		this.damage = 10 + this.rand.nextInt(3);
		this.ignoreFrustumCheck = true;
		this.noClip = true;
		this.setShooter(shooter);

		if (!world.isRemote) {
			this.setTargetX(target.posX);
			this.setTargetY(target.posY);
			this.setTargetZ(target.posZ);
		}

		double yPos = shooter.posY + (double)shooter.getEyeHeight();

		if (shooter instanceof MutantSkeletonEntity) {
			yPos = shooter.posY + (double)(shooter.getHeight() * 0.4F);
		}

		this.setPosition(shooter.posX, yPos, shooter.posZ);
		double x = this.getTargetX() - this.posX;
		double y = this.getTargetY() - this.posY;
		double z = this.getTargetZ() - this.posZ;
		double d = Math.sqrt(x * x + z * z);
		this.rotationYaw = 180.0F + (float)Math.toDegrees(Math.atan2(x, z));
		this.rotationPitch = (float)Math.toDegrees(Math.atan2(y, d));
	}

	@Override
	protected void registerData() {
		this.dataManager.register(TARGET_X, 0);
		this.dataManager.register(TARGET_Y, 0);
		this.dataManager.register(TARGET_Z, 0);
		this.dataManager.register(SPEED, 12.0F);
		this.dataManager.register(CLONES, 10);
	}

	public void setTargetX(double d) {
		this.dataManager.set(TARGET_X, (int)(d * 10000.0D));
	}

	public void setTargetY(double d) {
		this.dataManager.set(TARGET_Y, (int)(d * 10000.0D));
	}

	public void setTargetZ(double d) {
		this.dataManager.set(TARGET_Z, (int)(d * 10000.0D));
	}

	public void setSpeed(float f) {
		this.dataManager.set(SPEED, f * 10.0F);
	}

	public void setClones(int i) {
		this.dataManager.set(CLONES, i);
	}

	public double getTargetX() {
		return (double)this.dataManager.get(TARGET_X) / 10000.0D;
	}

	public double getTargetY() {
		return (double)this.dataManager.get(TARGET_Y) / 10000.0D;
	}

	public double getTargetZ() {
		return (double)this.dataManager.get(TARGET_Z) / 10000.0D;
	}

	public float getSpeed() {
		return this.dataManager.get(SPEED) / 10.0F;
	}

	public int getClones() {
		return this.dataManager.get(CLONES);
	}

	public void randomize(float scale) {
		this.setTargetX(this.getTargetX() + (double)((this.rand.nextFloat() - 0.5F) * scale * 2.0F));
		this.setTargetY(this.getTargetY() + (double)((this.rand.nextFloat() - 0.5F) * scale * 2.0F));
		this.setTargetZ(this.getTargetZ() + (double)((this.rand.nextFloat() - 0.5F) * scale * 2.0F));
	}

	public void setDamage(int i) {
		this.damage = i;
	}

	public void setPotionEffect(EffectInstance effect) {
		this.potionEffect = effect;
	}

	@Override
	public void tick() {
		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.baseTick();
		double x = this.getTargetX() - this.posX;
		double y = this.getTargetY() - this.posY;
		double z = this.getTargetZ() - this.posZ;
		double d = Math.sqrt(x * x + z * z);
		this.rotationYaw = 180.0F + (float)Math.toDegrees(Math.atan2(x, z));

		if (this.rotationYaw > 360.0F) {
			this.rotationYaw -= 360.0F;
		}

		this.rotationPitch = (float)Math.toDegrees(Math.atan2(y, d));

		if (!this.world.isRemote) {
			if (this.ticksExisted == 2) {
				this.hitEntities(0);
			}

			if (this.ticksExisted == 3) {
				this.hitEntities(32);
			}

			if (this.ticksExisted == 4) {
				this.handleEntities();
			}
		}

		if (this.ticksExisted > 10) {
			this.remove();
		}
	}

	protected void hitEntities(int offset) {
		double targetX = this.getTargetX();
		double targetY = this.getTargetY();
		double targetZ = this.getTargetZ();
		double d3 = this.posX - targetX;
		double d4 = this.posY - targetY;
		double d5 = this.posZ - targetZ;
		double dist = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
		double dx = (targetX - this.posX) / dist;
		double dy = (targetY - this.posY) / dist;
		double dz = (targetZ - this.posZ) / dist;

		for (int i = offset; i < offset + 200; ++i) {
			double x = this.posX + dx * (double)i * 0.5D;
			double y = this.posY + dy * (double)i * 0.5D;
			double z = this.posZ + dz * (double)i * 0.5D;
			AxisAlignedBB box = new AxisAlignedBB(x, y, z, x, y, z).grow(0.3D);
			this.pointedEntities.addAll(EntityUtil.getCollidingEntities(this.getShooter(), this.world, box));
		}
	}

	protected void handleEntities() {
		this.pointedEntities.remove(this.getShooter());
		this.pointedEntities.remove(this);

		for (Entity entity : this.pointedEntities) {
			if (entity.attackEntityFrom(DamageSource.causeArrowDamage(this, this.getShooter()).setDifficultyScaled(), (float)this.damage)) {
				this.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ITEM_CROSSBOW_HIT, this.getSoundCategory(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			}

			if (this.potionEffect != null && entity instanceof LivingEntity) {
				((LivingEntity)entity).addPotionEffect(this.potionEffect);
			}
		}

		this.pointedEntities.clear();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return this.getBoundingBox().grow(200.0D);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.setMotion(x, y, z);
	}

	@Override
	public void shoot(Entity shooter, float pitch, float yaw, float p_184547_4_, float velocity, float inaccuracy) {
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
	}

	@Override
	public void onCollideWithPlayer(PlayerEntity entityIn) {
	}

	@Override
	protected void onHit(RayTraceResult raytraceResultIn) {
	}

	@Override
	protected void func_213868_a(EntityRayTraceResult p_213868_1_) {
	}

	@Override
	public boolean getIsCritical() {
		return false;
	}

	@Override
	public void setIsCritical(boolean critical) {
	}

	@Override
	public boolean func_213873_r() {
		return false;
	}

	@Override
	public byte getPierceLevel() {
		return Byte.MAX_VALUE;
	}

	@Override
	public void setPierceLevel(byte level) {
	}

	@Override
	public void func_203045_n(boolean p_203045_1_) {
	}

	@Override
	public boolean func_203047_q() {
		return false;
	}

	@Override
	public void func_213865_o(boolean p_213865_1_) {
	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		if (this.shootingEntity != null) {
			compound.putUniqueId("OwnerUUID", this.shootingEntity);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		if (compound.hasUniqueId("OwnerUUID")) {
			this.shootingEntity = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}