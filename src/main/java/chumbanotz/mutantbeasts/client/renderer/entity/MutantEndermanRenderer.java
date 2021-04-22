package chumbanotz.mutantbeasts.client.renderer.entity;

import java.lang.reflect.Field;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantEndermanModel;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MutantEndermanRenderer extends MobRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> {
	private static final Field RENDER_POS_X = ObfuscationReflectionHelper.findField(EntityRendererManager.class, "field_78725_b");
	private static final Field RENDER_POS_Y = ObfuscationReflectionHelper.findField(EntityRendererManager.class, "field_78726_c");
	private static final Field RENDER_POS_Z = ObfuscationReflectionHelper.findField(EntityRendererManager.class, "field_78723_d");
	private static final ResourceLocation TEXTURE = MutantBeasts.getEntityTexture("mutant_enderman/mutant_enderman");
	private static final ResourceLocation EYES_TEXTURE = MutantBeasts.getEntityTexture("mutant_enderman/eyes");
	private static final ResourceLocation DEATH_TEXTURE = MutantBeasts.getEntityTexture("mutant_enderman/death");
	private final MutantEndermanModel endermanModel = (MutantEndermanModel)this.entityModel;
	private final EndermanModel<MutantEndermanEntity> cloneModel = new EndermanModel<>(0.0F);
	private boolean teleportAttack;

	public MutantEndermanRenderer(EntityRendererManager manager) {
		super(manager, new MutantEndermanModel(), 0.8F);
		this.addLayer(new MutantEndermanRenderer.EyesLayer(this));
		this.addLayer(new MutantEndermanRenderer.EndersoulLayer(this));
		this.addLayer(new MutantEndermanRenderer.HeldBlocksLayer(this));
	}

	@Override
	public boolean shouldRender(MutantEndermanEntity livingEntity, ICamera camera, double camX, double camY, double camZ) {
		if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
			return true;
		} else {
			if (livingEntity.getAttackID() == MutantEndermanEntity.TELEPORT_ATTACK) {
				BlockPos pos = livingEntity.getTeleportPosition();
				AxisAlignedBB bb = livingEntity.getType().func_220328_a((double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D);
				return camera.isBoundingBoxInFrustum(bb.grow(4.5D));
			}

			return false;
		}
	}

	@Override
	protected void renderModel(MutantEndermanEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (livingEntity.deathTime > 80) {
			GlStateManager.depthFunc(515);
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(516, (float)(livingEntity.deathTime - 80) / (float)(MutantEndermanEntity.MAX_DEATH_TIME - 80));
			this.bindTexture(DEATH_TEXTURE);
			this.entityModel.render(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.depthFunc(514);
		}

		super.renderModel(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		GlStateManager.depthFunc(515);
	}

	@Override
	public void doRender(MutantEndermanEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.shadowSize = entity.isClone() ? 0.5F : 0.8F;
		this.shadowOpaque = entity.isClone() ? 0.5F : 1.0F;
		this.teleportAttack = false;
		this.entityModel = entity.isClone() ? this.cloneModel : this.endermanModel;
		this.cloneModel.isAttacking = entity.isAggressive();
		boolean forcedLook = entity.getAttackID() == MutantEndermanEntity.STARE_ATTACK;
		boolean scream = entity.getAttackID() == MutantEndermanEntity.SCREAM_ATTACK;
		boolean clone = entity.isClone() && entity.isAggressive();
		boolean telesmash = entity.getAttackID() == MutantEndermanEntity.TELESMASH_ATTACK && entity.getAttackTick() < 18;
		boolean death = entity.getAttackID() == MutantEndermanEntity.DEATH_ATTACK;
		if (forcedLook || scream || clone || telesmash || death) {
			double shake = 0.03D;
			if (entity.getAttackTick() >= 40 && !death) {
				shake *= 0.5D;
			}

			if (clone) {
				shake = 0.02D;
			}

			if (death) {
				shake = entity.getAttackTick() < 80 ? 0.019999999552965164D : 0.05000000074505806D;
			}

			x += entity.getRNG().nextGaussian() * shake;
			z += entity.getRNG().nextGaussian() * shake;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		if (entity.getAttackID() == MutantEndermanEntity.TELEPORT_ATTACK) {
			this.teleportAttack = true;
			try {
				double renderPosX = (double)entity.getTeleportPosition().getX() + 0.5D - RENDER_POS_X.getDouble(this.renderManager);
				double renderPosY = (double)entity.getTeleportPosition().getY() - RENDER_POS_Y.getDouble(this.renderManager);
				double renderPosZ = (double)entity.getTeleportPosition().getZ() + 0.5D - RENDER_POS_Z.getDouble(this.renderManager);
				super.doRender(entity, renderPosX, renderPosY, renderPosZ, entityYaw, partialTicks);
				this.doRenderShadowAndFire(entity, renderPosX, renderPosY, renderPosZ, entityYaw, partialTicks);
			} catch (IllegalArgumentException | IllegalAccessException exception) {
				throw new RuntimeException("Failed to render mutant enderman teleport position", exception);
			}
		}
	}

	@Override
	protected float getDeathMaxRotation(MutantEndermanEntity livingEntity) {
		return 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(MutantEndermanEntity entity) {
		return entity.isClone() ? null : TEXTURE;
	}

	static class EyesLayer extends LayerRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> {
		public EyesLayer(IEntityRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantEndermanEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (!entityIn.isClone()) {
				GlStateManager.disableLighting();
				this.bindTexture(EYES_TEXTURE);
				GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				Minecraft.getInstance().gameRenderer.setupFogColor(true);
				this.getEntityModel().render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				Minecraft.getInstance().gameRenderer.setupFogColor(false);
				this.func_215334_a(entityIn);
				GlStateManager.enableLighting();
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	class EndersoulLayer extends LayerRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> {
		public EndersoulLayer(IEntityRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantEndermanEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			boolean teleport = entityIn.getAttackID() == MutantEndermanEntity.TELEPORT_ATTACK && entityIn.getAttackTick() < 10;
			boolean scream = entityIn.getAttackID() == MutantEndermanEntity.SCREAM_ATTACK;
			boolean clone = entityIn.isClone();

			if (teleport || scream || clone) {
				float glowScale = 2.0F;
				float alpha = 1.0F;

				if (teleport) {
					glowScale = 1.2F + ((float)entityIn.getAttackTick() + partialTicks) / 10.0F;
					if (teleportAttack) {
						glowScale = 2.2F - ((float)entityIn.getAttackTick() + partialTicks) / 10.0F;
						if (entityIn.getAttackTick() < 2) {
							alpha = ((float)entityIn.getAttackTick() + partialTicks) / 2.0F;
						}
					} else if (entityIn.getAttackTick() >= 8) {
						alpha -= ((float)(entityIn.getAttackTick() - 8) + partialTicks) / 2.0F;
					}
				}

				if (scream) {
					if (entityIn.getAttackTick() < 40) {
						glowScale = 1.2F + ((float)entityIn.getAttackTick() + partialTicks) / 40.0F;
						alpha = ((float)entityIn.getAttackTick() + partialTicks) / 40.0F;
					} else if (entityIn.getAttackTick() < 160) {
						glowScale = 2.2F;
					} else {
						glowScale = 2.2F - ((float)entityIn.getAttackTick() + partialTicks) / 10.0F;
						alpha = 1.0F - ((float)entityIn.getAttackTick() + partialTicks) / 40.0F;
					}
				}

				if (!clone) {
					GlStateManager.pushMatrix();
					GlStateManager.scalef(glowScale, glowScale * 0.8F, glowScale);
				}

				EndersoulCloneRenderer.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, this.getEntityModel(), alpha);

				if (!clone) {
					GlStateManager.popMatrix();
				}
			}
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	static class HeldBlocksLayer extends LayerRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> {
		public HeldBlocksLayer(IEntityRenderer<MutantEndermanEntity, EntityModel<MutantEndermanEntity>> entityRendererIn) {
			super(entityRendererIn);
		}

		@Override
		public void render(MutantEndermanEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if (!(this.getEntityModel() instanceof MutantEndermanModel)) {
				return;
			}

			GlStateManager.enableRescaleNormal();

			for (int i = 1; i < entityIn.heldBlock.length; i++) {
				if (entityIn.heldBlock[i] != 0) {
					GlStateManager.pushMatrix();
					((MutantEndermanModel)this.getEntityModel()).postRenderArm(0.0625F, i);
					GlStateManager.translatef(0.0F, 1.2F, 0.0F);
					float tick = (float)entityIn.ticksExisted + (float)i * 2.0F * (float)Math.PI + partialTicks;
					GlStateManager.rotatef(tick * 10.0F, 1.0F, 0.0F, 0.0F);
					GlStateManager.rotatef(tick * 8.0F, 0.0F, 1.0F, 0.0F);
					GlStateManager.rotatef(tick * 6.0F, 0.0F, 0.0F, 1.0F);
					float f = 0.75F;
					GlStateManager.scalef(-f, -f, f);
					int var4 = entityIn.getBrightnessForRender();
					int var5 = var4 % 65536;
					int var6 = var4 / 65536;
					GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var5, (float)var6);
					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
					GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
					Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(Block.getStateById(entityIn.heldBlock[i]), 1.0F);
					GlStateManager.popMatrix();
				}
			}

			GlStateManager.disableRescaleNormal();
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}