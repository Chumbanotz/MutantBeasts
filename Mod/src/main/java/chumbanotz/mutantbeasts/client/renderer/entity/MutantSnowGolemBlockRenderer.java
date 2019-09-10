package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.entity.projectile.MutantSnowGolemBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSnowGolemBlockRenderer extends EntityRenderer<MutantSnowGolemBlockEntity> {
	public MutantSnowGolemBlockRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}

	@Override
	public void doRender(MutantSnowGolemBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.shadowOpaque = (float)entity.getBlockState().getOpacity(entity.world, entity.getPosition());
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)x, (float)y, (float)z);
		GlStateManager.rotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(((float)entity.ticksExisted + partialTicks) * 20.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotatef(((float)entity.ticksExisted + partialTicks) * 12.0F, 0.0F, 0.0F, -1.0F);
		this.bindEntityTexture(entity);
		// GlStateManager.disableLighting();
		Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(entity.getBlockState(), 1.0F);
		// GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantSnowGolemBlockEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
}