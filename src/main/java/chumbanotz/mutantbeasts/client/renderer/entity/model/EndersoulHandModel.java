package chumbanotz.mutantbeasts.client.renderer.entity.model;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.model.BakedModelWrapper;

public class EndersoulHandModel extends EntityModel<Entity> {
	private final RendererModel hand;
	private final RendererModel[] finger = new RendererModel[3];
	private final RendererModel[] foreFinger = new RendererModel[3];
	private final RendererModel thumb;

	public EndersoulHandModel() {
		this.textureWidth = 32;
		this.hand = new RendererModel(this);
		this.hand.setRotationPoint(0.0F, 17.5F, 0.0F);
		float fingerScale = 0.6F;

		for (int i = 0; i < this.finger.length; ++i) {
			this.finger[i] = new RendererModel(this, i * 4, 0);
			this.finger[i].addBox(-0.5F, 0.0F, -0.5F, 1, i == 1 ? 6 : 5, 1, fingerScale);
		}

		this.finger[0].setRotationPoint(-0.5F, 0.0F, -1.0F);
		this.finger[1].setRotationPoint(-0.5F, 0.0F, 0.0F);
		this.finger[2].setRotationPoint(-0.5F, 0.0F, 1.0F);

		for (int i = 0; i < this.foreFinger.length; ++i) {
			this.foreFinger[i] = new RendererModel(this, 1 + i * 5, 0);
			this.foreFinger[i].addBox(-0.5F, 0.0F, -0.5F, 1, i == 1 ? 6 : 5, 1, fingerScale - 0.01F);
			this.foreFinger[i].setRotationPoint(0.0F, 0.5F + (float)(i == 1 ? 6 : 5), 0.0F);
		}

		for (int i = 0; i < this.finger.length; ++i) {
			this.hand.addChild(this.finger[i]);
			this.finger[i].addChild(this.foreFinger[i]);
		}

		this.thumb = new RendererModel(this, 14, 0);
		this.thumb.addBox(-0.5F, 0.0F, -0.5F, 1, 5, 1, fingerScale);
		this.thumb.setRotationPoint(0.5F, 0.0F, -0.5F);
		this.hand.addChild(this.thumb);
	}

	private void resetAngles(RendererModel model) {
		model.rotateAngleX = 0.0F;
		model.rotateAngleY = 0.0F;
		model.rotateAngleZ = 0.0F;
	}

	public void setAngles() {
		this.resetAngles(this.hand);

		for (int i = 0; i < this.finger.length; ++i) {
			this.resetAngles(this.finger[i]);
			this.resetAngles(this.foreFinger[i]);
		}

		this.resetAngles(this.thumb);
		this.hand.rotateAngleY = -0.3926991F;
		this.finger[0].rotateAngleX = -0.2617994F;
		this.finger[1].rotateAngleZ = 0.17453294F;
		this.finger[2].rotateAngleX = 0.2617994F;
		this.foreFinger[0].rotateAngleZ = -0.2617994F;
		this.foreFinger[1].rotateAngleZ = -0.3926991F;
		this.foreFinger[2].rotateAngleZ = -0.2617994F;
		this.thumb.rotateAngleX = -0.62831855F;
		this.thumb.rotateAngleZ = -0.3926991F;
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setAngles();
		this.hand.render(0.0625F);
	}

	public static class Baked extends BakedModelWrapper<IBakedModel> {
		private final IBakedModel bakedModel;

		public Baked(IBakedModel guiModel, IBakedModel bakedModel) {
			super(guiModel);
			this.bakedModel = bakedModel;
		}

		@SuppressWarnings("deprecation")
		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType cameraTransformType) {
			switch (cameraTransformType) {
			case GUI:
			case FIXED:
			case GROUND:
				return super.handlePerspective(cameraTransformType);
			default:
				return this.bakedModel.handlePerspective(cameraTransformType);
			}
		}
	}
}