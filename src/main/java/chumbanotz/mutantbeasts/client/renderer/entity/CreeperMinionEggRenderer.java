package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperChargeLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.CreeperMinionEggModel;
import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CreeperMinionEggRenderer extends EntityRenderer<CreeperMinionEggEntity> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("creeper_minion_egg");
	private final CreeperMinionEggModel eggModel = new CreeperMinionEggModel();
	private final CreeperMinionEggModel chargedModel = new CreeperMinionEggModel(1.0F);

	public CreeperMinionEggRenderer(EntityRendererManager manager) {
		super(manager);
		this.shadowSize = 0.4F;
	}

	@Override
	public void doRender(CreeperMinionEggEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
	    GlStateManager.pushMatrix();
	    GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
	    GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        GlStateManager.translatef(0.0F, -1.501F, 0.0F);
	    this.bindEntityTexture(entity);
		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

	    this.eggModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		if (this.renderOutlines) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		if (entity.isPowered()) {
			CreeperChargeLayer.render(entity, 0.0F, 0.0F, partialTicks, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F, this.eggModel, this.chargedModel);
		}

	    GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(CreeperMinionEggEntity entity) {
		return TEXTURE;
	}
}