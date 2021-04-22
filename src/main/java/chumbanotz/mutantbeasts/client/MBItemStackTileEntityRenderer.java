package chumbanotz.mutantbeasts.client;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.EndersoulCloneRenderer;
import chumbanotz.mutantbeasts.client.renderer.entity.model.EndersoulHandModel;
import chumbanotz.mutantbeasts.item.MBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MBItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {
	private static final ResourceLocation ENDER_SOUL_HAND_TEXTURE = MutantBeasts.prefix("textures/item/endersoul_hand_model.png");
	private final EndersoulHandModel enderSoulHandModel = new EndersoulHandModel();

	@Override
	public void renderByItem(ItemStack itemStackIn) {
		if (itemStackIn.getItem() == MBItems.ENDERSOUL_HAND) {
			GlStateManager.pushMatrix();
			ClientPlayerEntity player = Minecraft.getInstance().player;
			EndersoulCloneRenderer.render(player, ENDER_SOUL_HAND_TEXTURE, 0.0F, 0.0F, (float)player.ticksExisted + Minecraft.getInstance().getRenderPartialTicks(), 0.0F, 0.0F, 0.0F, this.enderSoulHandModel, 1.0F);
			GlStateManager.popMatrix();
		}
	}
}