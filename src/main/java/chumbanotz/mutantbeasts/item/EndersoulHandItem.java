package chumbanotz.mutantbeasts.item;

import com.google.common.collect.Multimap;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.client.MBItemStackTileEntityRenderer;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
		return 16;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.type == EnchantmentType.WEAPON && enchantment != Enchantments.SWEEPING;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		if (context.isPlacerSneaking()) {
			return ActionResultType.FAIL;
		} else if (!world.getBlockState(pos).isIn(MutantBeasts.ENDERSOUL_HAND_HOLDABLE)) {
			return ActionResultType.FAIL;
		} else if (!world.canMineBlockBody(context.getPlayer(), pos)) {
			return ActionResultType.FAIL;
		} else if (!context.getPlayer().canPlayerEdit(pos, context.getFace(), context.getItem())) {
			return ActionResultType.FAIL;
		} else if (world.getBlockState(pos).hasTileEntity()) {
			return ActionResultType.FAIL;
		} else {
			if (!world.isRemote) {
				world.addEntity(new ThrowableBlockEntity(world, context.getPlayer(), world.getBlockState(pos), pos));
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}

			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!playerIn.isSneaking()) {
			return new ActionResult<>(ActionResultType.PASS, stack);
		} else {
			RayTraceResult result = rayTrace(worldIn, playerIn, 128.0F);
			if (result.getType() == RayTraceResult.Type.MISS) {
				playerIn.sendStatusMessage(new TranslationTextComponent(this.getTranslationKey() + ".teleport_failed"), true);
				return new ActionResult<>(ActionResultType.FAIL, stack);
			} else {
				if (result.getType() == RayTraceResult.Type.BLOCK) {
					BlockPos pos = ((BlockRayTraceResult)result).getPos();
					Direction direction = ((BlockRayTraceResult)result).getFace();
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					x += direction.getXOffset();
					y += direction.getYOffset();
					z += direction.getZOffset();
					BlockPos checkPos = new BlockPos(x, y - 1, z);
					if (!worldIn.isAirBlock(checkPos) || !worldIn.getBlockState(checkPos).getMaterial().isSolid()) {
						Block block1 = worldIn.getBlockState(pos.up()).getBlock();
						Block block2 = worldIn.getBlockState(pos.up(2)).getBlock();
						Block block3 = worldIn.getBlockState(pos.up(3)).getBlock();
						if (block1 == Blocks.AIR) {
							x = pos.getX();
							y = pos.getY() + 1;
							z = pos.getZ();
						} else if (block2 == Blocks.AIR) {
							x = pos.getX();
							y = pos.getY() + 2;
							z = pos.getZ();
						} else if (block3 == Blocks.AIR) {
							x = pos.getX();
							y = pos.getY() + 3;
							z = pos.getZ();
						}
					}

					worldIn.playSound(null, playerIn.posX, playerIn.posY + (double)playerIn.getHeight() / 2.0D, playerIn.posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, playerIn.getSoundCategory(), 1.0F, 1.0F);
					playerIn.setPositionAndUpdate((double)x + 0.5D, (double)y, (double)z + 0.5D);
					playerIn.fallDistance = 0.0F;

					if (!worldIn.isRemote) {
						MutantEndermanEntity.teleportAttack(playerIn);
					}

					worldIn.playSound(null, playerIn.posX, playerIn.posY + (double)playerIn.getHeight() / 2.0D, playerIn.posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, playerIn.getSoundCategory(), 1.0F, 1.0F);
					EntityUtil.spawnEndersoulParticles(playerIn);
					playerIn.getCooldownTracker().setCooldown(this, 40);
					playerIn.swingArm(handIn);
					playerIn.addStat(Stats.ITEM_USED.get(this));
					stack.damageItem(4, playerIn, e -> e.sendBreakAnimation(handIn));
					return new ActionResult<>(ActionResultType.SUCCESS, stack);
				}

				return super.onItemRightClick(worldIn, playerIn, handIn);
			}
		}
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

	public static RayTraceResult rayTrace(World world, PlayerEntity player, float maxDist) {
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch);
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw);
		double d0 = player.prevPosX + (player.posX - player.prevPosX);
		double d1 = player.prevPosY + (player.posY - player.prevPosY) + (double)player.getEyeHeight();
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ);
		Vec3d start = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-pitch * 0.017453292F);
		float f6 = MathHelper.sin(-pitch * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		Vec3d end = start.add(f7 * maxDist, f6 * maxDist, f8 * maxDist);
		return world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
	}
}