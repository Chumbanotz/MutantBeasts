package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSkeletonRenderer extends MutantRenderer<MutantSkeletonEntity, MutantSkeletonModel> {
	public static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_skeleton");
	private static final ResourceLocation GEAR_TEXTURE = MutantBeasts.getEntityTexture("mutant_skeleton_gear");

	public MutantSkeletonRenderer(EntityRendererManager manager) {
		super(manager, new MutantSkeletonModel(), 0.6F);
		this.addLayer(new MutantSkeletonRenderer.GearLayer(this));
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

	@OnlyIn(Dist.CLIENT)
	static class GearLayer extends LayerRenderer<MutantSkeletonEntity, MutantSkeletonModel> {
		public GearLayer(IEntityRenderer<MutantSkeletonEntity, MutantSkeletonModel> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantSkeletonEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			this.bindTexture(GEAR_TEXTURE);
			this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}