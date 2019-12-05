package chumbanotz.mutantbeasts.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
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
	private final Explosion.Mode mode;
	private final World world;
	private final double x;
	private final double y;
	private final double z;
	private final Entity exploder;
	private final float size;

	public MutatedExplosion(World worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Mode modeIn) {
		super(worldIn, exploderIn, xIn, yIn, zIn, sizeIn, causesFireIn, modeIn);
		this.world = worldIn;
		this.exploder = exploderIn;
		this.x = xIn;
		this.y = yIn;
		this.z = zIn;
		this.size = sizeIn;
		this.mode = modeIn;
	}

	@Override
	public void doExplosionA() {
		Set<BlockPos> set = new HashSet<>();

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				for (int l = 0; l < 16; ++l) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 = d0 / d3;
						d1 = d1 / d3;
						d2 = d2 / d3;
						float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
						double d4 = this.x;
						double d6 = this.y;
						double d8 = this.z;

						for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
							BlockPos blockpos = new BlockPos(d4, d6, d8);
							BlockState blockstate = this.world.getBlockState(blockpos);
							IFluidState ifluidstate = this.world.getFluidState(blockpos);
							if (!blockstate.isAir(this.world, blockpos) || !ifluidstate.isEmpty()) {
								float f2 = Math.max(blockstate.getExplosionResistance(this.world, blockpos, exploder, this), ifluidstate.getExplosionResistance(this.world, blockpos, exploder, this));
								if (this.exploder != null) {
									f2 = this.exploder.getExplosionResistance(this, this.world, blockpos, blockstate, ifluidstate, f2);
								}

								f -= (f2 + f1) * f1;
							}

							if (f > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this, this.world, blockpos, blockstate, f))) {
								set.add(blockpos);
							}

							d4 += d0 * (double)f1;
							d6 += d1 * (double)f1;
							d8 += d2 * (double)f1;
						}
					}
				}
			}
		}

		this.getAffectedBlockPositions().addAll(set);
		float f3 = this.size * 2.0F;
		int k1 = MathHelper.floor(this.x - (double)f3 - 1.0D);
		int l1 = MathHelper.floor(this.x + (double)f3 + 1.0D);
		int i2 = MathHelper.floor(this.y - (double)f3 - 1.0D);
		int i1 = MathHelper.floor(this.y + (double)f3 + 1.0D);
		int j2 = MathHelper.floor(this.z - (double)f3 - 1.0D);
		int j1 = MathHelper.floor(this.z + (double)f3 + 1.0D);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

		for (Entity entity : list) {
			if (!entity.isImmuneToExplosions()) {
				double d12 = (double)(MathHelper.sqrt(entity.getDistanceSq(new Vec3d(this.x, this.y, this.z))) / f3);
				if (d12 <= 1.0D) {
					double d5 = entity.posX - this.x;
					double d7 = entity.posY + (double)entity.getEyeHeight() - this.y;
					double d9 = entity.posZ - this.z;
					double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
					if (d13 != 0.0D) {
						d5 = d5 / d13;
						d7 = d7 / d13;
						d9 = d9 / d13;
						double d14 = (double)getBlockDensity(vec3d, entity);
						double d10 = (1.0D - d12) * d14;
						entity.attackEntityFrom(this.getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 6.0D * (double)f3 + 1.0D)));
						double d11 = d10;
						if (entity instanceof LivingEntity) {
							d11 = ProtectionEnchantment.getBlastDamageReduction((LivingEntity)entity, d10);
						}

						if (!(entity instanceof MutantCreeperEntity)) {
							entity.setMotion(entity.getMotion().add(d5 * d11, d7 * d11, d9 * d11));
						}

						if (entity instanceof PlayerEntity) {
							PlayerEntity playerentity = (PlayerEntity)entity;
							if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.abilities.isFlying)) {
								this.getPlayerKnockbackMap().put(playerentity, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
							}
						}
					}
				}
			}
		}
	}

	public static void explode(World worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Mode mode) {
		MutatedExplosion explosion = new MutatedExplosion(worldIn, exploderIn, xIn, yIn, zIn, sizeIn, causesFireIn, mode);
		explosion.doExplosionA();
		explosion.doExplosionB(true);

		if (explosion.world instanceof ServerWorld) {
			if (explosion.mode == Explosion.Mode.NONE) {
				explosion.clearAffectedBlockPositions();
			}

			for (ServerPlayerEntity serverplayerentity : ((ServerWorld)explosion.world).getPlayers()) {
				if (serverplayerentity.getDistanceSq(explosion.x, explosion.y, explosion.z) < 4096.0D) {
					serverplayerentity.connection.sendPacket(new SExplosionPacket(explosion.x, explosion.y, explosion.z, explosion.size, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(serverplayerentity)));
				}
			}
		}
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
						Vec3d vec3d = new Vec3d(d5 + d3, d6, d7 + d4);
						if (entity.world.rayTraceBlocks(new RayTraceContext(vec3d, endVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, entity)).getType() == RayTraceResult.Type.MISS) {
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