package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.CreeperMinionEggModel;
import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperMinionEggRenderer extends EntityRenderer<CreeperMinionEggEntity> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("creeper_minion_egg");
	private final CreeperMinionEggModel eggModel = new CreeperMinionEggModel();

	public CreeperMinionEggRenderer(EntityRendererManager manager) {
		super(manager);
		this.shadowSize = 0.4F;
	}

	@Override
	public void doRender(CreeperMinionEggEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
	    GlStateManager.pushMatrix();
	    GlStateManager.translatef((float)x, (float)y + 0.2F, (float)z);
	    GlStateManager.rotatef(180.0F, 0, 0, 1.0F);
	    GlStateManager.scalef(1.5F, 1.5F, 1.5F);
	    this.bindEntityTexture(entity);
		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

	    this.eggModel.render();
		if (this.renderOutlines) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

	    GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(CreeperMinionEggEntity entity) {
		return TEXTURE;
	}
}