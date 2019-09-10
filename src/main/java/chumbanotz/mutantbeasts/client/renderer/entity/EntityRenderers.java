package chumbanotz.mutantbeasts.client.renderer.entity;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.EndermanCloneEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonPartEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantSnowGolemBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderers {
	public static void register() {
		render(CreeperMinionEntity.class, CreeperMinionRenderer::new);
		render(EndermanCloneEntity.class, EndermanCloneRenderer::new);
		render(MutantSnowGolemBlockEntity.class, MutantSnowGolemBlockRenderer::new);
		render(MutantCreeperEntity.class, MutantCreeperRenderer::new);
		render(MutantSkeletonEntity.class, MutantSkeletonRenderer::new);
		render(MutantArrowEntity.class, MutantArrowRenderer::new);
		render(MutantSkeletonPartEntity.class, MutantSkeletonPartRenderer::new);
		render(MutantSnowGolemEntity.class, MutantSnowGolemRenderer::new);
		render(MutantZombieEntity.class, MutantZombieRenderer::new);
		render(SpiderPigEntity.class, SpiderPigRenderer::new);
	}

	private static <T extends Entity> void render(Class<T> entityClass, IRenderFactory<? super T> renderFactory) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, renderFactory);
	}
}