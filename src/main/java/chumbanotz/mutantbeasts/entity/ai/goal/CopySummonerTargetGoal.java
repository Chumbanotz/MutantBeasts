package chumbanotz.mutantbeasts.entity.ai.goal;

import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.util.math.BlockPos;

public class CopySummonerTargetGoal extends TargetGoal {
	private MutantZombieEntity summoner;

	public CopySummonerTargetGoal(MobEntity mobIn, MutantZombieEntity summoner) {
		super(mobIn, true);
		this.summoner = summoner;
	}

	@Override
	public boolean shouldExecute() {
		if (this.summoner == null) {
			return false;
		} else if (!this.summoner.isAddedToWorld()) {
			if (this.goalOwner.detachHome()) {
				this.goalOwner.setHomePosAndDistance(BlockPos.ZERO, -1);
			}

			this.summoner = null;
			return false;
		}

		this.goalOwner.setHomePosAndDistance(this.summoner.getPosition(), 8);
		return this.isSuitableTarget(this.summoner.getAttackTarget(), EntityPredicate.DEFAULT);
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		this.target = this.summoner.getAttackTarget();
		this.goalOwner.setAttackTarget(this.target);
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (!super.shouldContinueExecuting()) {
			return false;
		} else {
			LivingEntity summonerTarget = this.summoner.getAttackTarget();
			if (summonerTarget != null && summonerTarget != this.target && this.goalOwner.getDistanceSq(summonerTarget) < this.goalOwner.getDistanceSq(this.target)) {
				return false;
			}

			return true;
		}
	}

	@Override
	public void tick() {
		if (this.goalOwner.ticksExisted % 3 == 0) {
			this.goalOwner.setHomePosAndDistance(this.summoner.getPosition(), 16);
		}
	}
}