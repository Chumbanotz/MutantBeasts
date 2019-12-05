package chumbanotz.mutantbeasts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class EntityUtil {
	public static final Predicate<Entity> CAN_AI_TARGET = EntityPredicates.CAN_AI_TARGET;

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

	public static void disableShield(LivingEntity livingEntity, DamageSource damageSource, int ticks) {
		if (livingEntity instanceof PlayerEntity && canBlockDamageSource(livingEntity, damageSource)) {
			((PlayerEntity)livingEntity).getCooldownTracker().setCooldown(livingEntity.getActiveItemStack().getItem(), ticks);
			livingEntity.resetActiveHand();
			livingEntity.world.setEntityState(livingEntity, (byte)30);
		}
	}

	/** Adjusted from {@link PlayerEntity#attackTargetEntityWithCurrentItem(Entity)} */
	public static void sendPlayerVelocityPacket(Entity entity) {
		if (entity instanceof ServerPlayerEntity) {
			((ServerPlayerEntity)entity).connection.sendPacket(new SEntityVelocityPacket(entity));
			entity.velocityChanged = false;
		}
	}

	public static boolean requireDarknessAndSky(EntityType<? extends MonsterEntity> entityType, IWorld world, SpawnReason spawnReason, BlockPos pos, Random random) {
		return spawnReason != SpawnReason.SPAWNER && MonsterEntity.func_223325_c(entityType, world, spawnReason, pos, random) && world.isSkyLightMax(pos);
	}

	public static boolean isMobFeline(LivingEntity livingEntity) {
		return livingEntity instanceof OcelotEntity || livingEntity instanceof CatEntity;
	}

	/** To be used for {@link TameableEntity#shouldAttackEntity(LivingEntity, LivingEntity)} */
	public static boolean shouldAttackEntity(LivingEntity target, LivingEntity owner, boolean canTargetCreepers) {
		if (owner instanceof PlayerEntity) {
			if (target instanceof CreeperEntity) {
				return canTargetCreepers;
			} else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
				return false;
			} else if (target instanceof IronGolemEntity || target instanceof SnowGolemEntity || target instanceof MutantSnowGolemEntity) {
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

	public static boolean isFacingEntity(LivingEntity livingEntity, double x, double z, float maxDifference) {
		float rot;

		for (rot = (float)(Math.atan2(z, x) * 180.0D / Math.PI) + 90.0F; rot > livingEntity.rotationYawHead + 180.0F; rot -= 360.0F) {
			;
		}

		while (rot <= livingEntity.rotationYawHead - 180.0F) {
			rot += 360.0F;
		}

		return Math.abs(livingEntity.rotationYawHead - rot) < maxDifference;
	}

	/** Returns true if the mob is able to drop experience, and then does so. Based off of {@link LivingEntity#onDeathUpdate()} */
	public static boolean dropExperience(MobEntity mob, int recentlyHit, int experiencePoints, PlayerEntity attackingPlayer) {
		if (!mob.world.isRemote && recentlyHit > 0 && mob.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			for (int i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(mob, attackingPlayer, experiencePoints), j; i > 0; i -= j) {
				mob.world.addEntity(new ExperienceOrbEntity(mob.world, mob.posX, mob.posY, mob.posZ, j = ExperienceOrbEntity.getXPSplit(i)));
			}

			return true;
		}

		return false;
	}

	/** Same as {@link LivingEntity#onDeath(DamageSource)} except no drops are spawned */
	public static void onDeath(MobEntity mobEntity, DamageSource damageSource, boolean dead) {
		if (net.minecraftforge.common.ForgeHooks.onLivingDeath(mobEntity, damageSource)) return;
		if (!dead) {
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

			dead = true;
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
	}

	public static void shatterGlass(World world, AxisAlignedBB bb) {
		for (BlockPos blockPos : BlockPos.getAllInBoxMutable(MathHelper.floor(bb.minX), MathHelper.floor(bb.minY), MathHelper.floor(bb.minZ), MathHelper.floor(bb.maxX), MathHelper.floor(bb.maxY), MathHelper.floor(bb.maxZ))) {
			if ((world.getBlockState(blockPos).getBlock() instanceof AbstractGlassBlock || world.getBlockState(blockPos).isIn(net.minecraftforge.common.Tags.Blocks.GLASS))) {
				world.destroyBlock(blockPos, false);
			}
		}
	}

	public static void copyNBT(LivingEntity oldEntity, LivingEntity newEntity) {
		if (oldEntity == newEntity) {
			throw new IllegalArgumentException("Old entity is the same as the new entity");
		}

		final CompoundNBT copiedNBT = oldEntity.writeWithoutTypeId(new CompoundNBT());
		copiedNBT.putUniqueId("UUID", newEntity.getUniqueID());
		copiedNBT.put("Attributes", SharedMonsterAttributes.writeAttributes(newEntity.getAttributes()));
		copiedNBT.putFloat("Health", newEntity.getHealth());

		if (copiedNBT.contains("ActiveEffects", 9)) {
			ListNBT listnbt = copiedNBT.getList("ActiveEffects", 10);

			for (int i = 0; i < listnbt.size(); ++i) {
				CompoundNBT compoundnbt = listnbt.getCompound(i);
				EffectInstance effectinstance = EffectInstance.read(compoundnbt);

				if (!newEntity.isPotionApplicable(effectinstance)) {
					listnbt.remove(i);
				}
			}
		}

		if (oldEntity instanceof MobEntity && newEntity instanceof MobEntity) {
			MobEntity oldMob = (MobEntity)oldEntity;
			MobEntity newMob = (MobEntity)newEntity;
			copiedNBT.putBoolean("CanPickUpLoot", false);

			if (oldMob.detachHome()) {
				newMob.setHomePosAndDistance(oldMob.getHomePosition(), (int)oldMob.getMaximumHomeDistance());
			}

			if (copiedNBT.contains("ArmorItems", 9) && !copiedNBT.getList("ArmorItems", 10).isEmpty()) {
				copiedNBT.getList("ArmorItems", 10).clear();
			}

			if (copiedNBT.contains("HandItems", 9) && !copiedNBT.getList("HandItems", 10).isEmpty()) {
				copiedNBT.getList("HandItems", 10).clear();
			}
		}

		if (oldEntity instanceof AgeableEntity && newEntity instanceof AgeableEntity) {
			AgeableEntity oldAgeable = (AgeableEntity)oldEntity;
			AgeableEntity newAgeable = (AgeableEntity)newEntity;

			if (oldAgeable.isChild() && newAgeable.createChild(newAgeable) == null) {
				copiedNBT.putInt("Age", 0);
			}
		}

		newEntity.deserializeNBT(copiedNBT);
	}

	// From here on is from Mutant Creatures
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

	@OnlyIn(Dist.CLIENT)
	public static void spawnEnderParticles(Entity entity) {
		spawnEnderParticles(entity, 256, 1.8F);
	}

	@OnlyIn(Dist.CLIENT)
	public static void spawnEnderParticles(Entity entity, int amount, float speed) {
		for (int i = 0; i < amount; i++) {
			float f = (entity.world.rand.nextFloat() - 0.5F) * speed;
			float f1 = (entity.world.rand.nextFloat() - 0.5F) * speed;
			float f2 = (entity.world.rand.nextFloat() - 0.5F) * speed;
			double tempX = entity.posX + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
			double tempY = entity.posY + ((entity.world.rand.nextFloat() - 0.5F) * entity.getHeight()) + 0.5D;
			double tempZ = entity.posZ + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
			entity.world.addParticle(MBParticleTypes.LARGE_PORTAL, tempX, tempY, tempZ, (double)f, (double)f1, (double)f2);
		}
	}

	public static void spawnEnderParticlesOnServer(Entity entity, int amount, float speed) {
		if (entity.world instanceof ServerWorld) {
			for (int i = 0; i < amount; i++) {
				float f = (entity.world.rand.nextFloat() - 0.5F) * speed;
				float f1 = (entity.world.rand.nextFloat() - 0.5F) * speed;
				float f2 = (entity.world.rand.nextFloat() - 0.5F) * speed;
				double tempX = entity.posX + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
				double tempY = entity.posY + ((entity.world.rand.nextFloat() - 0.5F) * entity.getHeight()) + 0.5D;
				double tempZ = entity.posZ + ((entity.world.rand.nextFloat() - 0.5F) * entity.getWidth());
				((ServerWorld)entity.world).spawnParticle(MBParticleTypes.LARGE_PORTAL, tempX, tempY, tempZ, 0, (double)f, (double)f1, (double)f2, 1.0D);
			}
		}
	}

	public static List<Entity> getCollidingEntities(Entity entity, World world, AxisAlignedBB box) {
		List<Entity> list = new ArrayList<Entity>();

		for (Entity entity1 : world.getEntitiesInAABBexcluding(entity, box.grow(4.0D), CAN_AI_TARGET)) {
			AxisAlignedBB box1 = entity1.getBoundingBox();

			if (box1 != null && box.intersects(box1)) {
				list.add(entity1);
			}
		}

		return list;
	}

	public static <T extends Entity> List<T> getCollidingEntities(Entity entity, Class<T> targetClass, AxisAlignedBB box) {
		List<T> list = new ArrayList<>();

		for (T entity1 : entity.world.getEntitiesWithinAABB(targetClass, box.grow(4.0D), CAN_AI_TARGET)) {
			AxisAlignedBB box1 = entity1.getBoundingBox();

			if (entity != entity1 && box1 != null && box.intersects(box1)) {
				list.add(entity1);
			}
		}

		return list;
	}

	public static Vec3d getDirVector(float rotation, float scale) {
		float rad = rotation * 0.017453292F;
		return new Vec3d((double)(-MathHelper.sin(rad) * scale), 0.0D, (double)(MathHelper.cos(rad) * scale));
	}

	public static boolean teleportTo(LivingEntity living, double x, double y, double z) {
		return teleportTo(living, x, y, z, true);
	}

	public static boolean teleportTo(LivingEntity living, double x, double y, double z, boolean changeY) {
		double oldX = living.posX;
		double oldY = living.posY;
		double oldZ = living.posZ;
		int teleX = MathHelper.floor(x);
		int teleY = MathHelper.floor(y);
		int teleZ = MathHelper.floor(z);
		boolean success = false;

		if (living.world.isBlockPresent(new BlockPos(teleX, teleY, teleZ))) {
			boolean temp = false;

			while (!temp && teleY > 0) {
				Block block = living.world.getBlockState(new BlockPos(teleX, teleY - 1, teleZ)).getBlock();

				if (block != Blocks.AIR && living.world.getBlockState(new BlockPos(teleX, teleY - 1, teleZ)).getMaterial().blocksMovement()) {
					temp = true;
				} else if (changeY) {
					--teleY;
				}
			}

			if (temp || !changeY) {
				living.setPosition(x, (double)teleY, z);

				if (living.world.isCollisionBoxesEmpty(living, living.getBoundingBox()) && !living.world.containsAnyLiquid(living.getBoundingBox())) {
					success = true;
				}
			}
		}

		if (!success) {
			living.setPosition(oldX, oldY, oldZ);
			return false;
		} else {
			return true;
		}
	}

	public static List<LivingEntity> getAttackers(MobEntity mobEntity, AxisAlignedBB box) {
		List<LivingEntity> list = new ArrayList<LivingEntity>();

		for (LivingEntity attacker : mobEntity.world.getEntitiesWithinAABB(LivingEntity.class, box, entity -> entity != mobEntity)) {
			if (attacker.getRevengeTarget() == mobEntity) {
				list.add(attacker);
			}

			if (attacker.getLastAttackedEntity() == mobEntity) {
				list.add(attacker);
			}

			if (attacker instanceof MobEntity && ((MobEntity)attacker).getAttackTarget() == mobEntity) {
				list.add(attacker);
			}
		}

		return list;
	}

	public static void divertAttackers(MobEntity targetedMob, LivingEntity newTarget) {
		if (targetedMob == newTarget) {
			return;
		}

		for (LivingEntity attacker : targetedMob.world.getEntitiesWithinAABB(LivingEntity.class, targetedMob.getBoundingBox().grow(16.0D, 10.0D, 16.0D))) {
			if (attacker.getRevengeTarget() == targetedMob) {
				attacker.setRevengeTarget(newTarget);
			}

			if (attacker.getLastAttackedEntity() == targetedMob) {
				attacker.setLastAttackedEntity(newTarget);
			}

			if (attacker instanceof MobEntity && ((MobEntity)attacker).getAttackTarget() == targetedMob) {
				((MobEntity)attacker).setAttackTarget(newTarget);
			}
		}
	}
}