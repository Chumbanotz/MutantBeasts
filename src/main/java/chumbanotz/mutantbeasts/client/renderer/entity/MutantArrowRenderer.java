package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantArrowModel;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MutantArrowRenderer extends EntityRenderer<MutantArrowEntity> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_arrow");
	private final MutantArrowModel arrowModel = new MutantArrowModel();

	public MutantArrowRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(MutantArrowEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.translatef((float)x, (float)y, (float)z);
		this.bindEntityTexture(entity);
		float ageInTicks = (float)entity.ticksExisted + partialTicks;

		for (int i = 0; i < entity.getClones(); ++i) {
			GlStateManager.pushMatrix();
			float scale = entity.getSpeed() - (float)i * 0.08F;
			double x1 = (entity.getTargetX() - entity.posX) * (double)ageInTicks * (double)scale;
			double y1 = (entity.getTargetY() - entity.posY) * (double)ageInTicks * (double)scale;
			double z1 = (entity.getTargetZ() - entity.posZ) * (double)ageInTicks * (double)scale;
			GlStateManager.translatef((float)x1, (float)y1, (float)z1);
			GlStateManager.rotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.scalef(1.2F, 1.2F, 1.2F);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F - (float)i * 0.08F);
			this.arrowModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.popMatrix();
		}

		GlStateManager.disableBlend();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantArrowEntity entity) {
		return TEXTURE;
	}
}