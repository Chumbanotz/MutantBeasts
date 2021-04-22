package chumbanotz.mutantbeasts.entity;

import java.util.EnumSet;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.client.ClientEventHandler;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.HurtByNearestTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.MutatedExplosion;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NonTamedTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CreeperMinionEntity extends ShoulderRidingEntity {
	private static final DataParameter<Byte> CREEPER_MINION_FLAGS = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> EXPLODE_STATE = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Float> EXPLOSION_RADIUS = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.FLOAT);
	private int lastActiveTime;
	private int timeSinceIgnited;
	private int fuseTime = 26;

	public CreeperMinionEntity(EntityType<? extends CreeperMinionEntity> type, World worldIn) {
		super(type, worldIn);
		this.setDestroyBlocks(true);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, this.sitGoal = new SitGoal(this));
		this.goalSelector.addGoal(2, new CreeperMinionEntity.SwellGoal());
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.2D));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<AnimalEntity>(this, AnimalEntity.class, 6.0F, 1.0D, 1.2D, EntityUtil::isFeline) {
			@Override
			public boolean shouldExecute() {
				return !isTamed() && super.shouldExecute();
			}
		});
		this.goalSelector.addGoal(4, new MBMeleeAttackGoal(this, 1.2D));
		this.goalSelector.addGoal(5, new CreeperMinionEntity.FollowOwnerGoal(this));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LandOnOwnersShoulderGoal(this) {
			@Override
			public boolean shouldExecute() {
				return isTamed() && getOwner() instanceof PlayerEntity && super.shouldExecute();
			}
		});
		this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(2, new HurtByNearestTargetGoal(this));
		this.targetSelector.addGoal(3, new NonTamedTargetGoal<>(this, PlayerEntity.class, true, null));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(EXPLODE_STATE, -1);
		this.dataManager.register(CREEPER_MINION_FLAGS, (byte)0);
		this.dataManager.register(EXPLOSION_RADIUS, 2.0F);
	}

	@Override
	@Nullable
	public LivingEntity getOwner() {
		UUID uuid = this.getOwnerId();
		if (uuid == null) {
			return null;
		} else {
			Entity entity = this.world.getPlayerByUuid(uuid);
			if (entity == null && this.world instanceof ServerWorld) {
				entity = ((ServerWorld)this.world).getEntityByUuid(uuid);
			}

			return entity instanceof LivingEntity ? (LivingEntity)entity : null;
		}
	}

	public int getExplodeState() {
		return this.dataManager.get(EXPLODE_STATE);
	}

	private void setExplodeState(int state) {
		this.dataManager.set(EXPLODE_STATE, state);
	}

	public boolean getPowered() {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 1) != 0;
	}

	public void setPowered(boolean powered) {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, powered ? (byte)(b0 | 1) : (byte)(b0 & ~1));
	}

	public boolean hasIgnited() {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 4) != 0;
	}

	public void ignite() {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, (byte)(b0 | 4));
	}

	public boolean canExplodeContinuously() {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 8) != 0;
	}

	public void setCanExplodeContinuously(boolean continuously) {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, continuously ? (byte)(b0 | 8) : (byte)(b0 & ~8));
	}

	public boolean canDestroyBlocks() {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 16) != 0;
	}

	public void setDestroyBlocks(boolean destroy) {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, destroy ? (byte)(b0 | 16) : (byte)(b0 & ~16));
	}

	public boolean canRideOnShoulder() {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 32) != 0;
	}

	public void setCanRideOnShoulder(boolean canRide) {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, canRide ? (byte)(b0 | 32) : (byte)(b0 & ~32));
	}

	public float getExplosionRadius() {
		return this.dataManager.get(EXPLOSION_RADIUS);
	}

	public void setExplosionRadius(float radius) {
		this.dataManager.set(EXPLOSION_RADIUS, radius);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (TAMED.equals(key)) {
			this.recalculateSize();
		}
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	public EntitySize getSize(Pose poseIn) {
		return this.isSitting() ? super.getSize(poseIn).scale(1.0F, 0.75F) : super.getSize(poseIn);
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	public boolean canSitOnShoulder() {
		return super.canSitOnShoulder() && this.canRideOnShoulder() && this.getAttackTarget() == null && this.getExplodeState() <= 0;
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity lightningBolt) {
		super.onStruckByLightning(lightningBolt);
		this.setPowered(true);
	}

	@Override
	public void tick() {
		if (!this.world.isRemote && !this.isTamed() && this.world.getDifficulty() == Difficulty.PEACEFUL) {
			this.remove();
		}

		if (this.isAlive()) {
			this.lastActiveTime = this.timeSinceIgnited;

			if (this.hasIgnited()) {
				this.setExplodeState(1);
			}

			int i = this.getExplodeState();

			if (i > 0 && this.timeSinceIgnited == 0) {
				this.playSound(MBSoundEvents.ENTITY_CREEPER_MINION_PRIMED, 1.0F, this.getSoundPitch());
			}

			this.timeSinceIgnited += i;

			if (this.timeSinceIgnited < 0) {
				this.timeSinceIgnited = 0;
			}

			if (this.timeSinceIgnited >= this.fuseTime) {
				this.timeSinceIgnited = 0;

				if (!this.world.isRemote) {
					MutatedExplosion.create(this, this.getExplosionRadius() + (this.getPowered() ? 2.0F : 0.0F), false, this.canDestroyBlocks() ? MutatedExplosion.Mode.DESTROY : MutatedExplosion.Mode.NONE);
					if (!this.canExplodeContinuously()) {
						if (this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
							this.getOwner().sendMessage(new TranslationTextComponent("death.attack.explosion", this.getDisplayName()));
						}

						this.dead = true;
						this.remove();
						EntityUtil.spawnLingeringCloud(this);
					}
				}

				this.setExplodeState(-this.fuseTime);
			}

			if (this.getMotion().lengthSquared() > 0.8F && this.getAttackTarget() != null && this.getBoundingBox().expand(this.getMotion()).grow(0.5D).intersects(this.getAttackTarget().getBoundingBox())) {
				this.timeSinceIgnited = this.fuseTime;
			}
		}

		super.tick();
	}

	public float getCreeperFlashIntensity(float partialTicks) {
		return MathHelper.lerp(partialTicks, (float)this.lastActiveTime, (float)this.timeSinceIgnited) / (float)(this.fuseTime - 2);
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		Item item = itemstack.getItem();
		if (itemstack.interactWithEntity(player, this, hand)) {
			return true;
		}

		if (this.isTamed()) {
			if (item == MBItems.CREEPER_MINION_TRACKER) {
				player.addStat(Stats.ITEM_USED.get(item));
				if (this.world.isRemote) {
					ClientEventHandler.INSTANCE.displayCreeperMinionTrackerGUI(this);
				}

				return true;
			}

			if (this.isOwner(player)) {
				if (item == Items.GUNPOWDER) {
					if (this.getHealth() < this.getMaxHealth()) {
						this.heal(1.0F);
						itemstack.shrink(1);
						EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.HEART, 1);
						return true;
					} else if (this.getMaxHealth() < 20.0F) {
						this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getMaxHealth() + 1.0F);
						itemstack.shrink(1);
						EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.HEART, 1);
						return true;
					}
				} else if (item == Items.TNT) {
					if (this.canExplodeContinuously()) {
						float explosionRadius = this.getExplosionRadius();
						if (explosionRadius < 4.0F) {
							this.forcedAgeTimer += 10;
							this.setExplosionRadius(explosionRadius + 0.11F);
							itemstack.shrink(1);
							return true;
						}
					} else {
						this.forcedAgeTimer += 15;
						this.setCanExplodeContinuously(true);
						itemstack.shrink(1);
						return true;
					}
				} else {
					if (!this.world.isRemote) {
						this.sitGoal.setSitting(!this.isSitting());
						this.setRevengeTarget(null);
						this.setAttackTarget(null);
					}

					return true;
				}
			}

			return false;
		} else if (item == Items.FLINT_AND_STEEL && !this.hasIgnited()) {
			this.world.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
			player.swingArm(hand);
			player.addStat(Stats.ITEM_USED.get(item));

			if (!this.world.isRemote) {
				this.ignite();
				itemstack.damageItem(1, player, livingEntity -> livingEntity.sendBreakAnimation(hand));
			}

			return true; // MC-99779
		} else if (player.isCreative() && item == MBItems.CREEPER_MINION_TRACKER && this.getOwner() == null) {
			if (!this.world.isRemote) {
				this.setTamedBy(player);
				player.sendMessage(new TranslationTextComponent(item.getTranslationKey() + ".tame_success", this.getDisplayName(), player.getDisplayName()));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
		return EntityUtil.shouldAttackEntity(target, owner, true);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isExplosion()) {
			if (this.isTamed()) {
				return false;
			}

			if (amount >= 2.0F) {
				amount = 2.0F;
			}
		}

		if (this.sitGoal != null) {
			this.sitGoal.setSitting(false);
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean isImmuneToExplosions() {
		return this.isTamed();
	}

	@Override
	public boolean canBeLeashedTo(PlayerEntity player) {
		return !this.getLeashed() && this.isTamed();
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return super.canAttack(target) && !target.isImmuneToExplosions() && target.getType() != MBEntityType.MUTANT_CREEPER;
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return !this.isTamed();
	}

	@Override
	@Nullable
	public Team getTeam() {
		LivingEntity owner = this.getOwner();
		return owner != null ? owner.getTeam() : super.getTeam();
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		LivingEntity owner = this.getOwner();
		return owner != null && (entityIn == owner || owner.isOnSameTeam(entityIn)) || super.isOnSameTeam(entityIn);
	}

	@Override
	public AgeableEntity createChild(AgeableEntity ageable) {
		return null;
	}

	@Override
	public void playAmbientSound() {
		if (this.getAttackTarget() == null && !this.hasIgnited()) {
			super.playAmbientSound();
		}
	}

	@Override
	protected float getSoundPitch() {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_CREEPER_MINION_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_CREEPER_MINION_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_CREEPER_MINION_DEATH;
	}

	@Override
	public SoundCategory getSoundCategory() {
		return this.isTamed() ? SoundCategory.NEUTRAL : SoundCategory.HOSTILE;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("Tamed", this.isTamed());
		compound.putBoolean("ExplodesContinuously", this.canExplodeContinuously());
		compound.putBoolean("DestroysBlocks", this.canDestroyBlocks());
		compound.putBoolean("CanRideOnShoulder", this.canRideOnShoulder());
		compound.putBoolean("Ignited", this.hasIgnited());
//		compound.putShort("Fuse", (short)this.fuseTime);
		compound.putFloat("ExplosionRadius", this.getExplosionRadius());

		if (this.getPowered()) {
			compound.putBoolean("Powered", true);
		}

		for (String s : new String[] {"Age", "ForcedAge", "InLove", "LoveCause"}) {
			compound.remove(s);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setTamed(compound.getBoolean("Tamed"));
		this.setCanExplodeContinuously(compound.getBoolean("ExplodesContinuously"));
		this.setDestroyBlocks(compound.getBoolean("DestroysBlocks"));
		this.setCanRideOnShoulder(compound.getBoolean("CanRideOnShoulder"));
		this.setPowered(compound.getBoolean("Powered"));
		this.setExplosionRadius(compound.getFloat("ExplosionRadius"));

//		if (compound.contains("Fuse", 99)) {
//			this.fuseTime = compound.getShort("Fuse");
//		}

		if (compound.getBoolean("Ignited")) {
			this.ignite();
		}
	}

	class SwellGoal extends Goal {
		public SwellGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity livingentity = getAttackTarget();
			return !isSitting() && (getExplodeState() > 0 || livingentity != null && getDistanceSq(livingentity) < 9.0D && getEntitySenses().canSee(livingentity));
		}

		@Override
		public void startExecuting() {
			getNavigator().clearPath();
		}

		@Override
		public void tick() {
			LivingEntity livingentity = getAttackTarget();
			setExplodeState(isSitting() || livingentity == null || getDistanceSq(livingentity) > 36.0D || !getEntitySenses().canSee(livingentity) ? -1 : 1);
		}
	}

	static class FollowOwnerGoal extends net.minecraft.entity.ai.goal.FollowOwnerGoal {
		public FollowOwnerGoal(CreeperMinionEntity tameableIn) {
			super(tameableIn, 1.2D, 4.0F, 20.0F);
		}

		@Override
		public boolean shouldExecute() {
			return this.tameable.getAttackTarget() == null && super.shouldExecute();
		}

		@Override
		public void tick() {
			if (!this.tameable.isTamed()) {
				if (this.tameable.getOwner() != null) {
					this.tameable.getNavigator().tryMoveToEntityLiving(this.tameable.getOwner(), 1.2D);
				}
			} else {
				super.tick();
			}
		}
	}
}