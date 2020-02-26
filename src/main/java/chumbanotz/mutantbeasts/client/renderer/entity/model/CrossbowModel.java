package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.renderer.model.LegacyRendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossbowModel {
	public final LegacyRendererModel armwear;
	public final LegacyRendererModel middle;
	public final LegacyRendererModel middle1;
	public final LegacyRendererModel middle2;
	public final LegacyRendererModel side1;
	public final LegacyRendererModel side2;
	public final LegacyRendererModel side3;
	public final LegacyRendererModel side4;
	public final LegacyRendererModel rope1;
	public final LegacyRendererModel rope2;

	public CrossbowModel(Model model) {
		this.armwear = new LegacyRendererModel(model, 0, 64);
		this.armwear.addBox(-2.0F, -3.0F, -2.0F, 4, 6, 4, 0.3F);
		this.middle = new LegacyRendererModel(model, 16, 64);
		this.middle.addBox(-2.0F, -2.0F, -3.0F, 4, 4, 6);
		this.middle.setRotationPoint(-3.5F, 0.0F, 0.0F);
		this.armwear.addChild(this.middle);
		this.middle1 = new LegacyRendererModel(model, 36, 64);
		this.middle1.addBox(-1.5F, -1.5F, -3.0F, 3, 3, 6);
		this.middle1.setRotationPoint(0.0F, 0.6F, -4.0F);
		this.middle.addChild(this.middle1);
		this.middle2 = new LegacyRendererModel(model, 36, 64);
		this.middle2.addBox(-1.5F, -1.5F, -3.0F, 3, 3, 6);
		this.middle2.setRotationPoint(0.0F, 0.6F, 4.0F);
		this.middle.addChild(this.middle2);
		this.side1 = new LegacyRendererModel(model, 0, 74);
		this.side1.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 8);
		this.side1.setRotationPoint(0.0F, 0.0F, -2.0F);
		this.middle1.addChild(this.side1);
		this.side2 = new LegacyRendererModel(model, 0, 74);
		this.side2.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 8);
		this.side2.setRotationPoint(0.0F, 0.0F, 2.0F);
		this.middle2.addChild(this.side2);
		this.side3 = new LegacyRendererModel(model, 20, 74);
		this.side3.addBox(-0.5F, -0.5F, -8.0F, 1, 1, 8);
		this.side3.setRotationPoint(0.0F, 0.0F, -5.0F);
		this.side1.addChild(this.side3);
		this.side4 = new LegacyRendererModel(model, 20, 74);
		this.side4.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 8);
		this.side4.setRotationPoint(0.0F, 0.0F, 5.0F);
		this.side2.addChild(this.side4);
		this.rope1 = new LegacyRendererModel(model, 0, 84);
		this.rope1.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 15, -0.4F);
		this.rope1.setRotationPoint(0.0F, 0.0F, -6.0F);
		this.side3.addChild(this.rope1);
		this.rope2 = new LegacyRendererModel(model, 0, 84);
		this.rope2.addBox(-0.5F, -0.5F, -15.0F, 1, 1, 15, -0.4F);
		this.rope2.setRotationPoint(0.0F, 0.0F, 6.0F);
		this.side4.addChild(this.rope2);
	}

	public void setAngles(float PI) {
		this.middle1.rotateAngleX = PI / 8.0F;
		this.middle2.rotateAngleX = -PI / 8.0F;
		this.side1.rotateAngleX = -PI / 5.0F;
		this.side2.rotateAngleX = PI / 5.0F;
		this.side3.rotateAngleX = -PI / 4.0F;
		this.side4.rotateAngleX = PI / 4.0F;
	}

	public void rotateRope() {
		this.rope1.rotateAngleX = -(this.middle1.rotateAngleX + this.side1.rotateAngleX + this.side3.rotateAngleX);
		this.rope2.rotateAngleX = -(this.middle2.rotateAngleX + this.side2.rotateAngleX + this.side4.rotateAngleX);
	}
}