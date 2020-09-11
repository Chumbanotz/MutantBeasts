package chumbanotz.mutantbeasts.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.SkullSpiritEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MutatedExplosion extends Explosion {
	private final Entity exploder;
	private final World world;
	private final float size;

	private MutatedExplosion(World worldIn, @Nullable Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Explosion.Mode modeIn) {
		super(worldIn, exploderIn, xIn, yIn, zIn, sizeIn, causesFireIn, modeIn);
		this.exploder = exploderIn;
		this.world = worldIn;
		this.size = sizeIn;
	}

	@Override
	public void doExplosionA() {
		if (this.size <= 0.0F) {
			return;
		}

		Set<BlockPos> set = new HashSet<>();

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				for (int l = 0; l < 16; ++l) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						float intensity = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
						double x = this.getPosition().x;
						double y = this.getPosition().y;
						double z = this.getPosition().z;

						for (float attenuation = 0.3F; intensity > 0.0F; intensity -= 0.22500001F) {
							BlockPos blockpos = new BlockPos(x, y, z);
							BlockState blockstate = this.world.getBlockState(blockpos);
							if (!blockstate.isAir(this.world, blockpos) && !blockstate.getMaterial().isLiquid()) {
								float resistance = blockstate.getExplosionResistance(this.world, blockpos, this.exploder, this);
								if (this.exploder != null) {
									resistance = this.exploder.getExplosionResistance(this, this.world, blockpos, blockstate, this.world.getFluidState(blockpos), resistance);
								}

								intensity -= (resistance + attenuation) * attenuation;
							}

							if (intensity > 0.0F && !blockstate.getMaterial().isLiquid() && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this, this.world, blockpos, blockstate, intensity))) {
								set.add(blockpos);
							}

							x += d0 * (double)attenuation;
							y += d1 * (double)attenuation;
							z += d2 * (double)attenuation;
						}
					}
				}
			}
		}

		this.getAffectedBlockPositions().addAll(set);
		float diameter = this.size * 2.0F;
		int minX = MathHelper.floor(this.getPosition().x - (double)diameter - 1.0D);
		int maxX = MathHelper.floor(this.getPosition().x + (double)diameter + 1.0D);
		int minY = MathHelper.floor(this.getPosition().y - (double)diameter - 1.0D);
		int maxY = MathHelper.floor(this.getPosition().y + (double)diameter + 1.0D);
		int minZ = MathHelper.floor(this.getPosition().z - (double)diameter - 1.0D);
		int maxZ = MathHelper.floor(this.getPosition().z + (double)diameter + 1.0D);
		List<Entity> list = this.world.getEntitiesInAABBexcluding(this.exploder, new AxisAlignedBB((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ), entity -> {
			if (entity.isImmuneToExplosions()) {
				return false;
			} else if (this.exploder instanceof SkullSpiritEntity) {
				return !(entity instanceof LivingEntity) || ChemicalXEntity.IS_APPLICABLE.test((LivingEntity)entity);
			} else {
				return true;
			}
		});
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, diameter);

		for (Entity entity : list) {
			double distance = (double)(MathHelper.sqrt(entity.getDistanceSq(this.getPosition())) / diameter);
			if (distance <= 1.0D) {
				double x = entity.posX - this.getPosition().x;
				double y = entity.posY + (double)entity.getEyeHeight() - this.getPosition().y;
				double z = entity.posZ - this.getPosition().z;
				double d13 = (double)MathHelper.sqrt(x * x + y * y + z * z);
				if (d13 != 0.0D) {
					x /= d13;
					y /= d13;
					z /= d13;
					double impact = (1.0D - distance) * (double)getBlockDensity(this.getPosition(), entity);
					float damage = (float)((int)((impact * impact + impact) / 2.0D * 6.0D * (double)diameter + 1.0D));
					if (!entity.attackEntityFrom(this.getDamageSource(), damage)) {
						if (this.exploder instanceof MutantCreeperEntity && entity instanceof PlayerEntity && ((PlayerEntity)entity).isActiveItemStackBlocking()) {
							MutantCreeperEntity mutantCreeper = (MutantCreeperEntity)this.exploder;
							PlayerEntity player = (PlayerEntity)entity;
							if (mutantCreeper.isJumpAttacking()) {
								EntityUtil.disableShield(player, mutantCreeper.getPowered() ? 200 : 100);
								entity.attackEntityFrom(this.getDamageSource(), damage * 0.5F);
							} else {
								player.getActiveItemStack().damageItem((int)damage * 2, player, e -> e.sendBreakAnimation(player.getActiveHand()));
								entity.attackEntityFrom(this.getDamageSource(), damage * 0.5F);
							}
						}
					}

					double exposure = impact;
					if (entity instanceof LivingEntity) {
						exposure = ProtectionEnchantment.getBlastDamageReduction((LivingEntity)entity, impact);
					}

					if (!(entity instanceof MutantCreeperEntity)) {
						entity.setMotion(entity.getMotion().add(x * exposure, y * exposure, z * exposure));
					}

					if (entity instanceof PlayerEntity) {
						PlayerEntity playerentity = (PlayerEntity)entity;
						if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.abilities.isFlying)) {
							this.getPlayerKnockbackMap().put(playerentity, new Vec3d(x * impact, y * impact, z * impact));
						}
					}
				}
			}
		}
	}

	public static MutatedExplosion create(@Nonnull Entity exploderIn, float sizeIn, boolean causesFireIn, Explosion.Mode mode) {
		return create(exploderIn.world, exploderIn, exploderIn.posX, exploderIn.posY, exploderIn.posZ, sizeIn, causesFireIn, mode);
	}

	public static MutatedExplosion create(World worldIn, @Nullable Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Explosion.Mode mode) {
		if (exploderIn instanceof MobEntity && !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, exploderIn)) {
			mode = Explosion.Mode.NONE;
		}

		MutatedExplosion explosion = new MutatedExplosion(worldIn, exploderIn, xIn, yIn, zIn, sizeIn, causesFireIn, mode);
		if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(worldIn, explosion)) return explosion;
		if (worldIn instanceof ServerWorld) {
			explosion.doExplosionA();
			explosion.doExplosionB(false);

			if (mode == Explosion.Mode.NONE) {
				explosion.clearAffectedBlockPositions();
			}

			for (ServerPlayerEntity serverplayerentity : ((ServerWorld)worldIn).getPlayers()) {
				if (serverplayerentity.getDistanceSq(xIn, yIn, zIn) < 4096.0D) {
					serverplayerentity.connection.sendPacket(new SExplosionPacket(xIn, yIn, zIn, explosion.size, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(serverplayerentity)));
				}
			}
		}

		return explosion;
	}

	public static float getBlockDensity(Vec3d endVec, Entity entity) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
		double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
		double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
		if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
						double d5 = MathHelper.lerp((double)f, axisalignedbb.minX, axisalignedbb.maxX);
						double d6 = MathHelper.lerp((double)f1, axisalignedbb.minY, axisalignedbb.maxY);
						double d7 = MathHelper.lerp((double)f2, axisalignedbb.minZ, axisalignedbb.maxZ);
						Vec3d vec3d = new Vec3d(d5 + d3, d6, d7 + d4); // Changed from RayTraceContext.BlockMode.OUTLINE
						if (entity.world.rayTraceBlocks(new RayTraceContext(vec3d, endVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity)).getType() == RayTraceResult.Type.MISS) {
							++i;
						}

						++j;
					}
				}
			}

			return (float)i / (float)j;
		} else {
			return 0.0F;
		}
	}
}