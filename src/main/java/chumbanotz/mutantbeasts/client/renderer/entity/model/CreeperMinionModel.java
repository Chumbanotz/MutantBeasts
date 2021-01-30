package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;

public class CreeperMinionModel extends EntityModel<CreeperMinionEntity> {
	private final RendererModel head;
	private final RendererModel body;
	private final RendererModel leg1;
	private final RendererModel leg2;
	private final RendererModel leg3;
	private final RendererModel leg4;

	public CreeperMinionModel() {
		this(0.0F);
	}

	public CreeperMinionModel(float scale) {
		this.head = new RendererModel(this, 0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scale);
		this.head.setRotationPoint(0.0F, 6.0F, 0.0F);
		this.body = new RendererModel(this, 16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale);
		this.body.setRotationPoint(0.0F, 6.0F, 0.0F);
		this.leg1 = new RendererModel(this, 0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.leg1.setRotationPoint(-2.0F, 18.0F, 4.0F);
		this.leg2 = new RendererModel(this, 0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.leg2.setRotationPoint(2.0F, 18.0F, 4.0F);
		this.leg3 = new RendererModel(this, 0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.leg3.setRotationPoint(-2.0F, 18.0F, -4.0F);
		this.leg4 = new RendererModel(this, 0, 16);
		this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.leg4.setRotationPoint(2.0F, 18.0F, -4.0F);
	}

	@Override
	public void render(CreeperMinionEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.head.render(scale);
		this.body.render(scale);
		this.leg1.render(scale);
		this.leg2.render(scale);
		this.leg3.render(scale);
		this.leg4.render(scale);
	}

	@Override
	public void setRotationAngles(CreeperMinionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		limbSwing *= 3.0F;
		this.head.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
		this.head.rotateAngleX = headPitch * ((float)Math.PI / 180F);
		this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.head.rotationPointY = 6.0F;
		this.body.rotationPointY = 6.0F;
		this.leg1.setRotationPoint(-2.0F, 18.0F, 4.0F);
		this.leg2.setRotationPoint(2.0F, 18.0F, 4.0F);
		this.leg3.setRotationPoint(-2.0F, 18.0F, -4.0F);
		this.leg4.setRotationPoint(2.0F, 18.0F, -4.0F);

		if (entity == null || entity.isSitting()) {
			this.head.rotationPointY += 6.0F;
			this.body.rotationPointY += 6.0F;
			this.leg1.rotationPointY += 4.0F;
			this.leg1.rotationPointZ -= 2.0F;
			this.leg2.rotationPointY += 4.0F;
			this.leg2.rotationPointZ -= 2.0F;
			this.leg3.rotationPointY += 4.0F;
			this.leg3.rotationPointZ += 2.0F;
			this.leg4.rotationPointY += 4.0F;
			this.leg4.rotationPointZ += 2.0F;
			this.leg1.rotateAngleX = 1.5707964F;
			this.leg2.rotateAngleX = 1.5707964F;
			this.leg3.rotateAngleX = -1.5707964F;
			this.leg4.rotateAngleX = -1.5707964F;
		}
	}
}