package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.MBConfig;
import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MutantBeasts.MOD_ID)
public class MBEntityType {
	public static final EntityType<BodyPartEntity> BODY_PART = null;
	public static final EntityType<ChemicalXEntity> CHEMICAL_X = null;
	public static final EntityType<CreeperMinionEntity> CREEPER_MINION = null;
	public static final EntityType<CreeperMinionEggEntity> CREEPER_MINION_EGG = null;
	public static final EntityType<EndersoulCloneEntity> ENDERSOUL_CLONE = null;
	public static final EntityType<EndersoulFragmentEntity> ENDERSOUL_FRAGMENT = null;
	public static final EntityType<MutantArrowEntity> MUTANT_ARROW = null;
	public static final EntityType<MutantCreeperEntity> MUTANT_CREEPER = null;
	public static final EntityType<MutantEndermanEntity> MUTANT_ENDERMAN = null;
	public static final EntityType<MutantSnowGolemEntity> MUTANT_SNOW_GOLEM = null;
	public static final EntityType<MutantSkeletonEntity> MUTANT_SKELETON = null;
	public static final EntityType<MutantZombieEntity> MUTANT_ZOMBIE = null;
	public static final EntityType<SkullSpiritEntity> SKULL_SPIRIT = null;
	public static final EntityType<SpiderPigEntity> SPIDER_PIG = null;
	public static final EntityType<ThrowableBlockEntity> THROWABLE_BLOCK = null;

	public static void addSpawns() {
		for (Biome biome : ForgeRegistries.BIOMES) {
			if (biome.getCategory() != Biome.Category.MUSHROOM && biome != Biomes.THE_VOID) {
				addSpawn(biome, MUTANT_ENDERMAN, MBConfig.mutantEndermanSpawnWeight, 1, 1);
				if (biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.NETHER) {
					addSpawn(biome, MUTANT_CREEPER, MBConfig.mutantCreeperSpawnWeight, 1, 1);
					addSpawn(biome, MUTANT_SKELETON, MBConfig.mutantSkeletonSpawnWeight, 1, 1);
					addSpawn(biome, MUTANT_ZOMBIE, MBConfig.mutantZombieSpawnWeight, 1, 1);
				}
			}
		}

		EntitySpawnPlacementRegistry.register(CREEPER_MINION, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		EntitySpawnPlacementRegistry.register(MUTANT_CREEPER, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::func_223325_c);
		EntitySpawnPlacementRegistry.register(MUTANT_ENDERMAN, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MutantEndermanEntity::canSpawn);
		EntitySpawnPlacementRegistry.register(MUTANT_SKELETON, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::func_223325_c);
		EntitySpawnPlacementRegistry.register(MUTANT_SNOW_GOLEM, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		EntitySpawnPlacementRegistry.register(MUTANT_ZOMBIE, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::func_223325_c);
		EntitySpawnPlacementRegistry.register(SPIDER_PIG, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::func_223316_b);
	}

	private static void addSpawn(Biome biome, EntityType<? extends MobEntity> entityType, int weight, int min, int max) {
		if (weight > 0) {
			biome.getSpawns(entityType.getClassification()).add(new Biome.SpawnListEntry(entityType, weight, min, max));
		}
	}
}