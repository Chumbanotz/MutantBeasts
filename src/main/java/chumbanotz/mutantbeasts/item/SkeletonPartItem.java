package chumbanotz.mutantbeasts.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SkeletonPartItem extends Item {

	public SkeletonPartItem(Item.Properties properties) {
		super(properties);
	}


	@Override
	public String getTranslationKey(ItemStack stack) {
		return super.getTranslationKey(stack) + '.' + stack.getTag().getInt("");
	}

	public enum Type {
		LIMB,
		RIB,
		PELVIS,
		SHOULDER,
		ARMS,
		RIBCAGE;
	}
}