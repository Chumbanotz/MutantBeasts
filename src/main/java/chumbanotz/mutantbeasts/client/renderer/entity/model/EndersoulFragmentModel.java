package chumbanotz.mutantbeasts.client.renderer.entity.model;

import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class EndersoulFragmentModel extends Model {
   private final RendererModel base = new RendererModel(this);
   private final RendererModel[] sticks = new RendererModel[8];

   public EndersoulFragmentModel() {
      this.base.addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4);
      this.base.setRotationPoint(0.0F, 22.0F, 0.0F);

      for(int i = 0; i < this.sticks.length; ++i) {
         this.sticks[i] = new RendererModel(this);
         if (i < this.sticks.length / 2) {
            this.sticks[i].addBox(-0.5F, -4.0F, -0.5F, 1, 8, 1);
         } else {
            this.sticks[i].addBox(-0.5F, -6.0F, -0.5F, 1, 10, 1, 0.15F);
         }

         this.base.addChild(this.sticks[i]);
      }
   }

   public void render(EndersoulFragmentEntity orb) {
      for(int i = 0; i < this.sticks.length; ++i) {
         this.sticks[i].rotateAngleX = orb.stickRotations[i][0];
         this.sticks[i].rotateAngleY = orb.stickRotations[i][1];
         this.sticks[i].rotateAngleZ = orb.stickRotations[i][2];
      }

      this.base.render(0.0625F);
   }
}