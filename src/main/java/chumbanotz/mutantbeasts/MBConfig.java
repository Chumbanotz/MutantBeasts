package chumbanotz.mutantbeasts;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class MBConfig {
	static final ForgeConfigSpec COMMON_SPEC;
	private static final MBConfig.Common COMMON;
	public static int globalSpawnRate;

	static class Common {
		private final ForgeConfigSpec.IntValue globalSpawnRate;

		Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Common configuration settings").push("common");
			globalSpawnRate = builder
					.comment("Affects spawn rate of all mutants", "The smaller the number, the lower the chance")
					.defineInRange("globalSpawnRate", 10, 1, 20);
			builder.pop();
		}
	}

	static {
		final Pair<MBConfig.Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(MBConfig.Common::new);
		COMMON_SPEC = commonPair.getRight();
		COMMON = commonPair.getLeft();
	}

	public static void bake(ForgeConfigSpec spec) {
		if (spec == COMMON_SPEC) {
			globalSpawnRate = COMMON.globalSpawnRate.get();
		}
	}
}