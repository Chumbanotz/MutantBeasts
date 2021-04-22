package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperChargeLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantCreeperModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class MutantCreeperRenderer extends MobRenderer<MutantCreeperEntity, MutantCreeperModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_creeper");

	public MutantCreeperRenderer(EntityRendererManager manager) {
		super(manager, new MutantCreeperModel(), 1.5F);
		this.addLayer(new CreeperChargeLayer<>(this, new MutantCreeperModel(2.0F)));
	}

	@Override
	protected void preRenderCallback(MutantCreeperEntity livingEntity, float partialTickTime) {
		float scale = 1.2F;
		if (livingEntity.deathTime > 0) {
			float f = (float)livingEntity.deathTime / (float)MutantCreeperEntity.MAX_DEATH_TIME;
			scale -= f * 0.4F;
		}

		GlStateManager.scalef(scale, scale, scale);
	}

	@Override
	protected int getColorMultiplier(MutantCreeperEntity livingEntity, float lightBrightness, float partialTickTime) {
		float color = livingEntity.getExplosionColor(partialTickTime);
		if (livingEntity.isJumpAttacking() && livingEntity.deathTime == 0) {
			if ((int)(color * 10.0F) % 2 == 0) {
				return 0;
			} else {
				int i = (int)(color * 0.2F * 255.0F);
				i = MathHelper.clamp(i, 0, 255);
				return i << 24 | 822083583;
			}
		}

		int a = -(int)color;
		int r = 255;
		int g = 255;
		int b = 255;

		if (livingEntity.getPowered()) {
			r = 160;
			g = 180;
		}

		return a << 24 | r << 16 | g << 8 | b;
	}

	@Override
	protected float getDeathMaxRotation(MutantCreeperEntity livingEntity) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantCreeperEntity entity) {
		return TEXTURE;
	}
}