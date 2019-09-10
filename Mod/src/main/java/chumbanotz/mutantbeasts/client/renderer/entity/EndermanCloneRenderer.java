package chumbanotz.mutantbeasts.client.renderer.entity;

import java.util.Random;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.EndermanCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermanCloneRenderer extends MobRenderer<EndermanCloneEntity, EndermanModel<EndermanCloneEntity>> {
	private static final ResourceLocation blankTexture = MutantBeasts.createResource("textures/blank.png");
	private final Random rnd = new Random();

	public EndermanCloneRenderer(EntityRendererManager rendererManager) {
		super(rendererManager, new EndermanModel<>(0.0F), 0.5F);
		this.addLayer(new EndermanCloneRenderer.GlowLayer());
	}

	@Override
	public void doRender(EndermanCloneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.getEntityModel().isAttacking = entity.isScreaming();

		if (entity.isScreaming()) {
			x += this.rnd.nextGaussian() * 0.02D;
			z += this.rnd.nextGaussian() * 0.02D;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected float getDeathMaxRotation(EndermanCloneEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EndermanCloneEntity entity) {
		return blankTexture;
	}

	@OnlyIn(Dist.CLIENT)
	class GlowLayer extends LayerRenderer<EndermanCloneEntity, EndermanModel<EndermanCloneEntity>> {
		public GlowLayer() {
			super(EndermanCloneRenderer.this);
		}

		@Override
		public void render(EndermanCloneEntity entityIn, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
			this.bindTexture(MutantBeasts.getEntityTexture(entityIn));
			GlStateManager.enableBlend();
			GlStateManager.disableAlphaTest();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(!entityIn.isInvisible());
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
			GlStateManager.enableLighting();
			GlStateManager.color4f(0.9F, 0.3F, 1.0F, 1.0F);
			GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;
			gamerenderer.setupFogColor(true);
			this.getEntityModel().render(entityIn, p_212842_2_, p_212842_3_, p_212842_5_, p_212842_6_, p_212842_7_, p_212842_8_);
			gamerenderer.setupFogColor(false);
			this.func_215334_a(entityIn);
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
			GlStateManager.enableAlphaTest();
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}