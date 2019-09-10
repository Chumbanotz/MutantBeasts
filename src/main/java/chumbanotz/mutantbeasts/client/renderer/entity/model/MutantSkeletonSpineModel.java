package chumbanotz.mutantbeasts.client.renderer.entity.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSkeletonSpineModel {
	public final RendererModel middle;
	public final ScalableRendererModel[] side1;
	public final ScalableRendererModel[] side2;

	public MutantSkeletonSpineModel(Model model) {
		this(model, false);
	}

	public MutantSkeletonSpineModel(Model model, boolean skeletonPart) {
		this.middle = new RendererModel(model, 50, 0);
		this.middle.addBox(-2.5F, -4.0F, -2.0F, 5, 4, 4, 0.5F);
		this.side1 = new ScalableRendererModel[3];
		this.side2 = new ScalableRendererModel[3];
		this.side1[0] = new ScalableRendererModel(model, 32, 12);
		this.side1[0].addBox(skeletonPart ? 0.0F : -6.0F, -2.0F, -2.0F, 6, 2, 2, 0.25F);

		if (!skeletonPart) {
			this.side1[0].setRotationPoint(-3.0F, -1.0F, 1.75F);
		}

		this.middle.addChild(this.side1[0]);
		this.side2[0] = new ScalableRendererModel(model, 32, 12);
		this.side2[0].mirror = true;
		this.side2[0].addBox(skeletonPart ? -6.0F : 0.0F, -2.0F, -2.0F, 6, 2, 2, 0.25F);

		if (!skeletonPart) {
			this.side2[0].setRotationPoint(3.0F, -1.0F, 1.75F);
		}

		this.middle.addChild(this.side2[0]);
		this.side1[1] = new ScalableRendererModel(model, 32, 12);
		this.side1[1].mirror = true;
		this.side1[1].addBox(-6.0F, -2.0F, -2.0F, 6, 2, 2, 0.2F);
		this.side1[1].setRotationPoint(skeletonPart ? -0.5F : -6.5F, 0.0F, 0.0F);
		this.side1[0].addChild(this.side1[1]);
		this.side2[1] = new ScalableRendererModel(model, 32, 12);
		this.side2[1].addBox(0.0F, -2.0F, -2.0F, 6, 2, 2, 0.2F);
		this.side2[1].setRotationPoint(skeletonPart ? 0.5F : 6.5F, 0.0F, 0.0F);
		this.side2[0].addChild(this.side2[1]);
		this.side1[2] = new ScalableRendererModel(model, 32, 12);
		this.side1[2].addBox(-6.0F, -2.0F, -2.0F, 6, 2, 2, 0.15F);
		this.side1[2].setRotationPoint(-6.4F, 0.0F, 0.0F);
		this.side1[1].addChild(this.side1[2]);
		this.side2[2] = new ScalableRendererModel(model, 32, 12);
		this.side2[2].mirror = true;
		this.side2[2].addBox(0.0F, -2.0F, -2.0F, 6, 2, 2, 0.15F);
		this.side2[2].setRotationPoint(6.4F, 0.0F, 0.0F);
		this.side2[1].addChild(this.side2[2]);
	}

	private void resetAngles(RendererModel... boxes) {
		for (RendererModel box : boxes) {
			box.rotateAngleX = 0.0F;
			box.rotateAngleY = 0.0F;
			box.rotateAngleZ = 0.0F;
		}
	}

	public void setAngles(float PI, boolean middleSpine) {
		this.resetAngles(this.middle);
		this.resetAngles(this.side1);
		this.resetAngles(this.side2);
		this.middle.rotateAngleX = PI / 18.0F;
		this.side1[0].rotateAngleY = -PI / 4.5F;
		this.side2[0].rotateAngleY = PI / 4.5F;
		this.side1[1].rotateAngleY = -PI / 3.0F;
		this.side2[1].rotateAngleY = PI / 3.0F;
		this.side1[2].rotateAngleY = -PI / 3.5F;
		this.side2[2].rotateAngleY = PI / 3.5F;

		if (middleSpine) {
			for (int i = 0; i < this.side1.length; ++i) {
				this.side1[i].rotateAngleY *= 0.98F;
				this.side2[i].rotateAngleY *= 0.98F;
			}
		}

		this.side1[0].setScale(1.0F);
		this.side2[0].setScale(1.0F);
	}

	public void animate(float breatheAnim) {
		this.side1[1].rotateAngleY += breatheAnim * 0.02F;
		this.side2[1].rotateAngleY -= breatheAnim * 0.02F;
	}
}