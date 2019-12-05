package chumbanotz.mutantbeasts.item;

import java.util.List;

import chumbanotz.mutantbeasts.client.renderer.entity.model.MutantSkeletonModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantSkeletonArmorItem extends ArmorItem {
	public MutantSkeletonArmorItem(EquipmentSlotType slot, Item.Properties properties) {
		super(ArmorMaterial.IRON, slot, properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
		if (this.slot == EquipmentSlotType.CHEST) {
			// MutantCreatures.proxy.increaseBowSpeed(player);
		}

		if (this.slot == EquipmentSlotType.LEGS) {
			player.addPotionEffect(new EffectInstance(Effects.SPEED, 1, 1, false, false));
		}

		if (this.slot == EquipmentSlotType.FEET) {
			//player.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(null);
			player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 1, player.isSprinting() ? 1 : 0, false, false));
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		int layer = (slot == EquipmentSlotType.LEGS) ? 2 : 1;
		return "mutantbeasts:textures/armor/mutant_skeleton_layer_" + layer + ".png";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		_default.bipedHeadwear = MutantSkeletonModel.createSkull(_default);
		return _default;
	}
}