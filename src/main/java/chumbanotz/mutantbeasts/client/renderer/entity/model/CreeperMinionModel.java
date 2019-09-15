package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperMinionModel extends EntityModel<CreeperMinionEntity> {
	private boolean sitting;
	private final RendererModel field_78135_a;
	private final RendererModel field_78134_c;
	private final RendererModel field_78131_d;
	private final RendererModel field_78132_e;
	private final RendererModel field_78129_f;
	private final RendererModel field_78130_g;

	public CreeperMinionModel() {
		this(0.0F);
	}

	public CreeperMinionModel(float scale) {
		this.field_78135_a = new RendererModel(this, 0, 0);
		this.field_78135_a.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scale);
		this.field_78135_a.setRotationPoint(0.0F, 6.0F, 0.0F);
		this.field_78134_c = new RendererModel(this, 16, 16);
		this.field_78134_c.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale);
		this.field_78134_c.setRotationPoint(0.0F, 6.0F, 0.0F);
		this.field_78131_d = new RendererModel(this, 0, 16);
		this.field_78131_d.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.field_78131_d.setRotationPoint(-2.0F, 18.0F, 4.0F);
		this.field_78132_e = new RendererModel(this, 0, 16);
		this.field_78132_e.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.field_78132_e.setRotationPoint(2.0F, 18.0F, 4.0F);
		this.field_78129_f = new RendererModel(this, 0, 16);
		this.field_78129_f.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.field_78129_f.setRotationPoint(-2.0F, 18.0F, -4.0F);
		this.field_78130_g = new RendererModel(this, 0, 16);
		this.field_78130_g.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
		this.field_78130_g.setRotationPoint(2.0F, 18.0F, -4.0F);
	}

	@Override
	public void render(CreeperMinionEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.sitting = this.isSitting || entityIn.isSitting();
		this.field_78135_a.render(scale);
		this.field_78134_c.render(scale);
		this.field_78131_d.render(scale);
		this.field_78132_e.render(scale);
		this.field_78129_f.render(scale);
		this.field_78130_g.render(scale);
	}

	@Override
	public void setRotationAngles(CreeperMinionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		this.field_78135_a.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
		this.field_78135_a.rotateAngleX = headPitch * ((float)Math.PI / 180F);
		this.field_78131_d.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.field_78132_e.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.field_78129_f.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.field_78130_g.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

		this.field_78135_a.rotationPointY = 4.0F;
		this.field_78134_c.rotationPointY = 4.0F;
		this.field_78131_d.setRotationPoint(-2.0F, 16.0F, 4.0F);
		this.field_78132_e.setRotationPoint(2.0F, 16.0F, 4.0F);
		this.field_78129_f.setRotationPoint(-2.0F, 16.0F, -4.0F);
		this.field_78130_g.setRotationPoint(2.0F, 16.0F, -4.0F);

		if (this.sitting) {
			this.field_78135_a.rotationPointY += 8.0F;
			this.field_78134_c.rotationPointY += 8.0F;
			this.field_78131_d.rotationPointY += 6.0F;
			this.field_78131_d.rotationPointZ -= 2.0F;
			this.field_78132_e.rotationPointY += 6.0F;
			this.field_78132_e.rotationPointZ -= 2.0F;
			this.field_78129_f.rotationPointY += 6.0F;
			this.field_78129_f.rotationPointZ += 2.0F;
			this.field_78130_g.rotationPointY += 6.0F;
			this.field_78130_g.rotationPointZ += 2.0F;
			this.field_78131_d.rotateAngleX = 1.5707964F;
			this.field_78132_e.rotateAngleX = 1.5707964F;
			this.field_78129_f.rotateAngleX = -1.5707964F;
			this.field_78130_g.rotateAngleX = -1.5707964F;
		}
	}
}