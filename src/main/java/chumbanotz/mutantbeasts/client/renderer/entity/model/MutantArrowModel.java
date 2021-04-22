package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class MutantArrowModel extends EntityModel<MutantArrowEntity> {
	private final RendererModel stick = new RendererModel(this, 0, 0);
	private final RendererModel point1;
	private final RendererModel point2;
	private final RendererModel point3;
	private final RendererModel point4;

	public MutantArrowModel() {
		this.stick.addBox(-0.5F, -0.5F, -13.0F, 1, 1, 26);
		this.stick.setRotationPoint(0.0F, 24.0F, 0.0F);
		this.point1 = new RendererModel(this, 0, 0);
		this.point1.addBox(-3.0F, -0.5F, 0.0F, 3, 1, 1, 0.25F);
		this.point1.setRotationPoint(0.0F, 0.0F, -12.0F);
		this.stick.addChild(this.point1);
		this.point2 = new RendererModel(this, 0, 0);
		this.point2.addBox(0.0F, -0.5F, 0.0F, 3, 1, 1, 0.251F);
		this.point2.setRotationPoint(0.0F, 0.0F, -12.0F);
		this.stick.addChild(this.point2);
		this.point3 = new RendererModel(this, 0, 2);
		this.point3.addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1, 0.25F);
		this.point3.setRotationPoint(0.0F, 0.0F, -13.0F);
		this.stick.addChild(this.point3);
		this.point4 = new RendererModel(this, 0, 2);
		this.point4.addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1, 0.251F);
		this.point4.setRotationPoint(0.0F, 0.0F, -13.0F);
		this.stick.addChild(this.point4);
		this.point1.rotateAngleY = 0.7853982F;
		this.point2.rotateAngleY = -0.7853982F;
		this.point3.rotateAngleX = -0.7853982F;
		this.point4.rotateAngleX = 0.7853982F;
	}

	@Override
	public void render(MutantArrowEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.stick.render(scale);
	}
}