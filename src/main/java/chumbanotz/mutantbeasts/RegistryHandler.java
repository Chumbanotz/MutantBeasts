package chumbanotz.mutantbeasts;

import java.util.HashMap;
import java.util.Map;

import chumbanotz.mutantbeasts.entity.BodyPartEntity;
import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.EndersoulCloneEntity;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.SkullSpiritEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.item.ChemicalXItem;
import chumbanotz.mutantbeasts.item.CreeperShardItem;
import chumbanotz.mutantbeasts.item.EndersoulHandItem;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MBArmorMaterial;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.item.SkeletonArmorItem;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
	private static final Map<EntityType<?>, SpawnEggItem> SPAWN_EGGS = new HashMap<>();

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(
				setRegistryName("chemical_x", new ChemicalXItem(defaultProperty().rarity(Rarity.EPIC))),
				setRegistryName("creeper_minion_tracker", new Item(defaultProperty().maxStackSize(1))),
				setRegistryName("creeper_shard", new CreeperShardItem(defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("endersoul_hand", new EndersoulHandItem(defaultProperty().rarity(Rarity.EPIC))),
				setRegistryName("hulk_hammer", new HulkHammerItem(defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("mutant_skeleton_arms", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_limb", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_pelvis", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_rib", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_rib_cage", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_shoulder_pad", new Item(defaultProperty())),
				setRegistryName("mutant_skeleton_skull", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.HEAD, defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("mutant_skeleton_chestplate", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.CHEST, defaultProperty())),
				setRegistryName("mutant_skeleton_leggings", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.LEGS, defaultProperty())),
				setRegistryName("mutant_skeleton_boots", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.FEET, defaultProperty()))
				);

		build("creeper_minion", EntityType.Builder.create(CreeperMinionEntity::new, EntityClassification.MISC).size(0.3F, 0.84F), 894731, 12040119);
		build("endersoul_clone", EntityType.Builder.create(EndersoulCloneEntity::new, EntityClassification.MONSTER).size(0.6F, 2.9F), 15027455, 15027455);
		build("mutant_creeper", EntityType.Builder.create(MutantCreeperEntity::new, EntityClassification.MONSTER).size(1.98F, 2.8F), 5349438, 11013646);
		build("mutant_enderman", EntityType.Builder.create(MutantEndermanEntity::new, EntityClassification.MONSTER).size(1.2F, 4.2F), 1447446, 8860812);
		build("mutant_skeleton", EntityType.Builder.<MutantSkeletonEntity>create(MutantSkeletonEntity::new, EntityClassification.MONSTER).setCustomClientFactory(MutantSkeletonEntity::new).size(1.2F, 3.6F), 12698049, 6310217);
		build("mutant_snow_golem", EntityType.Builder.create(MutantSnowGolemEntity::new, EntityClassification.MISC).size(1.1F, 2.2F), 15073279, 16753434);
		build("mutant_zombie", EntityType.Builder.<MutantZombieEntity>create(MutantZombieEntity::new, EntityClassification.MONSTER).setCustomClientFactory(MutantZombieEntity::new).size(1.8F, 3.2F), 7969893, 44975);
		build("spider_pig", EntityType.Builder.create(SpiderPigEntity::new, EntityClassification.CREATURE).size(1.4F, 0.9F), 3419431, 15771042);

		for (SpawnEggItem spawnEggItem : SPAWN_EGGS.values()) {
			event.getRegistry().register(spawnEggItem);
		}
	}

	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event) {
		for (EntityType<?> entityType : SPAWN_EGGS.keySet()) {
			event.getRegistry().register(entityType);
		}

		event.getRegistry().registerAll(
				build("body_part", EntityType.Builder.<BodyPartEntity>create(BodyPartEntity::new, EntityClassification.MISC).setCustomClientFactory(BodyPartEntity::new).setTrackingRange(4).setUpdateInterval(10).size(0.7F, 0.7F)),
				build("chemical_x", EntityType.Builder.<ChemicalXEntity>create(ChemicalXEntity::new, EntityClassification.MISC).setCustomClientFactory(ChemicalXEntity::new).setTrackingRange(10).setUpdateInterval(10).size(0.25F, 0.25F)),
				build("creeper_minion_egg", EntityType.Builder.<CreeperMinionEggEntity>create(CreeperMinionEggEntity::new, EntityClassification.MISC).setCustomClientFactory(CreeperMinionEggEntity::new).setTrackingRange(10).setUpdateInterval(20).size(0.5625F, 0.75F)),
				build("endersoul_fragment", EntityType.Builder.<EndersoulFragmentEntity>create(EndersoulFragmentEntity::new, EntityClassification.MISC).setCustomClientFactory(EndersoulFragmentEntity::new).setTrackingRange(64).setUpdateInterval(10).size(0.75F, 0.75F)),
				build("mutant_arrow", EntityType.Builder.<MutantArrowEntity>create(MutantArrowEntity::new, EntityClassification.MISC).setCustomClientFactory(MutantArrowEntity::new).setShouldReceiveVelocityUpdates(false).disableSerialization()),
				build("skull_spirit", EntityType.Builder.<SkullSpiritEntity>create(SkullSpiritEntity::new, EntityClassification.MISC).setCustomClientFactory(SkullSpiritEntity::new).setTrackingRange(10).setUpdateInterval(20).setShouldReceiveVelocityUpdates(false).size(0.1F, 0.1F)),
				build("throwable_block", EntityType.Builder.<ThrowableBlockEntity>create(ThrowableBlockEntity::new, EntityClassification.MISC).setCustomClientFactory(ThrowableBlockEntity::new).setTrackingRange(4).setUpdateInterval(100).size(1.0F, 1.0F))
				);
	}

	private static <T extends MobEntity> EntityType<T> build(String name, EntityType.Builder<T> builder, int eggPrimaryColor, int eggSecondaryColor) {
		EntityType<T> entityType = build(name, builder);
		SPAWN_EGGS.put(entityType, setRegistryName(name + "_spawn_egg", new SpawnEggItem(entityType, eggPrimaryColor, eggSecondaryColor, defaultProperty())));
		return entityType;
	}

	private static <T extends Entity> EntityType<T> build(String name, EntityType.Builder<T> builder) {
		ResourceLocation registryName = MutantBeasts.prefix(name);
		EntityType<T> entityType = builder.build(registryName.toString());
		entityType.setRegistryName(registryName);
		return entityType;
	}

	@SubscribeEvent
	public static void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
		event.getRegistry().registerAll(
				setRegistryName("endersoul", new BasicParticleType(false)),
				setRegistryName("skull_spirit", new BasicParticleType(true))
				);
	}

	@SubscribeEvent
	public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().registerAll(
				createSoundEvent("entity.creeper_minion.ambient"),
				createSoundEvent("entity.creeper_minion.death"),
				createSoundEvent("entity.creeper_minion.hurt"),
				createSoundEvent("entity.creeper_minion.primed"),
				createSoundEvent("entity.creeper_minion_egg.hatch"),
				createSoundEvent("entity.endersoul_clone.death"),
				createSoundEvent("entity.endersoul_clone.teleport"),
				createSoundEvent("entity.endersoul_fragment.explode"),
				createSoundEvent("entity.mutant_creeper.ambient"),
				createSoundEvent("entity.mutant_creeper.charge"),
				createSoundEvent("entity.mutant_creeper.death"),
				createSoundEvent("entity.mutant_creeper.hurt"),
				createSoundEvent("entity.mutant_creeper.primed"),
				createSoundEvent("entity.mutant_enderman.ambient"),
				createSoundEvent("entity.mutant_enderman.death"),
				createSoundEvent("entity.mutant_enderman.hurt"),
				createSoundEvent("entity.mutant_enderman.morph"),
				createSoundEvent("entity.mutant_enderman.scream"),
				createSoundEvent("entity.mutant_enderman.stare"),
				createSoundEvent("entity.mutant_enderman.teleport"),
				createSoundEvent("entity.mutant_skeleton.ambient"),
				createSoundEvent("entity.mutant_skeleton.death"),
				createSoundEvent("entity.mutant_skeleton.hurt"),
				createSoundEvent("entity.mutant_skeleton.step"),
				createSoundEvent("entity.mutant_snow_golem.death"),
				createSoundEvent("entity.mutant_snow_golem.hurt"),
				createSoundEvent("entity.mutant_zombie.ambient"),
				createSoundEvent("entity.mutant_zombie.attack"),
				createSoundEvent("entity.mutant_zombie.death"),
				createSoundEvent("entity.mutant_zombie.grunt"),
				createSoundEvent("entity.mutant_zombie.hurt"),
				createSoundEvent("entity.mutant_zombie.roar"),
				createSoundEvent("entity.spider_pig.ambient"),
				createSoundEvent("entity.spider_pig.death"),
				createSoundEvent("entity.spider_pig.hurt")
				);
	}

	@SubscribeEvent
	public static void remapParticleTypes(RegistryEvent.MissingMappings<ParticleType<?>> event) {
		for (RegistryEvent.MissingMappings.Mapping<ParticleType<?>> mapping : event.getMappings()) {
			if (mapping.key.getPath().equals("large_portal")) {
				mapping.remap(MBParticleTypes.ENDERSOUL);
			}
		}
	}

	@SubscribeEvent
	public static void remapBlocks(RegistryEvent.MissingMappings<Block> event) {
		for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings()) {
			String path = mapping.key.getPath();
			if (path.equals("mutant_skeleton_skull") || path.equals("mutant_skeleton_wall_skull")) {
				mapping.ignore();
			}
		}
	}

	@SubscribeEvent
	public static void remapTileEntityTypes(RegistryEvent.MissingMappings<TileEntityType<?>> event) {
		for (RegistryEvent.MissingMappings.Mapping<TileEntityType<?>> mapping : event.getMappings()) {
			if (mapping.key.getPath().equals("skull")) {
				mapping.ignore();
			}
		}
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

		for (SpawnEggItem spawnEggItem : SPAWN_EGGS.values()) {
			DispenserBlock.registerDispenseBehavior(spawnEggItem, defaultdispenseitembehavior);
		}

		SPAWN_EGGS.clear();
		DispenserBlock.registerDispenseBehavior(MBItems.CHEMICAL_X, (blockSource, itemStack) -> {
			return new ProjectileDispenseBehavior() {
				@Override
				protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
					return Util.make(new ChemicalXEntity(position.getX(), position.getY(), position.getZ(), worldIn), (e) -> e.setItem(stackIn));
				}

				@Override
				protected float getProjectileInaccuracy() {
					return super.getProjectileInaccuracy() * 0.5F;
				}

				@Override
				protected float getProjectileVelocity() {
					return super.getProjectileVelocity() * 1.25F;
				}
			}.dispense(blockSource, itemStack);
		});
	}

	private static SoundEvent createSoundEvent(String name) {
		ResourceLocation resourceLocation = MutantBeasts.prefix(name);
		return new SoundEvent(resourceLocation).setRegistryName(resourceLocation);
	}

	private static Item.Properties defaultProperty() {
		return new Item.Properties().group(MutantBeasts.ITEM_GROUP);
	}

	private static <T extends IForgeRegistryEntry<?>> T setRegistryName(String name, T entry) {
		entry.setRegistryName(MutantBeasts.prefix(name));
		return entry;
	}
}