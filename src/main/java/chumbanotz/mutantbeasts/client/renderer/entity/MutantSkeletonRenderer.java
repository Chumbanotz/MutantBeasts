package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MutantSkeletonRenderer extends MutantRenderer<MutantSkeletonEntity, MutantSkeletonModel> {
	public static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_skeleton");

	public MutantSkeletonRenderer(EntityRendererManager manager) {
		super(manager, new MutantSkeletonModel(), 0.6F);
	}

	@Override
	protected void preRenderCallback(MutantSkeletonEntity entitylivingbaseIn, float partialTickTime) {
		GlStateManager.translatef(0.0F, 0.0F, 0.1F);
	}

	@Override
	protected float getDeathMaxRotation(MutantSkeletonEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantSkeletonEntity entity) {
		return TEXTURE;
	}
}