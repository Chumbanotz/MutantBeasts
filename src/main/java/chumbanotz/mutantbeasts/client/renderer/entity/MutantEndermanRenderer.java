package chumbanotz.mutantbeasts.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantEndermanModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MutantEndermanRenderer extends MobAutoTextureRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> {
	private boolean teleportAttack = false;
	private MutantEndermanModel endermanModel;
	private EndermanModel<MutantEndermanEntity> cloneModel;
	private static final ResourceLocation texture = new ResourceLocation("mutantbeasts:textures/entity/enderman.png");
	private static final ResourceLocation blankTexture = new ResourceLocation("mutantbeasts:textures/entity/blank.png");
	private static final ResourceLocation glowTexture = new ResourceLocation("mutantbeasts:textures/entity/enderman_glow.png");
	private static final ResourceLocation cloneTexture = new ResourceLocation("mutantbeasts:textures/entity/enderman_clone.png");
	private static final ResourceLocation shuffleTexture = new ResourceLocation("mutantbeasts:textures/entity/enderman_shuffle.png");

	public MutantEndermanRenderer(EntityRendererManager manager) {
		super(manager, new MutantEndermanModel(), 1);
		this.endermanModel = (MutantEndermanModel)this.entityModel;
		this.cloneModel = new EndermanModel<>(1);
		this.cloneModel.isAttacking = true;
	}

	@Override
	protected void renderModel(MutantEndermanEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (entitylivingbaseIn.deathTick > 80) {
			float blendFactor = (float)(entitylivingbaseIn.deathTick - 80) / (float)(entitylivingbaseIn.maxDeathTick() - 80);
			GlStateManager.depthFunc(515);
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(516, blendFactor);
			this.bindTexture(shuffleTexture);
			this.entityModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.depthFunc(514);
		}

		this.bindEntityTexture(entitylivingbaseIn);
		this.entityModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		GlStateManager.depthFunc(515);
	}

	@Override
	public void doRender(MutantEndermanEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.teleportAttack = false;
		double addX = 0.0D;
		double addZ = 0.0D;
		this.entityModel = (entity.currentAttackID == 6 ? this.cloneModel : this.endermanModel);
		boolean forcedLook = entity.currentAttackID == 3;
		boolean scream = entity.currentAttackID == 5;
		boolean telesmash = entity.currentAttackID == 7 && entity.animTick < 18;
		boolean death = entity.currentAttackID == 10;
		double scale;
		if (forcedLook || scream || telesmash || death) {
			scale = 0.03D;
			if (entity.animTick >= 40 && !death) {
				scale *= 0.5D;
			}

			if (death) {
				if (entity.animTick < 80) {
					scale = 0.019999999552965164D;
				} else {
					scale = 0.05000000074505806D;
				}
			}

			addX = entity.getRNG().nextGaussian() * scale;
			addZ = entity.getRNG().nextGaussian() * scale;
		}

		super.doRender(entity, x + addX, y, z + addZ, entityYaw, partialTicks);
		if (entity.currentAttackID == 4) {
			this.teleportAttack = true;
			scale = (double)entity.teleX + 0.5D;
			double teleY = (double)entity.teleY;
			double teleZ = (double)entity.teleZ + 0.5D;
			double var10002 = scale - this.renderManager.playerViewX;
			double var24 = teleY - this.renderManager.playerViewY;
			super.doRender(entity, var10002, var24, teleZ - this.renderManager.playerViewY, entityYaw, partialTicks);
		}
	}

	// protected void renderCarrying(MutantEndermanEntity enderman, float par2) {
	// //super.renderEquippedItems(enderman, par2);
	// GL11.glEnable(32826);
	//
	// for(int i = 1; i < enderman.heldBlock.length; ++i) {
	// if (enderman.heldBlock[i] != 0) {
	// GL11.glPushMatrix();
	// this.postRenderArm(0.0625F, i);
	// GL11.glTranslatef(0.0F, 1.2F, 0.0F);
	// float var10000 = (float)enderman.ticksExisted;
	// float var10001 = (float)i * 2.0F;
	// MutantEndermanModel var10002 = this.endermanModel;
	// float tick = var10000 + var10001 * 3.1415927F + par2;
	// GL11.glRotatef(tick * 10.0F, 1.0F, 0.0F, 0.0F);
	// GL11.glRotatef(tick * 8.0F, 0.0F, 1.0F, 0.0F);
	// GL11.glRotatef(tick * 6.0F, 0.0F, 0.0F, 1.0F);
	// float f = 0.75F;
	// GL11.glScalef(-f, -f, f);
	// int var4 = enderman.getBrightnessForRender();
	// int var5 = var4 % 65536;
	// int var6 = var4 / 65536;
	// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
	// (float)var5, (float)var6);
	// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	// this.bindTexture(TextureMap.locationBlocksTexture);
	// this.field_147909_c.renderBlockAsItem(Block.getBlockById(enderman.heldBlock[i]),
	// enderman.heldBlockData[i], 1.0F);
	// GL11.glPopMatrix();
	// }
	// }
	//
	// GL11.glDisable(32826);
	// }

	protected void postRenderArm(float scale, int armID) {
		this.endermanModel.pelvis.postRender(scale);
		this.endermanModel.abdomen.postRender(scale);
		this.endermanModel.chest.postRender(scale);
		this.endermanModel.getArmFromID(armID).postRender(scale);
	}

	// protected int renderGlowModels(MutantEndermanEntity enderman, int state,
	// float animTick) {
	// if (enderman.isInvisible()) {
	// GL11.glDepthMask(false);
	// } else {
	// GL11.glDepthMask(true);
	// }
	//
	// if (state == 0 && enderman.currentAttackID != 6) {
	// GL11.glDisable(2896);
	// this.bindTexture(glowTexture);
	// int var5 = '\uf0f0';
	// int var6 = var5 % 65536;
	// int var7 = var5 / 65536;
	// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
	// (float)var6, (float)var7);
	// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	// return 1;
	// } else {
	// if (state == 1 && enderman.currentAttackID != 6) {
	// GL11.glEnable(2896);
	// } else {
	// boolean teleport;
	// boolean scream;
	// boolean clone;
	// if (state != 2) {
	// if (state == 3) {
	// teleport = enderman.currentAttackID == 4 && enderman.animTick < 10;
	// scream = enderman.currentAttackID == 5;
	// clone = enderman.currentAttackID == 6;
	// if (teleport || scream || clone) {
	// GL11.glMatrixMode(5890);
	// GL11.glLoadIdentity();
	// GL11.glMatrixMode(5888);
	// GL11.glDisable(3042);
	// }
	// }
	// } else {
	// teleport = enderman.currentAttackID == 4 && enderman.animTick < 10;
	// scream = enderman.currentAttackID == 5;
	// clone = enderman.currentAttackID == 6;
	// if (teleport || scream || clone) {
	// GL11.glDisable(2896);
	// this.bindTexture(cloneTexture);
	// GL11.glMatrixMode(5890);
	// GL11.glLoadIdentity();
	// float f = ((float)enderman.ticksExisted + animTick) * 0.008F;
	// GL11.glTranslatef(f, f, 0.0F);
	// GL11.glMatrixMode(5888);
	// GL11.glEnable(2977);
	// GL11.glEnable(3042);
	// GL11.glBlendFunc(770, 771);
	// int var5 = '\uf0f0';
	// int var6 = var5 % 65536;
	// int var7 = var5 / 65536;
	// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
	// (float)var6, (float)var7);
	// float alpha = 1.0F;
	// if (teleport) {
	// if (!this.teleportAttack && enderman.animTick >= 8) {
	// alpha -= ((float)(enderman.animTick - 8) + animTick) / 2.0F;
	// }
	//
	// if (this.teleportAttack && enderman.animTick < 2) {
	// alpha = ((float)enderman.animTick + animTick) / 2.0F;
	// }
	// }
	//
	// if (scream) {
	// if (enderman.animTick < 40) {
	// alpha = ((float)enderman.animTick + animTick) / 40.0F;
	// } else if (enderman.animTick >= 160) {
	// alpha = 1.0F - ((float)enderman.animTick + animTick) / 40.0F;
	// }
	// }
	//
	// GL11.glColor4f(0.9F, 0.3F, 1.0F, alpha);
	// GL11.glEnable(2896);
	// float scale = 0.0F;
	// if (teleport) {
	// scale = 1.2F + ((float)enderman.animTick + animTick) / 10.0F;
	// if (this.teleportAttack) {
	// scale = 2.2F - ((float)enderman.animTick + animTick) / 10.0F;
	// }
	// }
	//
	// if (scream) {
	// if (enderman.animTick < 40) {
	// scale = 1.2F + ((float)enderman.animTick + animTick) / 40.0F;
	// } else if (enderman.animTick < 160) {
	// scale = 2.2F;
	// } else {
	// scale = 2.2F - ((float)enderman.animTick + animTick) / 10.0F;
	// }
	// }
	//
	// if (clone) {
	// GL11.glScalef(1.25F, 1.25F, 1.25F);
	// } else {
	// GL11.glScalef(scale, scale * 0.8F, scale);
	// }
	//
	// return 1;
	// }
	// }
	// }
	//
	// return -1;
	// }
	// }
}