package chumbanotz.mutantbeasts.item;

import com.google.common.collect.Multimap;

import chumbanotz.mutantbeasts.util.MutatedExplosion;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreeperShardItem extends Item {
	public CreeperShardItem(Item.Properties properties) {
		super(properties.maxDamage(32));
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return !player.isCreative();
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return super.hasEffect(stack) || stack.getDamage() == 0;
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		PlayerEntity player = (PlayerEntity)attacker;
		int damage = stack.getDamage();

		if (damage > 0) {
			stack.setDamage(damage - 1);
			if (!player.isCreative() && player.getRNG().nextInt(4) == 0) {
				player.addPotionEffect(new EffectInstance(Effects.POISON, 80 + player.getRNG().nextInt(40)));
			}
		}

        target.knockBack(player, (float)0.8999999761581421D, player.posX - target.posX, player.posZ - target.posZ);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 0.3F, 0.8F + player.getRNG().nextFloat() * 0.4F);
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		int maxDmg = stack.getMaxDamage();
		int dmg = stack.getDamage();

		if (!worldIn.isRemote) {
			float damage = 5.0F * (float)(maxDmg - dmg) / 32.0F;

			if (dmg == 0) {
				damage += 2.0F;
			}

			MutatedExplosion.create(worldIn, playerIn, playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ, damage, false, playerIn.isAllowEdit() ? MutatedExplosion.Mode.DESTROY : MutatedExplosion.Mode.NONE);
		}

		if (!playerIn.abilities.isCreativeMode) {
			stack.setDamage(maxDmg);
		}

		playerIn.swingArm(handIn);
		playerIn.getCooldownTracker().setCooldown(this, (maxDmg - dmg) * 2);
		playerIn.addStat(Stats.ITEM_USED.get(this));
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 2.0D, AttributeModifier.Operation.ADDITION));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.5D, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
}