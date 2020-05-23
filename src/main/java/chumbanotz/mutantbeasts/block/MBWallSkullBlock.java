package chumbanotz.mutantbeasts.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MBWallSkullBlock extends WallSkullBlock {
	public MBWallSkullBlock(MBSkullBlock.ISkullType iSkullType, Properties properties) {
		super(iSkullType, properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return MBBlocks.MUTANT_SKELETON_SKULL.createTileEntity(state, world);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		MBBlocks.MUTANT_SKELETON_SKULL.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
}