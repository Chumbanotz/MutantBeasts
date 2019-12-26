package chumbanotz.mutantbeasts;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.item.ChemicalXItem;
import chumbanotz.mutantbeasts.item.CreeperShardItem;
import chumbanotz.mutantbeasts.item.EndersoulHandItem;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MutantSkeletonArmorItem;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
	public static final String[] SKELETON_PART_NAMES = new String[] {"limb", "rib", "pelvis", "shoulder_pad", "arms", "rib_cage"};

	@SubscribeEvent
	public static void onItemRegistry(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(
				setRegistryName("chemical_x", new ChemicalXItem(defaultProperty().rarity(Rarity.EPIC))),
				setRegistryName("creeper_minion_tracker", new Item(defaultProperty().maxStackSize(1))),
				setRegistryName("creeper_shard", new CreeperShardItem(defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("endersoul_hand", new EndersoulHandItem(defaultProperty().rarity(Rarity.EPIC))),
				setRegistryName("hulk_hammer", new HulkHammerItem(defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("mutant_skeleton_skull", new MutantSkeletonArmorItem(EquipmentSlotType.HEAD, defaultProperty().rarity(Rarity.UNCOMMON))),
				setRegistryName("mutant_skeleton_chestplate", new MutantSkeletonArmorItem(EquipmentSlotType.CHEST, defaultProperty())),
				setRegistryName("mutant_skeleton_leggings", new MutantSkeletonArmorItem(EquipmentSlotType.LEGS, defaultProperty())),
				setRegistryName("mutant_skeleton_boots", new MutantSkeletonArmorItem(EquipmentSlotType.FEET, defaultProperty()))
				);

		for (String partName : SKELETON_PART_NAMES) {
			event.getRegistry().register(setRegistryName("mutant_skeleton_" + partName, new Item(defaultProperty())));
		}

		MBEntityType.initialize();
		MBEntityType.registerSpawnEggs(event);
	}

	public static Item.Properties defaultProperty() {
		return new Item.Properties().group(MutantBeasts.ITEM_GROUP);
	}

	@SubscribeEvent
	public static void onEntityTypeRegistry(RegistryEvent.Register<EntityType<?>> event) {
		MBEntityType.register(event);
	}

	@SubscribeEvent
	public static void onParticleTypeRegistry(RegistryEvent.Register<ParticleType<?>> event) {
		event.getRegistry().registerAll(
				setRegistryName("large_portal", new BasicParticleType(false)),
				setRegistryName("skull_spirit", new BasicParticleType(true)));
	}

	@SubscribeEvent
	public static void onSoundEventRegistry(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().registerAll(
				create("entity.creeper_minion.ambient"),
				create("entity.creeper_minion.death"),
				create("entity.creeper_minion.hurt"),
				create("entity.creeper_minion.primed"),
				create("entity.creeper_minion_egg.hatch"),
				create("entity.mutant_creeper.ambient"),
				create("entity.mutant_creeper.charge"),
				create("entity.mutant_creeper.death"),
				create("entity.mutant_creeper.hurt"),
				create("entity.mutant_enderman.ambient"),
				create("entity.mutant_enderman.death"),
				create("entity.mutant_enderman.hurt"),
				create("entity.mutant_enderman.scream"),
				create("entity.mutant_enderman.stare"),
				create("entity.mutant_enderman.teleport"),
				create("entity.mutant_skeleton.ambient"),
				create("entity.mutant_skeleton.death"),
				create("entity.mutant_skeleton.hurt"),
				create("entity.mutant_skeleton.step"),
				create("entity.mutant_snow_golem.death"),
				create("entity.mutant_snow_golem.hurt"),
				create("entity.mutant_zombie.ambient"),
				create("entity.mutant_zombie.attack"),
				create("entity.mutant_zombie.death"),
				create("entity.mutant_zombie.grunt"),
				create("entity.mutant_zombie.hurt"),
				create("entity.mutant_zombie.roar"),
				create("entity.spider_pig.ambient"),
				create("entity.spider_pig.death"),
				create("entity.spider_pig.hurt")
				);
	}

	private static SoundEvent create(String name) {
		return setRegistryName(name, new SoundEvent(MutantBeasts.prefix(name)));
	}

	public static <T extends IForgeRegistryEntry<T>> T setRegistryName(String name, T entry) {
		return entry.setRegistryName(MutantBeasts.prefix(name));
	}
}