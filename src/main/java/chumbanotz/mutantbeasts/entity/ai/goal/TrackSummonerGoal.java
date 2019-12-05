package chumbanotz.mutantbeasts.entity.ai.goal;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.capability.ISummonable;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class TrackSummonerGoal extends Goal {
	private static final EntityPredicate NEW_SUMMONER_PREDICATE = new EntityPredicate().allowInvulnerable().allowFriendlyFire();
	private final ISummonable summonable;
	private final MobEntity summonedMob;
	private MobEntity summoner;

	public TrackSummonerGoal(MobEntity creatureEntity) {
		this.summonedMob = creatureEntity;
		this.summonable = SummonableCapability.get(creatureEntity);
		if (!SummonableCapability.getLazy(creatureEntity).isPresent()) {
			throw new IllegalArgumentException("Mob needs to have the SummonableCapability attached for TrackSummonerGoal");
		}
	}

	@Override
	public boolean shouldExecute() {
		this.summoner = this.summonable.findSummoner(this.summonedMob.world);
		return this.summoner != null || this.summonable.isSpawnedBySummoner();
	}

	@Override
	public void tick() {
		double followRange = this.summonedMob.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue();
		if (this.summoner == null) {
			if (this.summonable.isSpawnedBySummoner() && this.summonedMob.ticksExisted % 20 == 0) {
				MutantZombieEntity mutantZombieEntity = this.summonedMob.world.getClosestEntityWithinAABB(MutantZombieEntity.class, NEW_SUMMONER_PREDICATE, this.summonedMob, this.summonedMob.posX, this.summonedMob.posY, this.summonedMob.posZ, this.summonedMob.getBoundingBox().grow(followRange));
				if (mutantZombieEntity != null && !mutantZombieEntity.isAIDisabled()) {
					this.summonable.setSummoner(mutantZombieEntity);
					this.summoner = this.summonable.findSummoner(summonedMob.world);
					MutantBeasts.LOGGER.debug("Found new summoner");
				}
			}
		} else {
			if (!this.summoner.isAddedToWorld()) {
				this.summonable.setSummoner(null);
				this.summoner = null;
				MutantBeasts.LOGGER.debug(this.summonedMob.getName().getString() + "'s summoner was removed from world");
			} else {
				this.summonedMob.setIdleTime(this.summoner.getIdleTime());
				if (!this.summonedMob.detachHome() || this.summonedMob.getHomePosition() != this.summoner.getPosition()) {
					int i = (int)followRange;
					this.summonedMob.setHomePosAndDistance(this.summoner.getPosition(), this.summonedMob.getAttackTarget() != null ? i : i / 2);
				}
			}
		}
	}

	@Override
	public void resetTask() {
		this.summoner = null;
		this.summonable.setSummoner(null);
		this.summonedMob.setHomePosAndDistance(BlockPos.ZERO, -1);
		MutantBeasts.LOGGER.debug(this.summonedMob.getName().getString() + " is resetting track goal");
	}
}