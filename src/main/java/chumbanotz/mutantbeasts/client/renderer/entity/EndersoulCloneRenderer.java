package chumbanotz.mutantbeasts.client.renderer.entity;

import chumbanotz.mutantbeasts.client.renderer.entity.layers.EndersoulLayer;
import chumbanotz.mutantbeasts.entity.EndersoulCloneEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.util.ResourceLocation;

public class EndersoulCloneRenderer extends MobRenderer<EndersoulCloneEntity, EndermanModel<EndersoulCloneEntity>> {
	public EndersoulCloneRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new EndermanModel<>(0.0F), 0.5F);
		this.addLayer(new EndersoulLayer<>(this));
		this.shadowOpaque = 0.5F;
	}

	@Override
	public void doRender(EndersoulCloneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.entityModel.isAttacking = entity.isAggressive();
		if (entity.isAggressive()) {
			x += entity.getRNG().nextGaussian() * 0.02D;
			z += entity.getRNG().nextGaussian() * 0.02D;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected float getDeathMaxRotation(EndersoulCloneEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EndersoulCloneEntity entity) {
		return null;
	}
}