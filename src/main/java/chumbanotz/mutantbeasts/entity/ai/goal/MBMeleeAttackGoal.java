package chumbanotz.mutantbeasts.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Hand;

public class MBMeleeAttackGoal extends MeleeAttackGoal {
	private int maxAttackTick = 20;
	private final double moveSpeed;
	private Path path;
	private int delayCounter;

	public MBMeleeAttackGoal(CreatureEntity creatureEntity, double moveSpeed) {
		super(creatureEntity, moveSpeed, true);
		this.moveSpeed = moveSpeed;
	}

	@Override
	public boolean shouldExecute() {
		LivingEntity livingEntity = this.attacker.getAttackTarget();
		if (livingEntity == null || !livingEntity.isAlive()) {
			return false;
		} else {
			this.path = this.attacker.getNavigator().func_75494_a(livingEntity, 0);
			return this.path != null;
		}
	}

	@Override
	public void startExecuting() {
		this.attacker.getNavigator().setPath(this.path, (double)this.moveSpeed);
		this.attacker.setAggroed(true);
		this.delayCounter = 0;
	}

	@Override
	public void tick() {
		LivingEntity livingentity = this.attacker.getAttackTarget();
		this.attacker.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);

		if (--this.delayCounter <= 0) {
			this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
			this.attacker.getNavigator().tryMoveToEntityLiving(livingentity, (double)this.moveSpeed);
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);
		this.checkAndPerformAttack(livingentity, this.attacker.getDistanceSq(livingentity.posX, livingentity.getBoundingBox().minY, livingentity.posZ));
	}

	@Override
	protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
		if ((distToEnemySqr <= this.getAttackReachSqr(enemy) || this.attacker.getBoundingBox().intersects(enemy.getBoundingBox())) && this.attackTick <= 0) {
			this.attackTick = this.maxAttackTick;
			this.attacker.swingArm(Hand.MAIN_HAND);
			this.attacker.attackEntityAsMob(enemy);
		}
	}

	public MBMeleeAttackGoal setMaxAttackTick(int max) {
		this.maxAttackTick = max;
		return this;
	}
}