package chumbanotz.mutantbeasts;

import java.util.List;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.goal.CopyAttackTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.TrackSummonerGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.item.ArmorBlockItem;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.SeismicWave;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID)
public class EventHandler {
	//data get entity @e[type=!minecraft:player,limit=1,distance=..5]
	@SubscribeEvent
	public static void onAttachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
		if (SummonableCapability.isEntityEligible(event.getObject().getType())) {
			event.addCapability(SummonableCapability.ID, new SummonableCapability.Provider());
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof CreatureEntity) {
			CreatureEntity creature = (CreatureEntity)event.getEntity();

			if (EntityUtil.isFeline(creature)) {
				creature.goalSelector.addGoal(2, new AvoidEntityGoal<>(creature, MutantCreeperEntity.class, 16.0F, 1.33D, 1.33D));
			}

			if (creature.getType() == EntityType.PIG) {
				creature.goalSelector.addGoal(2, new TemptGoal(creature, 1.0D, Ingredient.fromItems(Items.FERMENTED_SPIDER_EYE), false));
			}

			if (creature instanceof VillagerEntity) {
				creature.goalSelector.addGoal(0, new AvoidEntityGoal<>(creature, MutantZombieEntity.class, 8.0F, 0.8F, 0.8F));
			}

			if (SummonableCapability.getLazy(creature).isPresent()) {
				creature.goalSelector.addGoal(0, new TrackSummonerGoal(creature));
				creature.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(creature, 1.0D));
				creature.targetSelector.addGoal(0, new CopyAttackTargetGoal(creature, false, SummonableCapability.get(creature)::getSummoner));
			}
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
		if (event.getTarget().getType() == EntityType.PIG && !event.getEntityLiving().isPotionActive(Effects.NAUSEA) && stack.getItem() == Items.FERMENTED_SPIDER_EYE) {
			if (!event.getPlayer().isCreative()) {
				stack.shrink(1);
			}

			((CreatureEntity)event.getTarget()).addPotionEffect(new EffectInstance(Effects.NAUSEA, 600, 99));
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (livingEntity instanceof PlayerEntity && !(livingEntity instanceof ClientPlayerEntity)) {
			PlayerEntity playerEntity = (PlayerEntity)livingEntity;
			ItemStack stack = playerEntity.getItemStackFromSlot(EquipmentSlotType.HEAD);
			if (stack.getItem() instanceof ArmorBlockItem && !event.getSource().isUnblockable()) {
				float damage = event.getAmount();
				if (!(damage <= 0.0F)) {
					damage /= 4.0F;
					if (damage < 1.0F) {
						damage = 1.0F;
					}

					stack.damageItem((int)damage, playerEntity, e -> e.sendBreakAnimation(EquipmentSlotType.HEAD));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		DamageSource source = event.getSource();
		if (livingEntity instanceof PlayerEntity && EntityUtil.canBlockDamageSource(livingEntity, source) && source.getTrueSource() instanceof MutantCreeperEntity && source.isExplosion()) {
			MutantCreeperEntity mutantCreeperEntity = ((MutantCreeperEntity)source.getTrueSource());
			if (mutantCreeperEntity.deathTime > 0) {
				livingEntity.getActiveItemStack().damageItem(Integer.MAX_VALUE, livingEntity, e -> e.sendBreakAnimation(livingEntity.getActiveHand()));
				livingEntity.attackEntityFrom(event.getSource(), event.getAmount() * 0.5F);
			} else {
				EntityUtil.disableShield(livingEntity, source, mutantCreeperEntity.getPowered() ? 200 : 100);
				livingEntity.attackEntityFrom(source, event.getAmount() * 0.5F);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (SpiderPigEntity.isPigOrSpider(event.getEntityLiving()) && event.getSource().getTrueSource() instanceof SpiderPigEntity) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		playShoulderEntitySound(event.player, event.player.getLeftShoulderEntity());
		playShoulderEntitySound(event.player, event.player.getRightShoulderEntity());
		if (!event.player.world.isRemote && HulkHammerItem.WAVES.keySet().contains(event.player.getUniqueID())) {
			PlayerEntity player = event.player;
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
				for (EndersoulFragmentEntity orb : world.getEntitiesWithinAABB(EndersoulFragmentEntity.class, player.getBoundingBox().grow(8.0D), EndersoulFragmentEntity::isCollected)) {
					if (orb.getCollector() == player) {
						count++;
						orb.remove();
					}
				}

				if (count > 0) {
					EntityUtil.spawnLargePortalParticles(player, 256, 1.8F, true);
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
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (SummonableCapability.isEntityEligible(livingEntity.getType()) && SummonableCapability.getLazy(livingEntity).isPresent()) {
			SummonableCapability.getLazy(livingEntity).invalidate();
		}
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