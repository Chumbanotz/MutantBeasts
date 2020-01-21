package chumbanotz.mutantbeasts.tileentity;

import java.util.Map;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.block.MBSkullBlock;
import chumbanotz.mutantbeasts.client.renderer.entity.model.SkullModel;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MBSkullTileEntity extends SkullTileEntity {
	private CompoundNBT itemData;

	@Override
	public TileEntityType<?> getType() {
		return MBTileEntityTypes.SKULL;
	}

	@Override
	public void tick() {
	}

	public CompoundNBT getItemData() {
		return this.itemData;
	}

	public void setItemData(CompoundNBT skullData) {
		this.itemData = skullData;
		this.markDirty();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		if (this.itemData != null) {
			compound.put("Item", this.itemData);
		}

		return compound;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		if (compound.contains("Item", 10)) {
			this.itemData = compound.getCompound("Item");
		}
	}

	public static void addModelsAndSkins() {
		final Map<SkullBlock.ISkullType, GenericHeadModel> MODELS_MAP = ObfuscationReflectionHelper.getPrivateValue(SkullTileEntityRenderer.class, SkullTileEntityRenderer.instance, "field_199358_e");
		final Map<SkullBlock.ISkullType, ResourceLocation> SKIN_MAP = ObfuscationReflectionHelper.getPrivateValue(SkullTileEntityRenderer.class, SkullTileEntityRenderer.instance, "field_199357_d");
		MODELS_MAP.putIfAbsent(MBSkullBlock.Types.MUTANT_SKELETON, new SkullModel());
		SKIN_MAP.putIfAbsent(MBSkullBlock.Types.MUTANT_SKELETON, MutantBeasts.getEntityTexture("mutant_skeleton"));
	}
}