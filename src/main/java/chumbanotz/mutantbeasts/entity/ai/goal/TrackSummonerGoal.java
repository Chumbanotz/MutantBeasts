package chumbanotz.mutantbeasts.entity.ai.goal;

import chumbanotz.mutantbeasts.capability.ISummonable;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TrackSummonerGoal extends Goal {
	private final ISummonable summonable;
	private final MobEntity mobEntity;
	private MobEntity summoner;

	public TrackSummonerGoal(MobEntity mobEntity) {
		this.mobEntity = mobEntity;
		this.summonable = SummonableCapability.get(mobEntity);
		if (!SummonableCapability.getLazy(mobEntity).isPresent()) {
			throw new IllegalArgumentException("Mob needs to have the SummonableCapability attached for TrackSummonerGoal");
		}
	}

	@Override
	public boolean shouldExecute() {
		return this.getSummoner() != null || this.summonable.isSpawnedBySummoner();
	}

	@Override
	public void tick() {
		double followRange = this.mobEntity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue();
		if (this.summoner == null) {
			if (this.summonable.isSpawnedBySummoner() && this.mobEntity.ticksExisted % 20 == 0) {
				MutantZombieEntity mutantZombieEntity = this.mobEntity.world.getClosestEntityWithinAABB(MutantZombieEntity.class, EntityPredicate.DEFAULT, this.mobEntity, this.mobEntity.posX, this.mobEntity.posY, this.mobEntity.posZ, this.mobEntity.getBoundingBox().grow(followRange));
				if (mutantZombieEntity != null && !mutantZombieEntity.isAIDisabled()) {
					this.summonable.setSummonerUUID(mutantZombieEntity.getUniqueID());
//					MutantBeasts.LOGGER.debug(this.summonedMob.getName().getFormattedText() + " found new summoner");
				}
			}
		} else {
			if (!this.summoner.isAddedToWorld()) {
//				MutantBeasts.LOGGER.debug(this.summonedMob.getName().getFormattedText() + "'s summoner was removed from world");
				this.resetTask();
			} else {
				this.mobEntity.setIdleTime(this.summoner.getIdleTime());
				if (this.mobEntity.getRevengeTarget() == null && this.summoner.isAlive() && this.mobEntity.getAttackTarget() != this.summoner.getAttackTarget()) {
					this.mobEntity.setAttackTarget(this.summoner.getAttackTarget());
				}

				if (!this.mobEntity.detachHome() || this.mobEntity.getHomePosition() != this.summoner.getPosition()) {
					int i = (int)followRange;
					this.mobEntity.setHomePosAndDistance(this.summoner.getPosition(), this.mobEntity.getAttackTarget() != null ? i : i / 2);
				}
			}
		}
	}

	@Override
	public void resetTask() {
		this.summoner = null;
		this.summonable.setSummonerUUID(null);
		this.mobEntity.setHomePosAndDistance(BlockPos.ZERO, -1);
//		MutantBeasts.LOGGER.debug(this.summonedMob.getName().getString() + " is resetting track goal");
	}

	private MobEntity getSummoner() {
		if (this.summoner == null && this.summonable.getSummonerUUID() != null && this.mobEntity.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.mobEntity.world).getEntityByUuid(this.summonable.getSummonerUUID());
			if (entity instanceof MobEntity) {
				this.summoner = (MobEntity)entity;
//				MutantBeasts.LOGGER.debug(this.summonedMob.getName().getFormattedText() + " found summoner");
			} else {
				this.summonable.setSummonerUUID(null);
//				MutantBeasts.LOGGER.debug(this.summonedMob.getName().getFormattedText() + " summoner doesn't exist");
			}
		}

		return this.summoner;
	}
}