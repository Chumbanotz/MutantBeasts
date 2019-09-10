package chumbanotz.mutantbeasts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class EntityUtil {
	/** Copied from LivingEntity. */
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

	/** Copied from CreeperEntity. */
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

	public static void disableShield(PlayerEntity playerEntity, int ticks) {
		if (playerEntity.isActiveItemStackBlocking()) {
			playerEntity.getCooldownTracker().setCooldown(playerEntity.getActiveItemStack().getItem(), ticks);
			playerEntity.resetActiveHand();
			playerEntity.world.setEntityState(playerEntity, (byte)30);
		}
	}

	public static boolean requireDarknessAndSky(EntityType<MonsterEntity> entityType, IWorld world, SpawnReason spawnReason, BlockPos pos, Random random) {
		return spawnReason != SpawnReason.SPAWNER && MonsterEntity.func_223325_c(entityType, world, spawnReason, pos, random) && world.isSkyLightMax(pos);
	}

	public static boolean isMobFeline(LivingEntity livingEntity) {
		return livingEntity instanceof OcelotEntity || livingEntity instanceof CatEntity;
	}

	public static void copyNBT(Entity oldEntity, Entity newEntity) {
		if (oldEntity == newEntity) {
			throw new IllegalArgumentException("Old entity is the same as the new entity");
		}

		final CompoundNBT copiedNBT = oldEntity.writeWithoutTypeId(new CompoundNBT());

		if (copiedNBT.hasUniqueId("UUID")) {
			copiedNBT.putUniqueId("UUID", newEntity.getUniqueID());
		}

		if (oldEntity instanceof LivingEntity && newEntity instanceof LivingEntity) {
			LivingEntity newLivingEntity = (LivingEntity)newEntity;

			if (copiedNBT.contains("Attributes")) {
				copiedNBT.put("Attributes", SharedMonsterAttributes.writeAttributes(newLivingEntity.getAttributes()));
			}

			if (copiedNBT.contains("Health")) {
				copiedNBT.putFloat("Health", newLivingEntity.getHealth());
			}

			if (copiedNBT.contains("ActiveEffects", 9)) {
				ListNBT listnbt = copiedNBT.getList("ActiveEffects", 10);

				for (int i = 0; i < listnbt.size(); ++i) {
					CompoundNBT compoundnbt = listnbt.getCompound(i);
					EffectInstance effectinstance = EffectInstance.read(compoundnbt);

					if (!newLivingEntity.isPotionApplicable(effectinstance)) {
						listnbt.remove(i);
					}
				}
			}
		}

		if (oldEntity instanceof MobEntity && newEntity instanceof MobEntity) {
			MobEntity oldMob = (MobEntity)oldEntity;
			MobEntity newMob = (MobEntity)newEntity;

			if (oldMob.detachHome()) {
				newMob.setHomePosAndDistance(oldMob.getHomePosition(), (int)oldMob.getMaximumHomeDistance());
			}
		}

		if (oldEntity instanceof AgeableEntity && newEntity instanceof AgeableEntity) {
			AgeableEntity oldAgeable = (AgeableEntity)oldEntity;
			AgeableEntity newAgeable = (AgeableEntity)newEntity;

			if (oldAgeable.isChild() && newAgeable.createChild(newAgeable) == null) {
				copiedNBT.putInt("Age", 0);
			}
		}

		newEntity.copyLocationAndAnglesFrom(oldEntity);
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

	public static List<Entity> getCollidingEntities(Entity entity, World world, AxisAlignedBB box) {
		List<Entity> list = new ArrayList<Entity>();

		for (Entity entity1 : world.getEntitiesWithinAABBExcludingEntity(entity, box.grow(4.0D))) {
			AxisAlignedBB box1 = entity1.getBoundingBox();

			if (box1 != null && box.intersects(box1)) {
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

			while (true) {
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

				break;
			}
		}

		if (!success) {
			living.setPosition(oldX, oldY, oldZ);
			return false;
		} else {
			return true;
		}
	}

	public static void removeAttackers(MobEntity living) {
		for (MobEntity attacker : living.world.getEntitiesWithinAABB(MobEntity.class, living.getBoundingBox().expand(16.0D, 10.0D, 16.0D))) {
			if (attacker != living && attacker.getAttackTarget() == living) {
				attacker.setAttackTarget(null);
				attacker.setRevengeTarget(null);
			}
		}
	}
}