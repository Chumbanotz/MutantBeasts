package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.SpiderPigModel;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderPigRenderer extends MutantRenderer<SpiderPigEntity, SpiderPigModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("spider_pig");
	private static final ResourceLocation SADDLE_TEXTURE = MutantBeasts.getEntityTexture("spider_pig_saddle");

	public SpiderPigRenderer(EntityRendererManager manager) {
		super(manager, new SpiderPigModel(0.0F), 0.8F);
		this.addLayer(new SpiderPigRenderer.SaddleLayer(this));
	}

	@Override
	protected float getDeathMaxRotation(SpiderPigEntity entityLivingBaseIn) {
		return 180.0F;
	}

	@Override
	protected void preRenderCallback(SpiderPigEntity entitylivingbaseIn, float partialTickTime) {
		float scale = 1.2F * entitylivingbaseIn.getRenderScale();
		GlStateManager.scalef(scale, scale, scale);
	}

	@Override
	protected ResourceLocation getEntityTexture(SpiderPigEntity entity) {
		return TEXTURE;
	}

	@OnlyIn(Dist.CLIENT)
	static class SaddleLayer extends LayerRenderer<SpiderPigEntity, SpiderPigModel> {
		private final SpiderPigModel spiderPigModel = new SpiderPigModel(-0.6F);
		public SaddleLayer(IEntityRenderer<SpiderPigEntity, SpiderPigModel> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(SpiderPigEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (entityIn.isSaddled()) {
				this.bindTexture(SADDLE_TEXTURE);
				this.getEntityModel().setModelAttributes(this.spiderPigModel);
				this.spiderPigModel.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}