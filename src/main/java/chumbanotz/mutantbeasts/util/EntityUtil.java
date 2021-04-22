package chumbanotz.mutantbeasts.util;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.packet.MBPacketHandler;
import chumbanotz.mutantbeasts.packet.SpawnParticlePacket;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntityUtil {
	private static final Field STUN_TICK = ObfuscationReflectionHelper.findField(RavagerEntity.class, "field_213692_bA");

	public static float getHeadAngle(float rotationYawHead, double x, double z) {
		return MathHelper.degreesDifferenceAbs((float)(Math.atan2(z, x) * 180.0D / Math.PI) + 90.0F, rotationYawHead);
	}

	public static void spawnLingeringCloud(LivingEntity livingEntity) {
		Collection<EffectInstance> collection = livingEntity.getActivePotionEffects();

		if (!collection.isEmpty()) {
			AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(livingEntity.world, livingEntity.posX, livingEntity.posY, livingEntity.posZ);
			areaeffectcloudentity.setRadius(2.5F);
			areaeffectcloudentity.setRadiusOnUse(-0.5F);
			areaeffectcloudentity.setWaitTime(10);
			areaeffectcloudentity.setDuration(areaeffectcloudentity.getDuration() / 2);
			areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / (float)areaeffectcloudentity.getDuration());

			for (EffectInstance effectinstance : collection) {
				areaeffectcloudentity.addEffect(new EffectInstance(effectinstance));
			}

			livingEntity.world.addEntity(areaeffectcloudentity);
		}
	}

	public static void stunRavager(LivingEntity livingEntity) {
		if (livingEntity instanceof RavagerEntity) {
			try {
				if (STUN_TICK.getInt(livingEntity) == 0) {
					STUN_TICK.setInt(livingEntity, 40);
					livingEntity.playSound(SoundEvents.ENTITY_RAVAGER_STUNNED, 1.0F, 1.0F);
					livingEntity.world.setEntityState(livingEntity, (byte)39);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Failed to access ravager stunTick", e);
			}
		}
	}

	public static boolean isHolding(LivingEntity livingEntity, Item item) {
		return livingEntity.getHeldItemMainhand().getItem() == item || livingEntity.getHeldItemOffhand().getItem() == item;
	}

	public static void disableShield(LivingEntity livingEntity, int ticks) {
		if (livingEntity instanceof PlayerEntity && livingEntity.isActiveItemStackBlocking()) {
			((PlayerEntity)livingEntity).getCooldownTracker().setCooldown(livingEntity.getActiveItemStack().getItem(), ticks);
			livingEntity.resetActiveHand();
			livingEntity.world.setEntityState(livingEntity, (byte)30);
		}
	}

	public static void sendPlayerVelocityPacket(Entity entity) {
		if (entity instanceof ServerPlayerEntity) {
			((ServerPlayerEntity)entity).connection.sendPacket(new SEntityVelocityPacket(entity));
		}
	}

	public static boolean isFeline(LivingEntity livingEntity) {
		return livingEntity instanceof OcelotEntity || livingEntity instanceof CatEntity;
	}

	public static boolean shouldAttackEntity(LivingEntity target, LivingEntity owner, boolean canTargetCreepers) {
		if (target instanceof CreeperEntity) {
			return canTargetCreepers;
		} else if (target instanceof TameableEntity && ((TameableEntity)target).isOwner(owner)) {
			return false;
		} else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
			return false;
		} else if (target instanceof GolemEntity && !(target instanceof IMob)) {
			return false;
		} else {
			return !(target instanceof AbstractHorseEntity) || !((AbstractHorseEntity)target).isTame();
		}
	}

	public static void dropExperience(MobEntity mob, int recentlyHit, int experienceValue, PlayerEntity attackingPlayer) {
		if (!mob.world.isRemote && recentlyHit > 0 && mob.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			experienceValue = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(mob, attackingPlayer, experienceValue);
            while(experienceValue > 0) {
               int j = ExperienceOrbEntity.getXPSplit(experienceValue);
               experienceValue -= j;
               mob.world.addEntity(new ExperienceOrbEntity(mob.world, mob.posX, mob.posY, mob.posZ, j));
            }
		}
	}

	public static <T extends MobEntity> T convertMobWithNBT(LivingEntity mobToConvert, EntityType<T> newEntityType, boolean dropInventory) {
		T newMob = newEntityType.create(mobToConvert.world);
		final CompoundNBT copiedNBT = mobToConvert.writeWithoutTypeId(new CompoundNBT());
		copiedNBT.putUniqueId("UUID", newMob.getUniqueID());
		copiedNBT.put("Attributes", SharedMonsterAttributes.writeAttributes(newMob.getAttributes()));
		copiedNBT.putFloat("Health", newMob.getHealth());
		if (mobToConvert.getTeam() != null) {
			copiedNBT.putString("Team", mobToConvert.getTeam().getName());
		}

		if (copiedNBT.contains("ActiveEffects", 9)) {
			ListNBT activeEffects = copiedNBT.getList("ActiveEffects", 10);

			for (int i = 0; i < activeEffects.size(); ++i) {
				CompoundNBT compoundnbt = activeEffects.getCompound(i);
				EffectInstance effectInstance = EffectInstance.read(compoundnbt);
				if (effectInstance != null && !newMob.isPotionApplicable(effectInstance)) {
					activeEffects.remove(i);
					i--;
				}
			}
		}

		if (dropInventory) {
			copiedNBT.putBoolean("CanPickUpLoot", false);
			if (copiedNBT.contains("ArmorItems", 9)) {
				ListNBT armorItems = copiedNBT.getList("ArmorItems", 10);
				ListNBT armorDropChances = copiedNBT.getList("ArmorDropChances", 5);
				for (int i = 0; i < armorItems.size(); ++i) {
					ItemStack itemStack = ItemStack.read(armorItems.getCompound(i));
					if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && armorDropChances.getFloat(i) > 1.0F) {
						mobToConvert.entityDropItem(itemStack);
					}
				}

				armorItems.clear();
				armorDropChances.clear();
			}

			if (copiedNBT.contains("HandItems", 9)) {
				ListNBT handItems = copiedNBT.getList("HandItems", 10);
				ListNBT handDropChances = copiedNBT.getList("HandDropChances", 5);
				for (int i = 0; i < handItems.size(); ++i) {
					ItemStack itemStack = ItemStack.read(handItems.getCompound(i));
					if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && handDropChances.getFloat(i) > 1.0F) {
						mobToConvert.entityDropItem(itemStack);
					}
				}

				handItems.clear();
				handDropChances.clear();
			}
		}

		newMob.read(copiedNBT);
		mobToConvert.world.addEntity(newMob);
		mobToConvert.remove();
		return newMob;
	}

	public static void spawnParticlesAtEntity(LivingEntity entity, IParticleData particleData, int amount) {
		if (entity.world.isRemote) {
			for (int i = 0; i < amount; ++i) {
				double posX = entity.posX + (double)(entity.getRNG().nextFloat() * entity.getWidth() * 2.0F) - (double)entity.getWidth();
				double posY = entity.posY + 0.5D + (double)(entity.getRNG().nextFloat() * entity.getHeight());
				double posZ = entity.posZ + (double)(entity.getRNG().nextFloat() * entity.getWidth() * 2.0F) - (double)entity.getWidth();
				double x = entity.getRNG().nextGaussian() * 0.02D;
				double y = entity.getRNG().nextGaussian() * 0.02D;
				double z = entity.getRNG().nextGaussian() * 0.02D;
				entity.world.addParticle(particleData, posX, posY, posZ, x, y, z);
			}
		}
	}

	public static void sendParticlePacket(Entity entity, IParticleData particleData, int amount) {
		double x = entity.posX;
		double y = entity.posY;
		double z = entity.posZ;
		MBPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 1024.0D, entity.dimension)), new SpawnParticlePacket(particleData, x, y, z, entity.getWidth(), entity.getHeight(), entity.getWidth(), amount));
	}

	public static void spawnEndersoulParticles(Entity entity, int amount, float speed) {
		for (int i = 0; i < amount; i++) {
			float f = (entity.world.rand.nextFloat() - 0.5F) * speed;
			float f1 = (entity.world.rand.nextFloat() - 0.5F) * speed;
			float f2 = (entity.world.rand.nextFloat() - 0.5F) * speed;
			double tempX = entity.posX + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
			double tempY = entity.posY + ((entity.world.rand.nextFloat() - 0.5F) * entity.getHeight()) + 0.5D;
			double tempZ = entity.posZ + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
			entity.world.addParticle(MBParticleTypes.ENDERSOUL, tempX, tempY, tempZ, (double)f, (double)f1, (double)f2);
		}
	}

	public static void spawnEndersoulParticles(Entity entity) {
		sendParticlePacket(entity, MBParticleTypes.ENDERSOUL, 256);
	}

	public static Vec3d getDirVector(float rotation, float scale) {
		float rad = rotation * 0.017453292F;
		return new Vec3d((double)(-MathHelper.sin(rad) * scale), 0.0D, (double)(MathHelper.cos(rad) * scale));
	}

	public static boolean teleportTo(MobEntity mob, double x, double y, double z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
		if (mob.world.isBlockPresent(pos)) {
			do {
				pos.move(Direction.DOWN);
			} while (pos.getY() > 0 && !mob.world.getBlockState(pos).getMaterial().blocksMovement());

			pos.move(Direction.UP);
		}

		if (!mob.isOffsetPositionInLiquid(pos.getX() - MathHelper.floor(mob.posX), pos.getY() - MathHelper.floor(mob.posY), pos.getZ() - MathHelper.floor(mob.posZ))) {
			return false;
		} else {
			mob.getNavigator().clearPath();
			mob.setPosition((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D);
			return true;
		}
	}

	public static void divertAttackers(MobEntity targetedMob, @Nullable LivingEntity newTarget) {
		if (targetedMob == newTarget) {
			return;
		}

		for (MobEntity attacker : targetedMob.world.getEntitiesWithinAABB(MobEntity.class, targetedMob.getBoundingBox().grow(16.0D, 10.0D, 16.0D))) {
			if (attacker.getRevengeTarget() == targetedMob) {
				attacker.setRevengeTarget(newTarget);
			}

			if (attacker.getAttackTarget() == targetedMob) {
				attacker.setAttackTarget(newTarget);
			}
		}
	}

	public static RayTraceResult rayTrace(Entity entity, double length, RayTraceContext.FluidMode fluidMode) {
		Vec3d startPos = entity.getEyePosition(1.0F);
		Vec3d endPos = startPos.add(entity.getLookVec().scale(length));
		return entity.world.rayTraceBlocks(new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.COLLIDER, fluidMode, entity));
	}
}