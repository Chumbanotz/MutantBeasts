package chumbanotz.mutantbeasts;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.ai.goal.CopySummonerTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.TrackSummonerGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.item.HulkHammerItem;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.ZombieChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = MutantBeasts.MOD_ID)
public class EventHandler {
	// data get entity @e[type=!minecraft:player,limit=1,distance=..5]
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

			if (SummonableCapability.getFor(creatureEntity).isPresent()) {
				creatureEntity.goalSelector.addGoal(0, new TrackSummonerGoal(creatureEntity));
				creatureEntity.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(creatureEntity, 1.0D));
				creatureEntity.targetSelector.addGoal(0, new CopySummonerTargetGoal(creatureEntity));
			}
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
	public static void onLivingDeathEvent(LivingDeathEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (SummonableCapability.isEntityEligible(livingEntity.getType()) && SummonableCapability.getFor((MobEntity)livingEntity).isPresent()) {
			SummonableCapability.getFor((MobEntity)livingEntity).invalidate();
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (livingEntity instanceof PlayerEntity && HulkHammerItem.chunkList.keySet().contains(livingEntity.getUniqueID())) {
			Iterable<ServerWorld> worlds = Minecraft.getInstance().getIntegratedServer().getWorlds();
			Iterator<UUID> i$ = HulkHammerItem.chunkList.keySet().iterator();

			while (true) {
				UUID name;
				List<ZombieChunk> chunkList;
				PlayerEntity player;
				ServerWorld worldObj;
				do {
					do {
						if (!i$.hasNext()) {
							return;
						}

						name = i$.next();
						chunkList = HulkHammerItem.chunkList.get(name);
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

				ZombieChunk chunk = chunkList.remove(0);
				chunk.handleBlocks(player.world, player);
				AxisAlignedBB box = new AxisAlignedBB((double)chunk.getX(), (double)(chunk.getY() + 1), (double)chunk.getZ(), (double)(chunk.getX() + 1), (double)(chunk.getY() + 2), (double)(chunk.getZ() + 1));

				for (Entity entity : EntityUtil.getCollidingEntities(player, worldObj, box)) {
					if (entity instanceof LivingEntity) {
						((LivingEntity)entity).attackEntityFrom(DamageSource.causePlayerDamage(player), (float)(6 + player.getRNG().nextInt(3)));
					}
				}

				if (chunkList.isEmpty()) {
					HulkHammerItem.chunkList.remove(name);
				}
			}
		}
	}
}