package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantZombieModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class MutantZombieRenderer extends MobRenderer<MutantZombieEntity, MutantZombieModel> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_zombie");

	public MutantZombieRenderer(EntityRendererManager manager) {
		super(manager, new MutantZombieModel(), 1.0F);
	}

	@Override
	protected void renderModel(MutantZombieEntity living, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (living.vanishTime > 0) {
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F - ((float)living.vanishTime + this.entityModel.getPartialTick()) / (float)MutantZombieEntity.MAX_DEATH_TIME * 0.6F);
		}

		super.renderModel(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

		if (living.vanishTime > 0) {
			GlStateManager.disableBlend();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	protected void preRenderCallback(MutantZombieEntity entitylivingbaseIn, float partialTickTime) {
		GlStateManager.scalef(1.3F, 1.3F, 1.3F);
	}

	@Override
	protected void applyRotations(MutantZombieEntity entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
		if (entityLiving.deathTime > 0) {
			GlStateManager.rotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
			int pitch = Math.min(20, entityLiving.deathTime);
			boolean reviving = false;

			if (entityLiving.deathTime > MutantZombieEntity.MAX_DEATH_TIME - 40) {
				pitch = MutantZombieEntity.MAX_DEATH_TIME - entityLiving.deathTime;
				reviving = true;
			}

			if (pitch > 0) {
				float f = ((float)pitch + partialTicks - 1.0F) / 20.0F * 1.6F;

				if (reviving) {
					f = ((float)pitch - partialTicks) / 40.0F * 1.6F;
				}

				f = MathHelper.sqrt(f);

				if (f > 1.0F) {
					f = 1.0F;
				}

				GlStateManager.rotatef(f * this.getDeathMaxRotation(entityLiving), -1.0F, 0.0F, 0.0F);
			}
		} else {
			super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
		}
	}

	@Override
	protected float getDeathMaxRotation(MutantZombieEntity living) {
		return 80.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantZombieEntity entity) {
		return TEXTURE;
	}
}