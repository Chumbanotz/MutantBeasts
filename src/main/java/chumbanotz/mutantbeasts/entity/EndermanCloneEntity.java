package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EndermanCloneEntity extends EndermanEntity {
	public EndermanCloneEntity(EntityType<? extends EndermanCloneEntity> type, World world) {
		super(type, world);
		this.experienceValue = this.rand.nextInt(2);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.0D, false));
		this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
	}

	@Override
	public void playEndermanSound() {
	}

	@Override
	protected void updateAITasks() {
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (!this.world.isRemote && this.rand.nextInt(3) != 0) {
			this.teleportRandomly();
		}

		return super.attackEntityAsMob(entityIn);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		this.deathTime = 19;
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	protected float getSoundVolume() {
		return this.isScreaming() ? 1.0F : 0.3F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return super.getAmbientSound();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}
}