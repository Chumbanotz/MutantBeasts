package chumbanotz.mutantbeasts.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Multimap;

import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.SeismicWave;
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
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HulkHammerItem extends Item {
	public static final Map<UUID, List<SeismicWave>> WAVES = new HashMap<>();

	public HulkHammerItem(Item.Properties properties) {
		super(properties.maxDamage(64));
	}

	@Override
	public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
		return true;
	}

	@Override
	public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
		return EquipmentSlotType.MAINHAND;
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(1, attacker, e -> e.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		return true;
	}

	@Override
	public int getItemEnchantability() {
		return 10;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.type == EnchantmentType.WEAPON && enchantment != Enchantments.SWEEPING;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (state.getBlockHardness(worldIn, pos) != 0.0F) {
			stack.damageItem(2, entityLiving, (e) -> e.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		}

		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack heldItemStack = playerIn.getHeldItem(handIn);
		RayTraceResult result = EntityUtil.rayTrace(playerIn, playerIn.getAttribute(PlayerEntity.REACH_DISTANCE).getValue(), RayTraceContext.FluidMode.ANY);
		if (result.getType() == RayTraceResult.Type.MISS || result.getType() == RayTraceResult.Type.BLOCK && ((BlockRayTraceResult)result).getFace().getOpposite() != Direction.DOWN) {
			return new ActionResult<>(ActionResultType.FAIL, heldItemStack);
		} else {
			if (!worldIn.isRemote) {
				List<SeismicWave> list = new ArrayList<>();
				Vec3d vec = Vec3d.fromPitchYaw(0.0F, playerIn.rotationYaw);
				int x = MathHelper.floor(playerIn.posX + vec.x * 1.0D);
				int y = MathHelper.floor(playerIn.getBoundingBox().minY);
				int z = MathHelper.floor(playerIn.posZ + vec.z * 1.0D);
				int x1 = MathHelper.floor(playerIn.posX + vec.x * 8.0D);
				int z1 = MathHelper.floor(playerIn.posZ + vec.z * 8.0D);
				SeismicWave.createWaves(worldIn, list, x, z, x1, z1, y);
				addWave(playerIn.getUniqueID(), list);
			}

			worldIn.playSound(playerIn, playerIn.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.8F, 0.8F + playerIn.getRNG().nextFloat() * 0.4F);
			playerIn.getCooldownTracker().setCooldown(this, 25);
			playerIn.swingArm(handIn);
			playerIn.addStat(Stats.ITEM_USED.get(this));
			heldItemStack.damageItem(1, playerIn, e -> e.sendBreakAnimation(handIn));
			return new ActionResult<>(ActionResultType.SUCCESS, heldItemStack);
		}
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 8.0D, AttributeModifier.Operation.ADDITION));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.0D, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}

	public static void addWave(UUID name, List<SeismicWave> list) {
		List<SeismicWave> waves = null;
		for (List<SeismicWave> waves1 : WAVES.values()) {
			waves = waves1;
		}

		if (waves == null) {
			WAVES.put(name, list);
		} else {
			waves.addAll(list);
			WAVES.put(name, waves);
		}
	}
}