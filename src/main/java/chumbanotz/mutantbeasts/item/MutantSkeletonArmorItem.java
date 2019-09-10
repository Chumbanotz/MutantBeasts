package chumbanotz.mutantbeasts.item;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantSkeletonArmorItem extends ArmorItem {
	public MutantSkeletonArmorItem(IArmorMaterial materialIn, EquipmentSlotType slot, Item.Properties properties) {
		super(materialIn, slot, properties);
	}

	@Override
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}

	class ExoskeletonMaterial implements IArmorMaterial {
		@Override
		public int getDurability(EquipmentSlotType slotIn) {
			return 0;
		}

		@Override
		public int getDamageReductionAmount(EquipmentSlotType slotIn) {
			return 0;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public SoundEvent getSoundEvent() {
			return null;
		}

		@Override
		public Ingredient getRepairMaterial() {
			return Ingredient.EMPTY;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public String getName() {
			return null;
		}

		@Override
		public float getToughness() {
			return 0;
		}
	}
}