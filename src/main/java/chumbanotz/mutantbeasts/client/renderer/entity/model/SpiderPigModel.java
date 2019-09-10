package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.animationapi.JointRendererModel;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderPigModel extends EntityModel<SpiderPigEntity> {
	public static RendererModel snout;
	public static JointRendererModel head;
	public static RendererModel base;
	public static RendererModel body1;
	public static RendererModel body2;
	public static RendererModel butt;
	public static JointRendererModel frontLeg1;
	public static JointRendererModel frontLegF1;
	public static JointRendererModel frontLeg2;
	public static JointRendererModel frontLegF2;
	public static JointRendererModel middleLeg1;
	public static JointRendererModel middleLegF1;
	public static JointRendererModel middleLeg2;
	public static JointRendererModel middleLegF2;
	public static JointRendererModel backLeg1;
	public static JointRendererModel backLegF1;
	public static JointRendererModel backLeg2;
	public static JointRendererModel backLegF2;

	public SpiderPigModel() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		base = new RendererModel(this);
		base.setRotationPoint(0.0F, 14.5F, -2.0F);
		body2 = new RendererModel(this, 32, 0);
		body2.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10);
		base.addChild(body2);
		body1 = new JointRendererModel(this, 64, 0);
		body1.addBox(-3.5F, -3.5F, -9.0F, 7, 7, 9);
		body1.setRotationPoint(0.0F, -1.0F, 1.5F);
		body2.addChild(body1);
		butt = new RendererModel(this, 0, 16);
		butt.addBox(-5.0F, -4.5F, 0.0F, 10, 9, 12);
		butt.setRotationPoint(0.0F, 0.0F, 7.0F);
		body2.addChild(butt);
		head = new JointRendererModel(this, 0, 0);
		head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8);
		head.setRotationPoint(0.0F, 0.0F, -8.0F);
		body1.addChild(head);
		snout = new RendererModel(this, 24, 0);
		snout.addBox(-2.0F, 0.0F, -9.0F, 4, 3, 1);
		head.addChild(snout);
		frontLeg1 = new JointRendererModel(this, 0, 37);
		frontLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		frontLeg1.setRotationPoint(-3.5F, 0.0F, -5.0F);
		body1.addChild(frontLeg1);
		frontLegF1 = new JointRendererModel(this, 8, 37);
		frontLegF1.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		frontLegF1.setRotationPoint(-0.0F, 12.0F, -0.1F);
		frontLeg1.addChild(frontLegF1);
		frontLeg2 = new JointRendererModel(this, 0, 37);
		frontLeg2.mirror = true;
		frontLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		frontLeg2.setRotationPoint(3.5F, 0.0F, -5.0F);
		body1.addChild(frontLeg2);
		frontLegF2 = new JointRendererModel(this, 8, 37);
		frontLegF2.mirror = true;
		frontLegF2.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		frontLegF2.setRotationPoint(0.0F, 12.0F, 0.1F);
		frontLeg2.addChild(frontLegF2);
		middleLeg1 = new JointRendererModel(this, 0, 37);
		middleLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		middleLeg1.setRotationPoint(-3.5F, 0.0F, -3.0F);
		body1.addChild(middleLeg1);
		middleLegF1 = new JointRendererModel(this, 8, 37);
		middleLegF1.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		middleLegF1.setRotationPoint(0.0F, 12.0F, -0.1F);
		middleLeg1.addChild(middleLegF1);
		middleLeg2 = new JointRendererModel(this, 0, 37);
		middleLeg2.mirror = true;
		middleLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2);
		middleLeg2.setRotationPoint(3.5F, 0.0F, -3.0F);
		body1.addChild(middleLeg2);
		middleLegF2 = new JointRendererModel(this, 8, 37);
		middleLegF2.mirror = true;
		middleLegF2.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2);
		middleLegF2.setRotationPoint(0.0F, 12.0F, 0.1F);
		middleLeg2.addChild(middleLegF2);
		backLeg1 = new JointRendererModel(this, 16, 37);
		backLeg1.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4);
		backLeg1.setRotationPoint(-2.5F, 2.0F, 7.0F);
		body2.addChild(backLeg1);
		backLegF1 = new JointRendererModel(this, 16, 45);
		backLegF1.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4, 0.2F);
		backLegF1.setRotationPoint(0.0F, 3.0F, 0.0F);
		backLeg1.addChild(backLegF1);
		backLeg2 = new JointRendererModel(this, 32, 37);
		backLeg2.mirror = true;
		backLeg2.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4);
		backLeg2.setRotationPoint(2.5F, 2.0F, 7.0F);
		body2.addChild(backLeg2);
		backLegF2 = new JointRendererModel(this, 16, 45);
		backLegF2.mirror = true;
		backLegF2.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4, 0.2F);
		backLegF2.setRotationPoint(0.0F, 3.0F, 0.0F);
		backLeg2.addChild(backLegF2);
	}

	@Override
	public void render(SpiderPigEntity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setAngles();
		this.animate(entity, f, f1, f2, f3, f4, f5);
		base.render(f5);
	}

	public void resetAngles(RendererModel... models) {
		RendererModel[] arr$ = models;
		int len$ = models.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			RendererModel model = arr$[i$];
			model.rotateAngleX = 0.0F;
			model.rotateAngleY = 0.0F;
			model.rotateAngleZ = 0.0F;
		}

	}

	public void setAngles() {
		this.resetAngles(head, head.getModel(), body1, body2, butt);
		this.resetAngles(frontLeg1, frontLeg1.getModel(), frontLegF1, frontLegF1.getModel(), frontLeg2, frontLeg2.getModel(), frontLegF2, frontLegF2.getModel());
		this.resetAngles(middleLeg1, middleLeg1.getModel(), middleLegF1, middleLegF1.getModel(), middleLeg2, middleLeg2.getModel(), middleLegF2, middleLegF2.getModel());
		this.resetAngles(backLeg1, backLeg1.getModel(), backLegF1, backLegF1.getModel(), backLeg2, backLeg2.getModel(), backLegF2, backLegF2.getModel());
		body1.rotateAngleX += 0.3926991F;
		body2.rotateAngleX += -0.05235988F;
		butt.rotateAngleX += 0.5711987F;
		head.rotateAngleX += -0.3926991F;
		frontLeg1.rotateAngleX += -(body1.rotateAngleX + body2.rotateAngleX);
		frontLeg1.rotateAngleY += -1.0471976F;
		RendererModel var10000 = frontLeg1.getModel();
		var10000.rotateAngleZ += 2.0943952F;
		frontLegF1.rotateAngleZ += -1.6534699F;
		frontLeg2.rotateAngleX += -(body1.rotateAngleX + body2.rotateAngleX);
		++frontLeg2.rotateAngleY;
		var10000 = frontLeg2.getModel();
		var10000.rotateAngleZ += -2.0943952F;
		frontLegF2.rotateAngleZ += 1.6534699F; // Fixed?
		middleLeg1.rotateAngleX += -(body1.rotateAngleX + body2.rotateAngleX);
		middleLeg1.rotateAngleY += -0.31415927F;
		var10000 = middleLeg1.getModel();
		var10000.rotateAngleZ += 2.0399954F;
		middleLegF1.rotateAngleZ += -1.6534699F;
		middleLeg2.rotateAngleX += -(body1.rotateAngleX + body2.rotateAngleX);
		middleLeg2.rotateAngleY += 0.31415927F;
		var10000 = middleLeg2.getModel();
		var10000.rotateAngleZ += -2.0399954F;
		middleLegF2.rotateAngleZ += 1.6534699F; // Fixed?
		backLeg1.rotateAngleX += -0.3926991F;
		var10000 = backLeg1.getModel();
		var10000.rotateAngleZ += 0.3926991F;
		backLegF1.rotateAngleZ += -0.3926991F;
		var10000 = backLegF1.getModel();
		var10000.rotateAngleX += 0.5711987F;
		backLeg2.rotateAngleX += -0.3926991F;
		var10000 = backLeg2.getModel();
		var10000.rotateAngleZ += -0.3926991F;
		backLegF2.rotateAngleZ += 0.3926991F;
		var10000 = backLegF2.getModel();
		var10000.rotateAngleX += 0.5711987F;
	}

	public void animate(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		float moveAnim = MathHelper.sin(f * 0.9F) * f1;
		// float moveAnim_d = MathHelper.sin(f * 0.9F + 0.5F) * f1;
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
		head.rotateAngleX += breatheAnim * 0.02F;
		body1.rotateAngleX += breatheAnim * 0.005F;
		butt.rotateAngleX += -breatheAnim * 0.015F;
		RendererModel var10000 = head.getModel();
		var10000.rotateAngleX += facePitch;
		var10000 = head.getModel();
		var10000.rotateAngleY += faceYaw;
		var10000 = frontLeg1.getModel();
		var10000.rotateAngleZ += -moveAnim1 * 3.1415927F / 6.0F;
		var10000 = frontLeg1.getModel();
		var10000.rotateAngleX += -0.3926991F * f1;
		frontLegF1.rotateAngleZ += moveAnim1d * 3.1415927F / 6.0F + 0.2617994F * f1;
		var10000 = frontLeg2.getModel();
		var10000.rotateAngleZ += moveAnim2 * 3.1415927F / 6.0F;
		var10000 = frontLeg2.getModel();
		var10000.rotateAngleX += -0.3926991F * f1;
		frontLegF2.rotateAngleZ += -(moveAnim2d * 3.1415927F / 6.0F + 0.2617994F * f1);
		var10000 = middleLeg1.getModel();
		var10000.rotateAngleZ += -moveAnim3 * 3.1415927F / 6.0F;
		var10000 = middleLeg1.getModel();
		var10000.rotateAngleX += -0.8975979F * f1;
		middleLegF1.rotateAngleZ += moveAnim3d * 3.1415927F / 6.0F + 0.3926991F * f1;
		var10000 = middleLeg2.getModel();
		var10000.rotateAngleZ += moveAnim4 * 3.1415927F / 6.0F;
		var10000 = middleLeg2.getModel();
		var10000.rotateAngleX += -0.8975979F * f1;
		middleLegF2.rotateAngleZ += -(moveAnim4d * 3.1415927F / 6.0F + 0.3926991F * f1);
		backLeg1.rotateAngleX += -moveAnim4 * 3.1415927F / 5.0F + 0.2617994F * f1;
		backLeg2.rotateAngleX += -moveAnim1 * 3.1415927F / 5.0F + 0.2617994F * f1;
		body2.rotateAngleX += -moveAnim * 3.1415927F / 20.0F;
		head.rotateAngleX += moveAnim * 3.1415927F / 20.0F;
	}
}