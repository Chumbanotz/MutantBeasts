package chumbanotz.mutantbeasts.client.renderer.entity.layers;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class CreeperChargeLayer<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
	private final M model;

	public CreeperChargeLayer(IEntityRenderer<T, M> renderer, M model) {
		super(renderer);
		this.model = model;
	}

	@Override
	public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entityIn instanceof CreeperMinionEntity && ((CreeperMinionEntity)entityIn).getPowered() || entityIn instanceof MutantCreeperEntity && ((MutantCreeperEntity)entityIn).getPowered()) {
			render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, this.getEntityModel(), this.model);
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

	public static <T extends Entity, M extends EntityModel<T>> void render(@Nullable T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, M mainModel, M chargedModel) {
		GlStateManager.depthMask(entityIn == null || !entityIn.isInvisible());
		Minecraft.getInstance().textureManager.bindTexture(LIGHTNING_TEXTURE);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.translatef(ageInTicks * 0.01F, ageInTicks * 0.01F, 0.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.enableBlend();
		GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		mainModel.setModelAttributes(chargedModel);
		Minecraft.getInstance().gameRenderer.setupFogColor(true);
		chargedModel.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getInstance().gameRenderer.setupFogColor(false);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}
}