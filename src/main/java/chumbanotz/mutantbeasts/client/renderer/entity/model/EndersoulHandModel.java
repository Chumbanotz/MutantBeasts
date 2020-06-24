package chumbanotz.mutantbeasts.client.renderer.entity.model;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.client.model.BakedModelWrapper;

public class EndersoulHandModel extends Model {
//	public static final ResourceLocation GUI_LOCATION = new ModelResourceLocation(MutantBeasts.prefix("endersoul_hand_gui"), "inventory");
//	public static final ResourceLocation MODEL_LOCATION = new ModelResourceLocation(MutantBeasts.prefix("endersoul_hand_model"), "inventory");
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

	public void render() {
		this.setAngles();
		this.hand.render(0.0625F);
	}

//	public static enum Loader implements ICustomModelLoader {
//		INSTANCE;
//
//		@Override
//		public void onResourceManagerReload(IResourceManager resourceManager) {
//		}
//
//		@Override
//		public boolean accepts(ResourceLocation modelLocation) {
//			return modelLocation.getNamespace().equals(MutantBeasts.MOD_ID) && modelLocation.getPath().equals("endersoul_hand");
//		}
//
//		@Override
//		public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception {
//			return new EndersoulHandModel.Unbaked();
//		}
//	}
//

//	static class Unbaked implements IUnbakedModel {
//		@Override
//		public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
//			try {
//				IBakedModel guiModel = ModelLoaderRegistry.getModel(GUI_LOCATION).bake(bakery, spriteGetter, sprite, format);
//				IBakedModel bakedModel = ModelLoaderRegistry.getModel(MODEL_LOCATION).bake(bakery, spriteGetter, sprite, format);
//				return new EndersoulHandModel.Baked(guiModel, bakedModel);
//			} catch (Exception exception) {
//				MutantBeasts.LOGGER.warn("Failed to load models for the endersoul hand", exception);
//				return null;
//			}
//		}
//
//		@Override
//		public Collection<ResourceLocation> getDependencies() {
//			return ImmutableList.of(GUI_LOCATION, MODEL_LOCATION);
//		}
//
//		@Override
//		public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
//			return ImmutableList.of();
//		}
//	}

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