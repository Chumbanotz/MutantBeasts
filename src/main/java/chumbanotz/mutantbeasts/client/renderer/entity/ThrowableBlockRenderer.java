package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

public class ThrowableBlockRenderer extends EntityRenderer<ThrowableBlockEntity> {
	public ThrowableBlockRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.6F;
	}

	@Override
	public void doRender(ThrowableBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		float ageInTicks = (float)entity.ticksExisted + partialTicks;
		if (entity.getThrower() instanceof MutantSnowGolemEntity) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)x, (float)y + 0.5F, (float)z);
			GlStateManager.rotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(ageInTicks * 20.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(ageInTicks * 12.0F, 0.0F, 0.0F, -1.0F);
			this.bindEntityTexture(entity);
			GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
			Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(entity.getBlockState(), 1.0F);
			GlStateManager.popMatrix();
		} else {
			GlStateManager.enableRescaleNormal();
			GlStateManager.pushMatrix();
			this.bindEntityTexture(entity);
			int var4 = entity.getBrightnessForRender();
			int var5 = var4 % 65536;
			int var6 = var4 / 65536;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var5, (float)var6);
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.translatef((float)x, (float)y + 0.5F, (float)z);
			GlStateManager.scalef(-0.75F, -0.75F, 0.75F);
			GlStateManager.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(ageInTicks * 20.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(ageInTicks * 12.0F, 0.0F, 0.0F, -1.0F);
			GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
			Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(entity.getBlockState(), 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(ThrowableBlockEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
}