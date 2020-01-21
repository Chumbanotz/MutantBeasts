package chumbanotz.mutantbeasts;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
//	static final ForgeConfigSpec CLIENT_SPEC;
//	private static final Config.Client CLIENT;
	static final ForgeConfigSpec COMMON_SPEC;
	private static final Config.Common COMMON;
	public static int globalSpawnRate;

	static class Client {

		Client(ForgeConfigSpec.Builder builder) {
		}
	}

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
//		final Pair<Config.Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Config.Client::new);
//		CLIENT_SPEC = clientPair.getRight();
//		CLIENT = clientPair.getLeft();
		final Pair<Config.Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Config.Common::new);
		COMMON_SPEC = commonPair.getRight();
		COMMON = commonPair.getLeft();
	}

	public static void bake(ForgeConfigSpec spec) {
		if (spec == COMMON_SPEC) {
			System.out.println(globalSpawnRate);
			globalSpawnRate = COMMON.globalSpawnRate.get();
		}
	}
}