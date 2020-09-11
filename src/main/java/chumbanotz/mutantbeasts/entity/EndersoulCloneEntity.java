package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EndersoulCloneEntity extends MonsterEntity {
	public EndersoulCloneEntity(EntityType<? extends EndersoulCloneEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.0F;
		this.experienceValue = this.rand.nextInt(2);
		this.setPathPriority(PathNodeType.DAMAGE_FIRE, -1.0F);
		this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.2D));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.55F;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	public void setCloner(MutantEndermanEntity cloner) {
		this.hurtResistantTime = 15;
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)cloner.getMaxHealth());
		this.setHealth(cloner.getHealth());
		if (cloner.hasCustomName()) {
			this.setCustomName(cloner.getCustomName());
			this.setCustomNameVisible(cloner.isCustomNameVisible());
		}
	}

	@Override
	public int getMaxFallHeight() {
		return 3;
	}

	@Override
	public void handleStatusUpdate(byte id) {
		super.handleStatusUpdate(id);
		if (id == 0) {
			EntityUtil.spawnEndersoulParticles(this, 256, 1.8F);
		}
	}

	@Override
	protected void updateAITasks() {
		Entity entity = this.getAttackTarget();
		if (this.rand.nextInt(10) == 0 && entity != null && (this.isInWater() || this.isRidingSameEntity(entity) || this.getDistanceSq(entity) > 1024.0D || !this.hasPath())) {
			this.teleportToEntity(entity);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = super.attackEntityAsMob(entityIn);
		if (!this.world.isRemote && this.rand.nextInt(3) != 0) {
			this.teleportToEntity(entityIn);
		}

		if (flag) {
			this.heal(2.0F);
		}

		this.swingArm(Hand.MAIN_HAND);
		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, 0.0F);
		if (flag) {
			EntityUtil.dropExperience(this, this.recentlyHit, this.experienceValue, this.attackingPlayer);
			this.spawnDrops(source);
			this.remove();
		}

		return flag;
	}

	private boolean teleportToEntity(Entity entity) {
		double x = entity.posX + (this.rand.nextDouble() - 0.5D) * 24.0D;
		double y = entity.posY + (double)this.rand.nextInt(5) + 4.0D;
		double z = entity.posZ + (this.rand.nextDouble() - 0.5D) * 24.0D;

		boolean teleport = EntityUtil.teleportTo(this, x, y, z);
		if (teleport) {
            this.world.playSound(null, this.prevPosX, this.prevPosY, this.prevPosZ, MBSoundEvents.ENTITY_ENDERSOUL_CLONE_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(MBSoundEvents.ENTITY_ENDERSOUL_CLONE_TELEPORT, 1.0F, 1.0F);
			this.stopRiding();
		}

		return teleport;
	}

	@Override
	protected void collideWithNearbyEntities() {
	}

	@Override
	public boolean addPotionEffect(EffectInstance effectInstanceIn) {
		return false;
	}

	@Override
	public boolean preventDespawn() {
		return this.isAggressive();
	}

	@Override
	public boolean isEntityEqual(Entity entityIn) {
		return super.isEntityEqual(entityIn) || entityIn instanceof MutantEndermanEntity;
	}

	@Override
	public void remove() {
		super.remove();
		this.world.setEntityState(this, (byte)0);
		this.playSound(this.getDeathSound(), this.getSoundVolume(), this.getSoundPitch());
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_ENDERSOUL_CLONE_DEATH;
	}
}