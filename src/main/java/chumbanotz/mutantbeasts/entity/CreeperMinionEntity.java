package chumbanotz.mutantbeasts.entity;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CreeperMinionEntity extends CreeperEntity {
	public static final Predicate<LivingEntity> IS_TAMED = e -> e instanceof CreeperMinionEntity && ((CreeperMinionEntity)e).isTamed();
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> STATE = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> CREEPER_MINION_FLAGS = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.BYTE);
	private static final DataParameter<Float> EXPLOSION_RADIUS = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> COLLAR_COLOR = EntityDataManager.createKey(CreeperMinionEntity.class, DataSerializers.VARINT);
	private int lastActiveTime;
	private int timeSinceIgnited;
	private int fuseTime = 30;
	private AvoidEntityGoal<AnimalEntity> avoidEntityGoal = new AvoidEntityGoal<>(this, AnimalEntity.class, 6.0F, 1.0D, 1.2D, EntityUtil::isMobFeline);
	private NearestAttackableTargetGoal<PlayerEntity> nearestAttackablePlayerGoal = new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true);

	public CreeperMinionEntity(EntityType<? extends CreeperMinionEntity> type, World worldIn) {
		super(type, worldIn);
		this.experienceValue = 3;
		this.setCanExplodeContinuously(false);
		this.setDestroyBlocks(true);
		this.setupTamedAI();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new CreeperMinionEntity.SitGoal());
		this.goalSelector.addGoal(2, new CreeperMinionEntity.SwellGoal());
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
		this.goalSelector.addGoal(5, new CreeperMinionEntity.FollowOwnerGoal());
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new CreeperMinionEntity.DefendOwnerGoal());
		this.targetSelector.addGoal(3, new MBHurtByTargetGoal(this, MutantCreeperEntity.class));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.empty());
		this.dataManager.register(STATE, -1);
		this.dataManager.register(CREEPER_MINION_FLAGS, (byte)0);
		this.dataManager.register(EXPLOSION_RADIUS, 20.0F);
		this.dataManager.register(COLLAR_COLOR, 16);
	}

	public boolean isTamed() {
		return this.getOwner() != null && this.getOwner() instanceof PlayerEntity;
	}

	@Nullable
	public UUID getOwnerUniqueId() {
		return this.dataManager.get(OWNER_UNIQUE_ID).orElse(null);
	}

	public void setOwnerUniqueId(@Nullable UUID uuid) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.ofNullable(uuid));
		this.setupTamedAI();
	}

	@Nullable
	public LivingEntity getOwner() {
		if (this.world instanceof ServerWorld) {
			UUID uuid = this.getOwnerUniqueId();
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(uuid);
			return entity instanceof LivingEntity ? (LivingEntity)entity : null;
		}

		return null;
	}

	public int getCreeperMinionState() {
		return this.dataManager.get(STATE);
	}

	public void setCreeperMinonState(int state) {
		this.dataManager.set(STATE, state);
	}

	private boolean getCreeperMinionFlag(int flag) {
		return (this.dataManager.get(CREEPER_MINION_FLAGS) & 1 << flag) != 0;
	}

	private void setCreeperMinionFlag(int flag, boolean set) {
		byte b0 = this.dataManager.get(CREEPER_MINION_FLAGS);
		this.dataManager.set(CREEPER_MINION_FLAGS, set ? (byte)(b0 | 1 << flag) : (byte)(b0 & ~(1 << flag)));
	}

	@Override
	public boolean getPowered() {
		return this.getCreeperMinionFlag(0);
	}

	public void setPowered(boolean powered) {
		this.setCreeperMinionFlag(0, powered);
	}

	public boolean isIgnited() {
		return this.getCreeperMinionFlag(1);
	}

	@Override
	public void ignite() {
		this.setCreeperMinionFlag(1, true);
	}

	public boolean isSitting() {
		return this.getCreeperMinionFlag(2);
	}

	public void setSitting(boolean sitting) {
		this.setCreeperMinionFlag(2, sitting);
	}

	public boolean canExplodeContinuously() {
		return this.getCreeperMinionFlag(3);
	}

	public void setCanExplodeContinuously(boolean continuously) {
		this.setCreeperMinionFlag(3, continuously);
	}

	public boolean canDestroyBlocks() {
		return this.getCreeperMinionFlag(4);
	}

	public void setDestroyBlocks(boolean destroy) {
		this.setCreeperMinionFlag(4, destroy);
	}

	public float getExplosionRadius() {
		return this.dataManager.get(EXPLOSION_RADIUS) / 10.0F;
	}

	public void setExplosionRadius(float radius) {
		this.dataManager.set(EXPLOSION_RADIUS, radius * 10.0F);
	}

	public DyeColor getCollarColor() {
		return DyeColor.byId(this.dataManager.get(COLLAR_COLOR));
	}

	public void setCollarColor(DyeColor collarcolor) {
		this.dataManager.set(COLLAR_COLOR, collarcolor.getId());
	}

	@Deprecated
	@Override
	public boolean hasIgnited() {
		return false;
	}

	@Deprecated
	@Override
	public int getCreeperState() {
		return -1;
	}

	@Deprecated
	@Override
	public void setCreeperState(int state) {
	}

	@Override
	public boolean isChild() {
		return true;
	}

	@Override
	public boolean ableToCauseSkullDrop() {
		return this.canExplodeContinuously() ? this.getPowered() : super.ableToCauseSkullDrop();
	}

	private void setupTamedAI() {
		this.goalSelector.removeGoal(this.avoidEntityGoal);
		this.targetSelector.removeGoal(this.nearestAttackablePlayerGoal);

		if (!this.isTamed()) {
			this.goalSelector.addGoal(4, this.avoidEntityGoal);
			this.targetSelector.addGoal(2, this.nearestAttackablePlayerGoal);
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, damageMultiplier);
		this.timeSinceIgnited += ((int)distance * 1.5F);

		if (this.timeSinceIgnited > this.fuseTime - 15) {
			distance = 0.0F;
			this.timeSinceIgnited = this.fuseTime - 15;
		}
	}

	@Override
	public void tick() {
		if (this.isAlive()) {
			this.lastActiveTime = this.timeSinceIgnited;

			if (this.isIgnited()) {
				this.setCreeperMinonState(1);
			}

			int i = this.getCreeperMinionState();

			if (i > 0 && this.timeSinceIgnited == 0) {
				this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, this.getSoundPitch() * 0.5F);
			}

			this.timeSinceIgnited += i;

			if (this.timeSinceIgnited < 0) {
				this.timeSinceIgnited = 0;
			}

			if (this.timeSinceIgnited >= this.fuseTime) {
				this.timeSinceIgnited = 0;

				if (!this.world.isRemote) {
					Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) && this.canDestroyBlocks() ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
					this.world.createExplosion(this, this.posX, this.posY, this.posZ, this.getExplosionRadius() + (this.getPowered() ? 2.0F : 0.0F), explosion$mode);

					if (!this.canExplodeContinuously()) {
						this.dead = true;
						this.remove();
						EntityUtil.spawnLingeringCloud(this);
					}
				}

				this.setCreeperMinonState(-30);
			}
		}

		super.tick();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getCreeperFlashIntensity(float partialTicks) {
		return MathHelper.lerp(partialTicks, (float)this.lastActiveTime, (float)this.timeSinceIgnited) / (float)(30 - 2);
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		if (this.isTamed()) {
			if (itemstack.getItem() instanceof DyeItem) {
				DyeColor dyecolor = ((DyeItem)itemstack.getItem()).getDyeColor();
				if (dyecolor != this.getCollarColor()) {
					this.setCollarColor(dyecolor);
					if (!player.abilities.isCreativeMode) {
						itemstack.shrink(1);
					}

					return true;
				}
			}

		} else if (itemstack.getItem() == Items.FLINT_AND_STEEL && !this.isIgnited()) {
			this.world.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
			player.swingArm(hand);

			if (!this.world.isRemote) {
				this.ignite();
				itemstack.damageItem(1, player, livingEntity -> livingEntity.sendBreakAnimation(hand));
			}

			return true; // MC-99779
		} else if (!this.world.isRemote && this.getOwner() == null) {
			this.setOwnerUniqueId(player.getUniqueID());
			this.setCanExplodeContinuously(true);
			this.setDestroyBlocks(false);
			player.sendMessage(new StringTextComponent("For testing only: Creeper Minion has been tamed by " + player.getName().getString()));
			return true;
		}

		return false;
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
		return this.getOwner() == target ? false : target instanceof CreeperMinionEntity && ((CreeperMinionEntity)target).getOwner() == this.getOwner() ? false : super.canAttack(target);
	}

	@Override
	public boolean isPreventingPlayerRest(PlayerEntity playerIn) {
		return !this.isTamed();
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return !this.isTamed();
	}

	@Override
	public Team getTeam() {
		LivingEntity livingentity = this.getOwner();

		if (livingentity != null) {
			return livingentity.getTeam();
		}

		return super.getTeam();
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		LivingEntity livingentity = this.getOwner();

		if (entityIn == livingentity) {
			return true;
		}

		if (livingentity != null) {
			return livingentity.isOnSameTeam(entityIn);
		}

		return super.isOnSameTeam(entityIn);
	}

	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
			this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
		}

		super.onDeath(cause);
	}

	@Override
	public void remove() {
		if (this.world.getDifficulty() != Difficulty.PEACEFUL || !this.isTamed() || this.dead) {
			super.remove();
		}
	}

	@Override
	public void playAmbientSound() {
		if (this.getAttackTarget() == null || this.getCreeperMinionState() <= 0) {
			super.playAmbientSound();
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_CREEPER_HURT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return super.getHurtSound(damageSourceIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return super.getDeathSound();
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putString("OwnerUUID", this.getOwnerUniqueId() == null ? "" : this.getOwnerUniqueId().toString());
		compound.putBoolean("Sitting", this.isSitting());
		compound.putBoolean("ExplodeContinuously", this.canExplodeContinuously());
		compound.putBoolean("DestroysBlocks", this.canDestroyBlocks());
		compound.putBoolean("Ignited", this.isIgnited());
		compound.putShort("Fuse", (short)this.fuseTime);
		compound.putFloat("ExplosionRadius", this.getExplosionRadius());

		if (this.getPowered()) {
			compound.putBoolean("Powered", true);
		}

		for (String s : new String[] {"powered", "ignited"}) {
			compound.remove(s);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setSitting(compound.getBoolean("Sitting"));
		this.setCanExplodeContinuously(compound.getBoolean("ExplodeContinuously"));
		this.setDestroyBlocks(compound.getBoolean("DestroysBlocks"));
		this.setPowered(compound.getBoolean("Powered"));
		this.setExplosionRadius(compound.getFloat("ExplosionRadius"));

		if (compound.contains("Fuse", 99)) {
			this.fuseTime = compound.getShort("Fuse");
		}

		if (compound.getBoolean("Ignited")) {
			this.ignite();
		}

		if (compound.contains("OwnerUUID") && !compound.getString("OwnerUUID").isEmpty()) {
			this.setOwnerUniqueId(UUID.fromString(compound.getString("OwnerUUID")));
		}
	}

	class SwellGoal extends Goal {
		public SwellGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity livingentity = getAttackTarget();
			return getCreeperMinionState() > 0 || livingentity != null && getDistanceSq(livingentity) < 9.0D;
		}

		@Override
		public void startExecuting() {
			getNavigator().clearPath();
		}

		@Override
		public void tick() {
			LivingEntity livingentity = getAttackTarget();

			if (livingentity == null) {
				setCreeperMinonState(-1);
			} else if (getDistanceSq(livingentity) > 36.0D) {
				setCreeperMinonState(-1);
			} else if (!getEntitySenses().canSee(livingentity)) {
				setCreeperMinonState(-1);
			} else {
				setCreeperMinonState(1);
			}
		}
	}

	class FollowOwnerGoal extends Goal {
		private LivingEntity owner;
		private int timeToRecalcPath;

		public FollowOwnerGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity livingentity = getOwner();

			if (livingentity == null) {
				return false;
			} else if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).isSpectator()) {
				return false;
			} else if (getDistanceSq(livingentity) < 16.0D || isSitting()) {
				return false;
			} else {
				this.owner = livingentity;
				return true;
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return !getNavigator().noPath() && getDistanceSq(this.owner) > 100.0D;
		}

		@Override
		public void startExecuting() {
			this.timeToRecalcPath = 0;
			setPathPriority(PathNodeType.WATER, 0.0F);
		}

		@Override
		public void resetTask() {
			this.owner = null;
			getNavigator().clearPath();
			setPathPriority(PathNodeType.WATER, 8.0F);
		}

		@Override
		public void tick() {
			if (isTamed()) {
				getLookController().setLookPositionWithEntity(this.owner, 10.0F, (float)getVerticalFaceSpeed());
			}

			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!getNavigator().tryMoveToEntityLiving(this.owner, 1.0D)) {
					if (isTamed() && !getLeashed() && !isPassenger() && !(getDistanceSq(this.owner) < 144.0D)) {
						int i = MathHelper.floor(this.owner.posX) - 2;
						int j = MathHelper.floor(this.owner.posZ) - 2;
						int k = MathHelper.floor(this.owner.getBoundingBox().minY);

						for (int l = 0; l <= 4; ++l) {
							for (int i1 = 0; i1 <= 4; ++i1) {
								if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.canTeleportToBlock(new BlockPos(i + l, k - 1, j + i1))) {
									setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), rotationYaw, rotationPitch);
									getNavigator().clearPath();
									return;
								}
							}
						}
					}
				}
			}
		}

		protected boolean canTeleportToBlock(BlockPos pos) {
			BlockState blockstate = world.getBlockState(pos);
			return blockstate.canEntitySpawn(world, pos, getType()) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2));
		}
	}

	class DefendOwnerGoal extends TargetGoal {
		public DefendOwnerGoal() {
			super(CreeperMinionEntity.this, false);
			this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity owner = CreeperMinionEntity.this.getOwner();

			if (owner == null) {
				return false;
			} else if (owner.getRevengeTarget() != null) {
				this.target = owner.getRevengeTarget();
			} else if (owner instanceof PlayerEntity && owner.getLastAttackedEntity() != null) {
				this.target = owner.getLastAttackedEntity();
			} else if (owner instanceof MobEntity && ((MobEntity)owner).getAttackTarget() != null) {
				this.target = ((MobEntity)owner).getAttackTarget();
			}

			return this.isSuitableTarget(this.target, EntityPredicate.DEFAULT) && this.shouldAttackEntity(this.target, owner);
		}

		@Override
		public void startExecuting() {
			this.goalOwner.setAttackTarget(this.target);
			super.startExecuting();
		}

		public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
			if (owner instanceof PlayerEntity) {
				if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
					return false;
				} else if (target instanceof IronGolemEntity && ((IronGolemEntity)target).isPlayerCreated() || target instanceof SnowGolemEntity || target instanceof MutantSnowGolemEntity) {
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
	}

	class SitGoal extends Goal {
		public SitGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return isSitting();
		}

		@Override
		public void startExecuting() {
			navigator.clearPath();
			setRevengeTarget(null);
			setAttackTarget(null);
		}
	}
}