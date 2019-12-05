package chumbanotz.mutantbeasts.client.renderer.entity.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndersoulHandModel extends Model {
   private final RendererModel hand;
   private final RendererModel[] finger;
   private final RendererModel[] foreFinger;
   private final RendererModel thumb;

   public EndersoulHandModel() {
      this.textureWidth = 32;
      this.textureHeight = 32;
      this.finger = new RendererModel[3];
      this.foreFinger = new RendererModel[3];
      this.hand = new RendererModel(this);
      this.hand.setRotationPoint(0.0F, 17.5F, 0.0F);
      float fingerScale = 0.6F;

      int i;
      for(i = 0; i < this.finger.length; ++i) {
         this.finger[i] = new RendererModel(this, i * 4, 0);
         this.finger[i].addBox(-0.5F, 0.0F, -0.5F, 1, i == 1 ? 6 : 5, 1, fingerScale);
      }

      this.finger[0].setRotationPoint(-0.5F, 0.0F, -1.0F);
      this.finger[1].setRotationPoint(-0.5F, 0.0F, 0.0F);
      this.finger[2].setRotationPoint(-0.5F, 0.0F, 1.0F);

      for(i = 0; i < this.foreFinger.length; ++i) {
         this.foreFinger[i] = new RendererModel(this, 1 + i * 5, 0);
         this.foreFinger[i].addBox(-0.5F, 0.0F, -0.5F, 1, i == 1 ? 6 : 5, 1, fingerScale - 0.01F);
         this.foreFinger[i].setRotationPoint(0.0F, 0.5F + (float)(i == 1 ? 6 : 5), 0.0F);
      }

      for(i = 0; i < this.finger.length; ++i) {
         this.hand.addChild(this.finger[i]);
         this.finger[i].addChild(this.foreFinger[i]);
      }

      this.thumb = new RendererModel(this, 14, 0);
      this.thumb.addBox(-0.5F, 0.0F, -0.5F, 1, 5, 1, fingerScale);
      this.thumb.setRotationPoint(0.5F, 0.0F, -0.5F);
      this.hand.addChild(this.thumb);
   }

   private void resetAngles(RendererModel model) {
      model.rotateAngleX = 0.0F;
      model.rotateAngleY = 0.0F;
      model.rotateAngleZ = 0.0F;
   }

   public void setAngles() {
      this.resetAngles(this.hand);

      for(int i = 0; i < this.finger.length; ++i) {
         this.resetAngles(this.finger[i]);
         this.resetAngles(this.foreFinger[i]);
      }

      this.resetAngles(this.thumb);
      this.hand.rotateAngleY = -0.3926991F;
      this.finger[0].rotateAngleX = -0.2617994F;
      this.finger[1].rotateAngleZ = 0.17453294F;
      this.finger[2].rotateAngleX = 0.2617994F;
      this.foreFinger[0].rotateAngleZ = -0.2617994F;
      this.foreFinger[1].rotateAngleZ = -0.3926991F;
      this.foreFinger[2].rotateAngleZ = -0.2617994F;
      this.thumb.rotateAngleX = -0.62831855F;
      this.thumb.rotateAngleZ = -0.3926991F;
   }

   public void render() {
      this.setAngles();
      this.hand.render(0.0625F);
   }
}