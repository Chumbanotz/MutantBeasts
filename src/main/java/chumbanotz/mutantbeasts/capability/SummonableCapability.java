package chumbanotz.mutantbeasts.capability;

import java.util.UUID;
import java.util.concurrent.Callable;

import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SummonableCapability implements ISummonable {
	private MobEntity summoner;
	private UUID summonerUUID;
	private boolean spawnedBySummoner;

	@Override
	public MobEntity getSummoner() {
		return this.summoner;
	}

	@Override
	public void setSummoner(MobEntity summoner) {
		this.summoner = summoner;
		this.summonerUUID = summoner == null ? null : summoner.getUniqueID();
	}

	@Override
	public UUID getSummonerUUID() {
		return this.summonerUUID;
	}

	@Override
	public void setSummonerUUID(UUID uuid) {
		this.summonerUUID = uuid;
	}

	@Override
	public boolean isSpawnedBySummoner() {
		return this.spawnedBySummoner;
	}

	@Override
	public void setSpawnedBySummoner(boolean spawnedBySummoner) {
		this.spawnedBySummoner = spawnedBySummoner;
	}

	@Override
	public MobEntity findSummoner(World world) {
		if (summoner == null && summonerUUID != null && world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)world).getEntityByUuid(summonerUUID);
			if (entity instanceof MutantZombieEntity) {
				summoner = (MobEntity)entity;
			} else {
				summonerUUID = null;
			}
		}

		return summoner;
	}

	public static LazyOptional<ISummonable> getLazy(Entity entity) {
		return entity.getCapability(Provider.SUMMONABLE);
	}

	public static ISummonable get(Entity entity) {
		return entity.getCapability(Provider.SUMMONABLE).orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for " + ID + " capability, must not be empty"));
	}

	public static boolean isEntityEligible(EntityType<?> type) {
		return type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.DROWNED;
	}

	public static class Provider implements ICapabilitySerializable<INBT> {
		@CapabilityInject(ISummonable.class)
		public static final Capability<ISummonable> SUMMONABLE = null;
		private final LazyOptional<ISummonable> holder = LazyOptional.of(SUMMONABLE::getDefaultInstance);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return SUMMONABLE.orEmpty(cap, this.holder);
		}

		@Override
		public INBT serializeNBT() {
			return SUMMONABLE.getStorage().writeNBT(SUMMONABLE, this.holder.orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for " + ID + " capability, must not be empty")), null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			SUMMONABLE.getStorage().readNBT(SUMMONABLE, this.holder.orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for " + ID + " capability, must not be empty")), null, nbt);
		}
	}

	public static class Storage implements Capability.IStorage<ISummonable> {
		@Override
		public INBT writeNBT(Capability<ISummonable> capability, ISummonable instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean("SpawnedBySummoner", instance.isSpawnedBySummoner());
			UUID uuid = instance.getSummonerUUID();
			if (uuid != null) {
				tag.putUniqueId("SummonerUUID", uuid);
			}

			return tag;
		}

		@Override
		public void readNBT(Capability<ISummonable> capability, ISummonable instance, Direction side, INBT nbt) {
			if (nbt instanceof CompoundNBT) {
				CompoundNBT tag = (CompoundNBT)nbt;
				instance.setSpawnedBySummoner(tag.getBoolean("SpawnedBySummoner"));
				UUID uuid = tag.getUniqueId("SummonerUUID");
				if (uuid != null) {
					instance.setSummonerUUID(uuid);
				}
			}
		}
	}

	public static class Factory implements Callable<ISummonable> {
		@Override
		public ISummonable call() throws Exception {
			return new SummonableCapability();
		}
	}
}