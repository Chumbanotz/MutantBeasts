package chumbanotz.mutantbeasts.block;

import chumbanotz.mutantbeasts.tileentity.MBSkullTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MBSkullBlock extends SkullBlock {
	public MBSkullBlock(MBSkullBlock.ISkullType skullType, Properties properties) {
		super(skullType, properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MBSkullTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof MBSkullTileEntity && stack.hasTag()) {
			((MBSkullTileEntity)tileentity).setItemData(stack.getTag());
		}
	}

	public static enum Types implements SkullBlock.ISkullType {
		MUTANT_SKELETON;
	}
}