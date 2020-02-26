package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import chumbanotz.mutantbeasts.client.animationapi.IAnimatedEntity;
import chumbanotz.mutantbeasts.entity.BodyPartEntity;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantSkeletonEntity extends MonsterEntity implements IAnimatedEntity {
	public static final byte MELEE_ATTACK = 4, SHOOT_ATTACK = 5, MULTI_SHOT_ATTACK = 6, CONSTRICT_RIBS_ATTACK = 7;
	private int attackID;
	private int attackTick;

	public MutantSkeletonEntity(EntityType<? extends MutantSkeletonEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.5F;
		this.experienceValue = 30;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantSkeletonEntity.MeleeGoal());
		this.goalSelector.addGoal(0, new MutantSkeletonEntity.ShootGoal());
		this.goalSelector.addGoal(0, new MutantSkeletonEntity.MultiShotGoal());
		this.goalSelector.addGoal(0, new MutantSkeletonEntity.ConstrictRibsAttackGoal());
		this.goalSelector.addGoal(2, new MBMeleeAttackGoal(this, 1.1D).setMaxAttackTick(6));
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.1D));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(5, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, WolfEntity.class, true, true));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(96.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(SWIM_SPEED).setBaseValue(5.0D);
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 3.25F;
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.UNDEAD;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 0 || id >= 4 && id <= 7) {
			this.attackID = id;
			this.attackTick = 0;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public void livingTick() {
		super.livingTick();
		this.setPathPriority(PathNodeType.WATER, this.isInWaterOrBubbleColumn() ? 16.0F : -1.0F);

		if (this.attackID != 0) {
			++this.attackTick;
		}

		if (this.isAlive() && this.ticksExisted % 100 == 0 && !this.world.isDaytime() && this.getHealth() < this.getMaxHealth()) {
			this.heal(2.0F);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (!this.world.isRemote && this.attackID == 0) {
			if (this.rand.nextInt(4) != 0) {
				this.setAttackID(MELEE_ATTACK);
			} else if (this.onGround || this.world.containsAnyLiquid(this.getBoundingBox())) {
				this.setAttackID(CONSTRICT_RIBS_ATTACK);
			}
		}

		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return !(source.getTrueSource() instanceof MutantSkeletonEntity) && super.attackEntityFrom(source, amount);
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return false;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	protected void constructKnockBackVector(LivingEntity livingEntity) {
		livingEntity.applyEntityCollision(this);
		livingEntity.velocityChanged = true;
	}

	@Override
	public int getAnimationID() {
		return this.attackID;
	}

	@Override
	public int getAnimationTick() {
		return this.attackTick;
	}

	@Override
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
		return spawnDataIn;
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		if (!this.world.isRemote) {
			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(3.0D, 2.0D, 3.0D))) {
				entity.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageBypassesArmor(), 7.0F);
			}

			for (int i = 0; i < 18; ++i) {
				int j = i;

				if (i >= 3) {
					j = i + 1;
				}

				if (j >= 4) {
					++j;
				}

				if (j >= 5) {
					++j;
				}

				if (j >= 6) {
					++j;
				}

				if (j >= 9) {
					++j;
				}

				if (j >= 10) {
					++j;
				}

				if (j >= 11) {
					++j;
				}

				if (j >= 12) {
					++j;
				}

				if (j >= 15) {
					++j;
				}

				if (j >= 16) {
					++j;
				}

				if (j >= 17) {
					++j;
				}

				if (j >= 18) {
					++j;
				}

				if (j >= 20) {
					++j;
				}

				BodyPartEntity part = new BodyPartEntity(this.world, this, j);
				part.setMotion(part.getMotion().add((double)(this.rand.nextFloat() * 0.8F * 2.0F - 0.8F), (double)(this.rand.nextFloat() * 0.25F + 0.1F), (double)(this.rand.nextFloat() * 0.8F * 2.0F - 0.8F)));
				this.world.addEntity(part);
			}
		}

		this.deathTime = 19;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_MUTANT_SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_MUTANT_SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_MUTANT_SKELETON_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(MBSoundEvents.ENTITY_MUTANT_SKELETON_STEP, 0.15F, 1.0F);
	}

	private void setAttackID(int id) {
		this.attackID = id;
		this.attackTick = 0;
		this.world.setEntityState(this, (byte)id);
	}

	class MeleeGoal extends Goal {
		public MeleeGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return getAttackTarget() != null && attackID == MELEE_ATTACK;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 14;
		}

		@Override
		public void startExecuting() {
			attackTick = 0;
		}
	
		@Override
		public void tick() {
			if (getAttackTarget() != null && getAttackTarget().isAlive()) {
				lookController.setLookPositionWithEntity(getAttackTarget(), 30.0F, 30.0F);
			}

			if (attackTick == 3) {
				double reach = (double)(2.3F + rand.nextFloat() * 0.3F);
				for (Entity entity : world.getEntitiesInAABBexcluding(MutantSkeletonEntity.this, getBoundingBox().grow(reach, 0.0D, reach), EntityPredicates.CAN_AI_TARGET)) {
					double dist = (double)getDistance(entity);
					double x = posX - entity.posX;
					double z = posZ - entity.posZ;

					if (EntityUtil.isFacing(MutantSkeletonEntity.this, x, z, 60.0F) && !(entity instanceof MutantSkeletonEntity)) {
						float power = 1.8F + (float)rand.nextInt(5) * 0.15F;
						entity.stopRiding();
						entity.attackEntityFrom(DamageSource.causeMobDamage(MutantSkeletonEntity.this), (float)getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue() + (float)rand.nextInt(2));
						entity.setMotion(-x / dist * (double)power, Math.max(0.2800000011920929D, entity.getMotion().y), -z / dist * (double)power);
						EntityUtil.sendPlayerVelocityPacket(entity);
					}
				}

				playSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F));
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
		}
	}

	class ConstrictRibsAttackGoal extends Goal {
		private LivingEntity attackTarget;
		public ConstrictRibsAttackGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return attackTarget != null && attackID == CONSTRICT_RIBS_ATTACK;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 20;
		}

		@Override
		public void startExecuting() {
			attackTick = 0;
		}

		@Override
		public void tick() {
			if (attackTick == 5)
				this.attackTarget.stopRiding();
			if (attackTick == 6) {
				this.attackTarget.attackEntityFrom(DamageSource.causeMobDamage(MutantSkeletonEntity.this), (float)getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue() + 7.0F);
				this.attackTarget.setMotion((double)((1.0F + rand.nextFloat() * 0.4F) * (float)(rand.nextBoolean() ? 1 : -1)), (double)(0.4F + rand.nextFloat() * 0.8F), (double)((1.0F + rand.nextFloat() * 0.4F) * (float)(rand.nextBoolean() ? 1 : -1)));
				playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 0.5F, 0.8F + rand.nextFloat() * 0.4F);
				EntityUtil.disableShield(this.attackTarget, DamageSource.causeMobDamage(MutantSkeletonEntity.this), 100);
				EntityUtil.sendPlayerVelocityPacket(this.attackTarget);
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.attackTarget = null;
		}
	}

	class ShootGoal extends Goal {
		private LivingEntity attackTarget;

		public ShootGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && canEntityBeSeen(this.attackTarget) && getDistanceSq(this.attackTarget) > 4.0D && attackID == 0 && rand.nextInt(12) == 0;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 32;
		}

		@Override
		public void startExecuting() {
			setAttackID(SHOOT_ATTACK);
		}

		@Override
		public void tick() {
			navigator.clearPath();
			lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);

			if (attackTick == 5) {
				playSound(SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2, 1.0F, 1.0F);
			}

			if (attackTick == 20) {
				playSound(SoundEvents.ITEM_CROSSBOW_LOADING_END, 1.0F, 1.0F / (rand.nextFloat() * 0.5F + 1.0F) + 0.2F);
			}

			if (attackTick == 26) {
				MutantArrowEntity arrowEntity = new MutantArrowEntity(world, MutantSkeletonEntity.this, this.attackTarget);

				if (hurtTime > 0 && lastDamage > 0.0F) {
					arrowEntity.randomize((float)hurtTime / 2.0F);
				} else if (!canEntityBeSeen(this.attackTarget)) {
					arrowEntity.randomize(0.5F + rand.nextFloat());
				}

				if (rand.nextInt(4) == 0) {
					arrowEntity.setPotionEffect(new EffectInstance(Effects.POISON, 80 + rand.nextInt(60), 0));
				}

				if (rand.nextInt(4) == 0) {
					arrowEntity.setPotionEffect(new EffectInstance(Effects.HUNGER, 120 + rand.nextInt(60), 1));
				}

				if (rand.nextInt(4) == 0) {
					arrowEntity.setPotionEffect(new EffectInstance(Effects.SLOWNESS, 120 + rand.nextInt(60), 1));
				}

				world.addEntity(arrowEntity);
				playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 0.25F);
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.attackTarget = null;
		}
	}

	class MultiShotGoal extends Goal {
		private final List<MutantArrowEntity> shots = new ArrayList<>();
		private LivingEntity attackTarget;

		public MultiShotGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && (onGround || world.containsAnyLiquid(getBoundingBox())) && attackID == 0 && rand.nextInt(26) == 0 && canEntityBeSeen(this.attackTarget);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 30;
		}

		@Override
		public void startExecuting() {
			setAttackID(MULTI_SHOT_ATTACK);
		}

		@Override
		public void tick() {
			lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);

			if (attackTick == 10) {
				double x = this.attackTarget.posX - posX;
				double z = this.attackTarget.posZ - posZ;
				float scale = 0.06F + rand.nextFloat() * 0.03F;
				if (getDistanceSq(this.attackTarget) < 16.0D) {
					x *= -1.0D;
					z *= -1.0D;
					scale *= 5.0D;
				}

				setMotion(x * (double)scale, 1.100000023841858D, z * (double)scale);
			}

			if (attackTick == 15) {
				playSound(SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3, 1.0F, 1.0F);
			}

			if (attackTick == 20) {
				playSound(SoundEvents.ITEM_CROSSBOW_LOADING_END, 1.0F, 1.0F / (rand.nextFloat() * 0.5F + 1.0F) + 0.2F);
			}

			if (attackTick >= 24 && attackTick < 28) {
				MutantArrowEntity shot;

				if (!this.shots.isEmpty()) {
					for (MutantArrowEntity arrowEntity : this.shots) {
						shot = arrowEntity;
						world.addEntity(arrowEntity);
					}

					this.shots.clear();
				}

				for (int i = 0; i < 6; ++i) {
					shot = new MutantArrowEntity(world, MutantSkeletonEntity.this, this.attackTarget);
					shot.setSpeed(1.2F - rand.nextFloat() * 0.1F);
					shot.setClones(2);
					shot.randomize(3.0F);
					shot.setDamage(5 + rand.nextInt(5));
					this.shots.add(shot);
				}

				playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 0.25F);
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.shots.clear();
			this.attackTarget = null;
		}
	}
}