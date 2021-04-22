package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.EndersoulCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class EndersoulCloneRenderer extends MobRenderer<EndersoulCloneEntity, EndermanModel<EndersoulCloneEntity>> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("endersoul");

	public EndersoulCloneRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new EndermanModel<>(0.0F), 0.5F);
		this.shadowOpaque = 0.5F;
	}

	@Override
	public void doRender(EndersoulCloneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.entityModel.isAttacking = entity.isAggressive();
		if (entity.isAggressive()) {
			x += entity.getRNG().nextGaussian() * 0.02D;
			z += entity.getRNG().nextGaussian() * 0.02D;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void renderModel(EndersoulCloneEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, this.entityModel, 1.0F);
	}

	@Override
	protected float getDeathMaxRotation(EndersoulCloneEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EndersoulCloneEntity entity) {
		return TEXTURE;
	}

	public static <T extends Entity, M extends EntityModel<T>> void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, M model, float alpha) {
		render(entityIn, TEXTURE, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, model, alpha);
	}

	public static <T extends Entity, M extends EntityModel<T>> void render(T entityIn, ResourceLocation texture, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, M model, float alpha) {
		GlStateManager.depthMask(!entityIn.isInvisible());
		GlStateManager.disableLighting();
		Minecraft.getInstance().textureManager.bindTexture(texture);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		float f = ageInTicks * 0.008F;
		GlStateManager.translatef(f, f, 0.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
		GlStateManager.enableLighting();
		GlStateManager.color4f(0.9F, 0.3F, 1.0F, alpha);
		Minecraft.getInstance().gameRenderer.setupFogColor(true);
		model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getInstance().gameRenderer.setupFogColor(false);
		int i = entityIn.getBrightnessForRender();
		int j = i % 65536;
		int k = i / 65536;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}
}