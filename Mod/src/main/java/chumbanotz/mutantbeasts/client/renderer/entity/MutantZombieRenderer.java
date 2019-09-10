package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantZombieModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantZombieRenderer extends MobAutoTextureRenderer<MutantZombieEntity, MutantZombieModel> {
	public MutantZombieRenderer(EntityRendererManager manager) {
		super(manager, new MutantZombieModel(), 1.3F);
	}

	@Override
	protected void renderModel(MutantZombieEntity living, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (living.vanishTime > 0) {
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F - ((float)living.vanishTime + this.entityModel.partialTick) / (float)MutantZombieEntity.MAX_VANISH_TIME * 0.6F);
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
		GlStateManager.rotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
		int pitch = Math.min(20, entityLiving.downTime);
		boolean reviving = false;

		if (entityLiving.downTime > MutantZombieEntity.MAX_DOWN_TIME - 40) {
			pitch = MutantZombieEntity.MAX_DOWN_TIME - entityLiving.downTime;
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

		if (entityLiving.hasCustomName()) {
			String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName().getString());
			if (s != null && ("Dinnerbone".equals(s) || "Grumm".equals(s))) {
				GlStateManager.translatef(0.0F, entityLiving.getHeight() + 0.1F, 0.0F);
				GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
			}
		}
	}

	@Override
	protected float getDeathMaxRotation(MutantZombieEntity living) {
		return 80.0F;
	}
}