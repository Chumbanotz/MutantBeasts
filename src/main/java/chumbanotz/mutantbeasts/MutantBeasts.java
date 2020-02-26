package chumbanotz.mutantbeasts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chumbanotz.mutantbeasts.capability.ISummonable;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.client.ClientEventHandler;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.item.ChemicalXItem;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MutantBeasts.MOD_ID)
public class MutantBeasts {
	public static final String MOD_ID = "mutantbeasts";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Tag<Block> THROWABLE_BLOCKS = new BlockTags.Wrapper(prefix("throwable_blocks"));
	public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack createIcon() {
			return new ItemStack(MBItems.CHEMICAL_X);
		}
	};

	public MutantBeasts() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFingerprintViolation);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MBConfig.COMMON_SPEC);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		MBEntityType.addSpawns();
		PacketHandler.register();
		BrewingRecipeRegistry.addRecipe(new ChemicalXItem.BrewingRecipe());
		CapabilityManager.INSTANCE.register(ISummonable.class, new SummonableCapability.Storage(), new SummonableCapability.Factory());
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		ClientEventHandler.registerEntityRenderers(event.getMinecraftSupplier().get());
		ClientRegistry.registerEntityShader(CreeperMinionEntity.class, new ResourceLocation("shaders/post/creeper.json"));
		ClientRegistry.registerEntityShader(MutantEndermanEntity.class, new ResourceLocation("shaders/post/invert.json"));
	}

	private void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
	}

	public static ResourceLocation prefix(String name) {
		return new ResourceLocation(MOD_ID, name);
	}

	public static ResourceLocation getEntityTexture(String name) {
		return prefix("textures/entity/" + name + ".png");
	}
}