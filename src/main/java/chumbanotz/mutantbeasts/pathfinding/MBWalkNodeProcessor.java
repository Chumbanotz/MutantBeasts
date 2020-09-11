package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.IFluidState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class MBWalkNodeProcessor extends WalkNodeProcessor {
	@Override
	protected PathNodeType func_215744_a(IBlockReader blockaccessIn, boolean canOpenDoorsIn, boolean canEnterDoorsIn, BlockPos pos, PathNodeType nodeType) {
		if (nodeType == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn) {
			nodeType = PathNodeType.WALKABLE;
		}

		if (nodeType == PathNodeType.DOOR_OPEN && (!canEnterDoorsIn || this.currentEntity.getWidth() > 0.85F)) {
			nodeType = PathNodeType.BLOCKED;
		}

		if (nodeType == PathNodeType.LEAVES) {
			nodeType = PathNodeType.BLOCKED;
		}

		return nodeType;
	}

	@Override
	public PathNodeType checkNeighborBlocks(IBlockReader blockaccessIn, int x, int y, int z, PathNodeType nodeType) {
		if (nodeType == PathNodeType.WALKABLE) {
			try (BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain()) {
				for (int i = -1; i <= 1; ++i) {
					for (int j = -1; j <= 1; ++j) {
						if (i != 0 || j != 0) {
							BlockPos newPos = pos.setPos(i + x, y, j + z);
							switch (this.getPathNodeTypeRaw(blockaccessIn, newPos.getX(), newPos.getY(), newPos.getZ())) {
							case DAMAGE_CACTUS:
								return PathNodeType.DANGER_CACTUS;
							case DANGER_FIRE:
							case DAMAGE_FIRE:
								return PathNodeType.DANGER_FIRE;
							case DAMAGE_OTHER:
							case DANGER_OTHER:
								return PathNodeType.DANGER_OTHER;
							case LAVA:
								return PathNodeType.LAVA;
							default:
								break;
							}
						}
					}
				}
			}
		}

		return nodeType;
	}

	@Override
	public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z) {
		PathNodeType pathNode = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);
		if (pathNode == PathNodeType.OPEN && y >= 1) {
			PathNodeType pathNodeBelow = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
			pathNode = pathNodeBelow != PathNodeType.WALKABLE && pathNodeBelow != PathNodeType.OPEN && pathNodeBelow != PathNodeType.WATER && pathNodeBelow != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
			switch (pathNodeBelow) {
			case DAMAGE_FIRE:
			case DAMAGE_CACTUS:
			case DAMAGE_OTHER:
			case DANGER_OTHER:
				pathNode = pathNodeBelow;
				break;
			default:
				break;
			}
		}

		pathNode = this.checkNeighborBlocks(blockaccessIn, x, y, z, pathNode);
		return pathNode;
	}

	@Override
	protected PathNodeType getPathNodeTypeRaw(IBlockReader blockaccessIn, int x, int y, int z) {
		BlockPos blockPos = new BlockPos(x, y, z);
		BlockState blockState = blockaccessIn.getBlockState(blockPos);
		PathNodeType forgeType = blockState.getAiPathNodeType(blockaccessIn, blockPos, this.currentEntity);
		if (forgeType != null) return forgeType;
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		if (blockState.isAir(blockaccessIn, blockPos)) {
			return PathNodeType.OPEN;
		} else if (!(block instanceof TrapDoorBlock) && !(block instanceof LilyPadBlock)) {
			if (block instanceof FireBlock || block instanceof MagmaBlock || block instanceof CampfireBlock && blockState.get(CampfireBlock.LIT)) {
				return PathNodeType.DAMAGE_FIRE;
			} else if (block instanceof CactusBlock) {
				return PathNodeType.DAMAGE_CACTUS;
			} else if (block instanceof SweetBerryBushBlock && blockState.get(SweetBerryBushBlock.AGE) > 0 || block instanceof WitherRoseBlock || block instanceof EndPortalBlock || block instanceof NetherPortalBlock) {
				return PathNodeType.DAMAGE_OTHER;
			} else if (block instanceof SweetBerryBushBlock && blockState.get(SweetBerryBushBlock.AGE) == 0 || block instanceof WebBlock || block instanceof AbstractPressurePlateBlock || block instanceof SoulSandBlock) {
				return PathNodeType.DANGER_OTHER;
			} else if (block instanceof DoorBlock && material == Material.WOOD && !blockState.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_WOOD_CLOSED;
			} else if (block instanceof DoorBlock && material == Material.IRON && !blockState.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_IRON_CLOSED;
			} else if (block instanceof DoorBlock && blockState.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_OPEN;
			} else if (block instanceof AbstractRailBlock) {
				return PathNodeType.RAIL;
			} else if (block instanceof LeavesBlock || block instanceof AbstractGlassBlock) {
				return PathNodeType.LEAVES;
			} else if (!block.isIn(BlockTags.FENCES) && !block.isIn(BlockTags.WALLS) && !block.isIn(BlockTags.FLOWER_POTS) && (!(block instanceof FenceGateBlock) || blockState.get(FenceGateBlock.OPEN)) && !(block instanceof EndRodBlock) && !(block instanceof AbstractSkullBlock)) {
				IFluidState ifluidstate = blockaccessIn.getFluidState(blockPos);
				if (ifluidstate.isTagged(FluidTags.WATER)) {
					return PathNodeType.WATER;
				} else if (ifluidstate.isTagged(FluidTags.LAVA)) {
					return PathNodeType.LAVA;
				} else {
					return blockState.allowsMovement(blockaccessIn, blockPos, PathType.LAND) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
				}
			} else {
				return PathNodeType.FENCE;
			}
		} else {
			return PathNodeType.TRAPDOOR;
		}
	}
}