package chumbanotz.mutantbeasts.client.renderer.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class JointRendererModel extends RendererModel {
	private final RendererModel model;

	public JointRendererModel(Model model, int x, int y) {
		super(model);
		this.model = new RendererModel(model, x, y);
		super.addChild(this.model);
	}

	@Override
	public RendererModel setTextureOffset(int x, int y) {
		if (this.model != null) {
			this.model.setTextureOffset(x, y);
		}

		return this;
	}

	@Override
	public RendererModel setTextureSize(int w, int h) {
		if (this.model != null) {
			this.model.setTextureSize(w, h);
		}

		return this;
	}

	@Override
	public void copyModelAngles(RendererModel p_217177_1_) {
		this.model.copyModelAngles(p_217177_1_);
	}

	@Override
	public void addChild(RendererModel renderer) {
		this.model.addChild(renderer);
	}

	@Override
	public void removeChild(RendererModel p_217179_1_) {
		this.model.removeChild(p_217179_1_);
	}

	@Override
	public RendererModel func_217178_a(String p_217178_1_, float p_217178_2_, float p_217178_3_, float p_217178_4_, int p_217178_5_, int p_217178_6_, int p_217178_7_, float p_217178_8_, int p_217178_9_, int p_217178_10_) {
		return this.model.func_217178_a(p_217178_1_, p_217178_2_, p_217178_3_, p_217178_4_, p_217178_5_, p_217178_6_, p_217178_7_, p_217178_8_, p_217178_9_, p_217178_10_);
	}

	@Override
	public RendererModel addBox(float offX, float offY, float offZ, int width, int height, int depth) {
		return this.model.addBox(offX, offY, offZ, width, height, depth);
	}

	@Override
	public RendererModel addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
		return this.model.addBox(offX, offY, offZ, width, height, depth, mirrored);
	}

	@Override
	public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
		this.model.addBox(offX, offY, offZ, width, height, depth, scaleFactor);
	}

	@Override
	public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor, boolean mirrorIn) {
		this.model.addBox(offX, offY, offZ, width, height, depth, scaleFactor, mirrorIn);
	}

	public RendererModel getModel() {
		return this.model;
	}
}