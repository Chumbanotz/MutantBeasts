package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSnowGolemModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class MutantSnowGolemRenderer extends MobRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_snow_golem/mutant_snow_golem");
	private static final ResourceLocation JACK_O_LANTERN_TEXTURE = MutantBeasts.getEntityTexture("mutant_snow_golem/jack_o_lantern");
	private static final ResourceLocation GLOW_TEXTURE = MutantBeasts.getEntityTexture("mutant_snow_golem/glow");

	public MutantSnowGolemRenderer(EntityRendererManager manager) {
		super(manager, new MutantSnowGolemModel(), 0.7F);
		this.addLayer(new MutantSnowGolemRenderer.JackOLanternLayer(this));
		this.addLayer(new MutantSnowGolemRenderer.GlowLayer(this));
		this.addLayer(new MutantSnowGolemRenderer.HeldBlockLayer(this));
	}

	@Override
	public void renderName(MutantSnowGolemEntity entity, double x, double y, double z) {
		super.renderName(entity, x, y, z);
		if (entity.getOwner() != null) {
			ITextComponent textComponent = entity.getOwner().getDisplayName();
			textComponent.getStyle().setItalic(true);
			if (this.canRenderName(entity)) {
				y += (double)(9.0F * 1.15F * 0.025F);
			}

			this.renderEntityName(entity, x, y, z, textComponent.getFormattedText(), 64);
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantSnowGolemEntity entity) {
		return TEXTURE;
	}

	static class JackOLanternLayer extends LayerRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
		public JackOLanternLayer(IEntityRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (entityIn.hasJackOLantern() && !entityIn.isInvisible()) {
				this.bindTexture(JACK_O_LANTERN_TEXTURE);
				this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return true;
		}
	}

	static class GlowLayer extends LayerRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
		public GlowLayer(IEntityRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (entityIn.hasJackOLantern()) {
				this.bindTexture(GLOW_TEXTURE);
				GlStateManager.disableLighting();
				GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
				float blue = MathHelper.cos(ageInTicks * 0.1F);
				float green = MathHelper.cos(ageInTicks * 0.15F);
				GlStateManager.color4f(1.0F, 0.8F + 0.05F * green, 0.15F + 0.2F * blue, 1.0F);
				Minecraft.getInstance().gameRenderer.setupFogColor(true);
				this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				Minecraft.getInstance().gameRenderer.setupFogColor(false);
				this.func_215334_a(entityIn);
				GlStateManager.enableLighting();
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	static class HeldBlockLayer extends LayerRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> {
		public HeldBlockLayer(IEntityRenderer<MutantSnowGolemEntity, MutantSnowGolemModel> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (entityIn.isThrowing() && entityIn.getThrowingTick() < 7) {
				GlStateManager.enableRescaleNormal();
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.4F, 0.0F, 0.0F);
				this.getEntityModel().postRenderArm(0.0625F);
				GlStateManager.translatef(0.0F, 0.9F, 0.0F);
				GlStateManager.scalef(-0.8F, -0.8F, 0.8F);
				int i = entityIn.getBrightnessForRender();
				int j = i % 65536;
				int k = i / 65536;
				GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
				GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
				Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(Blocks.ICE.getDefaultState(), 1.0F);
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