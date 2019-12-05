package chumbanotz.mutantbeasts.capability;

import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public interface ISummonable {
	ResourceLocation ID = MutantBeasts.prefix("summonable");

	@Nullable
	MobEntity getSummoner();

	void setSummoner(@Nullable MobEntity summoner);

	@Nullable
	UUID getSummonerUUID();

	void setSummonerUUID(@Nullable UUID uuid);

	boolean isSpawnedBySummoner();

	void setSpawnedBySummoner(boolean spawnedBySummoner);

	MobEntity findSummoner(World world);
}