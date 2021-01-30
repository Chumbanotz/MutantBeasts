package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.entity.CreeperMinionEggEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class CreeperMinionEggModel extends EntityModel<CreeperMinionEggEntity> {
    private final RendererModel egg;

    public CreeperMinionEggModel() {
        this(0.0F);
    }

    public CreeperMinionEggModel(float scale) {
        this.egg = new RendererModel(this, 0, 0);
		this.egg.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.egg.addBox(-2.0F, 1.0F, -2.0F, 4, 1, 4, scale);
        this.egg.addBox(-3.0F, -3.0F, -3.0F, 6, 4, 6, scale);
        this.egg.addBox(-1.0F, -6.0F, -1.0F, 2, 1, 2, scale);
        this.egg.addBox(-2.0F, -5.0F, -2.0F, 4, 2, 4, scale);
    }

    @Override
    public void render(CreeperMinionEggEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    	this.egg.render(scale);
    }
}