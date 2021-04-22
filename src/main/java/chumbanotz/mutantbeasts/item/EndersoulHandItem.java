package chumbanotz.mutantbeasts.item;

import com.google.common.collect.Multimap;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.MBItemStackTileEntityRenderer;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class EndersoulHandItem extends Item {
	public EndersoulHandItem(Item.Properties properties) {
		super(properties.maxDamage(240).setTEISR(() -> MBItemStackTileEntityRenderer::new));
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return !player.isCreative();
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(1, attacker, e -> e.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		return true;
	}

	@Override
	public int getItemEnchantability() {
		return 20;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.type == EnchantmentType.WEAPON && enchantment != Enchantments.SWEEPING;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState blockState = world.getBlockState(pos);
		PlayerEntity playerEntity = context.getPlayer();
		if (context.isPlacerSneaking()) {
			return ActionResultType.PASS;
		} else if (!blockState.isIn(MutantBeasts.ENDERSOUL_HAND_HOLDABLE)) {
			return ActionResultType.FAIL;
		} else if (!world.canMineBlockBody(playerEntity, pos)) {
			return ActionResultType.FAIL;
		} else if (!playerEntity.canPlayerEdit(pos, context.getFace(), context.getItem())) {
			return ActionResultType.FAIL;
		} else {
			if (!world.isRemote) {
				world.addEntity(new ThrowableBlockEntity(world, playerEntity, blockState.getBlock().getDefaultState(), pos));
				world.removeBlock(pos, false);
			}

			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!playerIn.isSneaking()) {
			return new ActionResult<>(ActionResultType.PASS, stack);
		}

		RayTraceResult result = EntityUtil.rayTrace(playerIn, 128.0F, RayTraceContext.FluidMode.NONE);
		if (result.getType() == RayTraceResult.Type.MISS || result.getType() != RayTraceResult.Type.BLOCK) {
			playerIn.sendStatusMessage(new TranslationTextComponent(this.getTranslationKey() + ".teleport_failed"), true);
			return new ActionResult<>(ActionResultType.FAIL, stack);
		}

		if (!worldIn.isRemote) {
			BlockPos startPos = ((BlockRayTraceResult)result).getPos();
			BlockPos endPos = startPos.offset(((BlockRayTraceResult)result).getFace());
			BlockPos posDown = startPos.down();
			if (!worldIn.isAirBlock(posDown) || !worldIn.getBlockState(posDown).getMaterial().blocksMovement()) {
				for (int tries = 0; tries < 3; tries++) {
					BlockPos checkPos = startPos.up(tries + 1);
					if (worldIn.isAirBlock(checkPos)) {
						endPos = checkPos;
						break;
					}
				}
			}

			worldIn.playSound(null, playerIn.prevPosX, playerIn.prevPosY, playerIn.prevPosZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, playerIn.getSoundCategory(), 1.0F, 1.0F);
			playerIn.setPositionAndUpdate((double)endPos.getX() + 0.5D, (double)endPos.getY(), (double)endPos.getZ() + 0.5D);
			worldIn.playSound(null, endPos, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, playerIn.getSoundCategory(), 1.0F, 1.0F);
			MutantEndermanEntity.teleportAttack(playerIn);
			EntityUtil.spawnEndersoulParticles(playerIn);
			playerIn.getCooldownTracker().setCooldown(this, 40);
			stack.damageItem(4, playerIn, e -> e.sendBreakAnimation(handIn));
		}

		playerIn.fallDistance = 0.0F;
		playerIn.swingArm(handIn);
		playerIn.addStat(Stats.ITEM_USED.get(this));
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5.0D, AttributeModifier.Operation.ADDITION));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
}