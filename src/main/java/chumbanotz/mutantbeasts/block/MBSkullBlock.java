package chumbanotz.mutantbeasts.block;

import chumbanotz.mutantbeasts.tileentity.MBSkullTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
		if (tileentity instanceof MBSkullTileEntity) {
			((MBSkullTileEntity)tileentity).setItemData(stack.write(new CompoundNBT()));
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof MBSkullTileEntity && !player.isCreative()) {
			spawnAsEntity(worldIn, pos, ItemStack.read(((MBSkullTileEntity)tileentity).getItemData()));
		}
	}

	public static enum Types implements SkullBlock.ISkullType {
		MUTANT_SKELETON;
	}
}