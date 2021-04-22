package chumbanotz.mutantbeasts.entity.mutant;

import java.util.EnumSet;

import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.controller.FixedBodyController;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.HurtByNearestTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.MutatedExplosion;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MutantCreeperEntity extends MonsterEntity {
	private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(MutantCreeperEntity.class, DataSerializers.BYTE);
	public static final int MAX_CHARGE_TIME = 100;
	public static final int MAX_DEATH_TIME = 100;
	private int chargeTime;
	private int chargeHits = 3 + this.rand.nextInt(3);
	private int lastJumpTick;
	private int jumpTick;
	private boolean summonLightning;
	private DamageSource deathCause;
	public int deathTime;

	public MutantCreeperEntity(EntityType<? extends MutantCreeperEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.0F;
		this.experienceValue = 30;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MutantCreeperEntity.JumpAttackGoal());
		this.goalSelector.addGoal(1, new MutantCreeperEntity.SpawnMinionsGoal());
		this.goalSelector.addGoal(1, new MutantCreeperEntity.ChargeAttackGoal());
		this.goalSelector.addGoal(2, new MBMeleeAttackGoal(this, 1.3D));
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.0D));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(5, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(0, new HurtByNearestTargetGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(100));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(SWIM_SPEED).setBaseValue(4.5D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(STATUS, (byte)0);
	}

	public boolean getPowered() {
		return (this.dataManager.get(STATUS) & 1) != 0;
	}

	private void setPowered(boolean powered) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, powered ? (byte)(b0 | 1) : (byte)(b0 & -2));
	}

	public boolean isJumpAttacking() {
		return (this.dataManager.get(STATUS) & 2) != 0;
	}

	private void setJumpAttacking(boolean jumping) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, jumping ? (byte)(b0 | 2) : (byte)(b0 & -3));
	}

	public boolean isCharging() {
		return (this.dataManager.get(STATUS) & 4) != 0;
	}

	private void setCharging(boolean charging) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, charging ? (byte)(b0 | 4) : (byte)(b0 & -5));
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.6F;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	protected BodyController createBodyController() {
		return new FixedBodyController(this);
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
		super.updateFallState(y, onGroundIn, state, pos);
		if (!this.world.isRemote && this.isJumpAttacking() && (onGroundIn || !state.getFluidState().isEmpty() || state.getMaterial() == Material.WEB)) {
			MutatedExplosion.create(this, this.getPowered() ? 6.0F : 4.0F, false, MutatedExplosion.Mode.DESTROY);
			this.setJumpAttacking(false);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		double x = entityIn.posX - this.posX;
		double y = entityIn.posY - this.posY;
		double z = entityIn.posZ - this.posZ;
		double d = Math.sqrt(x * x + y * y + z * z);
		entityIn.addVelocity(x / d * 0.5D, y / d * 0.05000000074505806D + 0.15000000596046448D, z / d * 0.5D);
		this.swingArm(Hand.MAIN_HAND);
		return super.attackEntityAsMob(entityIn);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else if (this.getPowered() && source.isFireDamage()) {
			this.extinguish();
			return false;
		} else if (source.isExplosion()) {
			float healAmount = amount / 2.0F;

			if (this.isAlive() && this.getHealth() < this.getMaxHealth() && !(source.getTrueSource() instanceof MutantCreeperEntity)) {
				this.heal(healAmount);
				EntityUtil.sendParticlePacket(this, ParticleTypes.HEART, (int)(healAmount / 2.0F));
			}

			return false;
		} else {
			boolean takenDamage = super.attackEntityFrom(source, amount);
			if (this.isCharging() && takenDamage && amount > 0.0F) {
				--this.chargeHits;
			}

			return takenDamage;
		}
	}

	@Override
	public double getVisibilityMultiplier(Entity lookingEntity) {
		return lookingEntity instanceof IronGolemEntity ? 0.0D : super.getVisibilityMultiplier(lookingEntity);
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity lightningBolt) {
		this.setPowered(true);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 6) {
			EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.HEART, 15);
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.lastJumpTick = this.jumpTick;
		if (this.isJumpAttacking()) {
			if (this.jumpTick == 0 && !this.isSilent()) {
				this.world.playMovingSound(null, this, MBSoundEvents.ENTITY_MUTANT_CREEPER_PRIMED, this.getSoundCategory(), 2.0F, this.getSoundPitch());
			}

			this.jumpTick++;
		} else if (this.jumpTick > 0) {
			this.jumpTick = 0;
		}
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return super.canBeRidden(entityIn) && entityIn instanceof LivingEntity;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	protected void constructKnockBackVector(LivingEntity livingEntity) {
		livingEntity.velocityChanged = true;
	}

	public float getExplosionColor(float partialTicks) {
		if (this.deathTime > 0) {
			return ((float)this.deathTime / (float)MAX_DEATH_TIME) * 255.0F;
		} else if (this.isCharging()) {
			 return (this.ticksExisted % 20 < 10 ? 0.6F : 0.0F) * 255.0F;
		} else {
	        return ((float)this.lastJumpTick + (float)(this.jumpTick - this.lastJumpTick) * partialTicks) / (float)(30 - 2);
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote) {
			this.deathCause = cause;
			this.setCharging(false);
			this.world.setEntityState(this, (byte)3);

			if (!this.isSilent()) {
				this.world.playMovingSound(null, this, MBSoundEvents.ENTITY_MUTANT_CREEPER_DEATH, this.getSoundCategory(), 2.0F, 1.0F);
			}

			if (this.recentlyHit > 0) {
				this.recentlyHit += MAX_DEATH_TIME;
			}
		}
	}

	@Override
	protected void onDeathUpdate() {
		++this.deathTime;
		this.stepHeight = 0.0F;
		float power = this.getPowered() ? 12.0F : 8.0F;
		float radius = power * 1.5F;

		for (Entity entity : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().grow((double)radius), EntityPredicates.CAN_AI_TARGET)) {
			double x = this.posX - entity.posX;
			double y = this.posY - entity.posY;
			double z = this.posZ - entity.posZ;
			double d = Math.sqrt(x * x + y * y + z * z);
			float scale = (float)this.deathTime / (float)MAX_DEATH_TIME;
			entity.setMotion(entity.getMotion().add(x / d * (double)scale * 0.09D, y / d * (double)scale * 0.09D, z / d * (double)scale * 0.09D));
		}

		this.posX += (double)(this.rand.nextFloat() * 0.2F) - 0.10000000149011612D;
		this.posZ += (double)(this.rand.nextFloat() * 0.2F) - 0.10000000149011612D;

		if (this.deathTime >= MAX_DEATH_TIME) {
			if (!this.world.isRemote) {
				MutatedExplosion.create(this, power, this.isBurning(), MutatedExplosion.Mode.DESTROY);
				super.onDeath(this.deathCause != null ? this.deathCause : DamageSource.GENERIC);

				if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && this.attackingPlayer != null) {
					CreeperMinionEggEntity egg = new CreeperMinionEggEntity(this.world, this.attackingPlayer);
					egg.setPowered(this.getPowered());
					egg.setPosition(this.posX, this.posY, this.posZ);
					this.world.addEntity(egg);
				}
			}

			EntityUtil.dropExperience(this, this.recentlyHit, this.experienceValue, this.attackingPlayer);
			this.remove();
		}
	}

	@Override
	protected void handleFluidJump(Tag<Fluid> fluidTag) {
		this.setMotion(this.getMotion().add(0.0D, 0.04D, 0.0D));
	}

	@Override
	public float getExplosionResistance(Explosion explosionIn, IBlockReader worldIn, BlockPos pos, BlockState blockStateIn, IFluidState fluidState, float resistance) {
		return this.getPowered() && blockStateIn.getBlockHardness(worldIn, pos) != -1.0F ? Math.min(0.8F, resistance) : resistance;
	}

	@Override
	public void playAmbientSound() {
		if (this.getAttackTarget() == null) {
			super.playAmbientSound();
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_MUTANT_CREEPER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_MUTANT_CREEPER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_MUTANT_CREEPER_HURT;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("JumpAttacking", this.isJumpAttacking());
		compound.putBoolean("Charging", this.isCharging());
		compound.putInt("ChargeTime", this.chargeTime);
		compound.putInt("ChargeHits", this.chargeHits);
		compound.putBoolean("SummonLightning", this.summonLightning);
		compound.putShort("DeathTime", (short)this.deathTime);

		if (this.getPowered()) {
			compound.putBoolean("Powered", true);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setPowered(compound.getBoolean("powered") || compound.getBoolean("Powered"));
		this.setJumpAttacking(compound.getBoolean("JumpAttacking"));
		this.setCharging(compound.getBoolean("Charging"));
		this.chargeTime = compound.getInt("ChargeTime");
		this.chargeHits = compound.getInt("ChargeHits");
		this.summonLightning = compound.getBoolean("SummonLightning");
		this.deathTime = super.deathTime;
	}

	class SpawnMinionsGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			return getAttackTarget() != null && getDistanceSq(getAttackTarget()) <= 1024.0D && !isCharging() && !isJumpAttacking() && rand.nextFloat() * 100.0F < 0.6F;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			double xx = getAttackTarget().posX - posX;
			double yy = getAttackTarget().posY - posY;
			double zz = getAttackTarget().posZ - posZ;
			for (int i = (int)Math.ceil((double)(getHealth() / getMaxHealth()) * 4.0D); i > 0; --i) {
				CreeperMinionEntity creeper = MBEntityType.CREEPER_MINION.create(world);
				double x = posX + (double)(rand.nextFloat() - rand.nextFloat());
				double y = posY + (double)(rand.nextFloat() * 0.5F);
				double z = posZ + (double)(rand.nextFloat() - rand.nextFloat());
				creeper.setMotion(xx * 0.15D + (double)(rand.nextFloat() * 0.05F), yy * 0.15D + (double)(rand.nextFloat() * 0.05F), zz * 0.15D + (double)(rand.nextFloat() * 0.05F));
				creeper.setLocationAndAngles(x, y, z, rotationYaw, 0.0F);
				creeper.setOwnerId(entityUniqueID);
				creeper.setAttackTarget(getAttackTarget());
				if (getPowered()) {
					creeper.setPowered(true);
				}

				world.addEntity(creeper);
			}
		}
	}

	class ChargeAttackGoal extends Goal {
		public ChargeAttackGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity target = getAttackTarget();
			return target != null && onGround && !(getMaxHealth() - getHealth() < getMaxHealth() / 6.0F) && getDistanceSq(target) >= 25.0D && getDistanceSq(target) <= 1024.0D && rand.nextFloat() * 100.0F < 0.7F || isCharging();
		}

		@Override
		public boolean shouldContinueExecuting() {
			LivingEntity target = getAttackTarget();
			if (summonLightning && target != null && getDistanceSq(target.posX, target.posY, target.posZ) < 25.0D && world.isSkyLightMax(getPosition())) {
				((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, false));
				return false;
			}

			return chargeTime < MAX_CHARGE_TIME && chargeHits > 0;
		}

		@Override
		public void startExecuting() {
			setCharging(true);

			if (chargeHits == 0) {
				chargeHits = 3 + rand.nextInt(3);
			}

			if (rand.nextInt(world.isThundering() ? 2 : 6) == 0 && !getPowered()) {
				summonLightning = true;
			}
		}

		@Override
		public void tick() {
			getNavigator().clearPath();
			int i = chargeTime % 20;

			if (i == 0 || i == 20) {
				playSound(MBSoundEvents.ENTITY_MUTANT_CREEPER_CHARGE, 0.6F, 0.7F + rand.nextFloat() * 0.6F);
			}

			chargeTime++;
		}

		@Override
		public void resetTask() {
			if (chargeTime >= MAX_CHARGE_TIME) {
				heal(getMaxHealth() / 4.0F);
				world.setEntityState(MutantCreeperEntity.this, (byte)6);
			}

			chargeTime = 0;
			chargeHits = 4 + rand.nextInt(3);
			setCharging(false);
			summonLightning = false;
		}
	}

	class JumpAttackGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			return getAttackTarget() != null && onGround && !isCharging() && getDistanceSq(getAttackTarget()) <= 1024.0D && rand.nextFloat() * 100.0F < 0.9F;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			motionMultiplier = Vec3d.ZERO;
			setJumpAttacking(true);
			setMotion((getAttackTarget().posX - posX) * 0.2D, 1.4D, (getAttackTarget().posZ - posZ) * 0.2D);
		}
	}
}