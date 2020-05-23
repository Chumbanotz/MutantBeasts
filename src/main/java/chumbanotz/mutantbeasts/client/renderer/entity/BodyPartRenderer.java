package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonPartModel;
import chumbanotz.mutantbeasts.entity.BodyPartEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class BodyPartRenderer extends EntityRenderer<BodyPartEntity> {
	private final MutantSkeletonPartModel modelSkelePart = new MutantSkeletonPartModel();

	public BodyPartRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		for (int i = this.modelSkelePart.boxList.size() - 1; i >= 0; --i) {
			RendererModel renderer = this.modelSkelePart.boxList.get(i);
			if (renderer.cubeList.isEmpty()) {
				this.modelSkelePart.boxList.remove(i);
			}
		}
	}

	@Override
	public void doRender(BodyPartEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)x, (float)y, (float)z);
		GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw), 0.2F, 0.9F, -0.1F);
		GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch), 0.9F, 0.1F, 0.2F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(1.2F, -1.2F, -1.2F);
		this.bindEntityTexture(entity);
		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

		this.modelSkelePart.setAngles();
		this.modelSkelePart.getSkeletonPart(entity.getPart()).render(0.0625F);
		if (this.renderOutlines) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(BodyPartEntity entity) {
		return MutantSkeletonRenderer.TEXTURE;
	}
}