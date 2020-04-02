package chumbanotz.mutantbeasts.capability;

import java.util.UUID;

import javax.annotation.Nullable;

public interface ISummonable {
	@Nullable
	UUID getSummonerUUID();

	void setSummonerUUID(@Nullable UUID summonerUUID);

	boolean isSpawnedBySummoner();

	void setSpawnedBySummoner(boolean spawnedBySummoner);
}