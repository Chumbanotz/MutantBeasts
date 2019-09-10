package chumbanotz.mutantbeasts.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChemicalXItem extends Item {
	// private static final Map<EntityType<?>, EntityType<?>> MUTATIONS = new
	// ImmutableMap.Builder<EntityType<?>, EntityType<?>>()
	// .put(EntityType.CREEPER, MBEntityType.MUTANT_CREEPER)
	// .put(EntityType.SKELETON, MBEntityType.MUTANT_SKELETON)
	// .put(EntityType.SNOW_GOLEM, MBEntityType.MUTANT_SNOW_GOLEM)
	// .put(EntityType.ZOMBIE, MBEntityType.MUTANT_ZOMBIE)
	// .build();

	public ChemicalXItem(Item.Properties properties) {
		super(properties.maxStackSize(1).rarity(Rarity.EPIC));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
}