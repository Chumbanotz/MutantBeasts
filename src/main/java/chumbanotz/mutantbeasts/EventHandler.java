package chumbanotz.mutantbeasts;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.BodyPartEntity;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.goal.CopyAttackTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.TrackSummonerGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.SeismicWave;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
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
			CreatureEntity creatureEntity = (CreatureEntity)event.getEntity();

			if (EntityUtil.isMobFeline(creatureEntity)) {
				creatureEntity.goalSelector.addGoal(2, new AvoidEntityGoal<>(creatureEntity, MutantCreeperEntity.class, 16.0F, 1.33D, 1.33D));
			}

			if (creatureEntity instanceof SnowGolemEntity) {
				creatureEntity.goalSelector.addGoal(3, new LookAtGoal(creatureEntity, MutantSnowGolemEntity.class, 6.0F));
			}

			if (creatureEntity instanceof VillagerEntity) {
				creatureEntity.goalSelector.addGoal(0, new AvoidEntityGoal<>(creatureEntity, MutantZombieEntity.class, 8.0F, 0.8F, 0.8F));
			}

			if (SummonableCapability.getLazy(creatureEntity).isPresent()) {
				creatureEntity.goalSelector.addGoal(0, new TrackSummonerGoal(creatureEntity));
				creatureEntity.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(creatureEntity, 1.0D));
				creatureEntity.targetSelector.addGoal(0, new CopyAttackTargetGoal(creatureEntity, false, SummonableCapability.get(creatureEntity)::getSummoner));
			}
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(EntityInteract event) {
		ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
		if (event.getTarget().getType() == EntityType.PIG && stack.getItem() == Items.FERMENTED_SPIDER_EYE) {
			if (!event.getPlayer().isCreative()) {
				stack.shrink(1);
			}

			((CreatureEntity)event.getTarget()).addPotionEffect(new EffectInstance(Effects.NAUSEA, 1000, 99));
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event) {
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
	public static void onProjectileImpact(ProjectileImpactEvent event) {
		if (event.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult)event.getRayTraceResult()).getEntity();
			if (entity instanceof BodyPartEntity) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(TickEvent.PlayerTickEvent event) {
		playShoulderCreeperMinionAmbientSound(event.player, event.player.getLeftShoulderEntity());
		playShoulderCreeperMinionAmbientSound(event.player, event.player.getRightShoulderEntity());
		if (HulkHammerItem.SEISMIC_WAVES.keySet().contains(event.player.getUniqueID())) {
			Iterable<ServerWorld> worlds = Minecraft.getInstance().getIntegratedServer().getWorlds();
			Iterator<UUID> i$ = HulkHammerItem.SEISMIC_WAVES.keySet().iterator();

			while (true) {
				UUID name;
				List<SeismicWave> chunkList;
				PlayerEntity player;
				ServerWorld worldObj;
				do {
					do {
						if (!i$.hasNext()) {
							return;
						}

						name = i$.next();
						chunkList = HulkHammerItem.SEISMIC_WAVES.get(name);
						player = null;
						worldObj = null;

						while (worlds.iterator().hasNext()) {
							player = worlds.iterator().next().getPlayerByUuid(name);
							if (player != null) {
								worldObj = worlds.iterator().next();
								break;
							}
						}
					} while (chunkList == null);
				} while (chunkList.isEmpty());

				while (chunkList.size() > 16) {
					chunkList.remove(0);
				}

				SeismicWave chunk = chunkList.remove(0);
				chunk.affectBlocks(worldObj, player);
				AxisAlignedBB box = new AxisAlignedBB((double)chunk.getX(), (double)(chunk.getY() + 1), (double)chunk.getZ(), (double)(chunk.getX() + 1), (double)(chunk.getY() + 2), (double)(chunk.getZ() + 1));

				for (Entity entity : EntityUtil.getCollidingEntities(player, worldObj, box)) {
					if (entity instanceof LivingEntity) {
						((LivingEntity)entity).attackEntityFrom(DamageSource.causePlayerDamage(player), (float)(6 + player.getRNG().nextInt(3)));
					}
				}

				if (chunkList.isEmpty()) {
					HulkHammerItem.SEISMIC_WAVES.remove(name);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerToss(ItemTossEvent event) {
		World world = event.getPlayer().world;
		PlayerEntity player = event.getPlayer();

		if (!world.isRemote) {
			ItemStack stack = event.getEntityItem().getItem();
			boolean isHand = (stack.getItem() == MBItems.ENDERSOUL_HAND);
			if (stack.getItem() == Items.ENDER_EYE || isHand) {
				int count = 0;
				for (EndersoulFragmentEntity orb : world.getEntitiesWithinAABB(EndersoulFragmentEntity.class, player.getBoundingBox().grow(8.0D))) {
					if (orb.isAlive() && orb.getCollector() == player) {
						count++;
						orb.remove();
					}
				}

				if (count > 0) {
					EntityUtil.spawnEnderParticlesOnServer(player, 256, 1.8F);
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
	public static void onLivingDeathEvent(LivingDeathEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (SummonableCapability.isEntityEligible(livingEntity.getType()) && SummonableCapability.getLazy(livingEntity).isPresent()) {
			SummonableCapability.getLazy(livingEntity).invalidate();
		}
	}

	private static void playShoulderCreeperMinionAmbientSound(PlayerEntity player, @Nullable CompoundNBT compoundNBT) {
		if (compoundNBT != null && !compoundNBT.contains("Silent") || !compoundNBT.getBoolean("Silent")) {
			EntityType.byKey(compoundNBT.getString("id"))
			.filter(MBEntityType.CREEPER_MINION::equals)
			.ifPresent(entityType -> {
				if (player.world.rand.nextInt(500) == 0)
					player.world.playSound(null, player.posX, player.posY, player.posZ, MBSoundEvents.ENTITY_CREEPER_MINION_AMBIENT, player.getSoundCategory(), 1.0F, (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F + 1.5F);
			});
		}
	}
}