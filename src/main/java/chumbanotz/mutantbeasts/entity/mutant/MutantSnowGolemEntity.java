package chumbanotz.mutantbeasts.entity.mutant;

import java.util.EnumSet;
import java.util.Optional;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.projectile.MutantSnowGolemBlockEntity;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import chumbanotz.mutantbeasts.packet.SimpleAnimationPacket;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.MoveTowardsVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MutantSnowGolemEntity extends GolemEntity implements IRangedAttackMob {
	private static final DataParameter<Optional<BlockState>> ICE_BLOCK = EntityDataManager.createKey(MutantSnowGolemEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	public int throwTick;
	public boolean isThrowing;

	public MutantSnowGolemEntity(EntityType<? extends MutantSnowGolemEntity> type, World worldIn) {
		super(type, worldIn);
		this.ignoreFrustumCheck = true;
		this.navigator = new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantSnowGolemEntity.SwimJumpGoal());
		this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.1D, 30, 12.0F));
		this.goalSelector.addGoal(2, new MutantSnowGolemEntity.ThrowIceGoal());
		this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 1.1D));
		this.goalSelector.addGoal(4, new MoveTowardsVillageGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 1.0000001E-5F));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(7, new LookAtGoal(this, MobEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this, MutantSnowGolemEntity.class, IronGolemEntity.class, PlayerEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MobEntity.class, 10, true, false, (entity) -> {
			return entity instanceof IMob && (!(entity instanceof CreeperEntity) || !CreeperMinionEntity.IS_TAMED.test(entity) && ((CreeperEntity)entity).getAttackTarget() == this);
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
		this.dataManager.register(ICE_BLOCK, Optional.of(Blocks.ICE.getDefaultState()));
	}

	public BlockState getIceBlock() {
		return this.dataManager.get(ICE_BLOCK).orElse(Blocks.ICE.getDefaultState());
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.0F;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return target instanceof IMob;
	}

	@Override
	public void tick() {
		super.tick();
		float biomeTemp = this.world.getBiome(this.getPosition()).func_225486_c(this.getPosition());

		if (this.isThrowing) {
			++this.throwTick;
		}

		if (!this.world.isRemote && this.getAttackTarget() == null && this.throwTick > 20) {
			if (this.isThrowing) {
				this.sendPacket(false);
			}
		}

		if (biomeTemp > 1.2F && !this.isPotionActive(Effects.FIRE_RESISTANCE)) {
			if (this.rand.nextFloat() > Math.min(80.0F, this.getHealth()) * 0.01F) {
				this.world.setEntityState(this, (byte)4);
			}

			if (this.ticksExisted % 60 == 0) {
				this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
			}
		}

		if (this.getHealth() > 0.0F && biomeTemp < 0.5F && this.ticksExisted % 200 == 0 && this.getHealth() < this.getMaxHealth()) {
			this.heal(1.0F);
		}

		if (!this.world.isRemote && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			int x = MathHelper.floor(this.posX);
			int y = MathHelper.floor(this.getBoundingBox().minY);
			int z = MathHelper.floor(this.posZ);

			for (int i = -2; i <= 2; ++i) {
				for (int j = -2; j <= 2; ++j) {
					if (Math.abs(i) != 2 || Math.abs(j) != 2) {
						BlockPos blockpos = new BlockPos(x + i, y, z + j);
						BlockPos blockpos1 = new BlockPos(x + i, y - 1, z + j);
						BlockPos blockpos2 = new BlockPos(x + i, y + 1, z + j);

						boolean placeSnow = biomeTemp < 0.95F && this.world.isAirBlock(blockpos) && Blocks.SNOW.getDefaultState().isValidPosition(this.world, blockpos);
						boolean placeIce = this.world.getBlockState(blockpos1).getBlock() == Blocks.WATER;

						if (this.world.getBlockState(blockpos).getBlock() == Blocks.WATER) {
							this.world.setBlockState(blockpos, Blocks.ICE.getDefaultState());
						}

						if (this.world.getBlockState(blockpos2).getBlock() == Blocks.WATER) {
							this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
						}

						if ((!placeSnow || (Math.abs(i) != 2 && Math.abs(j) != 2 || this.rand.nextInt(20) == 0) && (Math.abs(i) != 1 && Math.abs(j) != 1 || this.rand.nextInt(10) == 0)) && (!placeIce || (Math.abs(i) != 2 && Math.abs(j) != 2 || this.rand.nextInt(14) == 0) && (Math.abs(i) != 1 && Math.abs(j) != 1 || this.rand.nextInt(6) == 0))) {
							if (placeSnow) {
								this.world.setBlockState(blockpos, Blocks.SNOW.getDefaultState());
							}

							if (placeIce) {
								this.world.setBlockState(blockpos1, Blocks.ICE.getDefaultState());
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 4) {
			this.world.addParticle(ParticleTypes.FALLING_WATER, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), this.posY - 0.15D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), 0.0D, 0.0D, 0.0D);
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	protected void updateLeashedState() {
		super.updateLeashedState();
		if (this.getLeashHolder() instanceof PlayerEntity) {
			if (this.detachHome()) {
				this.setHomePosAndDistance(BlockPos.ZERO, -1);
			}
		} else if (this.getLeashHolder() instanceof HangingEntity) {
			if (!this.detachHome()) {
				int i = (int)this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue();
				this.setHomePosAndDistance(((HangingEntity)this.getLeashHolder()).getHangingPosition(), i);
			}
		}
	}

	@Override
	public void setHomePosAndDistance(BlockPos pos, int distance) {
		if (!this.getLeashed()) {
			super.setHomePosAndDistance(pos, distance);
		}
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand) {
//		ItemStack stack = player.getHeldItem(hand);
//
//		for (Block block : new Block[] {Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE}) {
//			if (!this.world.isRemote && stack.getItem() == block.asItem() && this.getIceBlock() != block.getDefaultState()) {
//				this.dataManager.set(ICE_BLOCK, Optional.of(block.getDefaultState()));
//				stack.shrink(1);
//				return true;
//			}
//		}

		return super.processInteract(player, hand);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getImmediateSource() instanceof SnowballEntity) {
			if (this.getHealth() < this.getMaxHealth()) {
				this.heal(1.0F);
				this.spawnParticles(ParticleTypes.HEART, 1);
				this.spawnParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW.getDefaultState()), 10);
			}

			return true;
		} else {
			this.spawnParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW.getDefaultState()), 30);
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		if (!this.isThrowing) {
			this.sendPacket(true);
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		this.spawnParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW.getDefaultState()), 500);
		this.deathTime = 19;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		if (this.detachHome()) {
			compound.put("HomePosition", NBTUtil.writeBlockPos(this.getHomePosition()));
			compound.putInt("MaximumHomeDistance", (int)this.getMaximumHomeDistance());
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("HomePosition") && compound.contains("MaximumHomeDistance")) {
			this.setHomePosAndDistance(NBTUtil.readBlockPos(compound.getCompound("HomePosition")), compound.getInt("MaximumHomeDistance"));
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

	private void spawnParticles(IParticleData particleData, int amount) {
		for (int i = 0; i < amount; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particleData, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}

	private void sendPacket(boolean isAnimating) {
		if (!isAnimating) {
			this.throwTick = 0;
		}

		this.isThrowing = isAnimating;
		PacketHandler.INSTANCE.sendToServer(new SimpleAnimationPacket(0, this.getEntityId(), isAnimating));
	}

	class SwimJumpGoal extends Goal {
		private int jumpTick;
		private boolean waterReplaced;
		private BlockPos prevPos;

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
			this.prevPos = new BlockPos(MathHelper.floor(posX), MathHelper.floor(getBoundingBox().minY) - 1, MathHelper.floor(posZ));
			setMotion(((rand.nextFloat() - rand.nextFloat()) * 0.9F), 1.5D, ((rand.nextFloat() - rand.nextFloat()) * 0.9F));
			attackEntityFrom(DamageSource.DROWN, 16.0F);
			world.setEntityState(MutantSnowGolemEntity.this, (byte)6);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.jumpTick > 0;
		}

		@Override
		public void tick() {
			--this.jumpTick;
			if (!this.waterReplaced && !isInWaterOrBubbleColumn() && this.jumpTick < 17 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, MutantSnowGolemEntity.this)) {
				int i = this.prevPos.getY();
				i = getWaterSurfaceHeight(world, this.prevPos);
				if ((double)i > posY) {
					return;
				}

				for (int x = -2; x <= 2; ++x) {
					for (int y = -1; y <= 1; ++y) {
						for (int z = -2; z <= 2; ++z) {
							if (y == 0 || Math.abs(x) != 2 && Math.abs(z) != 2) {
								int posX = this.prevPos.getX() + x;
								int posY = this.prevPos.getY() + y;
								int posZ = this.prevPos.getZ() + z;
								Block block = world.getBlockState(new BlockPos(posX, posY, posZ)).getBlock();
								if (block == Blocks.AIR || block == Blocks.WATER) {
									if (y != 0) {
										if ((Math.abs(x) == 1 || Math.abs(z) == 1) && rand.nextInt(4) == 0) {
											continue;
										}
									} else if ((Math.abs(x) == 2 || Math.abs(z) == 2) && rand.nextInt(3) == 0) {
										continue;
									}

									world.setBlockState(new BlockPos(posX, posY, posZ), Blocks.ICE.getDefaultState());
								}
							}
						}
					}
				}

				Block topBlock = world.getBlockState(this.prevPos.up(2)).getBlock();
				if (topBlock == Blocks.AIR) {
					world.setBlockState(this.prevPos.up(2), Blocks.ICE.getDefaultState());
				}

				this.waterReplaced = true;
			}
		}

		@Override
		public void resetTask() {
			this.jumpTick = 20;
			this.waterReplaced = false;
			// setSwimJump(false);
		}

		int getWaterSurfaceHeight(World world, BlockPos coord) {
			int y = coord.getY();
			while (true) {
				Block block = world.getBlockState(coord.up()).getBlock();
				if (block == Blocks.WATER) {
					y++;
					continue;
				}
				break;
			}
			return y;
		}
	}

	class ThrowIceGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			return getAttackTarget() == null ? false : isThrowing;
		}

		@Override
		public void startExecuting() {
			getNavigator().clearPath();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return isThrowing && throwTick < 20;
		}

		@Override
		public void tick() {
			renderYawOffset = rotationYaw;

			if (getAttackTarget() != null && throwTick == 7) {
				MutantSnowGolemBlockEntity block = new MutantSnowGolemBlockEntity(MutantSnowGolemEntity.this, world);
				++block.posY;
				double x = getAttackTarget().posX - block.posX;
				double y = getAttackTarget().posY - block.posY;
				double z = getAttackTarget().posZ - block.posZ;
				double xz = Math.sqrt(x * x + z * z);
				block.shoot(x, y + xz * 0.4000000059604645D, z, 0.9F, 1.0F);
				world.addEntity(block);
			}
		}

		@Override
		public void resetTask() {
			sendPacket(false);
		}
	}
}