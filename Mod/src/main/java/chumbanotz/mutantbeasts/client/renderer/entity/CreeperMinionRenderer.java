package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperChargeLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.CreeperMinionModel;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CreeperMinionRenderer extends MobRenderer<CreeperMinionEntity, CreeperMinionModel> {
	private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");

	public CreeperMinionRenderer(EntityRendererManager manager) {
		super(manager, new CreeperMinionModel(), 0.5F);
		this.addLayer(new CreeperChargeLayer<>(this, new CreeperMinionModel(2.0F)));
		this.addLayer(new CreeperMinionRenderer.CollarLayer());
	}

	@Override
	protected void preRenderCallback(CreeperMinionEntity entitylivingbaseIn, float partialTickTime) {
		float f = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
		float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		f = f * f;
		f = f * f;
		float f2 = (1.0F + f * 0.4F) * f1 * entitylivingbaseIn.getRenderScale();
		float f3 = (1.0F + f * 0.1F) / f1 * entitylivingbaseIn.getRenderScale();
		GlStateManager.scalef(f2, f3, f2);
		GlStateManager.translatef(0.0F, 0.1F, 0.0F);
	}

	@Override
	protected int getColorMultiplier(CreeperMinionEntity entitylivingbaseIn, float lightBrightness, float partialTickTime) {
		float f = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);

		if ((int)(f * 10.0F) % 2 == 0) {
			return 0;
		} else {
			int i = (int)(f * 0.2F * 255.0F);
			i = MathHelper.clamp(i, 0, 255);
			return i << 24 | 822083583;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(CreeperMinionEntity entity) {
		return CREEPER_TEXTURES;
	}

	@OnlyIn(Dist.CLIENT)
	class CollarLayer extends LayerRenderer<CreeperMinionEntity, CreeperMinionModel> {
		private final CreeperMinionModel creeperMinionModel = new CreeperMinionModel(0.01F);

		public CollarLayer() {
			super(CreeperMinionRenderer.this);
		}

		@Override
		public void render(CreeperMinionEntity entityIn, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
			if (entityIn.isTamed() && !entityIn.isInvisible()) {
				this.bindTexture(MutantBeasts.createResource("textures/entity/creeper_minion_collar.png"));
				float[] afloat = entityIn.getCollarColor().getColorComponentValues();
				GlStateManager.color3f(afloat[0], afloat[1], afloat[2]);
				this.getEntityModel().setModelAttributes(this.creeperMinionModel);
				this.creeperMinionModel.render(entityIn, p_212842_2_, p_212842_3_, p_212842_5_, p_212842_6_, p_212842_7_, p_212842_8_);
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return true;
		}
	}
}