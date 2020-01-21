package chumbanotz.mutantbeasts.entity.mutant;

import java.util.EnumSet;

import chumbanotz.mutantbeasts.entity.ai.goal.FleeRainGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.Block;
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
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveTowardsVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantSnowGolemEntity extends GolemEntity implements IRangedAttackMob {
	private static final DataParameter<Byte> PUMPKIN_EQUIPPED = EntityDataManager.createKey(MutantSnowGolemEntity.class, DataSerializers.BYTE);
	private boolean isThrowing;
	private int throwingTick;

	public MutantSnowGolemEntity(EntityType<? extends MutantSnowGolemEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantSnowGolemEntity.SwimJumpGoal());
		this.goalSelector.addGoal(1, new FleeRainGoal(this, 1.1D));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.1D, 30, 12.0F));
		this.goalSelector.addGoal(3, new MutantSnowGolemEntity.ThrowIceGoal());
		this.goalSelector.addGoal(4, new MoveTowardsVillageGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 1.0000001E-5F));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(7, new LookAtGoal(this, MobEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MobEntity.class, 10, true, false, entity -> {
			return entity instanceof IMob && (!(entity instanceof CreeperEntity) || ((CreeperEntity)entity).getAttackTarget() == this);
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
		this.dataManager.register(PUMPKIN_EQUIPPED, (byte)16);
	}

	public boolean isPumpkinEquipped() {
		return (this.dataManager.get(PUMPKIN_EQUIPPED) & 16) != 0;
	}

	public void setPumpkinEquipped(boolean pumpkinEquipped) {
		byte b0 = this.dataManager.get(PUMPKIN_EQUIPPED);
		this.dataManager.set(PUMPKIN_EQUIPPED, pumpkinEquipped ? (byte)(b0 | 16) : (byte)(b0 & -17));
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn).setAvoidRain(true);
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
	protected void frostWalk(BlockPos pos) {
	}

	@Override
	public void tick() {
		super.tick();
		this.setPathPriority(PathNodeType.WATER, net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? 16.0F : -1.0F);
		float biomeTemp = this.world.getBiome(this.getPosition()).func_225486_c(this.getPosition());

		if (this.isThrowing) {
			this.throwingTick++;
		}

		if (this.isWet() && this.ticksExisted % 20 == 0) {
			this.attackEntityFrom(DamageSource.DROWN, 1.0F);
		}

		if (biomeTemp > 1.2F && !this.isPotionActive(Effects.FIRE_RESISTANCE)) {
			if (this.rand.nextFloat() > Math.min(80.0F, this.getHealth()) * 0.01F) {
				this.world.setEntityState(this, (byte)4);
			}

			if (this.ticksExisted % 60 == 0) {
				this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
			}
		}

		if (this.isAlive() && biomeTemp < 0.5F && this.ticksExisted % 200 == 0 && this.getHealth() < this.getMaxHealth()) {
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
	protected boolean processInteract(PlayerEntity player, Hand hand) {
		if (player.getHeldItem(hand).getItem() == Items.SHEARS && this.isPumpkinEquipped()) {
			if (!this.world.isRemote) {
				this.setPumpkinEquipped(false);
			}

			player.getHeldItem(hand).damageItem(1, player, e -> e.sendBreakAnimation(hand));
			return true;
		}

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isThrowing() {
		return this.isThrowing;
	}

	@OnlyIn(Dist.CLIENT)
	public int getThrowingTick() {
		return this.throwingTick;
	}

	private void setThrowing(boolean isThrowing) {
		this.isThrowing = isThrowing;
		this.throwingTick = 0;
		this.world.setEntityState(this, isThrowing ? (byte)1 : (byte)0);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 0 || id == 1) {
			this.isThrowing = id == 1 ? true : false;
			this.throwingTick = 0;
		} else if (id == 4) {
			this.world.addParticle(ParticleTypes.FALLING_WATER, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), this.posY - 0.15D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 1.5F) - (double)this.getWidth(), 0.0D, 0.0D, 0.0D);
		} else if (id == 5 || id == 6 || id == 7) {
			EntityUtil.spawnParticlesAtEntity(this, id == 5 ? ParticleTypes.HEART : new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW.getDefaultState()), id == 5 ? 1 : id == 6 ? 10 : 30);
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getImmediateSource() instanceof SnowballEntity) {
			if (this.getHealth() < this.getMaxHealth()) {
				this.heal(1.0F);
				this.world.setEntityState(this, (byte)5);
				this.world.setEntityState(this, (byte)6);
			}

			return false;
		} else {
			boolean flag = super.attackEntityFrom(source, amount);
			if (flag && amount > 0.0F) {
				this.world.setEntityState(this, (byte)7);
			}

			return flag;
		}
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		if (!this.isThrowing) {
			this.setThrowing(true);
		}
	}

	public static boolean canHarm(LivingEntity attacker, Entity target) {
		if (!(attacker instanceof MutantSnowGolemEntity) || attacker == target) {
			return false;
		} else if (target instanceof MobEntity) {
			return target instanceof IMob || ((MobEntity)target).getAttackTarget() == attacker;
		} else {
			return ((MutantSnowGolemEntity)attacker).getAttackTarget() == target;
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("Pumpkin", this.isPumpkinEquipped());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("Pumpkin")) {
			this.setPumpkinEquipped(compound.getBoolean("Pumpkin"));
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
			this.prevPos = new BlockPos(posX, getBoundingBox().minY - 1, posZ);
			setMotion(((rand.nextFloat() - rand.nextFloat()) * 0.9F), 1.5D, ((rand.nextFloat() - rand.nextFloat()) * 0.9F));
			attackEntityFrom(DamageSource.DROWN, 16.0F);
			// world.setEntityState(MutantSnowGolemEntity.this, (byte)6);
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
		private LivingEntity attackTarget;

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && isThrowing;
		}

		@Override
		public void startExecuting() {
			navigator.clearPath();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return isThrowing && throwingTick < 20;
		}

		@Override
		public void tick() {
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
			setThrowing(false);
		}
	}
}