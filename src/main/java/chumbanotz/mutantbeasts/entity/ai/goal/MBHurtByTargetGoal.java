package chumbanotz.mutantbeasts.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MBHurtByTargetGoal extends HurtByTargetGoal {
	private LivingEntity lastTarget;

	public MBHurtByTargetGoal(CreatureEntity creatureIn) {
		super(creatureIn);
	}

	@Override
	public boolean shouldExecute() {
		if (!super.shouldExecute()) {
			if (this.lastTarget != null) {
				if (this.lastTarget instanceof PlayerEntity && !this.lastTarget.isAlive() || !this.lastTarget.isAddedToWorld()) {
					this.lastTarget = null;
				} else {
					this.goalOwner.setRevengeTarget(this.lastTarget);
				}
			}

			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (!super.shouldContinueExecuting()) {
			return false;
		} else {
			LivingEntity revengeTarget = this.goalOwner.getRevengeTarget();
			if (super.shouldExecute() && revengeTarget != this.target && this.goalOwner.getDistanceSq(revengeTarget) < this.goalOwner.getDistanceSq(this.target)) {
				this.lastTarget = this.target;
				return false;
			}

			return true;
		}
	}

	@Override
	protected boolean isSuitableTarget(LivingEntity potentialTarget, EntityPredicate targetPredicate) {
		if (potentialTarget instanceof WitherEntity && this.goalOwner.isEntityUndead()) {
			return false;
		} else {
			return super.isSuitableTarget(potentialTarget, targetPredicate);
		}
	}

	@Override
	protected void setAttackTarget(MobEntity mobIn, LivingEntity targetIn) {
		mobIn.setRevengeTarget(targetIn);
	}
}