package chumbanotz.mutantbeasts.util;

import java.util.List;

import chumbanotz.mutantbeasts.packet.FluidParticlePacket;
import chumbanotz.mutantbeasts.packet.MBPacketHandler;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class SeismicWave extends BlockPos {
	private boolean first;
	private final boolean spawnParticles;

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
		addWave(world, list, x, y, z, true);
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

				addWave(world, list, x, y, z, false);
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

				addWave(world, list, x, y, z, false);
			}
		}
	}

	public static SeismicWave addWave(World world, List<SeismicWave> list, int x, int y, int z, boolean first) {
		y = ZombieResurrection.getSuitableGround(world, x, y, z, 3, false);
		SeismicWave wave = null;

		if (y != -1 || first) {
			wave = new SeismicWave(x, y, z, y != -1);
			if (first) {
				wave.first = true;
			}

			list.add(wave);
		}

		if (world.rand.nextInt(2) == 0) {
			list.add(new SeismicWave(x, y + 1, z, false));
		}

		return wave;
	}

	public void affectBlocks(World world, LivingEntity livingEntity) {
		if (!this.spawnParticles) {
			return;
		}

		BlockPos posAbove = this.up();
		BlockState blockstate = world.getBlockState(this);
		Block block = blockstate.getBlock();
        PlayerEntity playerEntity = livingEntity instanceof PlayerEntity ? (PlayerEntity)livingEntity : null;

		if (playerEntity != null && playerEntity.isAllowEdit() || world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
			if (block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.GRASS_PATH) {
				world.setBlockState(this, Blocks.DIRT.getDefaultState());
			}

			float hardness = world.getBlockState(posAbove).getBlockHardness(world, posAbove);
			if (hardness > -1.0F && hardness <= 1.0F) {
				world.destroyBlock(posAbove, playerEntity != null);
			}

			if (block instanceof DoorBlock) {
				if (blockstate.getMaterial() == Material.WOOD) {
					world.playEvent(1021, posAbove, 0);
				} else if (blockstate.getMaterial() == Material.IRON) {
					world.playEvent(1020, posAbove, 0);
				}
			}

			if (block instanceof TNTBlock) {
				block.catchFire(blockstate, world, this, null, playerEntity);
				world.removeBlock(this, false);
			}
		}

		if (block instanceof BellBlock) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (((BellBlock)block).ring(world, blockstate, world.getTileEntity(this), new BlockRayTraceResult(new Vec3d(0.5D, 0.5D, 0.5D), direction, this, false), playerEntity, false)) {
					break;
				}
			}
		}

		if (block == Blocks.REDSTONE_ORE) {
			block.onEntityWalk(world, this, livingEntity);
		}

		if (blockstate.getFluidState().isEmpty()) {
			world.playEvent(2001, posAbove, Block.getStateId(blockstate));
		} else {
			MBPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(getX(), getY(), getZ(), 1024.0D, livingEntity.dimension)), new FluidParticlePacket(blockstate, this));
		}
	}
}