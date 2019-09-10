package chumbanotz.mutantbeasts.client.renderer.entity;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobAutoTextureRenderer<T extends MobEntity, M extends EntityModel<T>> extends MobRenderer<T, M> {
	public MobAutoTextureRenderer(EntityRendererManager manager, M model, float shadowSize) {
		super(manager, model, shadowSize);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return MutantBeasts.getEntityTexture(entity);
	}
}