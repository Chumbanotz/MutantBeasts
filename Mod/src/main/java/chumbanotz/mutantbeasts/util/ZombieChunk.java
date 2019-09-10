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
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ZombieChunk {
	public final int posX;
	public final int posY;
	public final int posZ;
	public boolean first;
	public boolean spawnParticles;

	public ZombieChunk(int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.first = false;
		this.spawnParticles = true;
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
		BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
		BlockState blockstate = world.getBlockState(pos);
		Block block = world.getBlockState(pos).getBlock();

		if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity)) {
			if (block instanceof SnowyDirtBlock || block instanceof GrassPathBlock || block instanceof FarmlandBlock) {
				world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
			}

			if (block instanceof BreakableBlock || block instanceof LeavesBlock) {
				world.destroyBlock(pos, false);
			}

			if (world.getBlockState(pos.up()).getBlockHardness(world, pos.up()) <= 1.0F) {
				world.destroyBlock(pos.up(), true);
			}
		}

		if (block instanceof BellBlock) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (((BellBlock)block).ring(world, blockstate, world.getTileEntity(pos), new BlockRayTraceResult(new Vec3d(0.5D, 0.5D, 0.5D), direction, pos, false), null, false)) {
					break;
				}
			}
		}

		if (block instanceof RedstoneOreBlock || block instanceof TurtleEggBlock && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity)) {
			block.onEntityWalk(world, pos, entity);
		}

		if (this.spawnParticles) {
			int id = Block.getStateId(world.getBlockState(pos));
			world.playEvent(2001, pos.up(), id);
		}
	}
}