package chumbanotz.mutantbeasts.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.EndersoulHandModel;
import chumbanotz.mutantbeasts.item.MBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MBItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {
	private static final ResourceLocation ENDER_SOUL_HAND_TEXTURE = MutantBeasts.prefix("textures/item/endersoul_hand_model.png");
	private final EndersoulHandModel enderSoulHandModel = new EndersoulHandModel();

	@Override
	public void renderByItem(ItemStack itemStackIn) {
		if (itemStackIn.getItem() == MBItems.ENDERSOUL_HAND) {
//			boolean thirdPerson = false;
//			float playerViewY = Minecraft.getInstance().getRenderManager().playerViewY;
			GlStateManager.pushMatrix();
			Minecraft.getInstance().getTextureManager().bindTexture(ENDER_SOUL_HAND_TEXTURE);

//			if (playerViewY == 180.0F) {
//				thirdPerson = true;
//			}
//
//			if (thirdPerson) {
//				GlStateManager.scalef(1.0F, 1.0F, -1.0F);
//			}
//
//			GlStateManager.rotatef(thirdPerson ? 90.0F : 30.0F, 0.0F, 1.0F, 0.0F);
//			GlStateManager.rotatef(30.0F, 1.0F, 0.0F, 0.0F);
//			if (thirdPerson) {
//				GlStateManager.translatef(0.0F, -0.6F, 0.5F);
//			} else {
//				GlStateManager.translatef(0.7F, -0.8F, 0.5F);
//			}
//
//			if (!thirdPerson) {
//				GlStateManager.scalef(1.2F, 1.2F, 1.2F);
//			}

			GlStateManager.disableLighting();
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			float partialTick = Minecraft.getInstance().getRenderPartialTicks();
			float add = ((float)Minecraft.getInstance().player.ticksExisted + partialTick) * 0.008F;
			GlStateManager.translatef(add, add, 0.0F);
			GlStateManager.matrixMode(5888);
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			int var5 = '\uf0f0';
			int var6 = var5 % 65536;
			int var7 = var5 / 65536;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var6, (float)var7);
			GlStateManager.color4f(0.9F, 0.3F, 1.0F, 1.0F);
			this.enderSoulHandModel.render();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(5888);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.enableNormalize();
			GlStateManager.popMatrix();
		}
	}
}