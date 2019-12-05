package chumbanotz.mutantbeasts.client.renderer.entity.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperMinionEggModel extends Model {
    private final RendererModel egg;

    public CreeperMinionEggModel() {
        this.textureWidth = 512;
        this.textureHeight = 256;
        this.egg = new RendererModel(this, 0, 0)
        		.addBox(-2.0F, 1.0F, -2.0F, 4, 1, 4)
        		.addBox(-3.0F, -3.0F, -3.0F, 6, 4, 6)
        		.addBox(-1.0F, -6.0F, -1.0F, 2, 1, 2)
        		.addBox(-2.0F, -5.0F, -2.0F, 4, 2, 4);
    }

    public void render() { 
        this.egg.render(0.0625F);
    }
}