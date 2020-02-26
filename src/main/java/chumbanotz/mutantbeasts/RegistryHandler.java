package chumbanotz.mutantbeasts;

import chumbanotz.mutantbeasts.block.MBBlocks;
import chumbanotz.mutantbeasts.block.MBSkullBlock;
import chumbanotz.mutantbeasts.block.MBWallSkullBlock;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.item.ArmorBlockItem;
import chumbanotz.mutantbeasts.item.ChemicalXItem;
import chumbanotz.mutantbeasts.item.CreeperShardItem;
import chumbanotz.mutantbeasts.item.EndersoulHandItem;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MBArmorMaterial;
import chumbanotz.mutantbeasts.item.SkeletonArmorItem;
import chumbanotz.mutantbeasts.tileentity.MBSkullTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(
				setRegistryName("mutant_skeleton_skull", new MBSkullBlock(MBSkullBlock.Types.MUTANT_SKELETON, Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(1.0F))),
				setRegistryName("mutant_skeleton_wall_skull", new MBWallSkullBlock(MBSkullBlock.Types.MUTANT_SKELETON, Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(1.0F)))
				);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		MBEntityType.initialize();
		MBEntityType.registerSpawnEggs(event);
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
				setRegistryName("mutant_skeleton_skull", new ArmorBlockItem(MBArmorMaterial.MUTANT_SKELETON, MBBlocks.MUTANT_SKELETON_SKULL, MBBlocks.MUTANT_SKELETON_WALL_SKULL, defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("mutant_skeleton_chestplate", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.CHEST, defaultProperty())),
				setRegistryName("mutant_skeleton_leggings", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.LEGS, defaultProperty())),
				setRegistryName("mutant_skeleton_boots", new SkeletonArmorItem(MBArmorMaterial.MUTANT_SKELETON, EquipmentSlotType.FEET, defaultProperty()))
				);
	}

	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event) {
		MBEntityType.register(event);
	}

	@SubscribeEvent
	public static void registerTileEntityTypes(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(
				setRegistryName("skull", TileEntityType.Builder.create(MBSkullTileEntity::new, MBBlocks.MUTANT_SKELETON_SKULL, MBBlocks.MUTANT_SKELETON_WALL_SKULL).build(null))
				);
		
	}

	@SubscribeEvent
	public static void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
		event.getRegistry().registerAll(
				setRegistryName("skull_spirit", new BasicParticleType(true)),
				setRegistryName("large_portal", new BasicParticleType(false))
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
				createSoundEvent("entity.endersoul_fragment.explode"),
				createSoundEvent("entity.mutant_creeper.ambient"),
				createSoundEvent("entity.mutant_creeper.charge"),
				createSoundEvent("entity.mutant_creeper.death"),
				createSoundEvent("entity.mutant_creeper.hurt"),
				createSoundEvent("entity.mutant_enderman.ambient"),
				createSoundEvent("entity.mutant_enderman.death"),
				createSoundEvent("entity.mutant_enderman.hurt"),
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

	private static SoundEvent createSoundEvent(String name) {
		return setRegistryName(name, new SoundEvent(MutantBeasts.prefix(name)));
	}

	@SubscribeEvent
	public static void onModConfigEvent(ModConfig.ModConfigEvent event) {
		MBConfig.bake(event.getConfig().getSpec());
	}

	public static Item.Properties defaultProperty() {
		return new Item.Properties().group(MutantBeasts.ITEM_GROUP);
	}

	public static <T extends IForgeRegistryEntry<T>> T setRegistryName(String name, T entry) {
		return entry.setRegistryName(MutantBeasts.prefix(name));
	}
}