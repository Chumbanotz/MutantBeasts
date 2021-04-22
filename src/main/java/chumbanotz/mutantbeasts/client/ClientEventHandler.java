package chumbanotz.mutantbeasts.client;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.gui.screen.CreeperMinionTrackerScreen;
import chumbanotz.mutantbeasts.client.particle.EndersoulParticle;
import chumbanotz.mutantbeasts.client.particle.SkullSpiritParticle;
import chumbanotz.mutantbeasts.client.renderer.entity.BodyPartRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.CreeperMinionEggRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.CreeperMinionRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.EndersoulCloneRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.EndersoulFragmentRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantArrowRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantCreeperRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantEndermanRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantSkeletonRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantSnowGolemRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.MutantZombieRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.SpiderPigRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.ThrowableBlockRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperMinionShoulderLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.EndersoulHandModel;
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
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public enum ClientEventHandler {
	INSTANCE;
	private BipedModel<?> mutantSkeletonArmor;

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		net.minecraftforge.fml.DeferredWorkQueue.runLater(() -> {
			for (PlayerRenderer renderer : event.getMinecraftSupplier().get().getRenderManager().getSkinMap().values()) {
				renderer.addLayer(new CreeperMinionShoulderLayer<>(renderer));
			}
		});

		ClientRegistry.registerEntityShader(CreeperMinionEntity.class, new ResourceLocation("shaders/post/creeper.json"));
		ClientRegistry.registerEntityShader(EndersoulCloneEntity.class, new ResourceLocation("shaders/post/invert.json"));
		ClientRegistry.registerEntityShader(MutantCreeperEntity.class, new ResourceLocation("shaders/post/creeper.json"));
		ClientRegistry.registerEntityShader(MutantEndermanEntity.class, new ResourceLocation("shaders/post/invert.json"));
		RenderingRegistry.registerEntityRenderingHandler(BodyPartEntity.class, BodyPartRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ChemicalXEntity.class, render -> new SpriteRenderer<>(render, event.getMinecraftSupplier().get().getItemRenderer()));
		RenderingRegistry.registerEntityRenderingHandler(CreeperMinionEntity.class, CreeperMinionRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(CreeperMinionEggEntity.class, CreeperMinionEggRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EndersoulCloneEntity.class, EndersoulCloneRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EndersoulFragmentEntity.class, EndersoulFragmentRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantArrowEntity.class, MutantArrowRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantCreeperEntity.class, MutantCreeperRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantEndermanEntity.class, MutantEndermanRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantSkeletonEntity.class, MutantSkeletonRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantSnowGolemEntity.class, MutantSnowGolemRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MutantZombieEntity.class, MutantZombieRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SkullSpiritEntity.class, manager -> new EntityRenderer<SkullSpiritEntity>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(SkullSpiritEntity entity) {
				return null;
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(SpiderPigEntity.class, SpiderPigRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ThrowableBlockEntity.class, ThrowableBlockRenderer::new);
	}

	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
		ModelLoader.addSpecialModel(new ModelResourceLocation(MutantBeasts.prefix("endersoul_hand_model"), "inventory"));
//		ModelLoaderRegistry.registerLoader(EndersoulHandModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		ResourceLocation loc = new ModelResourceLocation(MutantBeasts.prefix("endersoul_hand"), "inventory");
		ResourceLocation modelLoc = new ModelResourceLocation(MutantBeasts.prefix("endersoul_hand_model"), "inventory");
		IBakedModel bakedModel = event.getModelLoader().getUnbakedModel(modelLoc).bake(event.getModelLoader(), Minecraft.getInstance().getTextureMap()::getSprite, ModelRotation.X0_Y0, DefaultVertexFormats.ITEM);
		event.getModelRegistry().replace(loc, new EndersoulHandModel.Baked(event.getModelRegistry().get(loc), bakedModel));
	}

	@SubscribeEvent
	public static void onParticleFactoryRegistry(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particles.registerFactory(MBParticleTypes.ENDERSOUL, EndersoulParticle.Factory::new);
		Minecraft.getInstance().particles.registerFactory(MBParticleTypes.SKULL_SPIRIT, SkullSpiritParticle.Factory::new);
	}

	public void displayCreeperMinionTrackerGUI(CreeperMinionEntity creeperMinion) {
		Minecraft.getInstance().displayGuiScreen(new CreeperMinionTrackerScreen(creeperMinion));
	}

	public BipedModel<?> getMutantSkeletonArmor() {
		if (this.mutantSkeletonArmor == null) {
			this.mutantSkeletonArmor = new BipedModel<>(1.0F);
			this.mutantSkeletonArmor.bipedHead = new RendererModel(this.mutantSkeletonArmor, 0, 0);
			this.mutantSkeletonArmor.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.4F);
			RendererModel jaw = new RendererModel(this.mutantSkeletonArmor, 32, 0);
			jaw.addBox(-4.0F, -3.0F, -8.0F, 8, 3, 8, 0.7F);
			jaw.setRotationPoint(0.0F, -0.2F, 3.5F);
			jaw.rotateAngleX = 0.09817477F;
			this.mutantSkeletonArmor.bipedHead.addChild(jaw);
			this.mutantSkeletonArmor.bipedHeadwear = new RendererModel(this.mutantSkeletonArmor);
		}

		return this.mutantSkeletonArmor;
	}
}