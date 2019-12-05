package chumbanotz.mutantbeasts.client.renderer.entity.model;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScalableRendererModel extends RendererModel {
	private float scale = 1.0F;

	public ScalableRendererModel(Model par1Model) {
		super(par1Model);
	}

	public ScalableRendererModel(Model par1Model, int par2, int par3) {
		super(par1Model, par2, par3);
	}

	public void setScale(float f) {
		this.scale = f;
	}

	@Override
	public void render(float par1) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.render(par1);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void renderWithRotation(float par1) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.renderWithRotation(par1);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void postRender(float par1) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.postRender(par1);
			GlStateManager.popMatrix();
		}
	}
}