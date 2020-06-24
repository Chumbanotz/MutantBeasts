package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.renderer.model.JointRendererModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class MutantSkeletonPartModel extends Model {
	private final RendererModel pelvis;
	private final MutantSkeletonModel.Spine[] spine;
	private final JointRendererModel head;
	private final RendererModel jaw;
	private final JointRendererModel arm1;
	private final JointRendererModel arm2;
	private final JointRendererModel forearm1;
	private final JointRendererModel forearm2;
	private final JointRendererModel leg1;
	private final JointRendererModel leg2;
	private final JointRendererModel foreleg1;
	private final JointRendererModel foreleg2;
	private final RendererModel shoulder1;
	private final RendererModel shoulder2;

	public MutantSkeletonPartModel() {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.pelvis = new RendererModel(this, 0, 16);
		this.pelvis.addBox(-4.0F, -3.0F, -3.0F, 8, 6, 6);
		this.spine = new MutantSkeletonModel.Spine[3];

		for (int i = 0; i < this.spine.length; ++i) {
			this.spine[i] = new MutantSkeletonModel.Spine(this, true);
			this.boxList.remove(this.spine[i].middle);
		}

		this.head = new JointRendererModel(this, 0, 0);
		this.head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.4F);
		this.jaw = new RendererModel(this, 72, 0);
		this.jaw.addBox(-4.0F, -3.0F, -8.0F, 8, 3, 8, 0.7F);
		this.jaw.setRotationPoint(0.0F, 3.8F, 3.7F);
		this.head.addChild(this.jaw);
		this.arm1 = new JointRendererModel(this, 0, 28);
		this.arm1.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.arm2 = new JointRendererModel(this, 0, 28);
		this.arm2.mirror = true;
		this.arm2.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.forearm1 = new JointRendererModel(this, 16, 28);
		this.forearm1.addBox(-2.0F, -7.0F, -2.0F, 4, 14, 4, -0.01F);
		this.forearm2 = new JointRendererModel(this, 16, 28);
		this.forearm2.mirror = true;
		this.forearm2.addBox(-2.0F, -7.0F, -2.0F, 4, 14, 4, -0.01F);
		this.leg1 = new JointRendererModel(this, 0, 28);
		this.leg1.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.leg2 = new JointRendererModel(this, 0, 28);
		this.leg2.mirror = true;
		this.leg2.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.foreleg1 = new JointRendererModel(this, 32, 28);
		this.foreleg1.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.foreleg2 = new JointRendererModel(this, 32, 28);
		this.foreleg2.mirror = true;
		this.foreleg2.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
		this.shoulder1 = new RendererModel(this, 28, 16);
		this.shoulder1.addBox(-4.0F, -1.5F, -3.0F, 8, 3, 6);
		this.shoulder2 = new RendererModel(this, 28, 16);
		this.shoulder2.mirror = true;
		this.shoulder2.addBox(-4.0F, -1.5F, -3.0F, 8, 3, 6);
	}

	public void setAngles() {
		this.jaw.rotateAngleX = 0.09817477F;

		for (int i = 0; i < this.spine.length; ++i) {
			this.spine[i].setAngles((float)Math.PI, i == 1);
		}
	}

	public RendererModel getSkeletonPart(int i) {
		return this.boxList.get(i);
	}
}