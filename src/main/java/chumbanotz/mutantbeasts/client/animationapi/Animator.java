package chumbanotz.mutantbeasts.client.animationapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Animator {
	private int tempTick;
	private int prevTempTick;
	private boolean correctAnim;
	private final Model mainModel;
	private IAnimatedEntity animEntity;
	private final Map<RendererModel, Transform> transformMap = new HashMap<RendererModel, Transform>();
	private final Map<RendererModel, Transform> prevTransformMap = new HashMap<RendererModel, Transform>();

	public Animator(Model model) {
		this.mainModel = model;
	}

	public IAnimatedEntity getEntity() {
		return this.animEntity;
	}

	public void update(IAnimatedEntity entity) {
		this.tempTick = this.prevTempTick = 0;
		this.correctAnim = false;
		this.animEntity = entity;
		this.transformMap.clear();
		this.prevTransformMap.clear();

		for (int i = 0; i < this.mainModel.boxList.size(); ++i) {
			RendererModel box = this.mainModel.boxList.get(i);
			box.rotateAngleX = 0.0F;
			box.rotateAngleY = 0.0F;
			box.rotateAngleZ = 0.0F;
		}
	}

	public boolean setAnim(int animID) {
		this.tempTick = this.prevTempTick = 0;
		this.correctAnim = this.animEntity.getAnimationID() == animID;
		return this.correctAnim;
	}

	public void startPhase(int duration) {
		if (this.correctAnim) {
			this.prevTempTick = this.tempTick;
			this.tempTick += duration;
		}
	}

	public void setStationaryPhase(int duration) {
		this.startPhase(duration);
		this.endPhase(true);
	}

	public void resetPhase(int duration) {
		this.startPhase(duration);
		this.endPhase();
	}

	public void rotate(RendererModel box, float x, float y, float z) {
		if (this.correctAnim) {
			if (!this.transformMap.containsKey(box)) {
				this.transformMap.put(box, new Transform(x, y, z));
			} else {
				this.transformMap.get(box).addRot(x, y, z);
			}
		}
	}

	public void move(RendererModel box, float x, float y, float z) {
		if (this.correctAnim) {
			if (!this.transformMap.containsKey(box)) {
				this.transformMap.put(box, new Transform(x, y, z, 0.0F, 0.0F, 0.0F));
			} else {
				this.transformMap.get(box).addOffset(x, y, z);
			}
		}
	}

	public void endPhase() {
		this.endPhase(false);
	}

	private void endPhase(boolean stationary) {
		if (this.correctAnim) {
			int animTick = this.animEntity.getAnimationTick();

			if (animTick >= this.prevTempTick && animTick < this.tempTick) {
				RendererModel box;
				Transform transform;

				if (stationary) {
					for (Iterator<RendererModel> i$ = this.prevTransformMap.keySet().iterator(); i$.hasNext(); box.rotationPointZ += transform.offsetZ) {
						box = i$.next();
						transform = this.prevTransformMap.get(box);
						box.rotateAngleX += transform.rotX;
						box.rotateAngleY += transform.rotY;
						box.rotateAngleZ += transform.rotZ;
						box.rotationPointX += transform.offsetX;
						box.rotationPointY += transform.offsetY;
					}
				} else {
					float tick = ((float)(animTick - this.prevTempTick) + 1.0F) / (float)(this.tempTick - this.prevTempTick);
					float inc = MathHelper.sin(tick * 3.1415927F / 2.0F);
					float dec = 1.0F - inc;

					Iterator<RendererModel> i$;
					RendererModel box1;
					Transform transform1;

					for (i$ = this.prevTransformMap.keySet().iterator(); i$.hasNext(); box1.rotationPointZ += dec * transform1.offsetZ) {
						box1 = i$.next();
						transform1 = this.prevTransformMap.get(box1);
						box1.rotateAngleX += dec * transform1.rotX;
						box1.rotateAngleY += dec * transform1.rotY;
						box1.rotateAngleZ += dec * transform1.rotZ;
						box1.rotationPointX += dec * transform1.offsetX;
						box1.rotationPointY += dec * transform1.offsetY;
					}

					for (i$ = this.transformMap.keySet().iterator(); i$.hasNext(); box1.rotationPointZ += inc * transform1.offsetZ) {
						box1 = i$.next();
						transform1 = this.transformMap.get(box1);
						box1.rotateAngleX += inc * transform1.rotX;
						box1.rotateAngleY += inc * transform1.rotY;
						box1.rotateAngleZ += inc * transform1.rotZ;
						box1.rotationPointX += inc * transform1.offsetX;
						box1.rotationPointY += inc * transform1.offsetY;
					}
				}
			}

			if (!stationary) {
				this.prevTransformMap.clear();
				this.prevTransformMap.putAll(this.transformMap);
				this.transformMap.clear();
			}
		}
	}
}