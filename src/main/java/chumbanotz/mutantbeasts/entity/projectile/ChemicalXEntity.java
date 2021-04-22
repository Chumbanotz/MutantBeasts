package chumbanotz.mutantbeasts.entity.projectile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.SkullSpiritEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class ChemicalXEntity extends ProjectileItemEntity {
	public static final Predicate<LivingEntity> IS_APPLICABLE = target -> {
		return target.isNonBoss() && !ChemicalXEntity.MUTATIONS.containsValue(target.getType()) && target.getType() != MBEntityType.CREEPER_MINION && target.getType() != MBEntityType.ENDERSOUL_CLONE;
	};
	public static final EntityPredicate PREDICATE = new EntityPredicate().allowInvulnerable().setCustomPredicate(IS_APPLICABLE);
	private static final Map<EntityType<? extends MobEntity>, EntityType<? extends MobEntity>> MUTATIONS = Util.make(new HashMap<>(), map -> {
		map.put(EntityType.CREEPER, MBEntityType.MUTANT_CREEPER);
		map.put(EntityType.ENDERMAN, MBEntityType.MUTANT_ENDERMAN);
		map.put(EntityType.PIG, MBEntityType.SPIDER_PIG);
		map.put(EntityType.SKELETON, MBEntityType.MUTANT_SKELETON);
		map.put(EntityType.SNOW_GOLEM, MBEntityType.MUTANT_SNOW_GOLEM);
		map.put(EntityType.ZOMBIE, MBEntityType.MUTANT_ZOMBIE);
	});

	public ChemicalXEntity(EntityType<? extends ChemicalXEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public ChemicalXEntity(LivingEntity livingEntityIn, World worldIn) {
		super(MBEntityType.CHEMICAL_X, livingEntityIn, worldIn);
	}

	public ChemicalXEntity(double x, double y, double z, World worldIn) {
		super(MBEntityType.CHEMICAL_X, x, y, z, worldIn);
	}

	public ChemicalXEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(MBEntityType.CHEMICAL_X, worldIn);
	}

	@Override
	protected Item getDefaultItem() {
		return MBItems.CHEMICAL_X;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.05F;
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			for (int i = 5 + this.rand.nextInt(3); i >= 0; --i) {
				float x = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
				float y = 0.1F + this.rand.nextFloat() * 0.1F;
				float z = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
				this.world.addParticle(new ItemParticleData(ParticleTypes.ITEM, this.getItem()), this.posX, this.posY, this.posZ, (double)x, (double)y, (double)z);
			}

			for (int i = this.rand.nextInt(5); i < 50; ++i) {
				float x = (this.rand.nextFloat() - 0.5F) * 1.2F;
				float y = this.rand.nextFloat() * 0.2F;
				float z = (this.rand.nextFloat() - 0.5F) * 1.2F;
				this.world.addParticle(MBParticleTypes.SKULL_SPIRIT, this.posX, this.posY, this.posZ, (double)x, (double)y, (double)z);
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockRayTraceResult)result).getPos();
			if (this.world.getBlockState(blockPos).getCollisionShape(this.world, blockPos).isEmpty()) {
				return;
			}
		}

		if (!this.world.isRemote) {
			MobEntity target = null;
			boolean directHit = false;

			if (result.getType() == RayTraceResult.Type.ENTITY) {
				Entity entity = ((EntityRayTraceResult)result).getEntity();
				if (entity instanceof MobEntity && PREDICATE.canTarget(null, (MobEntity)entity)) {
					target = (MobEntity)entity;
					directHit = true;
				}
			}

			if (!directHit) {
				target = this.world.getClosestEntityWithinAABB(MobEntity.class, PREDICATE, null, this.posX, this.posY, this.posZ, this.getBoundingBox().grow(12.0D, 8.0D, 12.0D));
			}

			if (target != null) {
				SkullSpiritEntity spirit = new SkullSpiritEntity(this.world, target);
				spirit.setPosition(this.posX, this.posY, this.posZ);
				this.world.addEntity(spirit);
			}

			this.world.setEntityState(this, (byte)3);
			this.remove();
		}

		this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
	}

	@Nullable
	public static EntityType<? extends MobEntity> getMutantOf(MobEntity target) {
		EntityType<?> entityType = target.getType();
		if (!MUTATIONS.containsKey(entityType)) {
			return null;
		} else if (entityType == EntityType.PIG && (!target.isPotionActive(Effects.UNLUCK) || target.getActivePotionEffect(Effects.UNLUCK).getAmplifier() != 13)) {
			return null;
		} else if (entityType == EntityType.ZOMBIE && target.isChild()) {
			return null;
		} else {
			return MUTATIONS.get(entityType);
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}