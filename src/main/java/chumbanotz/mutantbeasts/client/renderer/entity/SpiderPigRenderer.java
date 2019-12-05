package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.SpiderPigModel;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderPigRenderer extends MutantRenderer<SpiderPigEntity, SpiderPigModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("spider_pig");

	public SpiderPigRenderer(EntityRendererManager manager) {
		super(manager, new SpiderPigModel(), 0.6F);
	}

	@Override
	protected float getDeathMaxRotation(SpiderPigEntity entityLivingBaseIn) {
		return 180.0F;
	}

	@Override
	protected void preRenderCallback(SpiderPigEntity entitylivingbaseIn, float partialTickTime) {
		float scale = 1.2F;

		if (entitylivingbaseIn.isChild()) {
			scale *= 0.5F;
		}

		GlStateManager.scalef(scale, scale, scale);
	}

	@Override
	protected ResourceLocation getEntityTexture(SpiderPigEntity entity) {
		return TEXTURE;
	}

	@OnlyIn(Dist.CLIENT)
	class SaddleLayer extends LayerRenderer<SpiderPigEntity, SpiderPigModel> {
		public SaddleLayer() {
			super(SpiderPigRenderer.this);
		}

		@Override
		public void render(SpiderPigEntity entityIn, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
			if (entityIn.isSaddled()) {
				//this.bindTexture(TEXTURE);
				//this.getEntityModel().setModelAttributes(this.pigModel);
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}