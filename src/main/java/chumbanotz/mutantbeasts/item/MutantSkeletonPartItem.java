package chumbanotz.mutantbeasts.item;

import net.minecraft.item.Item;

public class MutantSkeletonPartItem extends Item {

	public MutantSkeletonPartItem(Item.Properties properties) {
		super(properties);
	}

	public enum Type {
		ARMS, LIMB, PELVIS, RIB, RIBCAGE, SHOULDER;
	}
}