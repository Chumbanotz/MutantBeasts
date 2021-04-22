package chumbanotz.mutantbeasts;

import java.util.List;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.SeismicWave;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID)
public class EventHandler {
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.getWorld().isRemote && event.getEntity() instanceof CreatureEntity) {
			CreatureEntity creature = (CreatureEntity)event.getEntity();

			if (EntityUtil.isFeline(creature)) {
				creature.goalSelector.addGoal(0, new AvoidEntityGoal<>(creature, MutantCreeperEntity.class, 16.0F, 1.33D, 1.33D));
			}

			if (creature.getType() == EntityType.PIG) {
				creature.goalSelector.addGoal(2, new TemptGoal(creature, 1.0D, Ingredient.fromItems(Items.FERMENTED_SPIDER_EYE), false));
			}

			if (creature.getType() == EntityType.VILLAGER) {
				creature.goalSelector.addGoal(0, new AvoidEntityGoal<>(creature, MutantZombieEntity.class, 8.0F, 0.8F, 0.8F));
			}

			if (creature.getType() == EntityType.WANDERING_TRADER) {
				creature.goalSelector.addGoal(1, new AvoidEntityGoal<>(creature, MutantZombieEntity.class, 12.0F, 0.5F, 0.5F));
			}
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
		if (event.getTarget().getType() == EntityType.PIG && !((LivingEntity)event.getTarget()).isPotionActive(Effects.NAUSEA) && stack.getItem() == Items.FERMENTED_SPIDER_EYE) {
			if (!event.getPlayer().isCreative()) {
				stack.shrink(1);
			}

			((CreatureEntity)event.getTarget()).addPotionEffect(new EffectInstance(Effects.NAUSEA, 200));
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (SpiderPigEntity.isPigOrSpider(event.getEntityLiving()) && event.getSource().getTrueSource() instanceof SpiderPigEntity) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onLivingUseItem(LivingEntityUseItemEvent.Tick event) {
		if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == MBItems.MUTANT_SKELETON_CHESTPLATE && event.getItem().getUseAction() == UseAction.BOW && event.getDuration() > 4) {
			event.setDuration(event.getDuration() - 3);
		}
	}

	@SubscribeEvent
	public static void onPlayerShootArrow(ArrowLooseEvent event) {
		if (!event.getWorld().isRemote && event.getPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() == MBItems.MUTANT_SKELETON_SKULL && event.hasAmmo()) {
			event.setCanceled(true);
			PlayerEntity player = event.getPlayer();
			World world = event.getWorld();
			ItemStack bow = event.getBow();
			boolean inAir = !player.onGround && !player.isInWater() && !player.isInLava();
			ItemStack ammo = player.findAmmo(bow);
			if (!ammo.isEmpty() || event.hasAmmo()) {
				if (ammo.isEmpty()) {
					ammo = new ItemStack(Items.ARROW);
				}

				float velocity = BowItem.getArrowVelocity(bow.getUseDuration() - event.getCharge());
				boolean infiniteArrows = player.abilities.isCreativeMode || ammo.getItem() instanceof ArrowItem && ((ArrowItem)ammo.getItem()).isInfinite(ammo, bow, player);
				if (!world.isRemote) {
					ArrowItem arrowitem = (ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
					AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(world, ammo, player);
					abstractarrowentity = ((BowItem)bow.getItem()).customeArrow(abstractarrowentity);
					abstractarrowentity.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, velocity * 3.0F, 1.0F);
					if (velocity == 1.0F && inAir) {
						abstractarrowentity.setIsCritical(true);
					}

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, bow);
					if (j > 0) {
						abstractarrowentity.setDamage(abstractarrowentity.getDamage() + j * 0.5D + 0.5D);
					}

					int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, bow);
					if (k > 0) {
						abstractarrowentity.setKnockbackStrength(k);
					}

					if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, bow) > 0) {
						abstractarrowentity.setFire(100);
					}

					abstractarrowentity.setDamage(abstractarrowentity.getDamage() * (inAir ? 2.0D : 0.5D));
					bow.damageItem(1, player, p_220009_1_ -> p_220009_1_.sendBreakAnimation(player.getActiveHand()));
					if (infiniteArrows || player.abilities.isCreativeMode && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
						abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
					}

					world.addEntity(abstractarrowentity);
				}

				world.playSound((PlayerEntity)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (player.getRNG().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
				if (!infiniteArrows && !player.abilities.isCreativeMode) {
					ammo.shrink(1);
					if (ammo.isEmpty()) {
						player.inventory.deleteStack(ammo);
					}
				}

				player.addStat(Stats.ITEM_USED.get(bow.getItem()));
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		PlayerEntity player = event.player;
		playShoulderEntitySound(player, player.getLeftShoulderEntity());
		playShoulderEntitySound(player, player.getRightShoulderEntity());
		if (!player.world.isRemote && !HulkHammerItem.WAVES.isEmpty() && HulkHammerItem.WAVES.containsKey(player.getUniqueID())) {
			List<SeismicWave> waveList = HulkHammerItem.WAVES.get(player.getUniqueID());

			while (waveList.size() > 16) {
				waveList.remove(0);
			}

			SeismicWave wave = waveList.remove(0);
			wave.affectBlocks(player.world, player);
			AxisAlignedBB box = new AxisAlignedBB((double)wave.getX(), (double)(wave.getY() + 1), (double)wave.getZ(), (double)(wave.getX() + 1), (double)(wave.getY() + 2), (double)(wave.getZ() + 1));

			for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, box)) {
				if (entity instanceof LivingEntity && player.getRidingEntity() != entity) {
					entity.attackEntityFrom(DamageSource.causePlayerDamage(player).setDamageIsAbsolute(), (float)(6 + player.getRNG().nextInt(3)));
				}
			}

			if (waveList.isEmpty()) {
				HulkHammerItem.WAVES.remove(player.getUniqueID());
			}
		}
	}

	@SubscribeEvent
	public static void onItemToss(ItemTossEvent event) {
		World world = event.getPlayer().world;
		PlayerEntity player = event.getPlayer();

		if (!world.isRemote) {
			ItemStack stack = event.getEntityItem().getItem();
			boolean isHand = stack.getItem() == MBItems.ENDERSOUL_HAND && stack.isDamaged();
			if (stack.getItem() == Items.ENDER_EYE || isHand) {
				int count = 0;
				for (EndersoulFragmentEntity orb : world.getEntitiesWithinAABB(EndersoulFragmentEntity.class, player.getBoundingBox().grow(8.0D), EndersoulFragmentEntity::isTamed)) {
					if (orb.getOwner() == player) {
						count++;
						orb.remove();
					}
				}

				if (count > 0) {
					EntityUtil.spawnEndersoulParticles(player);
					int addDmg = count * 60;
					if (isHand) {
						int dmg = stack.getDamage() - addDmg;
						stack.setDamage(Math.max(dmg, 0));
					} else {
						ItemStack newStack = new ItemStack(MBItems.ENDERSOUL_HAND);
						newStack.setDamage(MBItems.ENDERSOUL_HAND.getMaxDamage(stack) - addDmg);
						event.getEntityItem().setItem(newStack);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
		event.getAffectedEntities().removeIf(entity -> {
			return entity instanceof ItemEntity && ((ItemEntity)entity).getItem().getItem() == MBItems.CREEPER_SHARD;
		});
	}

	private static void playShoulderEntitySound(PlayerEntity player, @Nullable CompoundNBT compoundNBT) {
		if (compoundNBT != null && !compoundNBT.contains("Silent") || !compoundNBT.getBoolean("Silent")) {
			EntityType.byKey(compoundNBT.getString("id")).filter(MBEntityType.CREEPER_MINION::equals).ifPresent(entityType -> {
				if (player.world.rand.nextInt(500) == 0) {
					player.world.playSound(null, player.posX, player.posY, player.posZ, MBSoundEvents.ENTITY_CREEPER_MINION_AMBIENT, player.getSoundCategory(), 1.0F, (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F + 1.5F);
				}
			});
		}
	}
}