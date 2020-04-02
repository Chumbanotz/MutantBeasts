package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.layers.CreeperChargeLayer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantCreeperModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantCreeperRenderer extends MutantRenderer<MutantCreeperEntity, MutantCreeperModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_creeper");

	public MutantCreeperRenderer(EntityRendererManager manager) {
		super(manager, new MutantCreeperModel(), 1.5F);
		this.addLayer(new CreeperChargeLayer<>(this, new MutantCreeperModel(2.0F), MutantCreeperEntity::getPowered));
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
		int a = livingEntity.getExplosionColor();
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