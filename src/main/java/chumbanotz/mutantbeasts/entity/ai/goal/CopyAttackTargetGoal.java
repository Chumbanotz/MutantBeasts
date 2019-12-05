package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Supplier;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class CopyAttackTargetGoal extends TargetGoal {
	protected final Supplier<LivingEntity> entityToCopy;

	public CopyAttackTargetGoal(CreatureEntity mobIn, boolean checkSight, Supplier<LivingEntity> entityToCopy) {
		super(mobIn, checkSight);
		this.entityToCopy = entityToCopy;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean shouldExecute() {
		if (this.getMobToCopy() == null) {
			return false;
		} else {
			return this.isSuitableTarget(this.getMobToCopy().getAttackTarget(), EntityPredicate.DEFAULT);
		}
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		this.target = this.getMobToCopy().getAttackTarget();
		this.goalOwner.setAttackTarget(this.target);
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.target != null && this.getMobToCopy() != null && this.getMobToCopy().getAttackTarget() != null && this.target != this.getMobToCopy().getAttackTarget() && this.goalOwner.getDistanceSq(this.getMobToCopy().getAttackTarget()) < this.goalOwner.getDistanceSq(this.target)) {
			return false;
		}

		return super.shouldContinueExecuting();
	}

	private MobEntity getMobToCopy() {
		LivingEntity livingEntity = this.entityToCopy.get();
		if (livingEntity == null) {
			return null;
		} else if (!(livingEntity instanceof MobEntity)) {
			return null;
		} else {
			return ((MobEntity)livingEntity);
		}
	}
}