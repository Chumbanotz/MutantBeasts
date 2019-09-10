package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.model.SpiderPigModel;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderPigRenderer extends MobAutoTextureRenderer<SpiderPigEntity, SpiderPigModel> {
	public SpiderPigRenderer(EntityRendererManager manager) {
		super(manager, new SpiderPigModel(), 0.9F);
	}

	@Override
	protected float getDeathMaxRotation(SpiderPigEntity entityLivingBaseIn) {
		return 180.0F;
	}

	@Override
	protected void preRenderCallback(SpiderPigEntity entitylivingbaseIn, float partialTickTime) {
		float f = 1.2F;

		if (entitylivingbaseIn.isChild()) {
			f *= 0.5F;
			this.shadowSize = 0.45F;
		} else {
			this.shadowSize = 0.9F;
		}

		GlStateManager.scalef(f, f, f);
	}
}