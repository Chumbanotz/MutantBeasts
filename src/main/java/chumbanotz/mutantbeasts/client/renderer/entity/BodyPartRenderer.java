package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonPartModel;
import chumbanotz.mutantbeasts.entity.BodyPartEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
		float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
		GlStateManager.rotatef(yaw, 0.2F, 0.9F, -0.1F);
		GlStateManager.rotatef(pitch, 0.9F, 0.1F, 0.2F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(1.2F, -1.2F, -1.2F);
		this.bindEntityTexture(entity);
		int bodyPart = entity.getPart();
		this.modelSkelePart.setAngles();
		this.modelSkelePart.getSkeletonPart(bodyPart).render(0.0625F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(BodyPartEntity entity) {
		switch (entity.getOwnerType()) {
		case "mutant_skeleton":
			return MutantSkeletonRenderer.TEXTURE;
		case "mutant_snow_golem":
			return MutantSnowGolemRenderer.TEXTURE;
		default:
			return MutantSkeletonRenderer.TEXTURE;
		}
	}
}