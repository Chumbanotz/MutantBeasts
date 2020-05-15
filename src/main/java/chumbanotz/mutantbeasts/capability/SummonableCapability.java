package chumbanotz.mutantbeasts.capability;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SummonableCapability implements ISummonable {
	private UUID summonerUUID;
	private boolean spawnedBySummoner;

	@Override
	@Nullable
	public UUID getSummonerUUID() {
		return this.summonerUUID;
	}

	@Override
	public void setSummonerUUID(@Nullable UUID summonerUUID) {
		this.summonerUUID = summonerUUID;
	}

	@Override
	public boolean isSpawnedBySummoner() {
		return this.spawnedBySummoner;
	}

	@Override
	public void setSpawnedBySummoner(boolean spawnedBySummoner) {
		this.spawnedBySummoner = spawnedBySummoner;
	}

	public static LazyOptional<ISummonable> getLazy(Entity entity) {
		return entity.getCapability(Provider.SUMMONABLE);
	}

	public static ISummonable get(Entity entity) {
		return getLazy(entity).orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for capability, must not be empty"));
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
			return SUMMONABLE.getStorage().writeNBT(SUMMONABLE, this.holder.orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for capability, must not be empty")), null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			SUMMONABLE.getStorage().readNBT(SUMMONABLE, this.holder.orElseThrow(() -> new IllegalStateException("Invalid LazyOptional for capability, must not be empty")), null, nbt);
		}
	}

	public static class Storage implements Capability.IStorage<ISummonable> {
		@Override
		public INBT writeNBT(Capability<ISummonable> capability, ISummonable instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			UUID uuid = instance.getSummonerUUID();
			if (uuid != null) {
				tag.putUniqueId("SummonerUUID", uuid);
			}

			tag.putBoolean("SpawnedBySummoner", instance.isSpawnedBySummoner());
			return tag;
		}

		@Override
		public void readNBT(Capability<ISummonable> capability, ISummonable instance, Direction side, INBT nbt) {
			if (nbt instanceof CompoundNBT) {
				CompoundNBT tag = (CompoundNBT)nbt;
				instance.setSummonerUUID(tag.getUniqueId("SummonerUUID"));
				instance.setSpawnedBySummoner(tag.getBoolean("SpawnedBySummoner"));
			}
		}
	}
}