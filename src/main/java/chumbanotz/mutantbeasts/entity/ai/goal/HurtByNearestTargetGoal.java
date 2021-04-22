package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.passive.TameableEntity;

public class HurtByNearestTargetGoal extends HurtByTargetGoal {
	public HurtByNearestTargetGoal(CreatureEntity creatureIn, Class<?>... excludeReinforcementTypes) {
		super(creatureIn, excludeReinforcementTypes);
	}

	@Override
	public boolean shouldExecute() {
		if (!super.shouldExecute()) {
			LivingEntity lastTarget = this.goalOwner.getLastAttackedEntity();
			if (lastTarget != null && this.goalOwner.getRevengeTarget() == null) {
				this.goalOwner.setRevengeTarget(lastTarget);
			}

			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void alertOthers() {
		if (this.goalOwner.getRevengeTarget() == null) {
			return;
		}

		super.alertOthers();
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (!super.shouldContinueExecuting()) {
			return false;
		} else {
			LivingEntity revengeTarget = this.goalOwner.getRevengeTarget();
			if (super.shouldExecute() && revengeTarget != this.target && this.goalOwner.getDistanceSq(revengeTarget) < this.goalOwner.getDistanceSq(this.target)) {
				this.goalOwner.setLastAttackedEntity(this.target);
				return false;
			}

			return true;
		}
	}

	@Override
	protected boolean isSuitableTarget(LivingEntity potentialTarget, EntityPredicate targetPredicate) {
		if (potentialTarget instanceof net.minecraft.entity.boss.WitherEntity && this.goalOwner.isEntityUndead()) {
			return false;
		} else if (potentialTarget instanceof TameableEntity && this.goalOwner instanceof TameableEntity) {
			UUID targetOwnerUUID = ((TameableEntity)potentialTarget).getOwnerId();
			UUID attackerOwnerUUID = ((TameableEntity)this.goalOwner).getOwnerId();
			if (Objects.equals(targetOwnerUUID, attackerOwnerUUID)) {
				return false;
			}
		}

		return potentialTarget != null && targetPredicate.canTarget(this.goalOwner, potentialTarget);
	}

	@Override
	protected void setAttackTarget(MobEntity mobIn, LivingEntity targetIn) {
		mobIn.setRevengeTarget(targetIn);
	}

	@Override
	protected double getTargetDistance() {
		return super.getTargetDistance() * 2.0D;
	}
}