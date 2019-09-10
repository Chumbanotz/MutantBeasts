package chumbanotz.mutantbeasts.client.renderer.entity;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSkeletonRenderer extends MobAutoTextureRenderer<MutantSkeletonEntity, MutantSkeletonModel> {
	public MutantSkeletonRenderer(EntityRendererManager manager) {
		super(manager, new MutantSkeletonModel(), 0.9F);
	}

	@Override
	protected float getDeathMaxRotation(MutantSkeletonEntity entityLivingBaseIn) {
		return 0.0F;
	}
}