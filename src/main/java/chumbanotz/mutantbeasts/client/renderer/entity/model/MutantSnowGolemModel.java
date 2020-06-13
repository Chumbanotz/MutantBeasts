package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.renderer.model.JointRendererModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;

public class MutantSnowGolemModel extends EntityModel<MutantSnowGolemEntity> {
	private final RendererModel pelvis;
	private final RendererModel abdomen;
	private final RendererModel chest;
	private final JointRendererModel head;
	private final RendererModel headCore;
	private final JointRendererModel arm1;
	private final JointRendererModel arm2;
	private final JointRendererModel forearm1;
	private final JointRendererModel forearm2;
	private final JointRendererModel leg1;
	private final JointRendererModel leg2;
	private final JointRendererModel foreleg1;
	private final JointRendererModel foreleg2;
	private float partialTick;

	public MutantSnowGolemModel() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.pelvis = new RendererModel(this);
		this.pelvis.setRotationPoint(0.0F, 13.5F, 5.0F);
		this.abdomen = new RendererModel(this, 0, 32);
		this.abdomen.addBox(-5.0F, -8.0F, -4.0F, 10, 8, 8);
		this.pelvis.addChild(this.abdomen);
		this.chest = new RendererModel(this, 24, 36);
		this.chest.addBox(-8.0F, -12.0F, -6.0F, 16, 12, 12);
		this.chest.setRotationPoint(0.0F, -6.0F, 0.0F);
		this.head = new JointRendererModel(this, 0, 0);
		this.head.setTextureSize(64, 32);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
		this.head.setRotationPoint(0.0F, -12.0F, -2.0F);
		this.chest.addChild(this.head);
		this.headCore = new RendererModel(this, 64, 0);
		this.headCore.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8);
		this.headCore.setTextureOffset(80, 46).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, -0.5F);
		this.headCore.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.head.addChild(this.headCore);
		this.abdomen.addChild(this.chest);
		this.arm1 = new JointRendererModel(this, 68, 16);
		this.arm1.addBox(-2.5F, 0.0F, -2.5F, 5, 10, 5);
		this.arm1.setRotationPoint(-9.0F, -11.0F, 0.0F);
		this.chest.addChild(this.arm1);
		this.forearm1 = new JointRendererModel(this, 96, 0);
		this.forearm1.addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6);
		this.forearm1.setRotationPoint(0.0F, 10.0F, 0.0F);
		this.arm1.addChild(this.forearm1);
		this.arm2 = new JointRendererModel(this, 68, 16);
		this.arm2.mirror = true;
		this.arm2.addBox(-2.5F, 0.0F, -2.5F, 5, 10, 5);
		this.arm2.setRotationPoint(9.0F, -11.0F, 0.0F);
		this.chest.addChild(this.arm2);
		this.forearm2 = new JointRendererModel(this, 96, 0);
		this.forearm2.mirror = true;
		this.forearm2.addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6);
		this.forearm2.setRotationPoint(0.0F, 10.0F, 0.0F);
		this.arm2.addChild(this.forearm2);
		this.leg1 = new JointRendererModel(this, 88, 18);
		this.leg1.addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6);
		this.leg1.setRotationPoint(-4.0F, -1.0F, -3.0F);
		this.pelvis.addChild(this.leg1);
		this.foreleg1 = new JointRendererModel(this, 88, 32);
		this.foreleg1.addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6);
		this.foreleg1.setRotationPoint(-1.0F, 6.0F, -0.0F);
		this.leg1.addChild(this.foreleg1);
		this.leg2 = new JointRendererModel(this, 88, 18);
		this.leg2.mirror = true;
		this.leg2.addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6);
		this.leg2.setRotationPoint(4.0F, -1.0F, -3.0F);
		this.pelvis.addChild(this.leg2);
		this.foreleg2 = new JointRendererModel(this, 88, 32);
		this.foreleg2.mirror = true;
		this.foreleg2.addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6);
		this.foreleg2.setRotationPoint(1.0F, 6.0F, -0.0F);
		this.leg2.addChild(this.foreleg2);
	}

	@Override
	public void render(MutantSnowGolemEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.pelvis.render(scale);
	}

	@Override
	public void setRotationAngles(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		this.pelvis.rotationPointY = 13.5F;
		this.abdomen.rotateAngleX = 0.1308997F;
		this.chest.rotateAngleX = 0.1308997F;
		this.chest.rotateAngleY = 0.0F;
		this.head.rotateAngleX = -0.2617994F;
		this.head.getModel().rotateAngleX = 0.0F;
		this.head.getModel().rotateAngleY = 0.0F;
		this.arm1.rotateAngleX = -0.31415927F;
		this.arm1.rotateAngleZ = 0.0F;
		this.arm1.getModel().rotateAngleX = 0.0F;
		this.arm1.getModel().rotateAngleY = 0.5235988F;
		this.arm1.getModel().rotateAngleZ = 0.5235988F;
		this.forearm1.rotateAngleY = -0.5235988F;
		this.forearm1.rotateAngleZ = -0.2617994F;
		this.forearm1.getModel().rotateAngleX = -0.5235988F;
		this.arm2.rotateAngleX = -0.31415927F;
		this.arm2.rotateAngleZ = 0.0F;
		this.arm2.getModel().rotateAngleX = 0.0F;
		this.arm2.getModel().rotateAngleY = -0.5235988F;
		this.arm2.getModel().rotateAngleZ = -0.5235988F;
		this.forearm2.rotateAngleY = 0.5235988F;
		this.forearm2.rotateAngleZ = 0.2617994F;
		this.forearm2.getModel().rotateAngleX = -0.5235988F;
		this.leg1.rotateAngleX = -0.62831855F;
		this.leg1.getModel().rotateAngleZ = 0.5235988F;
		this.foreleg1.rotateAngleZ = -0.5235988F;
		this.foreleg1.getModel().rotateAngleX = 0.69813174F;
		this.leg2.rotateAngleX = -0.62831855F;
		this.leg2.getModel().rotateAngleZ = -0.5235988F;
		this.foreleg2.rotateAngleZ = 0.5235988F;
		this.foreleg2.getModel().rotateAngleX = 0.69813174F;
		this.animate(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}

	private void animate(MutantSnowGolemEntity golem, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		float temp = 0.5F;
		float walkAnim = MathHelper.sin(limbSwing * 0.45F) * limbSwingAmount;
		float walkAnim1 = (MathHelper.cos((limbSwing - temp) * 0.45F) + temp) * limbSwingAmount;
		float walkAnim2 = (MathHelper.cos((limbSwing - temp + 6.2831855F) * 0.45F) + temp) * limbSwingAmount;
		float breatheAnim = MathHelper.sin(ageInTicks * 0.11F);
		float faceYaw = netHeadYaw * (float)Math.PI / 180.0F;
		float facePitch = headPitch * (float)Math.PI / 180.0F;

		if (golem.isThrowing()) {
			this.animateThrow(golem.getThrowingTick());
			float scale1 = 1.0F - MathHelper.clamp((float)golem.getThrowingTick() / 4.0F, 0.0F, 1.0F);
			walkAnim *= scale1;
		}

		this.head.getModel().rotateAngleX -= breatheAnim * 0.01F;
		this.chest.rotateAngleX -= breatheAnim * 0.01F;
		this.arm1.rotateAngleZ += breatheAnim * 0.03F;
		this.arm2.rotateAngleZ -= breatheAnim * 0.03F;
		this.head.getModel().rotateAngleX += facePitch;
		this.head.getModel().rotateAngleY += faceYaw;
		this.pelvis.rotationPointY += Math.abs(walkAnim) * 1.5F;
		this.abdomen.rotateAngleX += limbSwingAmount * 0.2F;
		this.chest.rotateAngleY -= walkAnim * 0.1F;
		this.head.rotateAngleX -= limbSwingAmount * 0.2F;
		this.arm1.rotateAngleX -= walkAnim * 0.6F;
		this.arm2.rotateAngleX += walkAnim * 0.6F;
		this.forearm1.getModel().rotateAngleX -= walkAnim * 0.2F;
		this.forearm2.getModel().rotateAngleX += walkAnim * 0.2F;
		this.leg1.rotateAngleX += walkAnim1 * 1.1F;
		this.leg2.rotateAngleX += walkAnim2 * 1.1F;
		this.foreleg1.getModel().rotateAngleX += walkAnim * 0.2F;
		this.foreleg2.getModel().rotateAngleX -= walkAnim * 0.2F;
	}

	private void animateThrow(int fullTick) {
		float tick;
		float f;

		if (fullTick < 7) {
			tick = ((float)fullTick + this.partialTick) / 7.0F;
			f = MathHelper.sin(tick * (float)Math.PI / 2.0F);
			this.abdomen.rotateAngleX += -f * 0.2F;
			this.chest.rotateAngleX += -f * 0.4F;
			this.arm1.rotateAngleX += -f * 1.6F;
			this.arm1.rotateAngleZ += f * 0.8F;
			this.arm2.rotateAngleX += -f * 1.6F;
			this.arm2.rotateAngleZ += -f * 0.8F;
		} else if (fullTick < 10) {
			tick = ((float)(fullTick - 7) + this.partialTick) / 3.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
			this.abdomen.rotateAngleX += -f * 0.4F + 0.2F;
			this.chest.rotateAngleX += -f * 0.6F + 0.2F;
			this.arm1.rotateAngleX += -f * 0.8F - 0.8F;
			this.arm1.rotateAngleZ += 0.8F;
			this.arm2.rotateAngleX += -f * 0.8F - 0.8F;
			this.arm2.rotateAngleZ += -0.8F;
		} else if (fullTick < 14) {
			this.abdomen.rotateAngleX += 0.2F;
			this.chest.rotateAngleX += 0.2F;
			this.arm1.rotateAngleX += -0.8F;
			this.arm1.rotateAngleZ += 0.8F;
			this.arm2.rotateAngleX += -0.8F;
			this.arm2.rotateAngleZ += -0.8F;
		} else if (fullTick < 20) {
			tick = ((float)(fullTick - 14) + this.partialTick) / 6.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
			this.abdomen.rotateAngleX += f * 0.2F;
			this.chest.rotateAngleX += f * 0.2F;
			this.arm1.rotateAngleX += -f * 0.8F;
			this.arm1.rotateAngleZ += f * 0.8F;
			this.arm2.rotateAngleX += -f * 0.8F;
			this.arm2.rotateAngleZ += -f * 0.8F;
		}
	}

	public void postRenderArm(float scale) {
		this.pelvis.postRender(scale);
		this.abdomen.postRender(scale);
		this.chest.postRender(scale);
		this.arm1.postRender(scale);
		this.arm1.getModel().postRender(scale);
		this.forearm1.postRender(scale);
		this.forearm1.getModel().postRender(scale);
	}

	@Override
	public void setLivingAnimations(MutantSnowGolemEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		this.partialTick = partialTick;
	}
}