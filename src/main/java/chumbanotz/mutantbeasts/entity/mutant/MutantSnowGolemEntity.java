package chumbanotz.mutantbeasts.entity.mutant;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.ai.controller.FixedBodyController;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.FleeRainGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.HurtByNearestTargetGoal;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.MoveTowardsVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;

public class MutantSnowGolemEntity extends GolemEntity implements IRangedAttackMob, net.minecraftforge.common.IShearable {
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(MutantSnowGolemEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(MutantSnowGolemEntity.class, DataSerializers.BYTE);
	private boolean isThrowing;
	private int throwingTick;

	public MutantSnowGolemEntity(EntityType<? extends MutantSnowGolemEntity> type, World worldIn) {
		super(type, worldIn);
		this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantSnowGolemEntity.SwimJumpGoal());
		this.goalSelector.addGoal(1, new FleeRainGoal(this, 1.1D));
		this.goalSelector.addGoal(2, new MutantSnowGolemEntity.ThrowIceGoal());
		this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.1D, 30, 12.0F));
		this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.1D));
		this.goalSelector.addGoal(5, new AvoidDamageGoal(this, 1.1D));
		this.goalSelector.addGoal(6, new MoveTowardsVillageGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 1.0000001E-5F));
		this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(9, new LookAtGoal(this, MobEntity.class, 6.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(0, new HurtByNearestTargetGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, MobEntity.class, 10, true, false, entity -> {
			return entity instanceof IMob;
		}));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26F);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.empty());
		this.dataManager.register(STATUS, (byte)1);
	}

	@Nullable
	public PlayerEntity getOwner() {
		UUID uuid = this.getOwnerId();
		return uuid == null ? null : this.world.getPlayerByUuid(uuid);
	}

	@Nullable
	public UUID getOwnerId() {
		return this.dataManager.get(OWNER_UNIQUE_ID).orElse(null);
	}

	public void setOwnerId(@Nullable UUID uuid) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.ofNullable(uuid));
	}

	public boolean hasJackOLantern() {
		return (this.dataManager.get(STATUS) & 1) != 0;
	}

	public void setJackOLantern(boolean jackOLantern) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, jackOLantern ? (byte)(b0 | 1) : (byte)(b0 & -2));
	}

	public boolean getSwimJump() {
		return (this.dataManager.get(STATUS) & 4) != 0;
	}

	public void setSwimJump(boolean swimJump) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, swimJump ? (byte)(b0 | 4) : (byte)(b0 & -5));
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn).setAvoidRain(true);
	}

	@Override
	protected BodyController createBodyController() {
		return new FixedBodyController(this);
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.0F;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return super.canAttack(target) && target instanceof IMob;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.world.isRemote && this.getSwimJump()) {
			EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.ITEM_SNOWBALL, 6);
			EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.SPLASH, 6);
		}

		if (this.isThrowing && this.throwingTick++ >= 20) {
			this.setThrowing(false);
		}

		if (this.ticksExisted % 20 == 0 && this.isWet()) {
			this.attackEntityFrom(DamageSource.DROWN, 1.0F);
		}

		if (this.world.dimension.isNether()) {
			if (this.rand.nextFloat() > Math.min(80.0F, this.getHealth()) * 0.01F) {
				this.world.addParticle(ParticleTypes.FALLING_WATER, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), this.posY - 0.15D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), 0.0D, 0.0D, 0.0D);
			}

			if (this.ticksExisted % 30 == 0) {
				this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
			}
		} else if (!this.world.isRemote && this.onGround && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.posX, this.posY, this.posZ);
			BlockPos.MutableBlockPos posDown = new BlockPos.MutableBlockPos();
			BlockPos.MutableBlockPos posUp = new BlockPos.MutableBlockPos();

			for (int i = -2; i <= 2; ++i) {
				for (int j = -2; j <= 2; ++j) {
					if (Math.abs(i) != 2 || Math.abs(j) != 2) {
						pos.setPos(this).move(i, 0, j);
						posDown.setPos(pos).move(0, -1, 0);
						posUp.setPos(pos).move(0, 1, 0);
						boolean placeSnow = this.world.isAirBlock(pos) && this.world.getBiome(pos).func_225486_c(pos) < 0.95F && Blocks.SNOW.getDefaultState().isValidPosition(this.world, pos);
						boolean placeIce = this.world.hasWater(posDown);

						if (this.world.getFluidState(pos).getFluid() == Fluids.FLOWING_WATER) {
							this.world.setBlockState(pos, Blocks.ICE.getDefaultState());
						}

						if (this.world.getFluidState(posUp).getFluid() == Fluids.FLOWING_WATER) {
							this.world.setBlockState(posUp, Blocks.ICE.getDefaultState());
						}

						if ((!placeSnow || (Math.abs(i) != 2 && Math.abs(j) != 2 || this.rand.nextInt(20) == 0) && (Math.abs(i) != 1 && Math.abs(j) != 1 || this.rand.nextInt(10) == 0)) && (!placeIce || (Math.abs(i) != 2 && Math.abs(j) != 2 || this.rand.nextInt(14) == 0) && (Math.abs(i) != 1 && Math.abs(j) != 1 || this.rand.nextInt(6) == 0))) {
							if (placeSnow) {
								this.world.setBlockState(pos, Blocks.SNOW.getDefaultState());
							}

							if (placeIce) {
								this.world.setBlockState(posDown, Blocks.ICE.getDefaultState());
							}
						}
					}
				}
			}
		}

		if (!this.world.isRemote && this.ticksExisted % 40 == 0 && this.isAlive() && this.getHealth() < this.getMaxHealth() && this.isSnowingAt(this.getPosition())) {
			this.heal(1.0F);
		}
	}

	private boolean isSnowingAt(BlockPos position) {
		if (!this.world.isRaining()) {
			return false;
		} else if (!this.world.isSkyLightMax(position)) {
			return false;
		} else if (this.world.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
			return false;
		} else {
			Biome biome = this.world.getBiome(position);
			return biome.getPrecipitation() == Biome.RainType.SNOW && biome.func_225486_c(position) < 0.15F;
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.getLeashed()) {
			return;
		}

		PlayerEntity owner = this.getOwner();
		if (owner != null && owner.isAlive() && !owner.isSpectator()) {
			this.setHomePosAndDistance(owner.getPosition(), this.getAttackTarget() == null ? 8 : 16);
		} else if (this.detachHome()) {
			this.setHomePosAndDistance(BlockPos.ZERO, -1);
		}
	}

	public boolean isThrowing() {
		return this.isThrowing;
	}

	public int getThrowingTick() {
		return this.throwingTick;
	}

	private void setThrowing(boolean isThrowing) {
		this.isThrowing = isThrowing;
		this.throwingTick = 0;
		if (!this.world.isRemote && isThrowing) {
			this.world.setEntityState(this, (byte)0);
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 0) {
			this.setThrowing(true);
		} else {
			super.handleStatusUpdate(id);
			if (id == 2 || id == 33 || id == 36 || id == 37 || id == 44) {
				EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.ITEM_SNOWBALL, 30);
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getImmediateSource() instanceof SnowballEntity) {
			if (this.isAlive() && this.getHealth() < this.getMaxHealth()) {
				if (!this.world.isRemote) {
					this.heal(1.0F);
				}

				EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.HEART, 1);
			}

			return false;
		} else {
			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		if (!this.isThrowing && distanceFactor < 1.0F) {
			this.setThrowing(true);
		}
	}

	public static boolean canHarm(LivingEntity attacker, Entity target) {
		if (!(attacker instanceof MutantSnowGolemEntity) || attacker == target) {
			return false;
		} else if (target instanceof CreeperMinionEntity) {
			return !((CreeperMinionEntity)target).isTamed();
		} else if (target instanceof MobEntity) {
			return target instanceof IMob || ((MobEntity)target).getAttackTarget() == attacker;
		} else {
			return ((MutantSnowGolemEntity)attacker).getAttackTarget() == target;
		}
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getHeldItem(hand);
		if (itemStack.interactWithEntity(player, this, hand)) {
			return true;
		} else if ((this.getOwnerId() == null || player == this.getOwner()) && itemStack.getItem() != Items.SNOWBALL) {
			if (!this.world.isRemote) {
				this.setOwnerId(this.getOwnerId() == null ? player.getUniqueID() : null);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isShearable(ItemStack item, IWorldReader world, BlockPos pos) {
		return this.isAlive() && this.hasJackOLantern();
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IWorld world, BlockPos pos, int fortune) {
		if (!this.world.isRemote) {
			this.setJackOLantern(false);
		}

		return Collections.singletonList(new ItemStack(Items.JACK_O_LANTERN));
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
			this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
		}

		super.onDeath(cause);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("JackOLantern", this.hasJackOLantern());
		if (this.getOwnerId() != null) {
			compound.putUniqueId("OwnerUUID", this.getOwnerId());
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("Pumpkin") || compound.contains("JackOLantern")) {
			this.setJackOLantern(compound.getBoolean("Pumpkin") || compound.getBoolean("JackOLantern"));
		}

		if (compound.hasUniqueId("OwnerUUID")) {
			this.setOwnerId(compound.getUniqueId("OwnerUUID"));
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_MUTANT_SNOW_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_MUTANT_SNOW_GOLEM_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.BLOCK_SNOW_STEP, 0.15F, 1.0F);
	}

	class SwimJumpGoal extends Goal {
		private int jumpTick;
		private boolean waterReplaced;
		private BlockPos.MutableBlockPos prevPos;

		public SwimJumpGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP));
			navigator.setCanSwim(true);
		}

		@Override
		public boolean shouldExecute() {
			return isInWaterOrBubbleColumn();
		}

		@Override
		public void startExecuting() {
			this.prevPos = new BlockPos.MutableBlockPos(posX, getBoundingBox().minY - 1, posZ);
			setMotion(((rand.nextFloat() - rand.nextFloat()) * 0.9F), 1.5D, ((rand.nextFloat() - rand.nextFloat()) * 0.9F));
			attackEntityFrom(DamageSource.DROWN, 16.0F);
			setSwimJump(true);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.jumpTick > 0;
		}

		@Override
		public void tick() {
			--this.jumpTick;
			if (!this.waterReplaced && !isInWaterOrBubbleColumn() && this.jumpTick < 17 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, MutantSnowGolemEntity.this)) {
				this.prevPos.setY(this.getWaterSurfaceHeight(world, this.prevPos));
				if ((double)this.prevPos.getY() > posY) {
					return;
				}

				for (int x = -2; x <= 2; ++x) {
					for (int y = -1; y <= 1; ++y) {
						for (int z = -2; z <= 2; ++z) {
							if (y == 0 || Math.abs(x) != 2 && Math.abs(z) != 2) {
								BlockPos blockPos = this.prevPos.add(x, y, z);
								if (world.isAirBlock(blockPos) || world.hasWater(blockPos)) {
									if (y != 0) {
										if ((Math.abs(x) == 1 || Math.abs(z) == 1) && rand.nextInt(4) == 0) {
											continue;
										}
									} else if ((Math.abs(x) == 2 || Math.abs(z) == 2) && rand.nextInt(3) == 0) {
										continue;
									}

									world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
								}
							}
						}
					}
				}

				BlockPos topBlockPos = this.prevPos.up(2);
				if (world.isAirBlock(topBlockPos)) {
					world.setBlockState(topBlockPos, Blocks.ICE.getDefaultState());
				}

				this.waterReplaced = true;
			}
		}

		@Override
		public void resetTask() {
			this.jumpTick = 20;
			this.waterReplaced = false;
			this.prevPos = null;
			setSwimJump(false);
		}

		private int getWaterSurfaceHeight(World world, BlockPos coord) {
			int y = coord.getY();
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			while (true) {
				pos.setPos(coord.getX(), y, coord.getZ());
				if (world.hasWater(pos)) {
					y++;
					continue;
				}

				break;
			}

			return y;
		}
	}

	class ThrowIceGoal extends Goal {
		private LivingEntity attackTarget;

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && isThrowing;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return isThrowing && throwingTick < 20;
		}

		@Override
		public void tick() {
			getNavigator().clearPath();
			renderYawOffset = rotationYaw;

			if (throwingTick == 7) {
				ThrowableBlockEntity block = new ThrowableBlockEntity(MutantSnowGolemEntity.this, world);
				++block.posY;
				double x = this.attackTarget.posX - block.posX;
				double y = this.attackTarget.posY - block.posY;
				double z = this.attackTarget.posZ - block.posZ;
				double xz = Math.sqrt(x * x + z * z);
				block.shoot(x, y + xz * 0.4000000059604645D, z, 0.9F, 1.0F);
				world.addEntity(block);
			}
		}

		@Override
		public void resetTask() {
			this.attackTarget = null;
		}
	}
}