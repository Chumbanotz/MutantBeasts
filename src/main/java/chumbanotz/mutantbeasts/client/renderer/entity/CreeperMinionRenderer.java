package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperChargeLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.CreeperMinionModel;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class CreeperMinionRenderer extends MobRenderer<CreeperMinionEntity, CreeperMinionModel> {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/creeper/creeper.png");

	public CreeperMinionRenderer(EntityRendererManager manager) {
		super(manager, new CreeperMinionModel(), 0.25F);
		this.addLayer(new CreeperChargeLayer<>(this, new CreeperMinionModel(2.0F)));
	}

	@Override
	protected void preRenderCallback(CreeperMinionEntity entitylivingbaseIn, float partialTickTime) {
		float f = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
		float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		f = f * f;
		f = f * f;
		float f2 = (1.0F + f * 0.4F) * f1 * 0.5F;
		float f3 = (1.0F + f * 0.1F) / f1 * 0.5F;
		GlStateManager.scalef(f2, f3, f2);
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
		return TEXTURE;
	}
}