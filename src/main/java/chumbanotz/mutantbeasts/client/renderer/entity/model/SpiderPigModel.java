package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.renderer.model.JointRendererModel;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderPigModel extends EntityModel<SpiderPigEntity> {
	private final RendererModel snout;
	private final JointRendererModel head;
	private final RendererModel base;
	private final RendererModel body1;
	private final RendererModel body2;
	private final RendererModel butt;
	private final JointRendererModel frontLeg1;
	private final JointRendererModel frontLegF1;
	private final JointRendererModel frontLeg2;
	private final JointRendererModel frontLegF2;
	private final JointRendererModel middleLeg1;
	private final JointRendererModel middleLegF1;
	private final JointRendererModel middleLeg2;
	private final JointRendererModel middleLegF2;
	private final JointRendererModel backLeg1;
	private final JointRendererModel backLegF1;
	private final JointRendererModel backLeg2;
	private final JointRendererModel backLegF2;

	public SpiderPigModel(float scale) {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.base = new RendererModel(this);
		this.base.setRotationPoint(0.0F, 14.5F, -2.0F);
		this.body2 = new RendererModel(this, 32, 0);
		this.body2.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10);
		this.body2.setTextureOffset(44, 16).addBox(-5.0F, -5.0F, -4.0F, 10, 8, 12, scale);
		this.base.addChild(this.body2);
		this.body1 = new JointRendererModel(this, 64, 0);
		this.body1.addBox(-3.5F, -3.5F, -9.0F, 7, 7, 9);
		this.body1.setRotationPoint(0.0F, -1.0F, 1.5F);
		this.body2.addChild(this.body1);
		this.butt = new RendererModel(this, 0, 16);
		this.butt.addBox(-5.0F, -4.5F, 0.0F, 10, 9, 12);
		this.butt.setRotationPoint(0.0F, 0.0F, 7.0F);
		this.body2.addChild(this.butt);
		this.head = new JointRendererModel(this, 0, 0);
		this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8);
		this.head.setRotationPoint(0.0F, 0.0F, -8.0F);
		this.body1.addChild(this.head);
		this.snout = new RendererModel(this, 24, 0);
		this.snout.addBox(-2.0F, 0.0F, -9.0F, 4, 3, 1);
		this.head.addChild(this.snout);
		this.frontLeg1 = new JointRendererModel(this, 0, 37);
		this.frontLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		this.frontLeg1.setRotationPoint(-3.5F, 0.0F, -5.0F);
		this.body1.addChild(this.frontLeg1);
		this.frontLegF1 = new JointRendererModel(this, 8, 37);
		this.frontLegF1.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		this.frontLegF1.setRotationPoint(-0.0F, 12.0F, -0.1F);
		this.frontLeg1.addChild(this.frontLegF1);
		this.frontLeg2 = new JointRendererModel(this, 0, 37);
		this.frontLeg2.mirror = true;
		this.frontLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		this.frontLeg2.setRotationPoint(3.5F, 0.0F, -5.0F);
		this.body1.addChild(this.frontLeg2);
		this.frontLegF2 = new JointRendererModel(this, 8, 37);
		this.frontLegF2.mirror = true;
		this.frontLegF2.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		this.frontLegF2.setRotationPoint(0.0F, 12.0F, 0.1F);
		this.frontLeg2.addChild(this.frontLegF2);
		this.middleLeg1 = new JointRendererModel(this, 0, 37);
		this.middleLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		this.middleLeg1.setRotationPoint(-3.5F, 0.0F, -3.0F);
		this.body1.addChild(this.middleLeg1);
		this.middleLegF1 = new JointRendererModel(this, 8, 37);
		this.middleLegF1.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		this.middleLegF1.setRotationPoint(0.0F, 12.0F, -0.1F);
		this.middleLeg1.addChild(this.middleLegF1);
		this.middleLeg2 = new JointRendererModel(this, 0, 37);
		this.middleLeg2.mirror = true;
		this.middleLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		this.middleLeg2.setRotationPoint(3.5F, 0.0F, -3.0F);
		this.body1.addChild(this.middleLeg2);
		this.middleLegF2 = new JointRendererModel(this, 8, 37);
		this.middleLegF2.mirror = true;
		this.middleLegF2.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		this.middleLegF2.setRotationPoint(0.0F, 12.0F, 0.1F);
		this.middleLeg2.addChild(this.middleLegF2);
		this.backLeg1 = new JointRendererModel(this, 16, 37);
		this.backLeg1.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4);
		this.backLeg1.setRotationPoint(-2.5F, 2.0F, 7.0F);
		this.body2.addChild(this.backLeg1);
		this.backLegF1 = new JointRendererModel(this, 16, 45);
		this.backLegF1.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4, 0.2F);
		this.backLegF1.setRotationPoint(0.0F, 3.0F, 0.0F);
		this.backLeg1.addChild(this.backLegF1);
		this.backLeg2 = new JointRendererModel(this, 32, 37);
		this.backLeg2.mirror = true;
		this.backLeg2.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4);
		this.backLeg2.setRotationPoint(2.5F, 2.0F, 7.0F);
		this.body2.addChild(this.backLeg2);
		this.backLegF2 = new JointRendererModel(this, 16, 45);
		this.backLegF2.mirror = true;
		this.backLegF2.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4, 0.2F);
		this.backLegF2.setRotationPoint(0.0F, 3.0F, 0.0F);
		this.backLeg2.addChild(this.backLegF2);
	}

	@Override
	public void render(SpiderPigEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setAngles();
		this.animate(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.base.render(scale);
	}

	private void setAngles() {
		resetAngles(this.head, this.head.getJoint(), this.body1, this.body2, this.butt);
		resetAngles(this.frontLeg1, this.frontLeg1.getJoint(), this.frontLegF1, this.frontLegF1.getJoint(), this.frontLeg2, this.frontLeg2.getJoint(), this.frontLegF2, this.frontLegF2.getJoint());
		resetAngles(this.middleLeg1, this.middleLeg1.getJoint(), this.middleLegF1, this.middleLegF1.getJoint(), this.middleLeg2, this.middleLeg2.getJoint(), this.middleLegF2, this.middleLegF2.getJoint());
		resetAngles(this.backLeg1, this.backLeg1.getJoint(), this.backLegF1, this.backLegF1.getJoint(), this.backLeg2, this.backLeg2.getJoint(), this.backLegF2, this.backLegF2.getJoint());
		this.body1.rotateAngleX += 0.3926991F;
		this.body2.rotateAngleX += -0.05235988F;
		this.butt.rotateAngleX += 0.5711987F;
		this.head.rotateAngleX += -0.3926991F;
		this.frontLeg1.rotateAngleX += -(this.body1.rotateAngleX + this.body2.rotateAngleX);
		this.frontLeg1.rotateAngleY += -1.0471976F;
		this.frontLeg1.getJoint().rotateAngleZ += 2.0943952F;
		this.frontLegF1.rotateAngleZ += -1.6534699F;
		this.frontLeg2.rotateAngleX += -(this.body1.rotateAngleX + this.body2.rotateAngleX);
		this.frontLeg2.rotateAngleY += 1.0471976F; //Fixed?
		this.frontLeg2.getJoint().rotateAngleZ += -2.0943952F;
		this.frontLegF2.rotateAngleZ += 1.6534699F; // Fixed?
		this.middleLeg1.rotateAngleX += -(this.body1.rotateAngleX + this.body2.rotateAngleX);
		this.middleLeg1.rotateAngleY += -0.31415927F;
		this.middleLeg1.getJoint().rotateAngleZ += 2.0399954F;
		this.middleLegF1.rotateAngleZ += -1.6534699F;
		this.middleLeg2.rotateAngleX += -(this.body1.rotateAngleX + this.body2.rotateAngleX);
		this.middleLeg2.rotateAngleY += 0.31415927F;
		this.middleLeg2.getJoint().rotateAngleZ += -2.0399954F;
		this.middleLegF2.rotateAngleZ += 1.6534699F; // Fixed?
		this.backLeg1.rotateAngleX += -0.3926991F;
		this.backLeg1.getJoint().rotateAngleZ += 0.3926991F;
		this.backLegF1.rotateAngleZ += -0.3926991F;
		this.backLegF1.getJoint().rotateAngleX += 0.5711987F;
		this.backLeg2.rotateAngleX += -0.3926991F;
		this.backLeg2.getJoint().rotateAngleZ += -0.3926991F;
		this.backLegF2.rotateAngleZ += 0.3926991F;
		this.backLegF2.getJoint().rotateAngleX += 0.5711987F;
	}

	private void animate(SpiderPigEntity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		float moveAnim = MathHelper.sin(f * 0.9F) * f1;
		float moveAnim1 = MathHelper.sin(f * 0.9F + 0.3F) * f1;
		float moveAnim1d = MathHelper.sin(f * 0.9F + 0.3F + 0.5F) * f1;
		float moveAnim2 = MathHelper.sin(f * 0.9F + 0.9F) * f1;
		float moveAnim2d = MathHelper.sin(f * 0.9F + 0.9F + 0.5F) * f1;
		float moveAnim3 = MathHelper.sin(f * 0.9F - 0.3F) * f1;
		float moveAnim3d = MathHelper.sin(f * 0.9F - 0.3F + 0.5F) * f1;
		float moveAnim4 = MathHelper.sin(f * 0.9F - 0.9F) * f1;
		float moveAnim4d = MathHelper.sin(f * 0.9F - 0.9F + 0.5F) * f1;
		float breatheAnim = MathHelper.sin(f2 * 0.2F);
		float faceYaw = f3 * 3.1415927F / 180.0F;
		float facePitch = f4 * 3.1415927F / 180.0F;
		this.head.rotateAngleX += breatheAnim * 0.02F;
		this.body1.rotateAngleX += breatheAnim * 0.005F;
		this.butt.rotateAngleX += -breatheAnim * 0.015F;
		this.head.getJoint().rotateAngleX += facePitch;
		this.head.getJoint().rotateAngleY += faceYaw;
		this.frontLeg1.getJoint().rotateAngleZ += -moveAnim1 * 3.1415927F / 6.0F;
		this.frontLeg1.getJoint().rotateAngleX += -0.3926991F * f1;
		this.frontLegF1.rotateAngleZ += moveAnim1d * 3.1415927F / 6.0F + 0.2617994F * f1;
		this.frontLeg2.getJoint().rotateAngleZ += moveAnim2 * 3.1415927F / 6.0F;
		this.frontLeg2.getJoint().rotateAngleX += -0.3926991F * f1;
		this.frontLegF2.rotateAngleZ += -(moveAnim2d * 3.1415927F / 6.0F + 0.2617994F * f1);
		this.middleLeg1.getJoint().rotateAngleZ += -moveAnim3 * 3.1415927F / 6.0F;
		this.middleLeg1.getJoint().rotateAngleX += -0.8975979F * f1;
		this.middleLegF1.rotateAngleZ += moveAnim3d * 3.1415927F / 6.0F + 0.3926991F * f1;
		this.middleLeg2.getJoint().rotateAngleZ += moveAnim4 * 3.1415927F / 6.0F;
		this.middleLeg2.getJoint().rotateAngleX += -0.8975979F * f1;
		this.middleLegF2.rotateAngleZ += -(moveAnim4d * 3.1415927F / 6.0F + 0.3926991F * f1);
		this.backLeg1.rotateAngleX += -moveAnim4 * 3.1415927F / 5.0F + 0.2617994F * f1;
		this.backLeg2.rotateAngleX += -moveAnim1 * 3.1415927F / 5.0F + 0.2617994F * f1;
		this.body2.rotateAngleX += -moveAnim * 3.1415927F / 20.0F;
		this.head.rotateAngleX += moveAnim * 3.1415927F / 20.0F;
	}

	public void resetAngles(RendererModel... boxes) {
		for (RendererModel box : boxes) {
			box.rotateAngleX = 0.0F;
			box.rotateAngleY = 0.0F;
			box.rotateAngleZ = 0.0F;
		}
	}
}