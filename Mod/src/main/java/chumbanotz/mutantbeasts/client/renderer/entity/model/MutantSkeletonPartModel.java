package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.client.animationapi.JointRendererModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantSkeletonPartModel extends Model {
	public RendererModel pelvis;
	public RendererModel waist;
	public MutantSkeletonSpineModel[] spine;
	public JointRendererModel head;
	public RendererModel jaw;
	public JointRendererModel arm1;
	public JointRendererModel arm2;
	public JointRendererModel forearm1;
	public JointRendererModel forearm2;
	public JointRendererModel leg1;
	public JointRendererModel leg2;
	public JointRendererModel foreleg1;
	public JointRendererModel foreleg2;
	public RendererModel shoulder1;
	public RendererModel shoulder2;

	public MutantSkeletonPartModel() {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.pelvis = new RendererModel(this, 0, 16);
		this.pelvis.addBox(-4.0F, -3.0F, -3.0F, 8, 6, 6);
		this.spine = new MutantSkeletonSpineModel[3];

		for (int i = 0; i < this.spine.length; ++i) {
			this.spine[i] = new MutantSkeletonSpineModel(this, true);
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
			this.spine[i].setAngles(3.1415927F, i == 1);
		}
	}

	public RendererModel getSkeletonPart(int i) {
		return this.boxList.get(i);
	}
}