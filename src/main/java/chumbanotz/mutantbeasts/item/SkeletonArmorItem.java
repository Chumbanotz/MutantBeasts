package chumbanotz.mutantbeasts.item;

import chumbanotz.mutantbeasts.client.ClientEventHandler;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class SkeletonArmorItem extends ArmorItem {
	public SkeletonArmorItem(IArmorMaterial material, EquipmentSlotType slot, Item.Properties properties) {
		super(material, slot, properties);
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
		if (this.slot == EquipmentSlotType.LEGS) {
			player.addPotionEffect(new EffectInstance(Effects.SPEED, 2, 1, false, false));
		}

		if (this.slot == EquipmentSlotType.FEET) {
			player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 2, player.isSprinting() ? 1 : 0, false, false));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		return armorSlot == EquipmentSlotType.HEAD ? (A)ClientEventHandler.INSTANCE.getMutantSkeletonArmor() : null;
	}
}