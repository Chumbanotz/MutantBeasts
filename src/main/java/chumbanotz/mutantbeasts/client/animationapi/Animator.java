package chumbanotz.mutantbeasts.client.animationapi;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.math.MathHelper;

public class Animator {
	private int tempTick;
	private int prevTempTick;
	private boolean correctAnim;
	private final Model mainModel;
	private IAnimatedEntity animEntity;
	private final Map<RendererModel, Transform> transformMap = new HashMap<>();
	private final Map<RendererModel, Transform> prevTransformMap = new HashMap<>();
	private float partialTick;

	public Animator(Model model) {
		this.mainModel = model;
	}

	public IAnimatedEntity getEntity() {
		return this.animEntity;
	}

	public void update(IAnimatedEntity entity, float partialTick) {
		this.tempTick = this.prevTempTick = 0;
		this.correctAnim = false;
		this.animEntity = entity;
		this.transformMap.clear();
		this.prevTransformMap.clear();
		this.partialTick = partialTick;

		for (RendererModel box : this.mainModel.boxList) {
			box.rotateAngleX = 0.0F;
			box.rotateAngleY = 0.0F;
			box.rotateAngleZ = 0.0F;
		}
	}

	public boolean setAnimation(int animID) {
		this.tempTick = this.prevTempTick = 0;
		this.correctAnim = this.animEntity.getAnimationID() == animID;
		return this.correctAnim;
	}

	public void startKeyframe(int duration) {
		if (this.correctAnim) {
			this.prevTempTick = this.tempTick;
			this.tempTick += duration;
		}
	}

	public void setStaticKeyframe(int duration) {
		this.startKeyframe(duration);
		this.endKeyframe(true);
	}

	public void resetKeyframe(int duration) {
		this.startKeyframe(duration);
		this.endKeyframe();
	}

	public void rotate(RendererModel box, float x, float y, float z) {
		if (this.correctAnim) {
			this.getTransform(box).addRotation(x, y, z);
		}
	}

	public void move(RendererModel box, float x, float y, float z) {
		if (this.correctAnim) {
			this.getTransform(box).addOffset(x, y, z);
		}
	}

	private Transform getTransform(RendererModel box) {
		return this.transformMap.computeIfAbsent(box, b -> new Transform());
	}

	public void endKeyframe() {
		this.endKeyframe(false);
	}

	private void endKeyframe(boolean stationary) {
		if (this.correctAnim) {
			int animTick = this.animEntity.getAnimationTick();

			if (animTick >= this.prevTempTick && animTick < this.tempTick) {
				if (stationary) {
					for (RendererModel model : this.prevTransformMap.keySet()) {
						Transform transform = this.prevTransformMap.get(model);
						model.rotateAngleX += transform.getRotationX();
						model.rotateAngleY += transform.getRotationY();
						model.rotateAngleZ += transform.getRotationZ();
						model.rotationPointX += transform.getOffsetX();
						model.rotationPointY += transform.getOffsetY();
						model.rotationPointZ += transform.getOffsetZ();
					}
				} else {
					float tick = ((float)(animTick - this.prevTempTick) + this.partialTick) / (float)(this.tempTick - this.prevTempTick);
					float inc = MathHelper.sin(tick * (float)Math.PI / 2.0F);
					float dec = 1.0F - inc;

					for (RendererModel model : this.prevTransformMap.keySet()) {
						Transform transform = this.prevTransformMap.get(model);
						model.rotateAngleX += dec * transform.getRotationX();
						model.rotateAngleY += dec * transform.getRotationY();
						model.rotateAngleZ += dec * transform.getRotationZ();
						model.rotationPointX += dec * transform.getOffsetX();
						model.rotationPointY += dec * transform.getOffsetY();
						model.rotationPointZ += dec * transform.getOffsetZ();
					}

					for (RendererModel model : this.transformMap.keySet()) {
						Transform transform = this.transformMap.get(model);
						model.rotateAngleX += inc * transform.getRotationX();
						model.rotateAngleY += inc * transform.getRotationY();
						model.rotateAngleZ += inc * transform.getRotationZ();
						model.rotationPointX += inc * transform.getOffsetX();
						model.rotationPointY += inc * transform.getOffsetY();
						model.rotationPointZ += inc * transform.getOffsetZ();
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