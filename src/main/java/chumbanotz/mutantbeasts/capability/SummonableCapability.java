package chumbanotz.mutantbeasts.capability;

import java.util.UUID;
import java.util.concurrent.Callable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SummonableCapability implements ISummonable {
	private MobEntity summoner;
	private UUID summonerUUID;

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

	public static LazyOptional<ISummonable> getFor(MobEntity mobEntity) {
		return isEntityEligible(mobEntity.getType()) && mobEntity.getCapability(Provider.SUMMONABLE).isPresent() ? mobEntity.getCapability(Provider.SUMMONABLE) : LazyOptional.empty();
	}

	public static ISummonable get(MobEntity mobEntity) {
		return getFor(mobEntity).orElse(Provider.SUMMONABLE.getDefaultInstance());
	}

	public static boolean isEntityEligible(EntityType<?> type) {
		return type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.DROWNED;
	}

	public static class Provider implements ICapabilitySerializable<INBT> {
		@CapabilityInject(ISummonable.class)
		public static final Capability<ISummonable> SUMMONABLE = null;
		private ISummonable instance = SUMMONABLE.getDefaultInstance();
		private final LazyOptional<ISummonable> holder = LazyOptional.of(() -> this.instance);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return SUMMONABLE.orEmpty(cap, this.holder);
		}

		@Override
		public INBT serializeNBT() {
			return SUMMONABLE.getStorage().writeNBT(SUMMONABLE, this.instance, null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			SUMMONABLE.getStorage().readNBT(SUMMONABLE, this.instance, null, nbt);
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

			return tag;
		}

		@Override
		public void readNBT(Capability<ISummonable> capability, ISummonable instance, Direction side, INBT nbt) {
			if (nbt instanceof CompoundNBT) {
				CompoundNBT tag = (CompoundNBT)nbt;
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