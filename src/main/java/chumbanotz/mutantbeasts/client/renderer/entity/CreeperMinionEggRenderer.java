package chumbanotz.mutantbeasts.client.renderer.entity;

import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperMinionEggRenderer extends EntityRenderer<CreeperMinionEggEntity> {
	private static final ResourceLocation texture = new ResourceLocation("MutantCreatures:textures/blocks/CreeperEgg.png");

	public CreeperMinionEggRenderer(EntityRendererManager manager) {
		super(manager);
		this.shadowSize = 0.4F;
	}

	public void doRender(CreeperMinionEggEntity entity, double d, double d1, double d2, float f, float f1) {
		// BlockRendererDispatcher blockrendererdispatcher =
		// Minecraft.getInstance().getBlockRendererDispatcher();
		// GL11.glPushMatrix();
		// GL11.glTranslatef((float)d, (float)d1, (float)d2);
		// GL11.glScalef(1.5F, 1.5F, 1.5F);
		// this.bindEntityTexture(entity);
		// GL11.glDisable(2896);
		// blockrendererdispatcher.blockAccess = entity.world;
		// Tessellator tess = Tessellator.getInstance();
		// tess.startDrawingQuads();
		// int x = MathHelper.floor_double(entity.posX);
		// int y = MathHelper.floor_double(entity.posY);
		// int z = MathHelper.floor_double(entity.posZ);
		// tess.setTranslation((double)((float)(-x) - 0.5F), (double)((float)(-y) -
		// 0.5F), (double)((float)(-z) - 0.5F));
		// Block block = Blocks.bedrock;
		// MutantCreatures.renderSmallEgg(this.renderBlocks, block, x, y, z);
		// tess.setTranslation(0.0D, 0.0D, 0.0D);
		// tess.draw();
		// GL11.glEnable(2896);
		// GL11.glPopMatrix();
	}

	protected ResourceLocation getEntityTexture(CreeperMinionEggEntity entity) {
		return texture;
	}
}