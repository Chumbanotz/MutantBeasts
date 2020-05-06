package chumbanotz.mutantbeasts.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndersoulLayer<T extends MobEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("endersoul");

	public EndersoulLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.disableLighting();
		Minecraft.getInstance().textureManager.bindTexture(TEXTURE);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		float f = ((float)entityIn.ticksExisted + partialTicks) * 0.008F;
		GlStateManager.translatef(f, f, 0.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		int var5 = '\uf0f0';
		int var6 = var5 % 65536;
		int var7 = var5 / 65536;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var6, (float)var7);
		GlStateManager.enableLighting();
		GlStateManager.color4f(0.9F, 0.3F, 1.0F, this.getAlpha(entityIn, partialTicks));
		Minecraft.getInstance().gameRenderer.setupFogColor(true);
		this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getInstance().gameRenderer.setupFogColor(false);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.disableBlend();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

	protected float getAlpha(T entity, float partialTicks) {
		return 1.0F;
	}
}