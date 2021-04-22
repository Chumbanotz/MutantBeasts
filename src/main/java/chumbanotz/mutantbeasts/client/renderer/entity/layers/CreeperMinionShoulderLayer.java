package chumbanotz.mutantbeasts.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.CreeperMinionRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.CreeperMinionModel;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class CreeperMinionShoulderLayer<T extends PlayerEntity> extends LayerRenderer<T, PlayerModel<T>> {
	private final CreeperMinionModel creeperMinionModel = new CreeperMinionModel();
	private final CreeperMinionModel chargedModel = new CreeperMinionModel(2.0F);

	public CreeperMinionShoulderLayer(IEntityRenderer<T, PlayerModel<T>> entityRenderer) {
		super(entityRenderer);
	}

	@Override
	public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.enableRescaleNormal();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderOnShoulder(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, true);
		this.renderOnShoulder(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, false);
		GlStateManager.disableRescaleNormal();
	}

	private void renderOnShoulder(T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean leftShoulder) {
		CompoundNBT compoundnbt = leftShoulder ? player.getLeftShoulderEntity() : player.getRightShoulderEntity();
		if (compoundnbt.isEmpty()) {
			return;
		}

		EntityType.byKey(compoundnbt.getString("id")).filter(MBEntityType.CREEPER_MINION::equals).ifPresent(entityType -> {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(leftShoulder ? 0.42F : -0.42F, player.shouldRenderSneaking() ? -0.55F : -0.75F, 0.0F);
			this.bindTexture(CreeperMinionRenderer.TEXTURE);
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			this.creeperMinionModel.render(null, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			if (compoundnbt.getBoolean("Powered")) {
				CreeperChargeLayer.render(null, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, this.creeperMinionModel, this.chargedModel);
			}

			GlStateManager.popMatrix();
		});
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}