package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSnowGolemModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSnowGolemRenderer extends MobAutoTextureRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
	public MutantSnowGolemRenderer(EntityRendererManager manager) {
		super(manager, new MutantSnowGolemModel(), 0.7F);
		this.addLayer(new MutantSnowGolemRenderer.GlowLayer());
		this.addLayer(new MutantSnowGolemRenderer.ThrownBlockLayer());
	}

	@Override
	protected float getDeathMaxRotation(MutantSnowGolemEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@OnlyIn(Dist.CLIENT)
	class GlowLayer extends LayerRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
		public GlowLayer() {
			super(MutantSnowGolemRenderer.this);
		}

		@Override
		public void render(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (!entityIn.isInvisible()) {
				this.bindTexture(MutantBeasts.createResource("textures/entity/mutant_snow_golem_glow.png"));
				// GlStateManager.enableBlend();
				// GlStateManager.disableAlphaTest();
				// GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE,
				// GlStateManager.DestFactor.ONE);
				GlStateManager.disableLighting();
				GlStateManager.depthMask(!entityIn.isInvisible());
				GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
				GlStateManager.enableLighting();
				float f = (float)entityIn.ticksExisted + partialTicks;
				float f1 = MathHelper.cos(f * 0.1F);
				float f2 = MathHelper.cos(f * 0.15F);
				GlStateManager.color4f(1.0F, 0.8F + 0.05F * f2, 0.15F + 0.2F * f1, 1.0F);
				GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;
				gamerenderer.setupFogColor(true);
				this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				gamerenderer.setupFogColor(false);
				this.func_215334_a(entityIn);
				GlStateManager.depthMask(true);
				// GlStateManager.disableBlend();
				// GlStateManager.enableAlphaTest();
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	@OnlyIn(Dist.CLIENT)
	class ThrownBlockLayer extends LayerRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
		public ThrownBlockLayer() {
			super(MutantSnowGolemRenderer.this);
		}

		@Override
		public void render(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (entityIn.isThrowing && entityIn.throwTick < 7) {
				GlStateManager.enableRescaleNormal();
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.4F, 0.0F, 0.0F);
				this.getEntityModel().postRenderArm(0.0625F);
				GlStateManager.translatef(0.0F, 0.9F, 0.0F);
				float f = 0.8F;
				GlStateManager.scalef(-f, -f, f);
				int i = entityIn.getBrightnessForRender();
				int j = i % 65536;
				int k = i / 65536;
				GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
				// GlStateManager.disableLighting();
				Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(entityIn.getIceBlock(), 1.0F);
				// GlStateManager.enableLighting();
				GlStateManager.popMatrix();
				GlStateManager.disableRescaleNormal();
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}