package chumbanotz.mutantbeasts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.packet.MBPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MutantBeasts.MOD_ID)
public class MutantBeasts {
	public static final String MOD_ID = "mutantbeasts";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Tag<Block> ENDERSOUL_HAND_HOLDABLE = new BlockTags.Wrapper(prefix("endersoul_hand_holdable"));
	public static final Tag<Block> MUTANT_ENDERMAN_HOLABLE = new BlockTags.Wrapper(prefix("mutant_enderman_holdable"));
	public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(MBItems.CHEMICAL_X);
		}
	};

	public MutantBeasts() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MBConfig.COMMON_SPEC);
	}

	@SuppressWarnings("deprecation")
	private void onCommonSetup(FMLCommonSetupEvent event) {
		MBPacketHandler.register();
		net.minecraftforge.fml.DeferredWorkQueue.runLater(() -> {
			MBEntityType.addSpawns();
			RegistryHandler.registerDispenseBehavior();
			Ingredient input = Ingredient.fromStacks(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), Potions.AWKWARD));
			Ingredient ingredients = Ingredient.fromItems(MBItems.CREEPER_SHARD, MBItems.ENDERSOUL_HAND, MBItems.HULK_HAMMER, MBItems.MUTANT_SKELETON_SKULL);
			BrewingRecipeRegistry.addRecipe(input, ingredients, new ItemStack(MBItems.CHEMICAL_X));
		});
	}

	public static ResourceLocation prefix(String name) {
		return new ResourceLocation(MOD_ID, name);
	}

	public static ResourceLocation getEntityTexture(String name) {
		return prefix("textures/entity/" + name + ".png");
	}
}