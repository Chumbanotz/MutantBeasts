package chumbanotz.mutantbeasts;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class MBConfig {
	static final ForgeConfigSpec COMMON_SPEC;
	private static final MBConfig.Common COMMON;
	public static int mutantCreeperSpawnWeight;
	public static int mutantEndermanSpawnWeight;
	public static int mutantSkeletonSpawnWeight;
	public static int mutantZombieSpawnWeight;
	public static List<String> biomeWhitelist;

	private static class Common {
		private final ForgeConfigSpec.IntValue mutantCreeperSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantEndermanSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantSkeletonSpawnWeight;
		private final ForgeConfigSpec.IntValue mutantZombieSpawnWeight;
		private final ForgeConfigSpec.ConfigValue<List<String>> biomeWhitelist;

		private Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Common configuration settings").push("common");
			this.mutantCreeperSpawnWeight = builder
					.comment("Mutant Creeper spawn weight", "Requires game restart")
					.worldRestart()
					.defineInRange("mutantCreeperSpawnWeight", 4, 0, 100);
			this.mutantEndermanSpawnWeight = builder
					.comment("Mutant Enderman spawn weight", "Requires game restart")
					.worldRestart()
					.defineInRange("mutantEndermanSpawnWeight", 3, 0, 100);
			this.mutantSkeletonSpawnWeight = builder
					.comment("Mutant Skeleton spawn weight", "Requires game restart")
					.worldRestart()
					.defineInRange("mutantSkeletonSpawnWeight", 4, 0, 100);
			this.mutantZombieSpawnWeight = builder
					.comment("Mutant Zombie spawn weight", "Requires game restart")
					.worldRestart()
					.defineInRange("mutantZombieSpawnWeight", 4, 0, 100);
			this.biomeWhitelist = builder
					.comment("Mutants will only spawn in the biomes from the given mod IDs",
							"Example - \"minecraft\", \"midnight\"",
							"You can see a mod's ID by clicking the 'Mods' button on the main screen and clicking on the mod's name on the left",
							"Requires game restart")
					.define("biomeWhitelist", Arrays.asList("minecraft"));
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
			mutantCreeperSpawnWeight = COMMON.mutantCreeperSpawnWeight.get();
			mutantEndermanSpawnWeight = COMMON.mutantEndermanSpawnWeight.get();
			mutantSkeletonSpawnWeight = COMMON.mutantSkeletonSpawnWeight.get();
			mutantZombieSpawnWeight = COMMON.mutantZombieSpawnWeight.get();
			biomeWhitelist = COMMON.biomeWhitelist.get();
		}
	}
}