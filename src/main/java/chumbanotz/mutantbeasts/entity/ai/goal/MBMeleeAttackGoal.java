package chumbanotz.mutantbeasts.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.util.math.Vec3d;

public class MBMeleeAttackGoal extends MeleeAttackGoal {
	private int maxAttackTick = 20;
	private final double moveSpeed;
	private int delayCounter;

	public MBMeleeAttackGoal(CreatureEntity creatureEntity, double moveSpeed) {
		super(creatureEntity, moveSpeed, true);
		this.moveSpeed = moveSpeed;
	}

	@Override
	public boolean shouldExecute() {
		this.attackTick = Math.max(this.attackTick - 1, 0);
		LivingEntity livingEntity = this.attacker.getAttackTarget();
		if (livingEntity == null) {
			return false;
		} else if (!livingEntity.isAlive()) {
			this.attacker.setAttackTarget(null);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void startExecuting() {
		this.attacker.setAggroed(true);
		this.delayCounter = 0;
	}

	@Override
	public void tick() {
		LivingEntity livingentity = this.attacker.getAttackTarget();
		if (livingentity == null) {
			return;
		}

		this.attacker.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
		double distSq = this.attacker.getDistanceSq(livingentity.posX, livingentity.getBoundingBox().minY, livingentity.posZ);
		if (--this.delayCounter <= 0) {
			this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
			if (distSq > Math.pow(this.attacker.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue(), 2.0D)) {
				if (!this.attacker.hasPath()) {
					Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.attacker, 16, 7, livingentity.getPositionVec());
					if (vec3d == null || !this.attacker.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.moveSpeed)) {
						this.delayCounter += 5;
					}
				}
			} else {
				this.attacker.getNavigator().tryMoveToEntityLiving(livingentity, (double)this.moveSpeed);
			}
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);
		this.checkAndPerformAttack(livingentity, distSq);
	}

	@Override
	protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
		if ((distToEnemySqr <= this.getAttackReachSqr(enemy) || this.attacker.getBoundingBox().intersects(enemy.getBoundingBox())) && this.attackTick <= 0) {
			this.attackTick = this.maxAttackTick;
			this.attacker.attackEntityAsMob(enemy);
		}
	}

	@Override
	public void resetTask() {
		this.attacker.getNavigator().clearPath();
		if (this.attacker.getAttackTarget() == null) {
			this.attacker.setAggroed(false);
		}
	}

	public MBMeleeAttackGoal setMaxAttackTick(int max) {
		this.maxAttackTick = max;
		return this;
	}
}