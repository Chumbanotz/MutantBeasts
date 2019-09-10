package chumbanotz.mutantbeasts;

import chumbanotz.mutantbeasts.item.ChemicalXItem;
import chumbanotz.mutantbeasts.item.CreeperShardItem;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.util.EntityEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
	@SubscribeEvent
	public static void onItemRegistry(RegistryEvent.Register<Item> event) {
		ItemGroup main = MutantBeasts.ITEM_GROUP;
		event.getRegistry().registerAll(
				setRegistryName("chemical_x", new ChemicalXItem(new Item.Properties().group(main))),
				setRegistryName("creeper_shard", new CreeperShardItem(new Item.Properties().group(main))),
				setRegistryName("creeper_stats", new Item(new Item.Properties().maxStackSize(1).group(main))),
				setRegistryName("hulk_hammer", new HulkHammerItem(new Item.Properties().group(main))));

		EntityEntry.construct();
		EntityEntry.SPAWN_EGGS.forEach(event.getRegistry()::register);
		EntityEntry.SPAWN_EGGS.clear();
	}

	@SubscribeEvent
	public static void onEntityTypeRegistry(RegistryEvent.Register<EntityType<?>> event) {
		EntityEntry.ENTITY_TYPES.forEach(event.getRegistry()::register);
		EntityEntry.ENTITY_TYPES.clear();
	}

	@SubscribeEvent
	public static void onSoundEventRegistry(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().registerAll(
				register("entity.creeper_minion.hatch"),
				register("entity.mutant_creeper.ambient"),
				register("entity.mutant_creeper.charge"),
				register("entity.mutant_creeper.death"),
				register("entity.mutant_creeper.hurt"),
				register("entity.mutant_skeleton.ambient"),
				register("entity.mutant_skeleton.death"),
				register("entity.mutant_skeleton.hurt"),
				register("entity.mutant_skeleton.step"),
				register("entity.mutant_snow_golem.death"),
				register("entity.mutant_snow_golem.hurt"),
				register("entity.mutant_zombie.ambient"),
				register("entity.mutant_zombie.attack"),
				register("entity.mutant_zombie.grunt"),
				register("entity.mutant_zombie.hurt"),
				register("entity.mutant_zombie.roar")
				);
	}

	private static SoundEvent register(String name) {
		return setRegistryName(name, new SoundEvent(MutantBeasts.createResource(name)));
	}

	public static <T extends IForgeRegistryEntry<?>> T setRegistryName(String name, T entry) {
		entry.setRegistryName(MutantBeasts.createResource(name));
		return entry;
	}
}