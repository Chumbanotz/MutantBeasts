package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.EnumSet;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.TameableEntity;

public class OwnerTargetGoal extends TargetGoal {
	private final TameableEntity tameable;
	private LivingEntity owner;

	public OwnerTargetGoal(TameableEntity tameable) {
		super(tameable, false);
		this.tameable = tameable;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean shouldExecute() {
		if ((this.tameable.isTamed() || this.tameable instanceof CreeperMinionEntity) && !this.tameable.isSitting()) {
			LivingEntity owner = this.tameable.getOwner();
			if (owner == null) {
				return false;
			} else {
				this.owner = owner;
				this.target = this.getOwnerTarget();
				return this.isSuitableTarget(this.target, EntityPredicate.DEFAULT);
			}
		} else {
			return false;
		}
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		this.goalOwner.setAttackTarget(this.target);
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (!super.shouldContinueExecuting()) {
			return false;
		} else {
			LivingEntity ownerTarget = this.getOwnerTarget();
			if (this.isSuitableTarget(ownerTarget, EntityPredicate.DEFAULT) && ownerTarget != this.target && this.tameable.getDistanceSq(ownerTarget) < this.tameable.getDistanceSq(this.target)) {
				return false;
			}

			return true;
		}
	}

	@Override
	public void resetTask() {
		super.resetTask();
		this.owner = null;
	}

	@Override
	protected boolean isSuitableTarget(LivingEntity potentialTarget, EntityPredicate targetPredicate) {
		if (potentialTarget == null) {
			return false;
		} else if (!targetPredicate.canTarget(this.goalOwner, potentialTarget)) {
			return false;
		} else {
			return this.tameable.shouldAttackEntity(potentialTarget, this.owner);
		}
	}

	private LivingEntity getOwnerTarget() {
		if (this.owner.getRevengeTarget() != null) {
			return this.owner.getRevengeTarget();
		} else if (this.owner instanceof MobEntity) {
			return ((MobEntity)this.owner).getAttackTarget();
		} else {
			return this.owner.getLastAttackedEntity();
		}
	}
}