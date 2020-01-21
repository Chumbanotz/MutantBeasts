package chumbanotz.mutantbeasts.client.renderer.entity.model;

import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullModel extends GenericHeadModel {
	private final RendererModel head;
	private final RendererModel jaw;

	public SkullModel() {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.head = new RendererModel(this, 0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.4F);
		this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.jaw = new RendererModel(this, 72, 0);
		this.jaw.addBox(-4.0F, -3.0F, -8.0F, 8, 3, 8, 0.7F);
		this.jaw.setRotationPoint(0.0F, -0.2F, 3.5F);
		this.head.addChild(this.jaw);
	}

	@Override
	public void func_217104_a(float animationProgress, float p_217104_2_, float p_217104_3_, float yaw, float pitch, float scale) {
		this.head.rotateAngleY = yaw * ((float)Math.PI / 180F);
		this.head.rotateAngleX = pitch * ((float)Math.PI / 180F);
		this.jaw.rotateAngleX = 0.09817477F;
		this.head.render(scale);
	}
}