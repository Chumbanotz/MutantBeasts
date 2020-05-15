package chumbanotz.mutantbeasts.entity;

import java.util.HashSet;
import java.util.Set;

import chumbanotz.mutantbeasts.MBConfig;
import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.RegistryHandler;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MutantBeasts.MOD_ID)
public class MBEntityType {
	private static final Set<EntityType<?>> ENTITY_TYPES = new HashSet<>();
	private static final Set<Item> SPAWN_EGGS = new HashSet<>();
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

	public static void registerSpawnEggs(RegistryEvent.Register<Item> event) {
		SPAWN_EGGS.forEach(event.getRegistry()::register);
	}

	public static void registerDispenseBehavior() {
		DefaultDispenseItemBehavior defaultdispenseitembehavior = new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				Direction direction = source.getBlockState().get(DispenserBlock.FACING);
				EntityType<?> entitytype = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
				entitytype.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
				stack.shrink(1);
				return stack;
			}
		};

		for (Item item : SPAWN_EGGS) {
			DispenserBlock.registerDispenseBehavior(item, defaultdispenseitembehavior);
		}

		SPAWN_EGGS.clear();
	}

	public static void register(RegistryEvent.Register<EntityType<?>> event) {
		ENTITY_TYPES.forEach(event.getRegistry()::register);
		ENTITY_TYPES.clear();
	}

	public static void initialize() {
		build("body_part", EntityType.Builder.<BodyPartEntity>create(BodyPartEntity::new, EntityClassification.MISC).setCustomClientFactory(BodyPartEntity::new).setTrackingRange(4).setUpdateInterval(10).size(0.7F, 0.7F));
		build("chemical_x", EntityType.Builder.<ChemicalXEntity>create(ChemicalXEntity::new, EntityClassification.MISC).setCustomClientFactory(ChemicalXEntity::new).setTrackingRange(10).setUpdateInterval(10).size(0.25F, 0.25F));
		build("creeper_minion", EntityType.Builder.create(CreeperMinionEntity::new, EntityClassification.MISC).size(0.3F, 0.84F), 894731, 12040119);
		build("creeper_minion_egg", EntityType.Builder.<CreeperMinionEggEntity>create(CreeperMinionEggEntity::new, EntityClassification.MISC).setCustomClientFactory(CreeperMinionEggEntity::new).setTrackingRange(10).setUpdateInterval(20).size(0.5625F, 0.75F));
		build("endersoul_clone", EntityType.Builder.create(EndersoulCloneEntity::new, EntityClassification.MISC).disableSerialization().size(0.6F, 2.9F));
		build("endersoul_fragment", EntityType.Builder.<EndersoulFragmentEntity>create(EndersoulFragmentEntity::new, EntityClassification.MISC).setCustomClientFactory(EndersoulFragmentEntity::new).setTrackingRange(4).setUpdateInterval(10).size(0.75F, 0.75F));
		build("mutant_arrow", EntityType.Builder.<MutantArrowEntity>create(MutantArrowEntity::new, EntityClassification.MISC).setCustomClientFactory(MutantArrowEntity::new));
		build("mutant_creeper", EntityType.Builder.create(MutantCreeperEntity::new, EntityClassification.MONSTER).size(1.6F, 2.8F), 5349438, 11013646);
		build("mutant_enderman", EntityType.Builder.create(MutantEndermanEntity::new, EntityClassification.MONSTER).size(1.2F, 4.2F), 1447446, 8860812);
		build("mutant_skeleton", EntityType.Builder.create(MutantSkeletonEntity::new, EntityClassification.MONSTER).size(1.2F, 3.6F), 12698049, 6310217);
		build("mutant_snow_golem", EntityType.Builder.create(MutantSnowGolemEntity::new, EntityClassification.MISC).size(1.05F, 2.2F), 15073279, 16753434);
		build("mutant_zombie", EntityType.Builder.create(MutantZombieEntity::new, EntityClassification.MONSTER).size(1.8F, 3.2F), 7969893, 44975);
		build("skull_spirit", EntityType.Builder.<SkullSpiritEntity>create(SkullSpiritEntity::new, EntityClassification.MISC).setCustomClientFactory(SkullSpiritEntity::new).setTrackingRange(10).setUpdateInterval(20).setShouldReceiveVelocityUpdates(false).size(0.1F, 0.1F));
		build("spider_pig", EntityType.Builder.create(SpiderPigEntity::new, EntityClassification.CREATURE).size(1.4F, 0.9F), 3419431, 15771042);
		build("throwable_block", EntityType.Builder.<ThrowableBlockEntity>create(ThrowableBlockEntity::new, EntityClassification.MISC).setCustomClientFactory(ThrowableBlockEntity::new).setTrackingRange(4).setUpdateInterval(100).size(1.0F, 1.0F));
	}

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

	private static <T extends Entity> EntityType<T> build(String name, EntityType.Builder<T> builder) {
		ResourceLocation registryName = MutantBeasts.prefix(name);
		EntityType<T> entityType = builder.build(registryName.toString());
		entityType.setRegistryName(registryName);
		ENTITY_TYPES.add(entityType);
		return entityType;
	}

	private static <T extends MobEntity> EntityType<T> build(String name, EntityType.Builder<T> builder, int eggPrimaryColor, int eggSecondaryColor) {
		EntityType<T> entityType = build(name, builder);
		Item item = RegistryHandler.setRegistryName(name + "_spawn_egg", new SpawnEggItem(entityType, eggPrimaryColor, eggSecondaryColor, RegistryHandler.defaultProperty()));
		SPAWN_EGGS.add(item);
		return entityType;
	}
}