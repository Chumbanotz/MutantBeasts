package chumbanotz.mutantbeasts.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Multimap;

import chumbanotz.mutantbeasts.util.ZombieChunk;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HulkHammerItem extends Item {
	public static Map<UUID, List<ZombieChunk>> chunkList = new HashMap<>();

	public HulkHammerItem(Item.Properties properties) {
		super(properties.maxDamage(64));
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(1, attacker, e -> e.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		return true;
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
		if (!worldIn.isRemote) {
			List<ZombieChunk> list = new ArrayList<>();
			float temp = playerIn.rotationPitch;
			playerIn.rotationPitch = 0.0F;
			Vec3d vec = playerIn.getLookVec();
			playerIn.rotationPitch = temp;
			int x = MathHelper.floor(playerIn.posX + vec.x * 1.5D);
			int y = MathHelper.floor(playerIn.getBoundingBox().minY);
			int z = MathHelper.floor(playerIn.posZ + vec.z * 1.5D);
			int x1 = MathHelper.floor(playerIn.posX + vec.x * 8.0D);
			int z1 = MathHelper.floor(playerIn.posZ + vec.z * 8.0D);
			ZombieChunk.addLinePositions(worldIn, list, x, z, x1, z1, y);
			addChunkAttack(playerIn.getUniqueID(), list);
		}

		worldIn.playSound(playerIn, playerIn.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.8F, 0.8F + playerIn.getRNG().nextFloat() * 0.4F);
		playerIn.getCooldownTracker().setCooldown(this, 30);
		playerIn.swingArm(handIn);
		playerIn.setActiveHand(handIn);
		playerIn.getHeldItem(handIn).damageItem(1, playerIn, e -> e.sendBreakAnimation(handIn));
		return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 2.0D, AttributeModifier.Operation.ADDITION));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.0D, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}

	public static void addChunkAttack(UUID name, List<ZombieChunk> list) {
		List<ZombieChunk> chunks = null;
		for (List<ZombieChunk> chunks1 : chunkList.values()) {
			chunks = chunks1;
		}

		if (chunks == null) {
			chunkList.put(name, list);
		} else {
			chunks.addAll(list);
			chunkList.put(name, chunks);
		}
	}
}