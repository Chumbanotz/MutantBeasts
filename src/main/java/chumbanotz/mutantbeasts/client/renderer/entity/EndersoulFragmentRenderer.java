package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.EndersoulFragmentModel;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class EndersoulFragmentRenderer extends EntityRenderer<EndersoulFragmentEntity> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("endersoul_fragment");
	private final EndersoulFragmentModel endersoulFragmentModel = new EndersoulFragmentModel();

	public EndersoulFragmentRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.3F;
		this.shadowOpaque = 0.5F;
	}

	@Override
	public void doRender(EndersoulFragmentEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)x, (float)y - 1.9F, (float)z);
		GlStateManager.scalef(1.6F, 1.6F, 1.6F);
		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

		EndersoulCloneRenderer.render(entity, TEXTURE, 0.0F, 0.0F, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0F, this.endersoulFragmentModel, 1.0F);
		if (this.renderOutlines) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EndersoulFragmentEntity entity) {
		return TEXTURE;
	}
}