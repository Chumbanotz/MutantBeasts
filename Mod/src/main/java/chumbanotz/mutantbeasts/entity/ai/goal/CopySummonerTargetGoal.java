package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.EnumSet;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class CopySummonerTargetGoal extends TargetGoal {
	private static final EntityPredicate PREDICATE = new EntityPredicate().setLineOfSiteRequired().setUseInvisibilityCheck();

	public CopySummonerTargetGoal(MobEntity mobIn) {
		super(mobIn, true);
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));

		if (!SummonableCapability.getFor(mobIn).isPresent()) {
			throw new IllegalArgumentException("Mob needs to have the SummonableCapability attached for this goal");
		}
	}

	@Override
	public boolean shouldExecute() {
		MobEntity summoner = SummonableCapability.get(this.goalOwner).getSummoner();
		if (summoner == null) {
			return false;
		} else if (summoner.getRevengeTarget() != null) {
			this.target = summoner.getRevengeTarget();
		} else if (summoner.getAttackTarget() != null) {
			this.target = summoner.getAttackTarget();
		}

		return this.isSuitableTarget(this.target, PREDICATE);
	}

	@Override
	public boolean shouldContinueExecuting() {
		MobEntity summoner = SummonableCapability.get(this.goalOwner).getSummoner();
		LivingEntity revengeTarget = summoner != null ? summoner.getRevengeTarget() : null;

		if (revengeTarget != null && revengeTarget != this.target && PREDICATE.canTarget(this.goalOwner, this.target) && this.goalOwner.getDistanceSq(revengeTarget) < this.goalOwner.getDistanceSq(this.target)) {
			this.resetTask();
			MutantBeasts.LOGGER.debug(this.goalOwner.getName().getString() + " is changing target");
			return false;
		}

		return super.shouldContinueExecuting();
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		this.goalOwner.setAttackTarget(this.target);
	}
}