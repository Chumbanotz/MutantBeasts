package chumbanotz.mutantbeasts.entity.projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.IndirectDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class MutantArrowEntity extends Entity {
	private static final DataParameter<Integer> TARGET_X = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TARGET_Y = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TARGET_Z = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Float> SPEED = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> CLONES = EntityDataManager.createKey(MutantArrowEntity.class, DataSerializers.VARINT);
	private int damage = 10 + this.rand.nextInt(3);
	private final List<Entity> pointedEntities = new ArrayList<>();
	private EffectInstance potionEffect;
	private UUID shootingEntity;

	public MutantArrowEntity(EntityType<? extends MutantArrowEntity> type, World world) {
		super(type, world);
		this.noClip = true;
	}

	public MutantArrowEntity(World world, LivingEntity shooter, LivingEntity target) {
		this(MBEntityType.MUTANT_ARROW, world);
		this.shootingEntity = shooter.getUniqueID();

		if (!world.isRemote) {
			this.setTargetX(target.posX);
			this.setTargetY(target.posY);
			this.setTargetZ(target.posZ);
		}

		double yPos = shooter.posY + (double)shooter.getEyeHeight();

		if (shooter instanceof MutantSkeletonEntity) {
			yPos = shooter.posY + (double)(3.2F * 0.4F);
		}

		this.setPosition(shooter.posX, yPos, shooter.posZ);
		double x = this.getTargetX() - this.posX;
		double y = this.getTargetY() - this.posY;
		double z = this.getTargetZ() - this.posZ;
		double d = Math.sqrt(x * x + z * z);
		this.rotationYaw = 180.0F + (float)Math.toDegrees(Math.atan2(x, z));
		this.rotationPitch = (float)Math.toDegrees(Math.atan2(y, d));
	}

	public MutantArrowEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.MUTANT_ARROW, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(TARGET_X, 0);
		this.dataManager.register(TARGET_Y, 0);
		this.dataManager.register(TARGET_Z, 0);
		this.dataManager.register(SPEED, 12.0F);
		this.dataManager.register(CLONES, 10);
	}

	public double getTargetX() {
		return (double)this.dataManager.get(TARGET_X) / 10000.0D;
	}

	public void setTargetX(double targetX) {
		this.dataManager.set(TARGET_X, (int)(targetX * 10000.0D));
	}

	public double getTargetY() {
		return (double)this.dataManager.get(TARGET_Y) / 10000.0D;
	}

	public void setTargetY(double targetY) {
		this.dataManager.set(TARGET_Y, (int)(targetY * 10000.0D));
	}

	public double getTargetZ() {
		return (double)this.dataManager.get(TARGET_Z) / 10000.0D;
	}

	public void setTargetZ(double targetZ) {
		this.dataManager.set(TARGET_Z, (int)(targetZ * 10000.0D));
	}

	public float getSpeed() {
		return this.dataManager.get(SPEED) / 10.0F;
	}

	public void setSpeed(float speed) {
		this.dataManager.set(SPEED, speed * 10.0F);
	}

	public int getClones() {
		return this.dataManager.get(CLONES);
	}

	public void setClones(int clones) {
		this.dataManager.set(CLONES, clones);
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

	@Nullable
	public Entity getShooter() {
		return this.shootingEntity != null && this.world instanceof ServerWorld ? ((ServerWorld)this.world).getEntityByUuid(this.shootingEntity) : null;
	}

	@Override
	public void tick() {
		super.tick();
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
			if (entity.attackEntityFrom(new IndirectDamageSource("arrow", this, this.getShooter(), null), (float)this.damage) && !this.isSilent()) {
				this.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ITEM_CROSSBOW_HIT, this.getSoundCategory(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			}

			if (this.potionEffect != null && entity instanceof LivingEntity) {
				((LivingEntity)entity).addPotionEffect(this.potionEffect);
			}
		}

		this.pointedEntities.clear();
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.putInt("TicksExisted", this.ticksExisted);
		compound.put("Target", this.newDoubleNBTList(this.getTargetX(), this.getTargetY(), this.getTargetZ()));
		compound.putFloat("Speed", this.getSpeed());
		compound.putInt("Clones", this.getClones());
		if (this.potionEffect != null) {
			compound.put("Effect", this.potionEffect.write(compound));
		}

		if (this.shootingEntity != null) {
			compound.putUniqueId("OwnerUUID", this.shootingEntity);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		this.ticksExisted = compound.getInt("TicksExisted");
		this.setSpeed(compound.getFloat("Speed"));
		this.setClones(compound.getInt("Clones"));
		if (compound.contains("Target", 9) && compound.getList("Target", 6).size() == 3) {
			ListNBT listnbt1 = compound.getList("Target", 6);
			this.setTargetX(listnbt1.getDouble(0));
			this.setTargetY(listnbt1.getDouble(1));
			this.setTargetZ(listnbt1.getDouble(2));
		}
		if (compound.contains("Effect", 9)) {
			this.setPotionEffect(EffectInstance.read(compound.getCompound("Effect")));
		}

		if (compound.hasUniqueId("OwnerUUID")) {
			this.shootingEntity = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}