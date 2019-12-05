package chumbanotz.mutantbeasts.item;

import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class ChemicalXItem extends Item {
	public ChemicalXItem(Item.Properties properties) {
		super(properties.maxStackSize(1));
		DispenserBlock.registerDispenseBehavior(this, new IDispenseItemBehavior() {
			public ItemStack dispense(IBlockSource p_dispense_1_, ItemStack p_dispense_2_) {
				return (new ProjectileDispenseBehavior() {
					@Override
					protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
						return Util.make(new ChemicalXEntity(position.getX(), position.getY(), position.getZ(), worldIn), (e) -> e.setItem(stackIn));
					}

					@Override
					protected float getProjectileInaccuracy() {
						return super.getProjectileInaccuracy() * 0.5F;
					}

					@Override
					protected float getProjectileVelocity() {
						return super.getProjectileVelocity() * 1.25F;
					}
				}).dispense(p_dispense_1_, p_dispense_2_);
			}
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		ItemStack itemstack1 = playerIn.abilities.isCreativeMode ? itemstack.copy() : itemstack.split(1);
		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		if (!worldIn.isRemote) {
			ChemicalXEntity potionentity = new ChemicalXEntity(playerIn, worldIn);
			potionentity.setItem(itemstack1);
			potionentity.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, -20.0F, 0.5F, 1.0F);
			worldIn.addEntity(potionentity);
		}

		playerIn.addStat(Stats.ITEM_USED.get(this));
		return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
	}

	public static class BrewingRecipe implements IBrewingRecipe {
		@Override
		public boolean isInput(ItemStack input) {
			return input.getItem() == Items.SPLASH_POTION && PotionUtils.getPotionFromItem(input) == Potions.THICK;
		}

		@Override
		public boolean isIngredient(ItemStack ingredient) {
			Item item = ingredient.getItem();
			return item == MBItems.ENDERSOUL_HAND || item == MBItems.HULK_HAMMER || item == MBItems.CREEPER_SHARD;
		}

		@Override
		public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
			return this.isInput(input) && this.isIngredient(ingredient) ? new ItemStack(MBItems.CHEMICAL_X) : ItemStack.EMPTY;
		}
	}
}