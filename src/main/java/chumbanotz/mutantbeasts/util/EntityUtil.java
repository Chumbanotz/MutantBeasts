package chumbanotz.mutantbeasts.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.MBConfig;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class EntityUtil {
	private static final Field STUN_TICK = ObfuscationReflectionHelper.findField(RavagerEntity.class, "field_213692_bA");

	/** Copied exactly from {@link LivingEntity#canBlockDamageSource(DamageSource)}. */
	public static boolean canBlockDamageSource(LivingEntity livingEntity, DamageSource damageSource) {
		Entity entity = damageSource.getImmediateSource();
		boolean flag = false;
		if (entity instanceof AbstractArrowEntity) {
			AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)entity;
			if (abstractarrowentity.getPierceLevel() > 0) {
				flag = true;
			}
		}

		if (!damageSource.isUnblockable() && livingEntity.isActiveItemStackBlocking() && !flag) {
			Vec3d vec3d2 = damageSource.getDamageLocation();
			if (vec3d2 != null) {
				Vec3d vec3d = livingEntity.getLook(1.0F);
				Vec3d vec3d1 = vec3d2.subtractReverse(new Vec3d(livingEntity.posX, livingEntity.posY, livingEntity.posZ)).normalize();
				vec3d1 = new Vec3d(vec3d1.x, 0.0D, vec3d1.z);
				if (vec3d1.dotProduct(vec3d) < 0.0D) {
					return true;
				}
			}
		}

		return false;
	}

	/** Copied from {@link CreeperEntity#spawnLingeringCloud()}. */
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
				STUN_TICK.setInt(livingEntity, 40);
				livingEntity.playSound(SoundEvents.ENTITY_RAVAGER_STUNNED, 1.0F, 1.0F);
				livingEntity.world.setEntityState(livingEntity, (byte)39);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isHolding(LivingEntity livingEntity, Item item) {
		return livingEntity.getHeldItemMainhand().getItem() == item || livingEntity.getHeldItemOffhand().getItem() == item;
	}

	public static void disableShield(LivingEntity livingEntity, DamageSource damageSource, int ticks) {
		if (livingEntity instanceof PlayerEntity && canBlockDamageSource(livingEntity, damageSource)) {
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

	public static boolean canMutantSpawn(EntityType<? extends MonsterEntity> entityType, IWorld world, SpawnReason spawnReason, BlockPos pos, Random random) {
		int i = Math.max(1, MBConfig.globalSpawnRate);
		i = Math.min(20, i);
		return MonsterEntity.func_223325_c(entityType, world, spawnReason, pos, random) && random.nextInt(50 / i) == 0;
	}

	public static boolean isFeline(LivingEntity livingEntity) {
		return livingEntity instanceof OcelotEntity || livingEntity instanceof CatEntity;
	}

	/** To be used for {@link TameableEntity#shouldAttackEntity(LivingEntity, LivingEntity)} */
	public static boolean shouldAttackEntity(LivingEntity target, LivingEntity owner, boolean canTargetCreepers) {
		if (owner instanceof PlayerEntity) {
			if (target instanceof CreeperEntity) {
				return canTargetCreepers;
			} else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
				return false;
			} else if (target instanceof GolemEntity && !(target instanceof IMob)) {
				return false;
			} else if (target instanceof TameableEntity && ((TameableEntity)target).getOwner() == owner) {
				return false;
			} else {
				return !(target instanceof AbstractHorseEntity) || !((AbstractHorseEntity)target).isTame();
			}
		} else {
			return true;
		}
	}

	public static boolean isFacing(float rotationYawHead, double x, double z, float maxDifference) {
		float rot;

		for (rot = (float)(Math.atan2(z, x) * 180.0D / Math.PI) + 90.0F; rot > rotationYawHead + 180.0F; rot -= 360.0F) {
			;
		}

		while (rot <= rotationYawHead - 180.0F) {
			rot += 360.0F;
		}

		return Math.abs(rotationYawHead - rot) < maxDifference;
	}

	/** Returns true if the mob is able to drop experience, and then does so. Based off of {@link LivingEntity#onDeathUpdate()} */
	public static boolean dropExperience(MobEntity mob, int recentlyHit, Function<PlayerEntity, Integer> experiencePoints, PlayerEntity attackingPlayer) {
		if (!mob.world.isRemote && recentlyHit > 0 && mob.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            int i = experiencePoints.apply(attackingPlayer);

            i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(mob, attackingPlayer, i);
            while(i > 0) {
               int j = ExperienceOrbEntity.getXPSplit(i);
               i -= j;
               mob.world.addEntity(new ExperienceOrbEntity(mob.world, mob.posX, mob.posY, mob.posZ, j));
            }

			return true;
		}

		return false;
	}

	/** Same as {@link LivingEntity#onDeath(DamageSource)} except no drops are spawned */
	public static void onDeath(MobEntity mobEntity, DamageSource damageSource) {
		if (net.minecraftforge.common.ForgeHooks.onLivingDeath(mobEntity, damageSource)) return;
		Entity entity = damageSource.getTrueSource();
		LivingEntity livingentity = mobEntity.getAttackingEntity();
		if (livingentity != null) {
			livingentity.awardKillScore(mobEntity, 0, damageSource);
		}

		if (entity != null) {
			entity.onKillEntity(mobEntity);
		}

		if (mobEntity.isSleeping()) {
			mobEntity.wakeUp();
		}

		mobEntity.getCombatTracker().reset();
		if (!mobEntity.world.isRemote) {
			boolean flag = false;
			if (livingentity instanceof WitherEntity) {
				if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(mobEntity.world, mobEntity)) {
					BlockPos blockpos = new BlockPos(mobEntity.posX, mobEntity.posY, mobEntity.posZ);
					BlockState blockstate = Blocks.WITHER_ROSE.getDefaultState();
					if (mobEntity.world.getBlockState(blockpos).isAir(mobEntity.world, blockpos) && blockstate.isValidPosition(mobEntity.world, blockpos)) {
						mobEntity.world.setBlockState(blockpos, blockstate, 3);
						flag = true;
					}
				}

				if (!flag) {
					ItemEntity itementity = new ItemEntity(mobEntity.world, mobEntity.posX, mobEntity.posY, mobEntity.posZ, new ItemStack(Items.WITHER_ROSE));
					mobEntity.world.addEntity(itementity);
				}
			}
		}

		mobEntity.world.setEntityState(mobEntity, (byte)3);
	}

	/** {@link HurtByTargetGoal#alertOthers()} */
	public static void alertOthers(MobEntity alertingMob, Class<?>... excludedReinforcementTypes) {
		if (alertingMob.isAIDisabled() || alertingMob.getRevengeTarget() == null) {
			return;
		}

		double d0 = (double)alertingMob.getNavigator().getPathSearchRange();
		for (MobEntity otherMob : alertingMob.world.func_225317_b(alertingMob.getClass(), new AxisAlignedBB(alertingMob.posX, alertingMob.posY, alertingMob.posZ, alertingMob.posX + 1.0D, alertingMob.posY + 1.0D, alertingMob.posZ + 1.0D).grow(d0, 10.0D, d0))) {
			if (otherMob != null && alertingMob != otherMob && (!(alertingMob instanceof TameableEntity) || ((TameableEntity)alertingMob).getOwner() == ((TameableEntity)otherMob).getOwner()) && !otherMob.isOnSameTeam(alertingMob.getRevengeTarget())) {
				boolean flag = false;
				for (Class<?> oclass : excludedReinforcementTypes) {
					if (otherMob.getClass() == oclass) {
						flag = true;
						break;
					}
				}

				if (!flag) {
					otherMob.setRevengeTarget(alertingMob.getRevengeTarget());
				}
			}
		}
	}

	public static void copyNBT(Entity oldEntity, Entity newEntity, boolean resetAttributes) {
		if (oldEntity == newEntity) {
			throw new IllegalArgumentException("Old entity is the same as the new entity");
		}

		final CompoundNBT copiedNBT = oldEntity.writeWithoutTypeId(new CompoundNBT());
		copiedNBT.putUniqueId("UUID", newEntity.getUniqueID());

		if (oldEntity instanceof LivingEntity && newEntity instanceof LivingEntity) {
			LivingEntity newLiving = (LivingEntity)newEntity;
			if (resetAttributes) {
				copiedNBT.put("Attributes", SharedMonsterAttributes.writeAttributes(newLiving.getAttributes()));
				copiedNBT.putFloat("Health", newLiving.getHealth());
			}

			if (copiedNBT.contains("ActiveEffects", 9)) {
				ListNBT listnbt = copiedNBT.getList("ActiveEffects", 10);

				for (int i = 0; i < listnbt.size(); ++i) {
					CompoundNBT compoundnbt = listnbt.getCompound(i);
					EffectInstance effectinstance = EffectInstance.read(compoundnbt);

					if (!newLiving.isPotionApplicable(effectinstance)) {
						listnbt.remove(i);
					}
				}
			}
		}

		copiedNBT.putString("id", newEntity.getEntityString());
		newEntity.deserializeNBT(copiedNBT);
	}

	@OnlyIn(Dist.CLIENT)
	public static void spawnParticlesAtEntity(Entity entity, IParticleData particleData, int amount) {
		for (int i = 0; i < amount; ++i) {
			double posX = entity.posX + (double)(entity.world.rand.nextFloat() * entity.getWidth() * 2.0F) - (double)entity.getWidth();
			double posY = entity.posY + 0.5D + (double)(entity.world.rand.nextFloat() * entity.getHeight());
			double posZ = entity.posZ + (double)(entity.world.rand.nextFloat() * entity.getWidth() * 2.0F) - (double)entity.getWidth();
			double x = entity.world.rand.nextGaussian() * 0.02D;
			double y = entity.world.rand.nextGaussian() * 0.02D;
			double z = entity.world.rand.nextGaussian() * 0.02D;
			entity.world.addParticle(particleData, posX, posY, posZ, x, y, z);
		}
	}

	public static void spawnLargePortalParticles(Entity entity, int amount, float speed, boolean serverSide) {
		if (!serverSide || entity.world instanceof ServerWorld) {
			for (int i = 0; i < amount; i++) {
				float f = (entity.world.rand.nextFloat() - 0.5F) * speed;
				float f1 = (entity.world.rand.nextFloat() - 0.5F) * speed;
				float f2 = (entity.world.rand.nextFloat() - 0.5F) * speed;
				double tempX = entity.posX + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
				double tempY = entity.posY + ((entity.world.rand.nextFloat() - 0.5F) * entity.getHeight()) + 0.5D;
				double tempZ = entity.posZ + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
				if (serverSide) {
					((ServerWorld)entity.world).spawnParticle(MBParticleTypes.LARGE_PORTAL, tempX, tempY, tempZ, 0, (double)f, (double)f1, (double)f2, 1.0D);
				} else {
					entity.world.addParticle(MBParticleTypes.LARGE_PORTAL, tempX, tempY, tempZ, (double)f, (double)f1, (double)f2);
				}
			}
		}
	}

	public static Vec3d getDirVector(float rotation, float scale) {
		float rad = rotation * 0.017453292F;
		return new Vec3d((double)(-MathHelper.sin(rad) * scale), 0.0D, (double)(MathHelper.cos(rad) * scale));
	}

	/** Similar to {@link LivingEntity#attemptTeleport(double, double, double, boolean)} */
	public static boolean teleportTo(MobEntity mob, double x, double y, double z) {
		double oldX = mob.posX;
		double oldY = mob.posY;
		double oldZ = mob.posZ;
		int teleX = MathHelper.floor(x);
		int teleY = MathHelper.floor(y);
		int teleZ = MathHelper.floor(z);
		boolean success = false;

		if (mob.world.isBlockPresent(new BlockPos(teleX, teleY, teleZ))) {
			boolean temp = false;

			while (!temp && teleY > 0) {
				Block block = mob.world.getBlockState(new BlockPos(teleX, teleY - 1, teleZ)).getBlock();

				if (block != Blocks.AIR && mob.world.getBlockState(new BlockPos(teleX, teleY - 1, teleZ)).getMaterial().blocksMovement()) {
					temp = true;
				} else {
					--teleY;
				}
			}

			if (temp) {
				mob.setPosition(x, (double)teleY, z);

				if (mob.world.isCollisionBoxesEmpty(mob, mob.getBoundingBox()) && !mob.world.containsAnyLiquid(mob.getBoundingBox())) {
					success = true;
				}
			}
		}

		if (!success) {
			mob.setPosition(oldX, oldY, oldZ);
			return false;
		} else {
			mob.getNavigator().clearPath();
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
}