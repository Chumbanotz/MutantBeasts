package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.animationapi.Animator;
import chumbanotz.mutantbeasts.client.animationapi.JointRendererModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSkeletonModel extends EntityModel<MutantSkeletonEntity> {
	private final RendererModel skeleBase;
	private final RendererModel pelvis;
	private final RendererModel waist;
	private final MutantSkeletonSpineModel[] spine;
	private final RendererModel neck;
	private final JointRendererModel head;
	private final RendererModel jaw;
	private final RendererModel shoulder1;
	private final RendererModel shoulder2;
	private final JointRendererModel arm1;
	private final JointRendererModel arm2;
	private final JointRendererModel forearm1;
	private final JointRendererModel forearm2;
	private final JointRendererModel leg1;
	private final JointRendererModel leg2;
	private final JointRendererModel foreleg1;
	private final JointRendererModel foreleg2;
	private final CrossbowModel bow;
	private Animator animator;
	private float partialTick;

	public MutantSkeletonModel() {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.skeleBase = new RendererModel(this);
		this.skeleBase.setRotationPoint(0.0F, 3.0F, 0.0F);
		this.pelvis = new RendererModel(this, 0, 16);
		this.pelvis.addBox(-4.0F, -6.0F, -3.0F, 8, 6, 6);
		this.skeleBase.addChild(this.pelvis);
		this.waist = new RendererModel(this, 32, 0);
		this.waist.addBox(-2.5F, -8.0F, -2.0F, 5, 8, 4);
		this.waist.setRotationPoint(0.0F, -5.0F, 0.0F);
		this.pelvis.addChild(this.waist);
		this.spine = new MutantSkeletonSpineModel[3];
		this.spine[0] = new MutantSkeletonSpineModel(this);
		this.spine[0].middle.setRotationPoint(0.0F, -7.0F, 0.0F);
		this.waist.addChild(this.spine[0].middle);

		for (int i = 1; i < this.spine.length; ++i) {
			this.spine[i] = new MutantSkeletonSpineModel(this);
			this.spine[i].middle.setRotationPoint(0.0F, -5.0F, 0.0F);
			this.spine[i - 1].middle.addChild(this.spine[i].middle);
		}

		this.neck = new RendererModel(this, 64, 0);
		this.neck.addBox(-1.5F, -4.0F, -1.5F, 3, 4, 3);
		this.neck.setRotationPoint(0.0F, -4.0F, 0.0F);
		this.spine[2].middle.addChild(this.neck);
		this.head = new JointRendererModel(this, 0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.4F);
		this.head.setRotationPoint(0.0F, -4.0F, -1.0F);
		this.neck.addChild(this.head);
		this.jaw = new RendererModel(this, 72, 0);
		this.jaw.addBox(-4.0F, -3.0F, -8.0F, 8, 3, 8, 0.7F);
		this.jaw.setRotationPoint(0.0F, -0.2F, 3.5F);
		this.head.addChild(this.jaw);
		this.shoulder1 = new RendererModel(this, 28, 16);
		this.shoulder1.addBox(-4.0F, -3.0F, -3.0F, 8, 3, 6);
		this.shoulder1.setRotationPoint(-7.0F, -3.0F, -1.0F);
		this.spine[2].middle.addChild(this.shoulder1);
		this.shoulder2 = new RendererModel(this, 28, 16);
		this.shoulder2.mirror = true;
		this.shoulder2.addBox(-4.0F, -3.0F, -3.0F, 8, 3, 6);
		this.shoulder2.setRotationPoint(7.0F, -3.0F, -1.0F);
		this.spine[2].middle.addChild(this.shoulder2);
		this.arm1 = new JointRendererModel(this, 0, 28);
		this.arm1.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.arm1.setRotationPoint(-1.0F, -1.0F, 0.0F);
		this.shoulder1.addChild(this.arm1);
		this.arm2 = new JointRendererModel(this, 0, 28);
		this.arm2.mirror = true;
		this.arm2.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.arm2.setRotationPoint(1.0F, -1.0F, 0.0F);
		this.shoulder2.addChild(this.arm2);
		this.forearm1 = new JointRendererModel(this, 16, 28);
		this.forearm1.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, -0.01F);
		this.forearm1.setRotationPoint(0.0F, 11.0F, 0.0F);
		this.arm1.addChild(this.forearm1);
		this.forearm2 = new JointRendererModel(this, 16, 28);
		this.forearm2.mirror = true;
		this.forearm2.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, -0.01F);
		this.forearm2.setRotationPoint(0.0F, 11.0F, 0.0F);
		this.arm2.addChild(this.forearm2);
		this.leg1 = new JointRendererModel(this, 0, 28);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.leg1.setRotationPoint(-2.5F, -2.5F, 0.0F);
		this.pelvis.addChild(this.leg1);
		this.leg2 = new JointRendererModel(this, 0, 28);
		this.leg2.mirror = true;
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.leg2.setRotationPoint(2.5F, -2.5F, 0.0F);
		this.pelvis.addChild(this.leg2);
		this.foreleg1 = new JointRendererModel(this, 32, 28);
		this.foreleg1.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.foreleg1.setRotationPoint(0.0F, 12.0F, 0.0F);
		this.leg1.addChild(this.foreleg1);
		this.foreleg2 = new JointRendererModel(this, 32, 28);
		this.foreleg2.mirror = true;
		this.foreleg2.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
		this.foreleg2.setRotationPoint(0.0F, 12.0F, 0.0F);
		this.leg2.addChild(this.foreleg2);
		this.bow = new CrossbowModel(this);
		this.bow.armwear.setRotationPoint(0.0F, 8.0F, 0.0F);
		this.forearm1.addChild(this.bow.armwear);
		this.animator = new Animator(this);
	}

	@Override
	public void render(MutantSkeletonEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.animator.update(entityIn);
		this.setAngles();
		this.animate(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.skeleBase.render(scale);
	}

	public void setAngles() {
		this.skeleBase.rotationPointY = 3.0F;
		this.pelvis.rotateAngleX = -0.31415927F;
		this.waist.rotateAngleX = 0.22439948F;

		for (int i = 0; i < this.spine.length; ++i) {
			this.spine[i].setAngles((float)Math.PI, i == 1);
		}

		this.neck.rotateAngleX = -0.1308997F;
		this.head.rotateAngleX = -0.1308997F;
		this.jaw.rotateAngleX = 0.09817477F;
		this.shoulder1.rotateAngleX = -0.7853982F;
		this.shoulder2.rotateAngleX = -0.7853982F;
		this.arm1.getModel().rotateAngleX = 0.5235988F;
		this.arm1.getModel().rotateAngleZ = 0.31415927F;
		this.arm2.getModel().rotateAngleX = 0.5235988F;
		this.arm2.getModel().rotateAngleZ = -0.31415927F;
		this.forearm1.getModel().rotateAngleX = -0.5235988F;
		this.forearm2.getModel().rotateAngleX = -0.5235988F;
		this.leg1.rotateAngleX = -0.2617994F - this.pelvis.rotateAngleX;
		this.leg1.rotateAngleZ = 0.19634955F;
		this.leg2.rotateAngleX = -0.2617994F - this.pelvis.rotateAngleX;
		this.leg2.rotateAngleZ = -0.19634955F;
		this.foreleg1.rotateAngleZ = -0.1308997F;
		this.foreleg1.getModel().rotateAngleX = 0.31415927F;
		this.foreleg2.rotateAngleZ = 0.1308997F;
		this.foreleg2.getModel().rotateAngleX = 0.31415927F;
		this.bow.setAngles((float)Math.PI);
	}

	public void animate(MutantSkeletonEntity skele, float f, float f1, float f2, float f3, float f4, float f5) {
		float walkAnim1 = MathHelper.sin(f * 0.5F);
		float walkAnim2 = MathHelper.sin(f * 0.5F - 1.1F);
		float breatheAnim = MathHelper.sin(f2 * 0.1F);
		float faceYaw = f3 * (float)Math.PI / 180.0F;
		float facePitch = f4 * (float)Math.PI / 180.0F;
		float scale;

		if (skele.getAnimationID() == MutantSkeletonEntity.MELEE_ATTACK) {
			this.animateMelee(skele.getAnimationTick());
			this.bow.rotateRope();
			scale = 1.0F - MathHelper.clamp((float)skele.getAnimationTick() / 4.0F, 0.0F, 1.0F);
			walkAnim1 *= scale;
			walkAnim2 *= scale;
		} else if (skele.getAnimationID() == MutantSkeletonEntity.SHOOT_ATTACK) {
			this.animateShoot(skele.getAnimationTick(), facePitch, faceYaw);
			scale = 1.0F - MathHelper.clamp((float)skele.getAnimationTick() / 4.0F, 0.0F, 1.0F);
			walkAnim1 *= scale;
			walkAnim2 *= scale;
			facePitch *= scale;
			faceYaw *= scale;
		} else if (skele.getAnimationID() == MutantSkeletonEntity.MULTI_SHOT_ATTACK) {
			this.animateMultiShoot(skele.getAnimationTick(), facePitch, faceYaw);
			scale = 1.0F - MathHelper.clamp((float)skele.getAnimationTick() / 4.0F, 0.0F, 1.0F);
			walkAnim1 *= scale;
			walkAnim2 *= scale;
			facePitch *= scale;
			faceYaw *= scale;
		} else if (this.animator.setAnim(MutantSkeletonEntity.CONSTRICT_RIBS_ATTACK)) {
			this.animateConstrict();
			this.bow.rotateRope();
			scale = 1.0F - MathHelper.clamp((float)skele.getAnimationTick() / 6.0F, 0.0F, 1.0F);
			facePitch *= scale;
			faceYaw *= scale;
		} else {
			this.bow.rotateRope();
		}

		this.skeleBase.rotationPointY -= (-0.5F + Math.abs(walkAnim1)) * f1;
		this.spine[0].middle.rotateAngleY -= walkAnim1 * 0.06F * f1;
		this.arm1.rotateAngleX -= walkAnim1 * 0.9F * f1;
		this.arm2.rotateAngleX += walkAnim1 * 0.9F * f1;
		this.leg1.rotateAngleX += (0.2F + walkAnim1) * 1.0F * f1;
		this.leg2.rotateAngleX -= (-0.2F + walkAnim1) * 1.0F * f1;
		RendererModel var10000 = this.foreleg1.getModel();
		var10000.rotateAngleX += (0.6F + walkAnim2) * 0.6F * f1;
		var10000 = this.foreleg2.getModel();
		var10000.rotateAngleX -= (-0.6F + walkAnim2) * 0.6F * f1;

		for (int i = 0; i < this.spine.length; ++i) {
			this.spine[i].animate(breatheAnim);
		}

		this.head.rotateAngleX -= breatheAnim * 0.02F;
		this.jaw.rotateAngleX += breatheAnim * 0.04F + 0.04F;
		this.arm1.rotateAngleZ += breatheAnim * 0.025F;
		this.arm2.rotateAngleZ -= breatheAnim * 0.025F;
		var10000 = this.head.getModel();
		var10000.rotateAngleX += facePitch;
		var10000 = this.head.getModel();
		var10000.rotateAngleY += faceYaw;
	}

	protected void animateMelee(int fullTick) {
		float tick;
		float f;

		if (fullTick < 3) {
			tick = ((float)fullTick + this.partialTick) / 3.0F;
			f = MathHelper.sin(tick * (float)Math.PI / 2.0F);

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += f * (float)Math.PI / 16.0F;
			}

			this.arm1.rotateAngleY += f * (float)Math.PI / 10.0F;
			this.arm1.rotateAngleZ += f * (float)Math.PI / 4.0F;
			this.arm2.rotateAngleZ += f * -(float)Math.PI / 16.0F;
		} else if (fullTick < 5) {
			tick = ((float)(fullTick - 3) + this.partialTick) / 2.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += f * 0.5890486F - 0.3926991F;
			}

			this.arm1.rotateAngleY += f * 2.7307692F - 2.41661F;
			this.arm1.rotateAngleZ += f * 1.1780972F - 0.3926991F;
			this.arm2.rotateAngleZ += -0.19634955F;
		} else if (fullTick < 8) {
			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += -0.3926991F;
			}

			this.arm1.rotateAngleY += -2.41661F;
			this.arm1.rotateAngleZ += -0.3926991F;
			this.arm2.rotateAngleZ += -0.19634955F;
		} else if (fullTick < 14) {
			tick = ((float)(fullTick - 8) + this.partialTick) / 6.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += f * -(float)Math.PI / 8.0F;
			}

			this.arm1.rotateAngleY += f * -(float)Math.PI / 1.3F;
			this.arm1.rotateAngleZ += f * -(float)Math.PI / 8.0F;
			this.arm2.rotateAngleZ += f * -(float)Math.PI / 16.0F;
		}
	}

	protected void animateShoot(int fullTick, float facePitch, float faceYaw) {
		RendererModel var10000;
		float tick;
		float f;

		if (fullTick < 5) {
			tick = ((float)fullTick + this.partialTick) / 5.0F;
			f = MathHelper.sin(tick * (float)Math.PI / 2.0F);
			var10000 = this.arm1.getModel();
			var10000.rotateAngleX += -f * (float)Math.PI / 4.0F;
			this.arm1.rotateAngleY += -f * (float)Math.PI / 2.0F;
			this.arm1.rotateAngleZ += f * (float)Math.PI / 16.0F;
			this.forearm1.rotateAngleX += f * (float)Math.PI / 7.0F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleX += -f * (float)Math.PI / 4.0F;
			this.arm2.rotateAngleY += f * (float)Math.PI / 2.0F;
			this.arm2.rotateAngleZ += -f * (float)Math.PI / 16.0F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleZ += -f * (float)Math.PI / 8.0F;
			this.forearm2.rotateAngleX += -f * (float)Math.PI / 6.0F;
			this.bow.rotateRope();
		} else if (fullTick < 12) {
			tick = ((float)(fullTick - 5) + this.partialTick) / 7.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
			float f1 = MathHelper.sin(tick * (float)Math.PI / 2.0F);
			float f1s = MathHelper.sin(tick * (float)Math.PI / 2.0F * 0.4F);
			var10000 = this.head.getModel();
			var10000.rotateAngleY += f1 * (float)Math.PI / 4.0F;

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += -f1 * (float)Math.PI / 12.0F;
				this.spine[i].middle.rotateAngleX += f1 * facePitch / 3.0F;
				this.spine[i].middle.rotateAngleY += f1 * faceYaw / 3.0F;
			}

			var10000 = this.arm1.getModel();
			var10000.rotateAngleX += f * 0.2617994F - 1.0471976F;
			this.arm1.rotateAngleY += f * -0.9424778F - 0.62831855F;
			this.arm1.rotateAngleZ += f * -0.850848F + 1.0471976F;
			this.forearm1.rotateAngleX += 0.44879895F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleX += f * 1.8325956F - 2.6179938F;
			this.arm2.rotateAngleY += f * 0.9424778F + 0.62831855F;
			this.arm2.rotateAngleZ += f * 0.850848F - 1.0471976F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleZ += -f * (float)Math.PI / 8.0F;
			this.forearm2.rotateAngleX += f * 0.10471976F - 0.62831855F;
			this.bow.middle1.rotateAngleX += -f1s * (float)Math.PI / 16.0F;
			this.bow.side1.rotateAngleX += -f1s * (float)Math.PI / 24.0F;
			this.bow.middle2.rotateAngleX += f1s * (float)Math.PI / 16.0F;
			this.bow.side2.rotateAngleX += f1s * (float)Math.PI / 24.0F;
			this.bow.rotateRope();
			this.bow.rope1.rotateAngleX += f1s * (float)Math.PI / 6.0F;
			this.bow.rope2.rotateAngleX += -f1s * (float)Math.PI / 6.0F;
		} else if (fullTick < 26) {
			var10000 = this.head.getModel();
			var10000.rotateAngleY += 0.7853982F;

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += -0.2617994F;
				this.spine[i].middle.rotateAngleX += facePitch / 3.0F;
				this.spine[i].middle.rotateAngleY += faceYaw / 3.0F;
			}

			var10000 = this.arm1.getModel();
			var10000.rotateAngleX += -1.0471976F;
			this.arm1.rotateAngleY += -0.62831855F;
			++this.arm1.rotateAngleZ;
			this.forearm1.rotateAngleX += 0.44879895F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleX += -2.6179938F;
			this.arm2.rotateAngleY += 0.62831855F;
			this.arm2.rotateAngleZ += -1.0471976F;
			this.forearm2.rotateAngleX += -0.62831855F;
			tick = MathHelper.clamp((float)(fullTick - 25) + this.partialTick, 0.0F, 1.0F);
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
			this.bow.middle1.rotateAngleX += -f * (float)Math.PI / 16.0F;
			this.bow.side1.rotateAngleX += -f * (float)Math.PI / 24.0F;
			this.bow.middle2.rotateAngleX += f * (float)Math.PI / 16.0F;
			this.bow.side2.rotateAngleX += f * (float)Math.PI / 24.0F;
			this.bow.rotateRope();
			this.bow.rope1.rotateAngleX += f * (float)Math.PI / 6.0F;
			this.bow.rope2.rotateAngleX += -f * (float)Math.PI / 6.0F;
		} else if (fullTick < 30) {
			tick = ((float)(fullTick - 26) + this.partialTick) / 4.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
			var10000 = this.head.getModel();
			var10000.rotateAngleY += f * (float)Math.PI / 4.0F;

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].middle.rotateAngleY += -f * (float)Math.PI / 12.0F;
				this.spine[i].middle.rotateAngleX += f * facePitch / 3.0F;
				this.spine[i].middle.rotateAngleY += f * faceYaw / 3.0F;
			}

			var10000 = this.arm1.getModel();
			var10000.rotateAngleX += -f * (float)Math.PI / 3.0F;
			this.arm1.rotateAngleY += -f * (float)Math.PI / 5.0F;
			this.arm1.rotateAngleZ += f * (float)Math.PI / 3.0F;
			this.forearm1.rotateAngleX += f * (float)Math.PI / 7.0F;
			var10000 = this.arm2.getModel();
			var10000.rotateAngleX += -f * (float)Math.PI / 1.2F;
			this.arm2.rotateAngleY += f * (float)Math.PI / 5.0F;
			this.arm2.rotateAngleZ += -f * (float)Math.PI / 3.0F;
			this.forearm2.rotateAngleX += -f * (float)Math.PI / 5.0F;
			this.bow.rotateRope();
		}

	}

	protected void animateMultiShoot(int fullTick, float facePitch, float faceYaw) {
		RendererModel var10000;
		float tick;
		float f;

		if (fullTick < 10) {
			tick = ((float)fullTick + this.partialTick) / 10.0F;
			f = MathHelper.sin(tick * (float)Math.PI / 2.0F);
			this.skeleBase.rotationPointY += f * 3.5F;
			this.spine[0].middle.rotateAngleX += f * (float)Math.PI / 6.0F;
			this.head.rotateAngleX += -f * (float)Math.PI / 4.0F;
			this.arm1.rotateAngleX += f * (float)Math.PI / 6.0F;
			this.arm1.rotateAngleZ += f * (float)Math.PI / 16.0F;
			this.arm2.rotateAngleX += f * (float)Math.PI / 6.0F;
			this.arm2.rotateAngleZ += -f * (float)Math.PI / 16.0F;
			this.leg1.rotateAngleX += -f * (float)Math.PI / 8.0F;
			this.leg2.rotateAngleX += -f * (float)Math.PI / 8.0F;
			var10000 = this.foreleg1.getModel();
			var10000.rotateAngleX += f * (float)Math.PI / 4.0F;
			var10000 = this.foreleg2.getModel();
			var10000.rotateAngleX += f * (float)Math.PI / 4.0F;
			this.bow.rotateRope();
		} else {
			float f1;

			if (fullTick < 12) {
				tick = ((float)(fullTick - 10) + this.partialTick) / 2.0F;
				f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
				f1 = MathHelper.sin(tick * (float)Math.PI / 2.0F);
				this.skeleBase.rotationPointY += f * 3.5F;
				this.spine[0].middle.rotateAngleX += f * (float)Math.PI / 6.0F;
				this.head.rotateAngleX += -f * (float)Math.PI / 4.0F;
				this.arm1.rotateAngleX += f * (float)Math.PI / 6.0F;
				this.arm1.rotateAngleZ += f * (float)Math.PI / 16.0F;
				this.arm2.rotateAngleX += f * (float)Math.PI / 6.0F;
				this.arm2.rotateAngleZ += -f * (float)Math.PI / 16.0F;
				this.leg1.rotateAngleX += -f * (float)Math.PI / 8.0F;
				this.leg2.rotateAngleX += -f * (float)Math.PI / 8.0F;
				var10000 = this.foreleg1.getModel();
				var10000.rotateAngleX += f * (float)Math.PI / 4.0F;
				var10000 = this.foreleg2.getModel();
				var10000.rotateAngleX += f * (float)Math.PI / 4.0F;
				this.arm1.rotateAngleZ += -f1 * (float)Math.PI / 14.0F;
				this.arm2.rotateAngleZ += f1 * (float)Math.PI / 14.0F;
				this.leg1.rotateAngleZ += -f1 * (float)Math.PI / 24.0F;
				this.leg2.rotateAngleZ += f1 * (float)Math.PI / 24.0F;
				this.foreleg1.rotateAngleZ += f1 * (float)Math.PI / 64.0F;
				this.foreleg2.rotateAngleZ += -f1 * (float)Math.PI / 64.0F;
				this.bow.rotateRope();
			} else if (fullTick < 14) {
				this.arm1.rotateAngleZ += -0.22439948F;
				this.arm2.rotateAngleZ += 0.22439948F;
				this.leg1.rotateAngleZ += -0.1308997F;
				this.leg2.rotateAngleZ += 0.1308997F;
				this.foreleg1.rotateAngleZ += 0.049087387F;
				this.foreleg2.rotateAngleZ += -0.049087387F;
				this.bow.rotateRope();
			} else if (fullTick < 17) {
				tick = ((float)(fullTick - 14) + this.partialTick) / 3.0F;
				f = MathHelper.sin(tick * (float)Math.PI / 2.0F);
				f1 = MathHelper.cos(tick * (float)Math.PI / 2.0F);
				this.arm1.rotateAngleZ += -f1 * (float)Math.PI / 14.0F;
				this.arm2.rotateAngleZ += f1 * (float)Math.PI / 14.0F;
				this.leg1.rotateAngleZ += -f1 * (float)Math.PI / 24.0F;
				this.leg2.rotateAngleZ += f1 * (float)Math.PI / 24.0F;
				this.foreleg1.rotateAngleZ += f1 * (float)Math.PI / 64.0F;
				this.foreleg2.rotateAngleZ += -f1 * (float)Math.PI / 64.0F;
				var10000 = this.arm1.getModel();
				var10000.rotateAngleX += -f * (float)Math.PI / 4.0F;
				this.arm1.rotateAngleY += -f * (float)Math.PI / 2.0F;
				this.arm1.rotateAngleZ += f * (float)Math.PI / 16.0F;
				this.forearm1.rotateAngleX += f * (float)Math.PI / 7.0F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleX += -f * (float)Math.PI / 4.0F;
				this.arm2.rotateAngleY += f * (float)Math.PI / 2.0F;
				this.arm2.rotateAngleZ += -f * (float)Math.PI / 16.0F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleZ += -f * (float)Math.PI / 8.0F;
				this.forearm2.rotateAngleX += -f * (float)Math.PI / 6.0F;
				this.bow.rotateRope();
			} else if (fullTick < 20) {
				tick = ((float)(fullTick - 17) + this.partialTick) / 3.0F;
				f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
				f1 = MathHelper.sin(tick * (float)Math.PI / 2.0F);
				float f1s = MathHelper.sin(tick * (float)Math.PI / 2.0F * 0.4F);
				var10000 = this.head.getModel();
				var10000.rotateAngleY += f1 * (float)Math.PI / 4.0F;

				for (int i = 0; i < this.spine.length; ++i) {
					this.spine[i].middle.rotateAngleY += -f1 * (float)Math.PI / 12.0F;
					this.spine[i].middle.rotateAngleX += f1 * facePitch / 3.0F;
					this.spine[i].middle.rotateAngleY += f1 * faceYaw / 3.0F;
				}

				var10000 = this.arm1.getModel();
				var10000.rotateAngleX += f * 0.2617994F - 1.0471976F;
				this.arm1.rotateAngleY += f * -0.9424778F - 0.62831855F;
				this.arm1.rotateAngleZ += f * -0.850848F + 1.0471976F;
				this.forearm1.rotateAngleX += 0.44879895F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleX += f * 1.8325956F - 2.6179938F;
				this.arm2.rotateAngleY += f * 0.9424778F + 0.62831855F;
				this.arm2.rotateAngleZ += f * 0.850848F - 1.0471976F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleZ += -f * (float)Math.PI / 8.0F;
				this.forearm2.rotateAngleX += f * 0.10471976F - 0.62831855F;
				this.bow.middle1.rotateAngleX += -f1s * (float)Math.PI / 16.0F;
				this.bow.side1.rotateAngleX += -f1s * (float)Math.PI / 24.0F;
				this.bow.middle2.rotateAngleX += f1s * (float)Math.PI / 16.0F;
				this.bow.side2.rotateAngleX += f1s * (float)Math.PI / 24.0F;
				this.bow.rotateRope();
				this.bow.rope1.rotateAngleX += f1s * (float)Math.PI / 6.0F;
				this.bow.rope2.rotateAngleX += -f1s * (float)Math.PI / 6.0F;
			} else if (fullTick < 24) {
				var10000 = this.head.getModel();
				var10000.rotateAngleY += 0.7853982F;

				for (int i = 0; i < this.spine.length; ++i) {
					this.spine[i].middle.rotateAngleY += -0.2617994F;
					this.spine[i].middle.rotateAngleX += facePitch / 3.0F;
					this.spine[i].middle.rotateAngleY += faceYaw / 3.0F;
				}

				var10000 = this.arm1.getModel();
				var10000.rotateAngleX += -1.0471976F;
				this.arm1.rotateAngleY += -0.62831855F;
				++this.arm1.rotateAngleZ;
				this.forearm1.rotateAngleX += 0.44879895F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleX += -2.6179938F;
				this.arm2.rotateAngleY += 0.62831855F;
				this.arm2.rotateAngleZ += -1.0471976F;
				this.forearm2.rotateAngleX += -0.62831855F;
				tick = MathHelper.clamp((float)(fullTick - 25) + this.partialTick, 0.0F, 1.0F);
				f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
				this.bow.middle1.rotateAngleX += -f * (float)Math.PI / 16.0F;
				this.bow.side1.rotateAngleX += -f * (float)Math.PI / 24.0F;
				this.bow.middle2.rotateAngleX += f * (float)Math.PI / 16.0F;
				this.bow.side2.rotateAngleX += f * (float)Math.PI / 24.0F;
				this.bow.rotateRope();
				this.bow.rope1.rotateAngleX += f * (float)Math.PI / 6.0F;
				this.bow.rope2.rotateAngleX += -f * (float)Math.PI / 6.0F;
			} else if (fullTick < 28) {
				tick = ((float)(fullTick - 24) + this.partialTick) / 4.0F;
				f = MathHelper.cos(tick * (float)Math.PI / 2.0F);
				var10000 = this.head.getModel();
				var10000.rotateAngleY += f * (float)Math.PI / 4.0F;

				for (int i = 0; i < this.spine.length; ++i) {
					this.spine[i].middle.rotateAngleY += -f * (float)Math.PI / 12.0F;
					this.spine[i].middle.rotateAngleX += f * facePitch / 3.0F;
					this.spine[i].middle.rotateAngleY += f * faceYaw / 3.0F;
				}

				var10000 = this.arm1.getModel();
				var10000.rotateAngleX += -f * (float)Math.PI / 3.0F;
				this.arm1.rotateAngleY += -f * (float)Math.PI / 5.0F;
				this.arm1.rotateAngleZ += f * (float)Math.PI / 3.0F;
				this.forearm1.rotateAngleX += f * (float)Math.PI / 7.0F;
				var10000 = this.arm2.getModel();
				var10000.rotateAngleX += -f * (float)Math.PI / 1.2F;
				this.arm2.rotateAngleY += f * (float)Math.PI / 5.0F;
				this.arm2.rotateAngleZ += -f * (float)Math.PI / 3.0F;
				this.forearm2.rotateAngleX += -f * (float)Math.PI / 5.0F;
				this.bow.rotateRope();
			}
		}

	}

	protected void animateConstrict() {
		this.animator.startPhase(5);
		this.animator.rotate(this.waist, 0.1308997F, 0.0F, 0.0F);
		int animTick;
		float tick;
		float f;

		for (animTick = 0; animTick < this.spine.length; ++animTick) {
			tick = animTick == 0 ? 0.3926991F : (animTick == 2 ? -0.3926991F : 0.0F);
			f = animTick == 1 ? 0.3926991F : 0.31415927F;
			this.animator.rotate(this.spine[animTick].side1[0], tick, f, 0.0F);
			this.animator.rotate(this.spine[animTick].side1[1], 0.0F, 0.15707964F, 0.0F);
			this.animator.rotate(this.spine[animTick].side1[2], 0.0F, 0.2617994F, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[0], tick, -f, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[1], 0.0F, -0.15707964F, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[2], 0.0F, -0.2617994F, 0.0F);
		}

		this.animator.rotate(this.arm1, 0.0F, 0.0F, 0.8975979F);
		this.animator.rotate(this.arm2, 0.0F, 0.0F, -0.8975979F);
		this.animator.move(this.skeleBase, 0.0F, 1.0F, 0.0F);
		this.animator.rotate(this.leg1, -0.44879895F, 0.0F, 0.0F);
		this.animator.rotate(this.leg2, -0.44879895F, 0.0F, 0.0F);
		this.animator.rotate(this.foreleg1.getModel(), 0.5235988F, 0.0F, 0.0F);
		this.animator.rotate(this.foreleg2.getModel(), 0.5235988F, 0.0F, 0.0F);
		this.animator.endPhase();
		this.animator.setStationaryPhase(2);
		this.animator.startPhase(1);
		this.animator.rotate(this.neck, 0.19634955F, 0.0F, 0.0F);
		this.animator.rotate(this.head, 0.15707964F, 0.0F, 0.0F);
		this.animator.rotate(this.waist, 0.31415927F, 0.0F, 0.0F);
		this.animator.rotate(this.spine[0].middle, 0.2617994F, 0.0F, 0.0F);

		for (animTick = 0; animTick < this.spine.length; ++animTick) {
			tick = animTick == 0 ? 0.1308997F : (animTick == 2 ? -0.1308997F : 0.0F);
			f = animTick == 1 ? -0.17453294F : -0.22439948F;
			this.animator.rotate(this.spine[animTick].side1[0], tick - 0.08F, f, 0.0F);
			this.animator.rotate(this.spine[animTick].side1[1], 0.0F, 0.15707964F, 0.0F);
			this.animator.rotate(this.spine[animTick].side1[2], 0.0F, 0.2617994F, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[0], tick + 0.08F, -f, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[1], 0.0F, -0.15707964F, 0.0F);
			this.animator.rotate(this.spine[animTick].side2[2], 0.0F, -0.2617994F, 0.0F);
		}

		this.animator.move(this.skeleBase, 0.0F, 1.0F, 0.0F);
		this.animator.rotate(this.leg1, -0.44879895F, 0.0F, 0.0F);
		this.animator.rotate(this.leg2, -0.44879895F, 0.0F, 0.0F);
		this.animator.rotate(this.foreleg1.getModel(), 0.5235988F, 0.0F, 0.0F);
		this.animator.rotate(this.foreleg2.getModel(), 0.5235988F, 0.0F, 0.0F);
		this.animator.endPhase();
		this.animator.setStationaryPhase(4);
		this.animator.resetPhase(8);
		animTick = this.animator.getEntity().getAnimationTick();

		if (animTick < 5) {
			tick = ((float)animTick + this.partialTick) / 5.0F;
			f = MathHelper.sin(tick * (float)Math.PI / 2.0F);

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].side1[0].setScale(1.0F + f * 0.6F);
				this.spine[i].side2[0].setScale(1.0F + f * 0.6F);
			}
		} else if (animTick < 12) {
			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].side1[0].setScale(1.6F);
				this.spine[i].side2[0].setScale(1.6F);
			}
		} else if (animTick < 20) {
			tick = ((float)(animTick - 12) + this.partialTick) / 8.0F;
			f = MathHelper.cos(tick * (float)Math.PI / 2.0F);

			for (int i = 0; i < this.spine.length; ++i) {
				this.spine[i].side1[0].setScale(1.0F + f * 0.6F);
				this.spine[i].side2[0].setScale(1.0F + f * 0.6F);
			}
		}
	}

	@Override
	public void setLivingAnimations(MutantSkeletonEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		this.partialTick = partialTick;
	}

	public static RendererModel createSkull(Model base) {
		RendererModel head = new RendererModel(base, 0, 0);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.4F);
		RendererModel jaw = new RendererModel(base, 32, 0);
		jaw.addBox(-4.0F, -3.0F, -8.0F, 8, 3, 8, 0.7F);
		jaw.setRotationPoint(0.0F, -0.2F, 3.5F);
		jaw.rotateAngleX = 0.09817477F;
		head.addChild(jaw);
		return head;
	}
}