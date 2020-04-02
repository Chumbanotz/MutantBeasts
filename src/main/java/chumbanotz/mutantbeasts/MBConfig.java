package chumbanotz.mutantbeasts;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class MBConfig {
	static final ForgeConfigSpec COMMON_SPEC;
	private static final MBConfig.Common COMMON;
	public static int globalSpawnRate = 10;
	public static int mutantCreeperSpawnWeight = 1;
	public static int mutantEndermanSpawnWeight = 1;
	public static int mutantSkeletonSpawnWeight = 1;
	public static int mutantZombieSpawnWeight = 1;

	private static class Common {
		private final ForgeConfigSpec.IntValue globalSpawnRate;
		private final ForgeConfigSpec.IntValue mutantCreeperSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantEndermanSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantSkeletonSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantZombieSpawnWeight;

		private Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Common configuration settings").push("common");
			this.globalSpawnRate = builder
					.comment("Affects spawn rate of all mutants", "The smaller the number, the lower the chance")
					.defineInRange("globalSpawnRate", 10, 1, 20);
			this.mutantCreeperSpawnWeight = builder
					.comment("Mutant Creeper spawn weight")
					.worldRestart()
					.defineInRange("mutantCreeperSpawnWeight", 1, 0, 100);
			this.mutantEndermanSpawnWeight = builder
					.comment("Mutant Enderman spawn weight")
					.worldRestart()
					.defineInRange("mutantEndermanSpawnWeight", 1, 0, 100);
			this.mutantSkeletonSpawnWeight = builder
					.comment("Mutant Skeleton spawn weight")
					.worldRestart()
					.defineInRange("mutantSkeletonSpawnWeight", 1, 0, 100);
			this.mutantZombieSpawnWeight = builder
					.comment("Mutant Zombie spawn weight")
					.worldRestart()
					.defineInRange("mutantZombieSpawnWeight", 1, 0, 100);
			builder.pop();
		}
	}

	static {
		Pair<MBConfig.Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(MBConfig.Common::new);
		COMMON_SPEC = commonPair.getRight();
		COMMON = commonPair.getLeft();
	}

	public static void bake(ForgeConfigSpec spec) {
		if (spec == COMMON_SPEC) {
			globalSpawnRate = COMMON.globalSpawnRate.get();
			mutantCreeperSpawnWeight = COMMON.mutantCreeperSpawnWeight.get();
			mutantEndermanSpawnWeight = COMMON.mutantEndermanSpawnWeight.get();
			mutantSkeletonSpawnWeight = COMMON.mutantSkeletonSpawnWeight.get();
			mutantZombieSpawnWeight = COMMON.mutantZombieSpawnWeight.get();
		}
	}
}