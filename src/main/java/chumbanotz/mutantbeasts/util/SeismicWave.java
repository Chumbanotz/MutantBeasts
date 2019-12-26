package chumbanotz.mutantbeasts.util;

import java.util.List;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.GrassPathBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SeismicWave extends Vec3i {
	private boolean first;
	private final boolean spawnParticles;

	public SeismicWave(int x, int y, int z) {
		super(x, y, z);
		this.spawnParticles = true;
	}

	public SeismicWave(int x, int y, int z, boolean spawnParticles) {
		super(x, y, z);
		this.spawnParticles = spawnParticles;
	}

	public boolean isFirst() {
		return this.first;
	}

	public static void createWaves(World world, List<SeismicWave> list, int x1, int z1, int x2, int z2, int y) {
		int deltaX = x2 - x1;
		int deltaZ = z2 - z1;
		int xStep = deltaX < 0 ? -1 : 1;
		int zStep = deltaZ < 0 ? -1 : 1;
		deltaX = Math.abs(deltaX);
		deltaZ = Math.abs(deltaZ);
		int x = x1;
		int z = z1;
		int deltaX2 = deltaX * 2;
		int deltaZ2 = deltaZ * 2;
		SeismicWave wave = addWave(world, list, x1, y, z1);

		if (wave != null) {
			wave.first = true;
		}

		int error;
		int i;

		if (deltaX2 >= deltaZ2) {
			error = deltaX;

			for (i = 0; i < deltaX; ++i) {
				x += xStep;
				error += deltaZ2;

				if (error > deltaX2) {
					z += zStep;
					error -= deltaX2;
				}

				addWave(world, list, x, y, z);
			}
		} else {
			error = deltaZ;

			for (i = 0; i < deltaZ; ++i) {
				z += zStep;
				error += deltaX2;

				if (error > deltaZ2) {
					x += xStep;
					error -= deltaZ2;
				}

				addWave(world, list, x, y, z);
			}
		}
	}

	public static SeismicWave addWave(World world, List<SeismicWave> list, int x, int y, int z) {
		y = ZombieResurrection.getSuitableGround(world, x, y, z, 3, false);
		SeismicWave wave = null;

		if (y != -1) {
			list.add(wave = new SeismicWave(x, y, z));
		}

		if (world.rand.nextInt(2) == 0) {
			list.add(new SeismicWave(x, y + 1, z, false));
		}

		return wave;
	}

	public void affectBlocks(World world, Entity entity) {
		boolean isPlayer = entity instanceof PlayerEntity;
		final BlockPos pos = new BlockPos(this);
		final BlockPos posAbove = pos.up();
		BlockState blockstate = world.getBlockState(pos);
		Block block = world.getBlockState(pos).getBlock();

		if (isPlayer && ((PlayerEntity)entity).isAllowEdit() || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity)) {
			if (block instanceof SnowyDirtBlock || block instanceof GrassPathBlock || block instanceof FarmlandBlock) {
				world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
			}

			if (block instanceof BreakableBlock || block instanceof LeavesBlock) {
				world.destroyBlock(pos, false);
			}

			if (world.getBlockState(posAbove).getBlockHardness(world, posAbove) <= 1.0F && !net.minecraft.tags.BlockTags.WITHER_IMMUNE.contains(block)) {
				world.destroyBlock(posAbove, true);
			}

			if (block instanceof DoorBlock) {
				if (blockstate.getMaterial() == Material.WOOD) {
					world.removeBlock(posAbove, false);
					world.playEvent(1021, posAbove, 0);
				} else if (blockstate.getMaterial() == Material.IRON) {
					world.playEvent(1020, posAbove, 0);
				}
			}
		}

		if (block instanceof BellBlock) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (((BellBlock)block).ring(world, blockstate, world.getTileEntity(pos), new BlockRayTraceResult(new Vec3d(0.5D, 0.5D, 0.5D), direction, pos, false), isPlayer ? (PlayerEntity)entity : null, false)) {
					break;
				}
			}
		}

		if (block instanceof RedstoneOreBlock) {
			block.onEntityWalk(world, pos, entity);
		}

		if (this.spawnParticles) {
			if (blockstate.getFluidState().isEmpty()) {
				int id = Block.getStateId(world.getBlockState(pos));
				world.playEvent(2001, posAbove, id);
			} else if (world instanceof ServerWorld) {
				if (world.getFluidState(pos).isTagged(FluidTags.WATER)) {
					world.playSound(null, posAbove, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
					((ServerWorld)world).spawnParticle(ParticleTypes.SPLASH, posAbove.getX(), posAbove.getY(), posAbove.getZ(), 100, 0.5D, 0.5D, 0.5D, 10);
				} else if (world.getFluidState(pos).isTagged(FluidTags.LAVA)) {
					world.playSound(null, posAbove, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 1.0F, 0.9F + world.rand.nextFloat() * 0.15F);
					((ServerWorld)world).spawnParticle(ParticleTypes.FALLING_LAVA, posAbove.getX(), posAbove.getY(), posAbove.getZ(), 100, 0.5, 0.5, 0.5, 10);
				}
			}
		}
	}
}