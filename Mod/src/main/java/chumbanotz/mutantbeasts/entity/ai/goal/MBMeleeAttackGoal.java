package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.EnumSet;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;

public class MBMeleeAttackGoal extends Goal {
	private CreatureEntity attacker;
	private int attackTick;
	private int maxAttackTick = 20;
	private float moveSpeed;
	boolean longMemory;
	private Path path;
	private Class<?> classTarget;
	private int delayCounter;
	private double reach;

	public MBMeleeAttackGoal(CreatureEntity creatureEntity, Class<?> classTarget, double moveSpeed, boolean longMemory) {
		this(creatureEntity, moveSpeed, longMemory);
		this.classTarget = classTarget;
	}

	public MBMeleeAttackGoal(CreatureEntity creatureEntity, double moveSpeed, boolean longMemory) {
		this.attacker = creatureEntity;
		this.moveSpeed = (float)moveSpeed;
		this.longMemory = longMemory;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean shouldExecute() {
		LivingEntity var1 = this.attacker.getAttackTarget();

		if (var1 == null) {
			return false;
		} else if (this.classTarget != null && !this.classTarget.isAssignableFrom(var1.getClass())) {
			return false;
		} else {
			this.path = this.attacker.getNavigator().getPathToEntityLiving(var1, 0);
			return this.path != null;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		LivingEntity livingentity = this.attacker.getAttackTarget();

		if (livingentity == null) {
			return false;
		} else if (!livingentity.isAlive()) {
			return false;
		} else if (!this.longMemory) {
			return !this.attacker.getNavigator().noPath();
		} else if (!this.attacker.isWithinHomeDistanceFromPosition(livingentity.getPosition())) {
			return false;
		} else {
			return !(livingentity instanceof PlayerEntity) || !livingentity.isSpectator() && !((PlayerEntity)livingentity).isCreative();
		}
	}

	@Override
	public void startExecuting() {
		this.attacker.getNavigator().setPath(this.path, (double)this.moveSpeed);
		this.delayCounter = 0;
	}

	@Override
	public void resetTask() {
		if (!EntityPredicates.CAN_AI_TARGET.test(this.attacker.getAttackTarget())) {
			this.attacker.setAttackTarget(null);
		}

		this.attacker.getNavigator().clearPath();
	}

	@Override
	public void tick() {
		LivingEntity livingentity = this.attacker.getAttackTarget();
		this.attacker.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);

		if ((this.longMemory || this.attacker.getEntitySenses().canSee(livingentity)) && --this.delayCounter <= 0) {
			this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
			this.attacker.getNavigator().tryMoveToEntityLiving(livingentity, (double)this.moveSpeed);
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);
		this.reach = (double)(this.attacker.getWidth() * 2.0F * this.attacker.getWidth() * 2.0F);

		if (this.attacker.getDistanceSq(livingentity.posX, livingentity.getBoundingBox().minY, livingentity.posZ) <= this.reach && this.attackTick <= 0) {
			this.resetAttackTick();
			this.attacker.swingArm(Hand.MAIN_HAND);
			this.attacker.attackEntityAsMob(livingentity);
		}
	}

	public int getAttackTick() {
		return this.attackTick;
	}

	public void resetAttackTick() {
		this.attackTick = this.maxAttackTick;
	}

	public MBMeleeAttackGoal setMaxAttackTick(int max) {
		this.maxAttackTick = max;
		return this;
	}

	public MBMeleeAttackGoal setReach(float f) {
		this.reach = f;
		return this;
	}
}