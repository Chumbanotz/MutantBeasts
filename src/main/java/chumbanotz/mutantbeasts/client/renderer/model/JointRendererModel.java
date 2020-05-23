package chumbanotz.mutantbeasts.client.renderer.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class JointRendererModel extends RendererModel {
	private RendererModel joint;

	public JointRendererModel(Model model) {
		this(model, null);
		super.addChild(this.joint = new RendererModel(model));
	}

	public JointRendererModel(Model model, int x, int y) {
		this(model);
		this.joint = new RendererModel(model, x, y);
		this.joint.setTextureOffset(x, y);
		super.addChild(this.joint);
	}

	public JointRendererModel(Model model, String name) {
		super(model, name);
		this.joint = new RendererModel(model, name);
		this.joint.setTextureOffset(0, 0);
		this.joint.setTextureSize(model.textureWidth, model.textureHeight);
		super.addChild(this.joint);
	}

	@Override
	public JointRendererModel setTextureOffset(int x, int y) {
		if (this.joint != null) {
			this.joint.setTextureOffset(x, y);
		}

		return this;
	}

	@Override
	public JointRendererModel setTextureSize(int w, int h) {
		if (this.joint != null) {
			this.joint.setTextureSize(w, h);
		}

		return this;
	}

	@Override
	public JointRendererModel addBox(float x, float y, float z, int w, int h, int d) {
		this.joint.addBox(x, y, z, w, h, d);
		return this;
	}

	@Override
	public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
		this.joint.addBox(offX, offY, offZ, width, height, depth, scaleFactor);
	}

	@Override
	public JointRendererModel addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
		this.joint.addBox(offX, offY, offZ, width, height, depth, mirrored);
		return this;
	}

	@Override
	public void addChild(RendererModel child) {
		this.joint.addChild(child);
	}

	public RendererModel getJoint() {
		return this.joint;
	}
}