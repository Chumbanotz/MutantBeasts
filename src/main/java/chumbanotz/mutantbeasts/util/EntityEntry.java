package chumbanotz.mutantbeasts.util;

import static net.minecraft.entity.EntityType.Builder.create;

import java.util.HashSet;
import java.util.Set;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.RegistryHandler;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.EndermanCloneEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonPartEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantSnowGolemBlockEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Direction;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public final class EntityEntry<T extends Entity> {
	public static final Set<EntityType<?>> ENTITY_TYPES = new HashSet<>();
	public static final Set<SpawnEggItem> SPAWN_EGGS = new HashSet<>();
	private final EntityType<T> entityType;
	private final EntityClassification classification;
	private EntitySpawnPlacementRegistry.PlacementType placementType;
	private Heightmap.Type heightMapType;
	private EntitySpawnPlacementRegistry.IPlacementPredicate<T> placementPredicate;

	private EntityEntry(String name, EntityType.Builder<T> builder) {
		this.entityType = RegistryHandler.setRegistryName(name, builder.build(MutantBeasts.MOD_ID + ':' + name));
		this.classification = this.entityType.getClassification();
		ENTITY_TYPES.add(this.entityType);
	}

	public static void construct() {
		add("creeper_minion", create(CreeperMinionEntity::new, EntityClassification.MISC).size(0.6F, 1.7F), 894731, 12040119);
		add("enderman_clone", create(EndermanCloneEntity::new, EntityClassification.MONSTER).size(0.6F, 2.9F));
		add("mutant_creeper", create(MutantCreeperEntity::new, EntityClassification.MONSTER).size(1.4F, 2.7F), 5349438, 11013646);
		add("mutant_enderman", create(MutantEndermanEntity::new, EntityClassification.MONSTER).size(1.2F, 4.8F), 1447446, 8860812);
		add("mutant_skeleton", create(MutantSkeletonEntity::new, EntityClassification.MONSTER).size(1.2F, 3.6F), 12698049, 6310217);
		add("mutant_skeleton_arrow", create(EntityClassification.MISC).setCustomClientFactory(MutantArrowEntity::new).setTrackingRange(80).setUpdateInterval(3));
		add("mutant_skeleton_part", create(EntityClassification.MISC).setCustomClientFactory(MutantSkeletonPartEntity::new).setTrackingRange(64).setUpdateInterval(10).size(0.7F, 0.7F));
		add("mutant_snow_golem", create(MutantSnowGolemEntity::new, EntityClassification.MISC).size(0.9F, 2.2F), 15073279, 16753434);
		add("mutant_snow_golem_block", create(EntityClassification.MISC).setCustomClientFactory(MutantSnowGolemBlockEntity::new).setTrackingRange(64).setUpdateInterval(10).size(1.0F, 1.0F));
		add("mutant_zombie", create(MutantZombieEntity::new, EntityClassification.MONSTER).size(1.8F, 3.2F), 7969893, 44975);
		add("spider_pig", create(SpiderPigEntity::new, EntityClassification.CREATURE).size(1.4F, 0.9F), 3419431, 15771042);
	}

	public static <T extends MobEntity> EntityEntry<T> add(String name, EntityType.Builder<T> builder, int eggPrimaryColor, int eggSecondaryColor) {
		EntityEntry<T> entry = new EntityEntry<>(name, builder);
		EntitySpawnPlacementRegistry.register(entry.entityType, entry.placementType, entry.heightMapType, entry.placementPredicate);
		SpawnEggItem spawnEgg = RegistryHandler.setRegistryName(name + "_spawn_egg", new SpawnEggItem(entry.entityType, eggPrimaryColor, eggSecondaryColor, new Item.Properties().group(MutantBeasts.ITEM_GROUP)));
		SPAWN_EGGS.add(spawnEgg);
		DispenserBlock.registerDispenseBehavior(spawnEgg, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				Direction direction = source.getBlockState().get(DispenserBlock.FACING);
				EntityType<?> entitytype = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
				entitytype.spawn(source.getWorld(), stack, (PlayerEntity)null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
				stack.shrink(1);
				return stack;
			}
		});

		return entry;
	}

	public static <T extends Entity> EntityEntry<T> add(String name, EntityType.Builder<T> builder) {
		return new EntityEntry<>(name, builder);
	}

	public EntityEntry<T> setSpawnPlacementType(EntitySpawnPlacementRegistry.PlacementType placementType) {
		this.placementType = placementType;
		return this;
	}

	public EntityEntry<T> setHeightmapType(Heightmap.Type heightmapType) {
		this.heightMapType = heightmapType;
		return this;
	}

	public EntityEntry<T> setPlacementPredicate(EntitySpawnPlacementRegistry.IPlacementPredicate<T> placementPredicate) {
		this.placementPredicate = placementPredicate;
		return this;
	}

	public EntityEntry<T> copySpawns(EntityType<?> type, float weightMultiplier, int min, int max) {
		for (Biome biome : ForgeRegistries.BIOMES) {
			biome.getSpawns(type.getClassification()).stream().filter(entry -> entry.entityType == type).findFirst().ifPresent(spawnListEntry -> biome.getSpawns(this.classification).add(new Biome.SpawnListEntry(this.entityType, spawnListEntry.itemWeight * (int)weightMultiplier, min, max)));
		}

		return this;
	}

	public EntityEntry<T> addSpawn(int weight, int min, int max, Biome... biomes) {
		for (Biome biome : biomes) {
			if (biome != null) {
				biome.getSpawns(this.classification).add(new Biome.SpawnListEntry(this.entityType, weight, min, max));
			}
		}

		return this;
	}
}