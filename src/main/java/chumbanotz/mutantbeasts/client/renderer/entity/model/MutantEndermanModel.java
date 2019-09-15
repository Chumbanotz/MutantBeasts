package chumbanotz.mutantbeasts.client.renderer.entity.model;

import java.util.Arrays;

import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantEndermanModel extends EntityModel<MutantEndermanEntity> {
	public RendererModel pelvis;
	public RendererModel abdomen;
	public RendererModel chest;
	public RendererModel neck;
	public RendererModel head;
	public RendererModel mouth;
	public EndermanArm rightArm;
	public EndermanArm leftArm;
	public EndermanArm lowerRightArm;
	public EndermanArm lowerLeftArm;
	public RendererModel legjoint1;
	public RendererModel legjoint2;
	public RendererModel leg1;
	public RendererModel leg2;
	public RendererModel foreleg1;
	public RendererModel foreleg2;
	private float animTick;
	public static final float PI = 3.1415927F;

	public MutantEndermanModel() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.animTick = 0.0F;
		this.pelvis = new RendererModel(this);
		this.pelvis.setRotationPoint(0.0F, -15.5F, 8.0F);
		this.abdomen = new RendererModel(this, 32, 0);
		this.abdomen.addBox(-4.0F, -10.0F, -2.0F, 8, 10, 4);
		this.pelvis.addChild(this.abdomen);
		this.chest = new RendererModel(this, 50, 8);
		this.chest.addBox(-5.0F, -16.0F, -3.0F, 10, 16, 6);
		this.chest.setRotationPoint(0.0F, -8.0F, 0.0F);
		this.abdomen.addChild(this.chest);
		this.neck = new RendererModel(this, 32, 14);
		this.neck.addBox(-1.5F, -4.0F, -1.5F, 3, 4, 3);
		this.neck.setRotationPoint(0.0F, -15.0F, 0.0F);
		this.chest.addChild(this.neck);
		this.head = new RendererModel(this);
		this.head.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8, 6, 8, 0.5F);
		this.head.setTextureOffset(0, 14).addBox(-4.0F, 3.0F, -8.0F, 8, 2, 8, 0.5F);
		this.head.setRotationPoint(0.0F, -5.0F, 3.0F);
		this.neck.addChild(this.head);
		this.mouth = new RendererModel(this, 0, 24);
		this.mouth.addBox(-4.0F, 3.0F, -8.0F, 8, 2, 8);
		this.head.addChild(this.mouth);
		this.rightArm = new EndermanArm(true);
		this.rightArm.init(this, this.chest);
		this.leftArm = new EndermanArm(false);
		this.leftArm.init(this, this.chest);
		this.lowerRightArm = new EndermanArm(true);
		this.lowerRightArm.init(this, this.chest);
		this.lowerRightArm.arm.rotationPointY += 6.0F;
		this.lowerLeftArm = new EndermanArm(false);
		this.lowerLeftArm.init(this, this.chest);
		this.lowerLeftArm.arm.rotationPointY += 6.0F;
		this.legjoint1 = new RendererModel(this);
		this.legjoint1.setRotationPoint(-1.5F, 0.0F, 0.75F);
		this.abdomen.addChild(this.legjoint1);
		this.legjoint2 = new RendererModel(this);
		this.legjoint2.setRotationPoint(1.5F, 0.0F, 0.75F);
		this.abdomen.addChild(this.legjoint2);
		this.leg1 = new RendererModel(this, 0, 34);
		this.leg1.addBox(-1.5F, 0.0F, -1.5F, 3, 24, 3, 0.5F);
		this.leg1.setRotationPoint(0.0F, -2.0F, 0.0F);
		this.legjoint1.addChild(this.leg1);
		this.leg2 = new RendererModel(this, 0, 34);
		this.leg2.mirror = true;
		this.leg2.addBox(-1.5F, 0.0F, -1.5F, 3, 24, 3, 0.5F);
		this.leg2.setRotationPoint(0.0F, -2.0F, 0.0F);
		this.legjoint2.addChild(this.leg2);
		this.foreleg1 = new RendererModel(this, 12, 34);
		this.foreleg1.addBox(-1.5F, 0.0F, -1.5F, 3, 24, 3, 0.5F);
		this.foreleg1.setRotationPoint(0.0F, 23.0F, 0.0F);
		this.leg1.addChild(this.foreleg1);
		this.foreleg2 = new RendererModel(this, 12, 34);
		this.foreleg2.mirror = true;
		this.foreleg2.addBox(-1.5F, 0.0F, -1.5F, 3, 24, 3, 0.5F);
		this.foreleg2.setRotationPoint(0.0F, 23.0F, 0.0F);
		this.leg2.addChild(this.foreleg2);
	}

	@Override
	public void render(MutantEndermanEntity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setAngles();
		this.animate(entity, f, f1, f2, f3, f4, f5);
		this.lowerRightArm.arm.animTick = this.animTick;
		this.lowerRightArm.arm.enderman = entity;
		this.lowerLeftArm.arm.animTick = this.animTick;
		this.lowerLeftArm.arm.enderman = entity;
		this.pelvis.render(f5);
	}

	public void setAngles() {
		this.pelvis.rotationPointY = -15.5F;
		this.abdomen.rotateAngleX = 0.31415927F;
		this.chest.rotateAngleX = 0.3926991F;
		this.chest.rotateAngleY = 0.0F;
		this.chest.rotateAngleZ = 0.0F;
		this.neck.rotateAngleX = 0.19634955F;
		this.neck.rotateAngleZ = 0.0F;
		this.head.rotateAngleX = -0.7853982F;
		this.head.rotateAngleY = 0.0F;
		this.head.rotateAngleZ = 0.0F;
		this.mouth.rotateAngleX = 0.0F;
		this.rightArm.setAngles();
		this.leftArm.setAngles();
		this.lowerRightArm.setAngles();
		this.lowerRightArm.arm.rotateAngleX += 0.1F;
		this.lowerRightArm.arm.rotateAngleZ -= 0.2F;
		this.lowerLeftArm.setAngles();
		this.lowerLeftArm.arm.rotateAngleX += 0.1F;
		this.lowerLeftArm.arm.rotateAngleZ += 0.2F;
		this.legjoint1.rotateAngleX = 0.0F;
		this.legjoint2.rotateAngleX = 0.0F;
		this.leg1.rotateAngleX = -0.8975979F;
		this.leg1.rotateAngleY = 0.0F;
		this.leg1.rotateAngleZ = 0.2617994F;
		this.leg2.rotateAngleX = -0.8975979F;
		this.leg2.rotateAngleY = 0.0F;
		this.leg2.rotateAngleZ = -0.2617994F;
		this.foreleg1.rotateAngleX = 0.7853982F;
		this.foreleg1.rotateAngleZ = -0.1308997F;
		this.foreleg2.rotateAngleX = 0.7853982F;
		this.foreleg2.rotateAngleZ = 0.1308997F;
	}

	public void animate(MutantEndermanEntity enderman, float f, float f1, float f2, float f3, float f4, float f5) {
		float walkSpeed = 0.3F;
		float walkAnim1 = (MathHelper.sin((f - 0.8F) * walkSpeed) + 0.8F) * f1;
		float walkAnim2 = -(MathHelper.sin((f + 0.8F) * walkSpeed) - 0.8F) * f1;
		float walkAnim3 = (MathHelper.sin((f + 0.8F) * walkSpeed) - 0.8F) * f1;
		float walkAnim4 = -(MathHelper.sin((f - 0.8F) * walkSpeed) + 0.8F) * f1;
		float[] walkAnim = new float[5];
		Arrays.fill(walkAnim, MathHelper.sin(f * walkSpeed) * f1);
		float breatheAnim = MathHelper.sin(f2 * 0.15F);
		float faceYaw = f3 * 3.1415927F / 180.0F;
		float facePitch = f4 * 3.1415927F / 180.0F;

		int arm;
		for (arm = 1; arm < enderman.heldBlock.length; ++arm) {
			if (enderman.heldBlock[arm] != 0) {
				this.animateHoldBlock(enderman.heldBlockTick[arm], arm, enderman.hasTarget > 0);
				walkAnim[arm] *= 0.4F;
			}
		}

		if (enderman.currentAttackID == 1) {
			arm = enderman.getMeleeArm();
			this.animateMelee(enderman.animTick, arm);
			walkAnim[arm] = 0.0F;
		}

		if (enderman.currentAttackID == 2) {
			arm = enderman.getThrownBlock();
			this.animateThrowBlock(enderman.animTick, arm);
		}

		float scale;
		if (enderman.currentAttackID == 5) {
			this.animateScream(enderman.animTick);
			scale = 1.0F - MathHelper.clamp((float)enderman.deathTick / 6.0F, 0.0F, 1.0F);
			faceYaw *= scale;
			facePitch *= scale;
			walkAnim1 *= scale;
			walkAnim2 *= scale;
			walkAnim3 *= scale;
			walkAnim4 *= scale;
			Arrays.fill(walkAnim, 0.0F);
		}

		if (enderman.currentAttackID == 7) {
			this.animateTeleSmash(enderman.animTick);
		}

		if (enderman.currentAttackID == 10) {
			this.animateDeath(enderman.deathTick);
			scale = 1.0F - MathHelper.clamp((float)enderman.deathTick / 6.0F, 0.0F, 1.0F);
			faceYaw *= scale;
			facePitch *= scale;
			walkAnim1 *= scale;
			walkAnim2 *= scale;
			walkAnim3 *= scale;
			walkAnim4 *= scale;
			Arrays.fill(walkAnim, 0.0F);
		}

		this.head.rotateAngleX += facePitch * 0.5F;
		this.head.rotateAngleY += faceYaw * 0.7F;
		this.head.rotateAngleZ -= faceYaw * 0.7F;
		this.neck.rotateAngleX += facePitch * 0.3F;
		this.chest.rotateAngleX += facePitch * 0.2F;
		this.mouth.rotateAngleX += breatheAnim * 0.02F + 0.02F;
		this.neck.rotateAngleX -= breatheAnim * 0.02F;
		this.rightArm.arm.rotateAngleZ += breatheAnim * 0.004F;
		this.leftArm.arm.rotateAngleZ -= breatheAnim * 0.004F;
		RendererModel[] arr$ = this.rightArm.finger;
		int len$ = arr$.length;

		int i$;
		RendererModel finger;
		for (i$ = 0; i$ < len$; ++i$) {
			finger = arr$[i$];
			finger.rotateAngleZ += breatheAnim * 0.05F;
		}

		this.rightArm.thumb.rotateAngleZ -= breatheAnim * 0.05F;
		arr$ = this.leftArm.finger;
		len$ = arr$.length;

		for (i$ = 0; i$ < len$; ++i$) {
			finger = arr$[i$];
			finger.rotateAngleZ -= breatheAnim * 0.05F;
		}

		this.leftArm.thumb.rotateAngleZ += breatheAnim * 0.05F;
		this.lowerRightArm.arm.rotateAngleZ += breatheAnim * 0.002F;
		this.lowerLeftArm.arm.rotateAngleZ -= breatheAnim * 0.002F;
		arr$ = this.lowerRightArm.finger;
		len$ = arr$.length;

		for (i$ = 0; i$ < len$; ++i$) {
			finger = arr$[i$];
			finger.rotateAngleZ += breatheAnim * 0.02F;
		}

		this.lowerRightArm.thumb.rotateAngleZ -= breatheAnim * 0.02F;
		arr$ = this.lowerLeftArm.finger;
		len$ = arr$.length;

		for (i$ = 0; i$ < len$; ++i$) {
			finger = arr$[i$];
			finger.rotateAngleZ -= breatheAnim * 0.02F;
		}

		this.lowerLeftArm.thumb.rotateAngleZ += breatheAnim * 0.02F;
		this.pelvis.rotationPointY -= Math.abs(walkAnim[0]);
		this.chest.rotateAngleY -= walkAnim[0] * 0.06F;
		this.rightArm.arm.rotateAngleX -= walkAnim[1] * 0.6F;
		this.leftArm.arm.rotateAngleX += walkAnim[2] * 0.6F;
		this.rightArm.forearm.rotateAngleX -= walkAnim[1] * 0.2F;
		this.leftArm.forearm.rotateAngleX += walkAnim[2] * 0.2F;
		this.lowerRightArm.arm.rotateAngleX -= walkAnim[3] * 0.3F;
		this.lowerLeftArm.arm.rotateAngleX += walkAnim[4] * 0.3F;
		this.lowerRightArm.forearm.rotateAngleX -= walkAnim[3] * 0.1F;
		this.lowerLeftArm.forearm.rotateAngleX += walkAnim[4] * 0.1F;
		this.legjoint1.rotateAngleX += walkAnim1 * 0.6F;
		this.legjoint2.rotateAngleX += walkAnim2 * 0.6F;
		this.foreleg1.rotateAngleX += walkAnim3 * 0.3F;
		this.foreleg2.rotateAngleX += walkAnim4 * 0.3F;
	}

	private void animateHoldBlock(int fullTick, int armID, boolean hasTarget) {
		float tick = ((float)fullTick + this.animTick) / 10.0F;
		if (!hasTarget) {
			tick = fullTick == 0 ? 0.0F : ((float)fullTick - this.animTick) / 10.0F;
		}

		float f = MathHelper.sin(tick * 3.1415927F / 2.0F);
		int i;
		if (armID == 1) {
			this.rightArm.arm.rotateAngleZ += f * 0.8F;
			this.rightArm.forearm.rotateAngleZ += f * 0.6F;
			this.rightArm.hand.rotateAngleY += f * 0.8F;
			this.rightArm.finger[0].rotateAngleX += -f * 0.2F;
			this.rightArm.finger[2].rotateAngleX += f * 0.2F;

			for (i = 0; i < this.rightArm.finger.length; ++i) {
				this.rightArm.finger[i].rotateAngleZ += f * 0.6F;
			}

			this.rightArm.thumb.rotateAngleZ += -f * 0.4F;
		} else if (armID == 2) {
			this.leftArm.arm.rotateAngleZ += -f * 0.8F;
			this.leftArm.forearm.rotateAngleZ += -f * 0.6F;
			this.leftArm.hand.rotateAngleY += -f * 0.8F;
			this.leftArm.finger[0].rotateAngleX += -f * 0.2F;
			this.leftArm.finger[2].rotateAngleX += f * 0.2F;

			for (i = 0; i < this.leftArm.finger.length; ++i) {
				this.leftArm.finger[i].rotateAngleZ += -f * 0.6F;
			}

			this.leftArm.thumb.rotateAngleZ += f * 0.4F;
		} else if (armID == 3) {
			this.lowerRightArm.arm.rotateAngleZ += f * 0.5F;
			this.lowerRightArm.forearm.rotateAngleZ += f * 0.4F;
			this.lowerRightArm.hand.rotateAngleY += f * 0.4F;
			this.lowerRightArm.finger[0].rotateAngleX += -f * 0.2F;
			this.lowerRightArm.finger[2].rotateAngleX += f * 0.2F;

			for (i = 0; i < this.lowerRightArm.finger.length; ++i) {
				this.lowerRightArm.finger[i].rotateAngleZ += f * 0.6F;
			}

			this.lowerRightArm.thumb.rotateAngleZ += -f * 0.4F;
		} else if (armID == 4) {
			this.lowerLeftArm.arm.rotateAngleZ += -f * 0.5F;
			this.lowerLeftArm.forearm.rotateAngleZ += -f * 0.4F;
			this.lowerLeftArm.hand.rotateAngleY += -f * 0.4F;
			this.lowerLeftArm.finger[0].rotateAngleX += -f * 0.2F;
			this.lowerLeftArm.finger[2].rotateAngleX += f * 0.2F;

			for (i = 0; i < this.lowerLeftArm.finger.length; ++i) {
				this.lowerLeftArm.finger[i].rotateAngleZ += -f * 0.6F;
			}

			this.lowerLeftArm.thumb.rotateAngleZ += f * 0.4F;
		}

	}

	private void animateMelee(int fullTick, int armID) {
		int right = (armID & 1) == 1 ? 1 : -1;
		// boolean lower = armID >= 3;
		EndermanArm arm = this.getArmFromID(armID);
		float tick;
		float f;
		if (fullTick < 2) {
			tick = ((float)fullTick + this.animTick) / 2.0F;
			f = MathHelper.sin(tick * 3.1415927F / 2.0F);
			arm.arm.rotateAngleX += f * 0.2F;
			arm.finger[0].rotateAngleZ += f * 0.3F * (float)right;
			arm.finger[1].rotateAngleZ += f * 0.3F * (float)right;
			arm.finger[2].rotateAngleZ += f * 0.3F * (float)right;
			arm.foreFinger[0].rotateAngleZ += -f * 0.5F * (float)right;
			arm.foreFinger[1].rotateAngleZ += -f * 0.5F * (float)right;
			arm.foreFinger[2].rotateAngleZ += -f * 0.5F * (float)right;
		} else if (fullTick < 5) {
			tick = ((float)(fullTick - 2) + this.animTick) / 3.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			float f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.chest.rotateAngleY += -f1 * 0.1F * (float)right;
			arm.arm.rotateAngleX += f * 1.1F - 1.1F;
			arm.forearm.rotateAngleX += -f * 0.4F;
			arm.finger[0].rotateAngleZ += 0.3F * (float)right;
			arm.finger[1].rotateAngleZ += 0.3F * (float)right;
			arm.finger[2].rotateAngleZ += 0.3F * (float)right;
			arm.foreFinger[0].rotateAngleZ += -0.5F * (float)right;
			arm.foreFinger[1].rotateAngleZ += -0.5F * (float)right;
			arm.foreFinger[2].rotateAngleZ += -0.5F * (float)right;
		} else if (fullTick < 6) {
			this.chest.rotateAngleY += -0.1F * (float)right;
			arm.arm.rotateAngleX += -1.1F;
			arm.forearm.rotateAngleX += -0.4F;
			arm.finger[0].rotateAngleZ += 0.3F * (float)right;
			arm.finger[1].rotateAngleZ += 0.3F * (float)right;
			arm.finger[2].rotateAngleZ += 0.3F * (float)right;
			arm.foreFinger[0].rotateAngleZ += -0.5F * (float)right;
			arm.foreFinger[1].rotateAngleZ += -0.5F * (float)right;
			arm.foreFinger[2].rotateAngleZ += -0.5F * (float)right;
		} else if (fullTick < 10) {
			tick = ((float)(fullTick - 6) + this.animTick) / 4.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			this.chest.rotateAngleY += -f * 0.1F * (float)right;
			arm.arm.rotateAngleX += -f * 1.1F;
			arm.forearm.rotateAngleX += -f * 0.4F;
			arm.finger[0].rotateAngleZ += f * 0.3F * (float)right;
			arm.finger[1].rotateAngleZ += f * 0.3F * (float)right;
			arm.finger[2].rotateAngleZ += f * 0.3F * (float)right;
			arm.foreFinger[0].rotateAngleZ += -f * 0.5F * (float)right;
			arm.foreFinger[1].rotateAngleZ += -f * 0.5F * (float)right;
			arm.foreFinger[2].rotateAngleZ += -f * 0.5F * (float)right;
		}
	}

	private void animateThrowBlock(int fullTick, int armID) {
		float tick;
		float f;
		float f1;
		int i;
		if (armID == 1) {
			if (fullTick < 4) {
				tick = ((float)fullTick + this.animTick) / 4.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
				this.rightArm.arm.rotateAngleX += -f1 * 1.5F;
				this.rightArm.arm.rotateAngleZ += f * 0.8F;
				this.rightArm.forearm.rotateAngleZ += f * 0.6F;
				this.rightArm.hand.rotateAngleY += f * 0.8F;
				this.rightArm.finger[0].rotateAngleX += -f * 0.2F;
				this.rightArm.finger[2].rotateAngleX += f * 0.2F;

				for (i = 0; i < this.rightArm.finger.length; ++i) {
					this.rightArm.finger[i].rotateAngleZ += f * 0.6F;
				}

				this.rightArm.thumb.rotateAngleZ += -f * 0.4F;
			} else if (fullTick < 7) {
				this.rightArm.arm.rotateAngleX += -1.5F;
			} else if (fullTick < 14) {
				tick = ((float)(fullTick - 7) + this.animTick) / 7.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				this.rightArm.arm.rotateAngleX += -f * 1.5F;
			}
		} else if (armID == 2) {
			if (fullTick < 4) {
				tick = ((float)fullTick + this.animTick) / 4.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
				this.leftArm.arm.rotateAngleX += -f1 * 1.5F;
				this.leftArm.arm.rotateAngleZ += -f * 0.8F;
				this.leftArm.forearm.rotateAngleZ += -f * 0.6F;
				this.leftArm.hand.rotateAngleY += -f * 0.8F;
				this.leftArm.finger[0].rotateAngleX += -f * 0.2F;
				this.leftArm.finger[2].rotateAngleX += f * 0.2F;

				for (i = 0; i < this.leftArm.finger.length; ++i) {
					this.leftArm.finger[i].rotateAngleZ += -f * 0.6F;
				}

				this.leftArm.thumb.rotateAngleZ += f * 0.4F;
			} else if (fullTick < 7) {
				this.leftArm.arm.rotateAngleX += -1.5F;
			} else if (fullTick < 14) {
				tick = ((float)(fullTick - 7) + this.animTick) / 7.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				this.leftArm.arm.rotateAngleX += -f * 1.5F;
			}
		} else if (armID == 3) {
			if (fullTick < 4) {
				tick = ((float)fullTick + this.animTick) / 4.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
				this.lowerRightArm.arm.rotateAngleX += -f1 * 1.5F;
				this.lowerRightArm.arm.rotateAngleZ += f * 0.5F;
				this.lowerRightArm.forearm.rotateAngleZ += f * 0.4F;
				this.lowerRightArm.hand.rotateAngleY += f * 0.4F;
				this.lowerRightArm.finger[0].rotateAngleX += -f * 0.2F;
				this.lowerRightArm.finger[2].rotateAngleX += f * 0.2F;

				for (i = 0; i < this.lowerRightArm.finger.length; ++i) {
					this.lowerRightArm.finger[i].rotateAngleZ += f * 0.6F;
				}

				this.lowerRightArm.thumb.rotateAngleZ += -f * 0.4F;
			} else if (fullTick < 7) {
				this.lowerRightArm.arm.rotateAngleX += -1.5F;
			} else if (fullTick < 14) {
				tick = ((float)(fullTick - 7) + this.animTick) / 7.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				this.lowerRightArm.arm.rotateAngleX += -f * 1.5F;
			}
		} else if (armID == 4) {
			if (fullTick < 4) {
				tick = ((float)fullTick + this.animTick) / 4.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
				this.lowerLeftArm.arm.rotateAngleX += -f1 * 1.5F;
				this.lowerLeftArm.arm.rotateAngleZ += -f * 0.5F;
				this.lowerLeftArm.forearm.rotateAngleZ += -f * 0.4F;
				this.lowerLeftArm.hand.rotateAngleY += -f * 0.4F;
				this.lowerLeftArm.finger[0].rotateAngleX += -f * 0.2F;
				this.lowerLeftArm.finger[2].rotateAngleX += f * 0.2F;

				for (i = 0; i < this.lowerLeftArm.finger.length; ++i) {
					this.lowerLeftArm.finger[i].rotateAngleZ += -f * 0.6F;
				}

				this.lowerLeftArm.thumb.rotateAngleZ += f * 0.4F;
			} else if (fullTick < 7) {
				this.lowerLeftArm.arm.rotateAngleX += -1.5F;
			} else if (fullTick < 14) {
				tick = ((float)(fullTick - 7) + this.animTick) / 7.0F;
				f = MathHelper.cos(tick * 3.1415927F / 2.0F);
				this.lowerLeftArm.arm.rotateAngleX += -f * 1.5F;
			}
		}
	}

	private void animateScream(int fullTick) {
		float tick;
		float f;
		if (fullTick < 35) {
			tick = ((float)fullTick + this.animTick) / 35.0F;
			f = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.abdomen.rotateAngleX += f * 0.3F;
			this.chest.rotateAngleX += f * 0.4F;
			this.neck.rotateAngleX += f * 0.2F;
			this.head.rotateAngleX += f * 0.3F;
			this.rightArm.arm.rotateAngleX += -f * 0.6F;
			this.rightArm.arm.rotateAngleY += f * 0.4F;
			this.rightArm.forearm.rotateAngleX += -f * 0.8F;
			this.rightArm.hand.rotateAngleZ += -f * 0.4F;

			int i;
			for (i = 0; i < 3; ++i) {
				this.rightArm.finger[i].rotateAngleZ += f * 0.3F;
				this.rightArm.foreFinger[i].rotateAngleZ += -f * 0.5F;
			}

			this.leftArm.arm.rotateAngleX += -f * 0.6F;
			this.leftArm.arm.rotateAngleY += -f * 0.4F;
			this.leftArm.forearm.rotateAngleX += -f * 0.8F;
			this.leftArm.hand.rotateAngleZ += f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.leftArm.finger[i].rotateAngleZ += -f * 0.3F;
				this.leftArm.foreFinger[i].rotateAngleZ += f * 0.5F;
			}

			this.lowerRightArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerRightArm.arm.rotateAngleY += f * 0.2F;
			this.lowerRightArm.forearm.rotateAngleX += -f * 0.8F;
			this.lowerRightArm.hand.rotateAngleZ += -f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerRightArm.finger[i].rotateAngleZ += f * 0.3F;
				this.lowerRightArm.foreFinger[i].rotateAngleZ += -f * 0.5F;
			}

			this.lowerLeftArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerLeftArm.arm.rotateAngleY += -f * 0.2F;
			this.lowerLeftArm.forearm.rotateAngleX += -f * 0.8F;
			this.lowerLeftArm.hand.rotateAngleZ += f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerLeftArm.finger[i].rotateAngleZ += -f * 0.3F;
				this.lowerLeftArm.foreFinger[i].rotateAngleZ += f * 0.5F;
			}
		} else if (fullTick < 40) {
			this.abdomen.rotateAngleX += 0.3F;
			this.chest.rotateAngleX += 0.4F;
			this.neck.rotateAngleX += 0.2F;
			this.head.rotateAngleX += 0.3F;
			this.rightArm.arm.rotateAngleX += -0.6F;
			this.rightArm.arm.rotateAngleY += 0.4F;
			this.rightArm.forearm.rotateAngleX += -0.8F;
			this.rightArm.hand.rotateAngleZ += -0.4F;

			int i;
			for (i = 0; i < 3; ++i) {
				this.rightArm.finger[i].rotateAngleZ += 0.3F;
				this.rightArm.foreFinger[i].rotateAngleZ += -0.5F;
			}

			this.leftArm.arm.rotateAngleX += -0.6F;
			this.leftArm.arm.rotateAngleY += -0.4F;
			this.leftArm.forearm.rotateAngleX += -0.8F;
			this.leftArm.hand.rotateAngleZ += 0.4F;

			for (i = 0; i < 3; ++i) {
				this.leftArm.finger[i].rotateAngleZ += -0.3F;
				this.leftArm.foreFinger[i].rotateAngleZ += 0.5F;
			}

			this.lowerRightArm.arm.rotateAngleX += -0.4F;
			this.lowerRightArm.arm.rotateAngleY += 0.2F;
			this.lowerRightArm.forearm.rotateAngleX += -0.8F;
			this.lowerRightArm.hand.rotateAngleZ += -0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerRightArm.finger[i].rotateAngleZ += 0.3F;
				this.lowerRightArm.foreFinger[i].rotateAngleZ += -0.5F;
			}

			this.lowerLeftArm.arm.rotateAngleX += -0.4F;
			this.lowerLeftArm.arm.rotateAngleY += -0.2F;
			this.lowerLeftArm.forearm.rotateAngleX += -0.8F;
			this.lowerLeftArm.hand.rotateAngleZ += 0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerLeftArm.finger[i].rotateAngleZ += -0.3F;
				this.lowerLeftArm.foreFinger[i].rotateAngleZ += 0.5F;
			}
		} else if (fullTick < 44) {
			tick = ((float)(fullTick - 40) + this.animTick) / 4.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			float f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.abdomen.rotateAngleX += -f * 0.1F + 0.4F;
			this.chest.rotateAngleX += f * 0.1F + 0.3F;
			this.chest.rotateAngleZ += f1 * 0.5F;
			this.neck.rotateAngleX += f * 0.2F;
			this.neck.rotateAngleZ += f1 * 0.2F;
			this.head.rotateAngleX += f * 1.2F - 0.8F;
			this.head.rotateAngleZ += f1 * 0.4F;
			this.mouth.rotateAngleX += f1 * 0.6F;
			this.rightArm.arm.rotateAngleX += -f * 0.6F;
			this.rightArm.arm.rotateAngleY += 0.4F;
			this.rightArm.forearm.rotateAngleX += -f * 0.8F;
			this.rightArm.hand.rotateAngleZ += -f * 0.4F;

			int i;
			for (i = 0; i < 3; ++i) {
				this.rightArm.finger[i].rotateAngleZ += f * 0.3F;
				this.rightArm.foreFinger[i].rotateAngleZ += -f * 0.5F;
			}

			this.leftArm.arm.rotateAngleX += -f * 0.6F;
			this.leftArm.arm.rotateAngleY += -0.4F;
			this.leftArm.forearm.rotateAngleX += -f * 0.8F;
			this.leftArm.hand.rotateAngleZ += f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.leftArm.finger[i].rotateAngleZ += -f * 0.3F;
				this.leftArm.foreFinger[i].rotateAngleZ += f * 0.5F;
			}

			this.lowerRightArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerRightArm.arm.rotateAngleY += -f * 0.1F + 0.3F;
			this.lowerRightArm.forearm.rotateAngleX += -f * 0.8F;
			this.lowerRightArm.hand.rotateAngleZ += -f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerRightArm.finger[i].rotateAngleZ += f * 0.3F;
				this.lowerRightArm.foreFinger[i].rotateAngleZ += -f * 0.5F;
			}

			this.lowerLeftArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerLeftArm.arm.rotateAngleY += f * 0.1F - 0.3F;
			this.lowerLeftArm.forearm.rotateAngleX += -f * 0.8F;
			this.lowerLeftArm.hand.rotateAngleZ += f * 0.4F;

			for (i = 0; i < 3; ++i) {
				this.lowerLeftArm.finger[i].rotateAngleZ += -f * 0.3F;
				this.lowerLeftArm.foreFinger[i].rotateAngleZ += f * 0.5F;
			}

			this.leg1.rotateAngleZ += f1 * 0.1F;
			this.leg2.rotateAngleZ += -f1 * 0.1F;
		} else if (fullTick < 155) {
			tick = ((float)(fullTick - 44) + this.animTick) / 111.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			this.abdomen.rotateAngleX += 0.4F;
			this.chest.rotateAngleX += 0.3F;
			this.chest.rotateAngleZ += f * 1.0F - 0.5F;
			this.neck.rotateAngleZ += f * 0.4F - 0.2F;
			this.head.rotateAngleX += -0.8F;
			this.head.rotateAngleZ += f * 0.8F - 0.4F;
			this.mouth.rotateAngleX += 0.6F;
			this.rightArm.arm.rotateAngleY += 0.4F;
			this.leftArm.arm.rotateAngleY += -0.4F;
			this.lowerRightArm.arm.rotateAngleY += 0.3F;
			this.lowerLeftArm.arm.rotateAngleY += -0.3F;
			this.leg1.rotateAngleZ += 0.1F;
			this.leg2.rotateAngleZ += -0.1F;
		} else if (fullTick < 160) {
			tick = ((float)(fullTick - 155) + this.animTick) / 5.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			this.abdomen.rotateAngleX += f * 0.4F;
			this.chest.rotateAngleX += f * 0.3F;
			this.chest.rotateAngleZ += -f * 0.5F;
			this.neck.rotateAngleZ += -f * 0.2F;
			this.head.rotateAngleX += -f * 0.8F;
			this.head.rotateAngleZ += -f * 0.4F;
			this.mouth.rotateAngleX += f * 0.6F;
			this.rightArm.arm.rotateAngleY += f * 0.4F;
			this.leftArm.arm.rotateAngleY += -f * 0.4F;
			this.lowerRightArm.arm.rotateAngleY += f * 0.3F;
			this.lowerLeftArm.arm.rotateAngleY += -f * 0.3F;
			this.leg1.rotateAngleZ += f * 0.1F;
			this.leg2.rotateAngleZ += -f * 0.1F;
		}
	}

	private void animateTeleSmash(int fullTick) {
		float tick;
		float f;
		if (fullTick < 18) {
			tick = ((float)fullTick + this.animTick) / 18.0F;
			f = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.chest.rotateAngleX += -f * 0.3F;
			this.rightArm.arm.rotateAngleY += f * 0.2F;
			this.rightArm.arm.rotateAngleZ += f * 0.8F;
			this.rightArm.hand.rotateAngleY += f * 1.7F;
			this.leftArm.arm.rotateAngleY += -f * 0.2F;
			this.leftArm.arm.rotateAngleZ += -f * 0.8F;
			this.leftArm.hand.rotateAngleY += -f * 1.7F;
			this.lowerRightArm.arm.rotateAngleY += f * 0.2F;
			this.lowerRightArm.arm.rotateAngleZ += f * 0.6F;
			this.lowerRightArm.hand.rotateAngleY += f * 1.7F;
			this.lowerLeftArm.arm.rotateAngleY += -f * 0.2F;
			this.lowerLeftArm.arm.rotateAngleZ += -f * 0.6F;
			this.lowerLeftArm.hand.rotateAngleY += -f * 1.7F;
		} else if (fullTick < 20) {
			tick = ((float)(fullTick - 18) + this.animTick) / 2.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			float f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.chest.rotateAngleX += -f * 0.3F;
			this.rightArm.arm.rotateAngleX += -f1 * 0.8F;
			this.rightArm.arm.rotateAngleY += 0.2F;
			this.rightArm.arm.rotateAngleZ += 0.8F;
			++this.rightArm.hand.rotateAngleY;
			this.leftArm.arm.rotateAngleX += -f1 * 0.8F;
			this.leftArm.arm.rotateAngleY += -0.2F;
			this.leftArm.arm.rotateAngleZ += -0.8F;
			this.leftArm.hand.rotateAngleY += -1.7F;
			this.lowerRightArm.arm.rotateAngleX += -f1 * 0.9F;
			this.lowerRightArm.arm.rotateAngleY += 0.2F;
			this.lowerRightArm.arm.rotateAngleZ += 0.6F;
			++this.lowerRightArm.hand.rotateAngleY;
			this.lowerLeftArm.arm.rotateAngleX += -f1 * 0.9F;
			this.lowerLeftArm.arm.rotateAngleY += -0.2F;
			this.lowerLeftArm.arm.rotateAngleZ += -0.6F;
			this.lowerLeftArm.hand.rotateAngleY += -1.7F;
		} else if (fullTick < 24) {
			this.rightArm.arm.rotateAngleX += -0.8F;
			this.rightArm.arm.rotateAngleY += 0.2F;
			this.rightArm.arm.rotateAngleZ += 0.8F;
			++this.rightArm.hand.rotateAngleY;
			this.leftArm.arm.rotateAngleX += -0.8F;
			this.leftArm.arm.rotateAngleY += -0.2F;
			this.leftArm.arm.rotateAngleZ += -0.8F;
			this.leftArm.hand.rotateAngleY += -1.7F;
			this.lowerRightArm.arm.rotateAngleX += -0.9F;
			this.lowerRightArm.arm.rotateAngleY += 0.2F;
			this.lowerRightArm.arm.rotateAngleZ += 0.6F;
			++this.lowerRightArm.hand.rotateAngleY;
			this.lowerLeftArm.arm.rotateAngleX += -0.9F;
			this.lowerLeftArm.arm.rotateAngleY += -0.2F;
			this.lowerLeftArm.arm.rotateAngleZ += -0.6F;
			this.lowerLeftArm.hand.rotateAngleY += -1.7F;
		} else if (fullTick < 30) {
			tick = ((float)(fullTick - 24) + this.animTick) / 6.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			this.rightArm.arm.rotateAngleX += -f * 0.8F;
			this.rightArm.arm.rotateAngleY += f * 0.2F;
			this.rightArm.arm.rotateAngleZ += f * 0.8F;
			this.rightArm.hand.rotateAngleY += f * 1.7F;
			this.leftArm.arm.rotateAngleX += -f * 0.8F;
			this.leftArm.arm.rotateAngleY += -f * 0.2F;
			this.leftArm.arm.rotateAngleZ += -f * 0.8F;
			this.leftArm.hand.rotateAngleY += -f * 1.7F;
			this.lowerRightArm.arm.rotateAngleX += -f * 0.9F;
			this.lowerRightArm.arm.rotateAngleY += f * 0.2F;
			this.lowerRightArm.arm.rotateAngleZ += f * 0.6F;
			this.lowerRightArm.hand.rotateAngleY += f * 1.7F;
			this.lowerLeftArm.arm.rotateAngleX += -f * 0.9F;
			this.lowerLeftArm.arm.rotateAngleY += -f * 0.2F;
			this.lowerLeftArm.arm.rotateAngleZ += -f * 0.6F;
			this.lowerLeftArm.hand.rotateAngleY += -f * 1.7F;
		}

	}

	private void animateDeath(int deathTick) {
		float tick;
		float f;
		if (deathTick < 80) {
			tick = ((float)deathTick + this.animTick) / 80.0F;
			f = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.head.rotateAngleX += f * 0.4F;
			this.neck.rotateAngleX += f * 0.3F;
			this.pelvis.rotationPointY += -f * 12.0F;
			this.rightArm.arm.rotateAngleX += -f * 0.4F;
			this.rightArm.arm.rotateAngleY += f * 0.4F;
			this.rightArm.arm.rotateAngleZ += f * 0.6F;
			this.rightArm.forearm.rotateAngleX += -f * 1.2F;
			this.leftArm.arm.rotateAngleX += -f * 0.4F;
			this.leftArm.arm.rotateAngleY += -f * 0.2F;
			this.leftArm.arm.rotateAngleZ += -f * 0.6F;
			this.leftArm.forearm.rotateAngleX += -f * 1.2F;
			this.lowerRightArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerRightArm.arm.rotateAngleY += f * 0.4F;
			this.lowerRightArm.arm.rotateAngleZ += f * 0.6F;
			this.lowerRightArm.forearm.rotateAngleX += -f * 1.2F;
			this.lowerLeftArm.arm.rotateAngleX += -f * 0.4F;
			this.lowerLeftArm.arm.rotateAngleY += -f * 0.2F;
			this.lowerLeftArm.arm.rotateAngleZ += -f * 0.6F;
			this.lowerLeftArm.forearm.rotateAngleX += -f * 1.2F;
			this.leg1.rotateAngleX += -f * 0.9F;
			this.leg1.rotateAngleY += f * 0.3F;
			this.leg2.rotateAngleX += -f * 0.9F;
			this.leg2.rotateAngleY += -f * 0.3F;
			this.foreleg1.rotateAngleX += f * 1.6F;
			this.foreleg2.rotateAngleX += f * 1.6F;
		} else if (deathTick < 84) {
			tick = ((float)(deathTick - 80) + this.animTick) / 4.0F;
			f = MathHelper.cos(tick * 3.1415927F / 2.0F);
			float f1 = MathHelper.sin(tick * 3.1415927F / 2.0F);
			this.head.rotateAngleX += f * 0.4F;
			this.neck.rotateAngleX += f * 0.4F - 0.1F;
			this.chest.rotateAngleX += -f1 * 0.8F;
			this.abdomen.rotateAngleX += -f1 * 0.2F;
			this.pelvis.rotationPointY += -12.0F;
			this.rightArm.arm.rotateAngleX += -f * 0.4F;
			this.rightArm.arm.rotateAngleY += -f * 1.4F + 1.8F;
			this.rightArm.arm.rotateAngleZ += f * 0.6F;
			this.rightArm.forearm.rotateAngleX += -f * 1.2F;
			this.leftArm.arm.rotateAngleX += -f * 0.4F;
			this.leftArm.arm.rotateAngleY += f * 1.6F - 1.8F;
			this.leftArm.arm.rotateAngleZ += -f * 0.6F;
			this.leftArm.forearm.rotateAngleX += -f * 1.2F;
			this.lowerRightArm.arm.rotateAngleX += -f * 0.5F + 0.1F;
			this.lowerRightArm.arm.rotateAngleY += -f * 1.1F + 1.5F;
			this.lowerRightArm.arm.rotateAngleZ += f * 0.6F;
			this.lowerRightArm.forearm.rotateAngleX += -f * 1.2F;
			this.lowerLeftArm.arm.rotateAngleX += -f * 0.5F + 0.1F;
			this.lowerLeftArm.arm.rotateAngleY += f * 1.1F - 1.5F;
			this.lowerLeftArm.arm.rotateAngleZ += -f * 0.6F;
			this.lowerLeftArm.forearm.rotateAngleX += -f * 1.2F;
			this.leg1.rotateAngleX += -f * 1.7F + 0.8F;
			this.leg1.rotateAngleY += f * 0.3F;
			this.leg1.rotateAngleZ += f1 * 0.2F;
			this.leg2.rotateAngleX += -f * 1.7F + 0.8F;
			this.leg2.rotateAngleY += -f * 0.3F;
			this.leg2.rotateAngleZ += -f1 * 0.2F;
			this.foreleg1.rotateAngleX += f * 1.6F;
			this.foreleg2.rotateAngleX += f * 1.6F;
		} else {
			this.neck.rotateAngleX += -0.1F;
			this.chest.rotateAngleX += -0.8F;
			this.abdomen.rotateAngleX += -0.2F;
			this.pelvis.rotationPointY += -12.0F;
			++this.rightArm.arm.rotateAngleY;
			this.leftArm.arm.rotateAngleY += -1.8F;
			this.lowerRightArm.arm.rotateAngleX += 0.1F;
			++this.lowerRightArm.arm.rotateAngleY;
			this.lowerLeftArm.arm.rotateAngleX += 0.1F;
			this.lowerLeftArm.arm.rotateAngleY += -1.5F;
			this.leg1.rotateAngleX += 0.8F;
			this.leg1.rotateAngleZ += 0.2F;
			this.leg2.rotateAngleX += 0.8F;
			this.leg2.rotateAngleZ += -0.2F;
		}
	}

	public EndermanArm getArmFromID(int armID) {
		return armID == 1 ? this.rightArm : (armID == 2 ? this.leftArm : (armID == 3 ? this.lowerRightArm : this.lowerLeftArm));
	}
}