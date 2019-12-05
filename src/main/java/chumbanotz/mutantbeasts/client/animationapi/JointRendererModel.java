package chumbanotz.mutantbeasts.client.animationapi;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JointRendererModel extends RendererModel {
	private RendererModel model;

	public JointRendererModel(Model model) {
		this(model, null);
		super.addChild(this.model = new RendererModel(model));
	}

	public JointRendererModel(Model model, int x, int y) {
		this(model);
		this.model = new RendererModel(model, x, y);
		this.model.setTextureOffset(x, y);
		super.addChild(this.model);
	}

	public JointRendererModel(Model model, String name) {
		super(model, name);
		this.model = new RendererModel(model, name);
		this.model.setTextureOffset(0, 0);
		this.model.setTextureSize(model.textureWidth, model.textureHeight);
		super.addChild(this.model);
	}

	@Override
	public JointRendererModel setTextureOffset(int x, int y) {
		if (this.model != null) {
			this.model.setTextureOffset(x, y);
		}

		return this;
	}

	@Override
	public JointRendererModel setTextureSize(int w, int h) {
		if (this.model != null) {
			this.model.setTextureSize(w, h);
		}

		return this;
	}

	@Override
	public JointRendererModel addBox(float x, float y, float z, int w, int h, int d) {
		this.model.addBox(x, y, z, w, h, d);
		return this;
	}

	@Override
	public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
		this.model.addBox(offX, offY, offZ, width, height, depth, scaleFactor);
	}

	@Override
	public JointRendererModel addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
		this.model.addBox(offX, offY, offZ, width, height, depth, mirrored);
		return this;
	}

	@Override
	public void addChild(RendererModel child) {
		this.model.addChild(child);
	}

	public RendererModel getModel() {
		return this.model;
	}
}