package chumbanotz.mutantbeasts.client.renderer.model;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ScalableRendererModel extends RendererModel {
	private float scale = 1.0F;

	public ScalableRendererModel(Model model, int texOffX, int texOffY) {
		super(model, texOffX, texOffY);
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public void render(float scale) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.render(scale);
			GlStateManager.popMatrix();
		}
	}
}