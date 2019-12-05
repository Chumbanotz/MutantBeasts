package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.EndersoulFragmentModel;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndersoulFragmentRenderer extends EntityRenderer<EndersoulFragmentEntity> {
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("endersoul_fragment");
	private final EndersoulFragmentModel modelRod = new EndersoulFragmentModel();

	public EndersoulFragmentRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EndersoulFragmentEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)x, (float)y - 1.9F, (float)z);
		float scale = 1.6F;
		GlStateManager.scalef(scale, scale, scale);
		this.bindEntityTexture(entity);
		GlStateManager.disableLighting();
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		float add = ((float)entity.ticksExisted + partialTicks) * 0.008F;
		GlStateManager.translatef(add, add, 0.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		int var5 = '\uf0f0';
		int var6 = var5 % 65536;
		int var7 = var5 / 65536;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var6, (float)var7);
		GlStateManager.color4f(0.9F, 0.3F, 1.0F, 1.0F);
		GlStateManager.enableLighting();
		this.modelRod.render(entity);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EndersoulFragmentEntity entity) {
		return TEXTURE;
	}
}