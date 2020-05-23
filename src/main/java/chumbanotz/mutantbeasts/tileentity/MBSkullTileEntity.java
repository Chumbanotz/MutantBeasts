package chumbanotz.mutantbeasts.tileentity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntityType;

public class MBSkullTileEntity extends SkullTileEntity {
	@Nullable
	private CompoundNBT itemData;

	@Override
	public TileEntityType<?> getType() {
		return MBTileEntityTypes.SKULL;
	}

	@Override
	public void tick() {
	}

	@Nullable
	public CompoundNBT getItemData() {
		return this.itemData;
	}

	public void setItemData(@Nullable CompoundNBT skullData) {
		this.itemData = skullData;
		this.markDirty();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		if (this.itemData != null) {
			compound.put("ItemTag", this.itemData);
		}

		return compound;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);

		if (compound.contains("Item", 10)) {
			this.itemData = compound.getCompound("Item");
		} else if (compound.contains("ItemTag", 10)) {
			this.itemData = compound.getCompound("ItemTag");
		}
	}
}