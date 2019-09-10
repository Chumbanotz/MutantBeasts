package chumbanotz.mutantbeasts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chumbanotz.mutantbeasts.capability.ISummonable;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.client.renderer.entity.EntityRenderers;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MutantBeasts.MOD_ID)
public class MutantBeasts {
	public static final String MOD_ID = "mutantbeasts";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(MBItems.CHEMICAL_X);
		}
	};

	public MutantBeasts() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(ISummonable.class, new SummonableCapability.Storage(), new SummonableCapability.Factory());
		PacketHandler.register();
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		EntityRenderers.register();
	}

	public static ResourceLocation createResource(String name) {
		return new ResourceLocation(MOD_ID, name);
	}

	public static ResourceLocation getEntityTexture(Entity entity) {
		return createResource("textures/entity/" + entity.getType().getRegistryName().getPath() + ".png");
	}
}