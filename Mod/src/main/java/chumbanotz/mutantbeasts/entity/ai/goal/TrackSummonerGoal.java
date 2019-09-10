package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.UUID;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TrackSummonerGoal extends Goal {
	private final CreatureEntity creatureEntity;
	private MobEntity summoner;

	public TrackSummonerGoal(CreatureEntity creatureEntity) {
		this.creatureEntity = creatureEntity;
		if (!SummonableCapability.getFor(creatureEntity).isPresent()) {
			throw new IllegalArgumentException("Mob needs to have the SummonableCapability attached for this goal");
		}
	}

	@Override
	public boolean shouldExecute() {
		this.summoner = getSummoner();
		return this.summoner != null;
	}

	@Override
	public void tick() {
		if (this.summoner != null && !this.summoner.isAddedToWorld()) {
			this.summoner = null;
			SummonableCapability.get(this.creatureEntity).setSummoner(null);
		}

		if (this.summoner == null) {
			this.creatureEntity.setHomePosAndDistance(BlockPos.ZERO, -1);
		} else {
			this.creatureEntity.setIdleTime(this.summoner.getIdleTime());
			if (!this.creatureEntity.detachHome() || this.creatureEntity.getHomePosition() != this.summoner.getPosition()) {
				int i = (int)this.creatureEntity.getNavigator().getPathSearchRange();
				this.creatureEntity.setHomePosAndDistance(this.summoner.getPosition(), this.creatureEntity.getAttackTarget() != null ? i : i / 2);
			}
		}
	}

	@Override
	public void resetTask() {
		this.summoner = null;
		this.creatureEntity.setHomePosAndDistance(BlockPos.ZERO, -1);
		SummonableCapability.get(this.creatureEntity).setSummoner(null);
	}

	public MobEntity getSummoner() {
		MobEntity summoner = SummonableCapability.get(this.creatureEntity).getSummoner();
		UUID summonerUUID = SummonableCapability.get(this.creatureEntity).getSummonerUUID();
		if (summoner == null && summonerUUID != null && this.creatureEntity.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.creatureEntity.world).getEntityByUuid(summonerUUID);
			if (entity instanceof MobEntity) {
				MutantBeasts.LOGGER.debug(this.creatureEntity.getName().getString() + " has found summoner successfully");
				SummonableCapability.get(this.creatureEntity).setSummoner((MobEntity)entity);
			} else {
				MutantBeasts.LOGGER.debug(this.creatureEntity.getName().getString() + "'s summoner no longer exists, clearing summoner NBT");
				SummonableCapability.get(this.creatureEntity).setSummonerUUID(null);
			}
		}

		return summoner;
	}
}