package chumbanotz.mutantbeasts.util;

import java.util.List;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.GrassPathBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ZombieChunk extends BlockPos {
	public boolean first;
	public boolean spawnParticles;

	public ZombieChunk(int x, int y, int z) {
		super(x, y, z);
		this.first = false;
		this.spawnParticles = true;
	}

	public ZombieChunk(BlockPos pos) {
		super(pos);
	}

	public ZombieChunk setFirst(boolean flag) {
		this.first = flag;
		return this;
	}

	public ZombieChunk setParticles(boolean flag) {
		this.spawnParticles = flag;
		return this;
	}

	public static void addLinePositions(World world, List<ZombieChunk> list, int x1, int z1, int x2, int z2, int y) {
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
		ZombieChunk chunk = addPoint(world, list, x1, y, z1);

		if (chunk != null) {
			chunk.setFirst(true);
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

				addPoint(world, list, x, y, z);
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

				addPoint(world, list, x, y, z);
			}
		}
	}

	public static ZombieChunk addPoint(World world, List<ZombieChunk> list, int x, int y, int z) {
		y = ZombieResurrect.getSuitableGround(world, x, y, z, 3, false);
		ZombieChunk chunk = null;

		if (y != -1) {
			list.add(chunk = new ZombieChunk(x, y, z));
		}

		if (world.rand.nextInt(2) == 0) {
			list.add(new ZombieChunk(x, y + 1, z).setParticles(false));
		}

		return chunk;
	}

	public void handleBlocks(World world, Entity entity) {
		boolean isPlayer = entity instanceof PlayerEntity;
		BlockState blockstate = world.getBlockState(this);
		Block block = world.getBlockState(this).getBlock();

		if (isPlayer && ((PlayerEntity)entity).isAllowEdit() || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity)) {
			if (block instanceof SnowyDirtBlock || block instanceof GrassPathBlock || block instanceof FarmlandBlock) {
				world.setBlockState(this, Blocks.DIRT.getDefaultState(), 2);
			}

			if (block instanceof BreakableBlock || block instanceof LeavesBlock) {
				world.destroyBlock(this, false);
			}

			if (world.getBlockState(this.up()).getBlockHardness(world, this.up()) <= 1.0F) {
				world.destroyBlock(this.up(), true);
			}
		}

		if (block instanceof BellBlock) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (((BellBlock)block).ring(world, blockstate, world.getTileEntity(this), new BlockRayTraceResult(new Vec3d(0.5D, 0.5D, 0.5D), direction, this, false), isPlayer ? (PlayerEntity)entity : null, false)) {
					break;
				}
			}
		}

		if (block instanceof RedstoneOreBlock) {
			block.onEntityWalk(world, this, entity);
		}

		if (this.spawnParticles) {
			int id = Block.getStateId(world.getBlockState(this));
			world.playEvent(2001, this.up(), id);
		}
	}
}