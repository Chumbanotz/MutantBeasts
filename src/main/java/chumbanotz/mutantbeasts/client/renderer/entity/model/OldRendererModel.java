package chumbanotz.mutantbeasts.client.renderer.entity.model;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Taken from 1.12.2 ModelRenderer */
@OnlyIn(Dist.CLIENT)
public class OldRendererModel extends RendererModel {
	private boolean compiled;
	private int displayList;

	public OldRendererModel(Model model, String boxNameIn) {
		super(model, boxNameIn);
		this.setTextureSize(model.textureWidth, model.textureHeight);
	}

	public OldRendererModel(Model model) {
		this(model, (String)null);
	}

	public OldRendererModel(Model model, int texOffX, int texOffY) {
		this(model);
		this.setTextureOffset(texOffX, texOffY);
	}

	@Override
	public void render(float scale) {
		if (!this.isHidden) {
			if (this.showModel) {
				if (!this.compiled) {
					this.compileDisplayList(scale);
				}

				GlStateManager.translatef(this.offsetX, this.offsetY, this.offsetZ);

				if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
					if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
						GlStateManager.callList(this.displayList);

						if (this.childModels != null) {
							for (int k = 0; k < this.childModels.size(); ++k) {
								this.childModels.get(k).render(scale);
							}
						}
					} else {
						GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
						GlStateManager.callList(this.displayList);

						if (this.childModels != null) {
							for (int j = 0; j < this.childModels.size(); ++j) {
								this.childModels.get(j).render(scale);
							}
						}

						GlStateManager.translatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
					}
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

					if (this.rotateAngleZ != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
					}

					if (this.rotateAngleY != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
					}

					if (this.rotateAngleX != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
					}

					GlStateManager.callList(this.displayList);

					if (this.childModels != null) {
						for (int i = 0; i < this.childModels.size(); ++i) {
							this.childModels.get(i).render(scale);
						}
					}

					GlStateManager.popMatrix();
				}

				GlStateManager.translatef(-this.offsetX, -this.offsetY, -this.offsetZ);
			}
		}
	}

	@Override
	public void renderWithRotation(float scale) {
		if (!this.isHidden) {
			if (this.showModel) {
				if (!this.compiled) {
					this.compileDisplayList(scale);
				}

				GlStateManager.pushMatrix();
				GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

				if (this.rotateAngleY != 0.0F) {
					GlStateManager.rotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
				}

				if (this.rotateAngleX != 0.0F) {
					GlStateManager.rotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
				}

				if (this.rotateAngleZ != 0.0F) {
					GlStateManager.rotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
				}

				GlStateManager.callList(this.displayList);
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public void postRender(float scale) {
		if (!this.isHidden) {
			if (this.showModel) {
				if (!this.compiled) {
					this.compileDisplayList(scale);
				}

				if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
					if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
						GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
					}
				} else {
					GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

					if (this.rotateAngleZ != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
					}

					if (this.rotateAngleY != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
					}

					if (this.rotateAngleX != 0.0F) {
						GlStateManager.rotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
					}
				}
			}
		}
	}

	/**
	 * Compiles a GL display list for this model
	 */
	private void compileDisplayList(float scale) {
		this.displayList = GLAllocation.generateDisplayLists(1);
		GlStateManager.newList(this.displayList, 4864);
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

		for (int i = 0; i < this.cubeList.size(); ++i) {
			this.cubeList.get(i).render(bufferbuilder, scale);
		}

		GlStateManager.endList();
		this.compiled = true;
	}
}