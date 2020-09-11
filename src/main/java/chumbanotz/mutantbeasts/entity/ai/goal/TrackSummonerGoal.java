package chumbanotz.mutantbeasts.entity.ai.goal;

import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.math.BlockPos;

public class TrackSummonerGoal extends Goal {
	private final ZombieEntity zombie;
	private MutantZombieEntity mutantZombie;

	public TrackSummonerGoal(ZombieEntity zombie, MutantZombieEntity mutantZombie) {
		this.zombie = zombie;
		this.mutantZombie = mutantZombie;
	}

	@Override
	public boolean shouldExecute() {
		return this.mutantZombie != null;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.mutantZombie.isAddedToWorld();
	}

	@Override
	public void tick() {
		if (this.zombie.getRevengeTarget() == null && this.mutantZombie.getAttackTarget() != null && this.zombie.getAttackTarget() != this.mutantZombie.getAttackTarget()) {
			this.zombie.setAttackTarget(this.mutantZombie.getAttackTarget());
		}

		if (!this.zombie.detachHome() || this.zombie.ticksExisted % 20 == 0) {
			BlockPos pos = this.mutantZombie.getPosition();
			if (!this.zombie.getHomePosition().equals(pos)) {
				this.zombie.setHomePosAndDistance(pos, this.zombie.getAttackTarget() == null ? 8 : 16);
			}
		}
	}

	@Override
	public void resetTask() {
		this.mutantZombie = null;
		this.zombie.setHomePosAndDistance(BlockPos.ZERO, -1);
	}
}