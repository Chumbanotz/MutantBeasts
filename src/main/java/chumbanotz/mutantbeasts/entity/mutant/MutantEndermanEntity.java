package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import chumbanotz.mutantbeasts.packet.HeldBlockPacket;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantEndermanEntity extends MonsterEntity {
	private static final DataParameter<Optional<BlockPos>> TELEPORT_POSITION = EntityDataManager.createKey(MutantEndermanEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private static final DataParameter<Byte> ACTIVE_ARM = EntityDataManager.createKey(MutantEndermanEntity.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> CLONE_STATE = EntityDataManager.createKey(MutantEndermanEntity.class, DataSerializers.BYTE);
	public static final int MAX_DEATH_TIME = 280;
	public static final byte MELEE_ATTACK = 4, THROW_ATTACK = 5, STARE_ATTACK = 6, TELEPORT_ATTACK = 7, SCREAM_ATTACK = 8, CLONE_ATTACK = 9, TELESMASH_ATTACK = 10, DEATH_ATTACK = 11;
	private int attackID;
	private int attackTick;
	private int prevArmScale;
	private int armScale;
	public int hasTarget;
	private int screamDelayTick;
	public final int[] heldBlock = new int[5];
	public final int[] heldBlockTick = new int[5];
	private boolean triggerThrowBlock;
	private int blockSearchTick;
	private List<MutantEndermanEntity> cloneList;
	private List<Entity> deathEntities;
	private int dirty = -1;
	private MutantEndermanEntity cloner;
	private DamageSource deathCause = DamageSource.GENERIC;

	public MutantEndermanEntity(EntityType<? extends MutantEndermanEntity> type, World worldIn) {
		super(type, worldIn);
		this.experienceValue = 40;
		this.stepHeight = 2.5F;
		this.cloneList = new ArrayList<>();
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MutantEndermanEntity.MeleeGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.ThrowHeldBlockGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.ForcedLookGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.TeleportGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.ScreamGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.CloneGoal());
		this.goalSelector.addGoal(1, new MutantEndermanEntity.TeleSmashGoal());
		this.goalSelector.addGoal(2, new MBMeleeAttackGoal(this, 1.2D).setMaxAttackTick(15));
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.2D));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 0.0F));
		this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new MutantEndermanEntity.TargetLookerGoal(this));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EndermiteEntity.class, 10, true, false, e -> ((EndermiteEntity)e).isSpawnedByPlayer()));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(96.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(TELEPORT_POSITION, Optional.empty());
		this.dataManager.register(ACTIVE_ARM, (byte)0);
		this.dataManager.register(CLONE_STATE, (byte)0);
	}

	public BlockPos getTeleportPosition() {
		return this.dataManager.get(TELEPORT_POSITION).orElse(null);
	}

	private void setTeleportPosition(BlockPos pos) {
		this.dataManager.set(TELEPORT_POSITION, Optional.ofNullable(pos));
	}

	public int getActiveArm() {
		return this.dataManager.get(ACTIVE_ARM);
	}

	private void setActiveArm(int armId) {
		this.dataManager.set(ACTIVE_ARM, (byte)armId);
	}

	public boolean isClone() {
		return this.dataManager.get(CLONE_STATE) > 0;
	}

	public boolean isDecoyClone() {
		return this.dataManager.get(CLONE_STATE) > 1;
	}

	private void setCloneState(int newState) {
		byte currentState = this.dataManager.get(CLONE_STATE);
		if (currentState == newState) {
			return;
		}

		if (currentState == 1 && newState == 0) {
			for (MutantEndermanEntity clone : this.cloneList) {
				if (clone != null && (clone.isAlive() || clone.isAddedToWorld())) {
					clone.onDeathUpdate();
				}
			}

			this.cloneList.clear();
			this.spawnTeleportParticles(true);
			this.navigator.clearPath();
		}

		this.dataManager.set(CLONE_STATE, (byte)newState);
		this.setAttackID(newState > 0 ? CLONE_ATTACK : 0);
	}

	@OnlyIn(Dist.CLIENT)
	public int getAttackID() {
		return this.attackID;
	}

	@OnlyIn(Dist.CLIENT)
	public int getAttackTick() {
		return this.attackTick;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return this.isClone() ? 2.55F : 3.9F;
	}

	@Override
	public EntitySize getSize(Pose poseIn) {
		return this.isClone() ? EntitySize.flexible(0.6F, 2.9F) : super.getSize(poseIn);
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn).setAvoidRain(true);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (CLONE_STATE.equals(key)) {
			this.recalculateSize();
		}

		if (TELEPORT_POSITION.equals(key) && this.getTeleportPosition() != null && this.world.isRemote) {
			this.attackID = TELEPORT_ATTACK;
			this.attackTick = 0;
			this.spawnBigParticles();
		}
	}

	@Override
	public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
		super.setAttackTarget(entitylivingbaseIn);
		this.setAggroed(entitylivingbaseIn != null);
	}

	@OnlyIn(Dist.CLIENT)
	public float getArmScale(float partialTicks) {
		return MathHelper.lerp(partialTicks, (float)this.prevArmScale, (float)this.armScale) / 10.0F;
	}

	private void updateTargetTick() {
		this.prevArmScale = this.armScale;
		if (this.isAggressive()) {
			this.hasTarget = 20;
		}

		boolean emptyHanded = true;

		int i;
		for (i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] > 0) {
				emptyHanded = false;
			}

			if (this.hasTarget > 0) {
				if (this.heldBlock[i] > 0) {
					this.heldBlockTick[i] = Math.min(10, this.heldBlockTick[i] + 1);
				}
			} else {
				this.heldBlockTick[i] = Math.max(0, this.heldBlockTick[i] - 1);
			}
		}

		if (this.hasTarget > 0) {
			this.armScale = Math.min(10, this.armScale + 1);
		} else if (emptyHanded) {
			this.armScale = Math.max(0, this.armScale - 1);
		} else if (!this.world.isRemote) {
			for (i = 1; i < this.heldBlock.length; ++i) {
				if (this.heldBlock[i] != 0 && this.heldBlockTick[i] == 0) {
					int x = MathHelper.floor(this.posX - 1.5D + this.rand.nextDouble() * 4.0D);
					int y = MathHelper.floor(this.posY - 0.5D + this.rand.nextDouble() * 2.5D);
					int z = MathHelper.floor(this.posZ - 1.5D + this.rand.nextDouble() * 4.0D);
					BlockPos blockPos = new BlockPos(x, y, z);
					if (this.world.isAirBlock(blockPos) && !this.world.isAirBlock(blockPos.down()) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
						this.world.setBlockState(blockPos, Block.getStateById(this.heldBlock[i]));
						this.sendHoldBlock(i, 0);
					} else {
						this.triggerThrowBlock = true;
					}
				}
			}
		}

		this.hasTarget = Math.max(0, this.hasTarget - 1);
	}

	private void updateScreamEntities() {
		this.screamDelayTick = Math.max(0, this.screamDelayTick - 1);
		if (this.attackID == SCREAM_ATTACK && this.attackTick >= 40 && this.attackTick <= 160) {
			for (LivingEntity livingEntity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(20.0D, 12.0D, 20.0D), EndersoulFragmentEntity.IS_VALID_TARGET)) {
				if (this.getDistanceSq(livingEntity) < 400.0D) {
					livingEntity.rotationPitch += (this.rand.nextFloat() - 0.3F) * 6.0F;
				}
			}
		}
	}

	private void spawnTeleportParticles(boolean clone) {
		if (clone && !this.isSilent()) {
			this.world.playSound(null, this.posX, this.posY + (double)this.getHeight() / 2.0D, this.posZ, MBSoundEvents.ENTITY_MUTANT_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
		}

		this.world.setEntityState(this, clone ? (byte)12 : (byte)1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 1) {
			this.spawnBigParticles();
		} else if (id == 12) {
			EntityUtil.spawnLargePortalParticles(this, 256, 1.8F, false);
		} else if (id == 0 || id >= 4 && id <= 11) {
			this.attackID = id;
			this.attackTick = 0;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public void livingTick() {
		super.livingTick();

		if (this.attackID != 0) {
			++this.attackTick;
		}

		if (this.isAlive() && this.ticksExisted % 100 == 0 && this.dimension == DimensionType.THE_END && this.getHealth() < this.getMaxHealth()) {
			this.heal(2.0F);
		}

		this.updateTargetTick();
		this.updateScreamEntities();
		double h = this.attackID != DEATH_ATTACK ? (double)this.getHeight() : (double)(this.getHeight() + 1.0F);
		double w = this.attackID != DEATH_ATTACK ? (double)this.getWidth() : (double)(this.getWidth() * 1.5F);
		boolean targetBlind = this.getAttackTarget() != null && this.getAttackTarget().isPotionActive(Effects.BLINDNESS);
		if (!targetBlind && !this.isClone()) {
			for (int i = 0; i < 3; ++i) {
				double x = this.posX + (this.rand.nextDouble() - 0.5D) * w;
				double y = this.posY + this.rand.nextDouble() * h - 0.25D;
				double z = this.posZ + (this.rand.nextDouble() - 0.5D) * w;
				this.world.addParticle(ParticleTypes.PORTAL, x, y, z, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
			}
		}
	}

	private void updateDirtyHands() {
		if (this.dirty >= 0) {
			++this.dirty;
		}

		if (this.dirty >= 8) {
			this.dirty = -1;

			for (int i = 1; i < this.heldBlock.length; ++i) {
				if (this.heldBlock[i] > 0) {
					this.sendHoldBlock(i, this.heldBlock[i]);
				}
			}
		}

		if (this.isDecoyClone() && this.cloner != null && this.getAttackTarget() != this.cloner.getAttackTarget()) {
			this.setAttackTarget(this.cloner.getAttackTarget());
		}
	}

	private void updateBlockFrenzy() {
		this.blockSearchTick = Math.max(0, this.blockSearchTick - 1);
		if (this.getAttackTarget() != null && this.attackID == 0) {
			if (this.blockSearchTick == 0 && this.rand.nextInt(600) == 0) {
				this.blockSearchTick = 200 + this.rand.nextInt(80);
			}

			if (this.blockSearchTick > 0 && this.rand.nextInt(8) == 0) {
				int x = MathHelper.floor(this.posX - 2.5D + this.rand.nextDouble() * 5.0D);
				int y = MathHelper.floor(this.posY - 0.5D + this.rand.nextDouble() * 3.0D);
				int z = MathHelper.floor(this.posZ - 2.5D + this.rand.nextDouble() * 5.0D);
				int index = this.getFavorableHand();
				BlockPos pos = new BlockPos(x, y, z);
				if (index != -1 && this.world.getBlockState(pos).isIn(MutantBeasts.THROWABLE_BLOCKS)) {
					this.sendHoldBlock(index, Block.getStateId(this.world.getBlockState(pos)));
					if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
						this.world.removeBlock(pos, false);
					}
				}
			}
		}
	}

	private void updateTeleport() {
		Entity entity = this.getAttackTarget();
		this.teleportByChance(entity == null ? 1600 : 800, entity);
		if (entity != null && (this.getDistanceSq(entity) > 1024.0D || this.getDistanceSq(entity) > 16.0D && !this.hasPath())) {
			this.teleportByChance(10, entity);
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.isInWaterRainOrBubbleColumn() && this.ticksExisted % 100 == 0 && !this.isClone()) {
			this.attackEntityFrom(DamageSource.DROWN, 1.0F);
		}

		this.updateDirtyHands();
		this.updateBlockFrenzy();
		this.updateTeleport();
	}

	private int getAvailableHand() {
		List<Integer> list = new ArrayList<>();

		for (int i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] == 0) {
				list.add(i);
			}
		}

		if (list.isEmpty()) {
			return -1;
		} else {
			return list.get(this.rand.nextInt(list.size()));
		}
	}

	private int getFavorableHand() {
		List<Integer> outer = new ArrayList<>();
		List<Integer> inner = new ArrayList<>();

		for (int i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] == 0) {
				if (i <= 2) {
					outer.add(i);
				} else {
					inner.add(i);
				}
			}
		}

		if (outer.isEmpty() && inner.isEmpty()) {
			return -1;
		} else if (!outer.isEmpty()) {
			return outer.get(this.rand.nextInt(outer.size()));
		} else {
			return inner.get(this.rand.nextInt(inner.size()));
		}
	}

	private int getThrowingHand() {
		List<Integer> outer = new ArrayList<>();
		List<Integer> inner = new ArrayList<>();

		for (int i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] != 0) {
				if (i <= 2) {
					outer.add(i);
				} else {
					inner.add(i);
				}
			}
		}

		if (outer.isEmpty() && inner.isEmpty()) {
			return -1;
		} else if (!inner.isEmpty()) {
			return inner.get(this.rand.nextInt(inner.size()));
		} else {
			return outer.get(this.rand.nextInt(outer.size()));
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (!this.world.isRemote && this.attackID == 0) {
			int i = this.getAvailableHand();
			if (!this.teleportByChance(6, entityIn)) {
				if (i != -1) {
					boolean allHandsFree = this.heldBlock[1] == 0 && this.heldBlock[2] == 0;
					if (allHandsFree && this.rand.nextInt(10) == 0) {
						this.setCloneState(1);
					} else if (allHandsFree && this.rand.nextInt(7) == 0) {
						this.setAttackID(TELESMASH_ATTACK);
					} else {
						this.setActiveArm(i);
						this.setAttackID(MELEE_ATTACK);
					}
				} else {
					this.triggerThrowBlock = true;
				}
			}
		}

		if (this.isClone()) {
			boolean flag = super.attackEntityAsMob(entityIn);
			if (!this.world.isRemote && this.isDecoyClone() ? this.rand.nextInt(3) != 0 : this.rand.nextInt(2) == 0) {
				double x = entityIn.posX + (double)((this.rand.nextFloat() - 0.5F) * 24.0F);
				double z = entityIn.posZ + (double)((this.rand.nextFloat() - 0.5F) * 24.0F);
				double y = entityIn.posY + (double)this.rand.nextInt(5) + 4.0D;
				this.teleportTo(x, y, z);
			}

			if (flag) {
				this.heal(2.0F);
			}

			return flag;
		}

		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else if (!(source.getTrueSource() instanceof EnderDragonEntity) && !(source.getTrueSource() instanceof MutantEndermanEntity)) {
			if ((this.attackID == TELEPORT_ATTACK || this.attackID == SCREAM_ATTACK) && source != DamageSource.OUT_OF_WORLD) {
				return false;
			} else if (!this.world.isRemote) {
				if (this.isClone()) {
					if (this.isDecoyClone()) {
						amount = 1.0F;
						this.onDeathUpdate();
					} else {
						this.setCloneState(0);
					}
				} else {
					Entity entity = source.getTrueSource();
					if (this.attackID == STARE_ATTACK) {
						this.attackTick = 100;
					}

					boolean betterDodge = entity == null;

					if (source.isProjectile() || source == DamageSource.FALL) {
						betterDodge = true;
					}

					if (this.teleportByChance(betterDodge ? 3 : 6, entity) && source != DamageSource.OUT_OF_WORLD) {
						if (entity != null && entity instanceof LivingEntity) {
							this.setRevengeTarget((LivingEntity)entity);
						}

						return false;
					}

					boolean betterTeleport = source == DamageSource.DROWN || source == DamageSource.LIGHTNING_BOLT;
					this.teleportByChance(betterTeleport ? 3 : 5, entity);
				}
			}

			return super.attackEntityFrom(source, amount);
		} else {
			return false;
		}
	}

	@Override
	public boolean addPotionEffect(EffectInstance effectInstanceIn) {
		return !this.isClone() && super.addPotionEffect(effectInstanceIn);
	}

	private boolean teleportByChance(int chance, Entity entity) {
		if (this.attackID != 0 && !this.isClone()) {
			return false;
		} else {
			if (this.rand.nextInt(Math.max(1, chance)) == 0) {
				return entity == null ? this.teleportRandomly() : this.teleportToEntity(entity);
			} else {
				return false;
			}
		}
	}

	private boolean teleportRandomly() {
		if (this.attackID != 0 && !this.isClone()) {
			return false;
		} else {
			double radius = 24.0D;
			double x = this.posX + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			double y = this.posY + (double)this.rand.nextInt((int)radius * 2) - radius;
			double z = this.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			return this.teleportTo(x, y, z);
		}
	}

	private boolean teleportToEntity(Entity entity) {
		if (this.attackID != 0 && !this.isClone()) {
			return false;
		} else {
			double x = 0.0D;
			double y = 0.0D;
			double z = 0.0D;
			double radius = 16.0D;
			if (this.getDistanceSq(entity) < 100.0D) {
				x = entity.posX + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
				y = entity.posY + this.rand.nextDouble() * radius;
				z = entity.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			} else {
				Vec3d vec = new Vec3d(this.posX - entity.posX, this.getBoundingBox().minY + (double)this.getHeight() / 2.0D - entity.posY + (double)entity.getEyeHeight(), this.posZ - entity.posZ);
				vec = vec.normalize();
				x = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec.x * radius;
				y = this.posY + (double)this.rand.nextInt(8) - vec.y * radius;
				z = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec.z * radius;
			}

			return this.teleportTo(x, y, z);
		}
	}

	private boolean teleportTo(double targetX, double targetY, double targetZ) {
		if (this.isAIDisabled() || !this.isAlive()) {
			return false;
		} else if (this.isClone()) {
			boolean flag = EntityUtil.teleportTo(this, targetX, targetY, targetZ);
			if (flag && !this.isSilent()) {
				this.world.playSound(this.prevPosX, this.prevPosY, this.prevPosZ, MBSoundEvents.ENTITY_MUTANT_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F, false);
				this.playSound(MBSoundEvents.ENTITY_MUTANT_ENDERMAN_TELEPORT, 1.0F, 1.0F);
			}

			return flag;
		} else if (this.attackID == 0) {
			this.attackID = TELEPORT_ATTACK;
			double oldX = this.posX;
			double oldY = this.posY;
			double oldZ = this.posZ;
			double newY = targetY;
			BlockPos pos = new BlockPos(targetX, targetY, targetZ);
			boolean success = false;
			if (this.world.isBlockPresent(pos)) {
				boolean temp = false;

				while (!temp && pos.getY() > 0) {
					BlockPos posDown = pos.down();
					if (this.world.getBlockState(posDown).getMaterial().blocksMovement()) {
						temp = true;
					} else {
						--newY;
						pos = posDown;
					}
				}

				if (temp) {
					this.setPosition(targetX, newY, targetZ);
					if (this.world.isCollisionBoxesEmpty(this, this.getBoundingBox()) && !this.world.containsAnyLiquid(this.getBoundingBox())) {
						success = true;
					}
				}
			}

			this.setPosition(oldX, oldY, oldZ);
			if (!success) {
				this.attackID = 0;
				return false;
			} else {
				this.setTeleportPosition(pos);
				return true;
			}
		} else {
			return false;
		}
	}

	public static void teleportAttack(LivingEntity attacker) {
		double r = 3.0D;
		int duration = 140 + attacker.getRNG().nextInt(60);
		DamageSource damageSource = DamageSource.causeMobDamage(attacker);

		if (attacker instanceof PlayerEntity) {
			r = 2.0D;
			duration = 100;
			damageSource = DamageSource.causePlayerDamage((PlayerEntity)attacker);
		}

		for (Entity entity : attacker.world.getEntitiesInAABBexcluding(attacker, attacker.getBoundingBox().grow(r), EndersoulFragmentEntity.IS_VALID_TARGET)) {
			if (entity instanceof LivingEntity) {
				LivingEntity living = (LivingEntity)entity;
				living.attackEntityFrom(damageSource, 4.0F);
				if (attacker.getRNG().nextInt(3) == 0) {
					living.addPotionEffect(new EffectInstance(Effects.BLINDNESS, duration));
				}

				double x = entity.posX - attacker.posX;
				double z = entity.posZ - attacker.posZ;
				double signX = x / Math.abs(x);
				double signZ = z / Math.abs(z);
				living.setMotion((r * signX * 2.0D - x) * 0.20000000298023224D, 0.20000000298023224D, (r * signZ * 2.0D - z) * 0.20000000298023224D);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnBigParticles() {
		int temp = 256;
		if (this.attackID == TELEPORT_ATTACK) {
			temp *= 2;
		}

		for (int i = 0; i < temp; ++i) {
			float f = (this.rand.nextFloat() - 0.5F) * 1.8F;
			float f1 = (this.rand.nextFloat() - 0.5F) * 1.8F;
			float f2 = (this.rand.nextFloat() - 0.5F) * 1.8F;
			boolean flag = i < temp / 2;
			if (this.attackID != TELEPORT_ATTACK) {
				flag = true;
			}

			boolean death = this.attackID != DEATH_ATTACK;
			double h = death ? (double)this.getHeight() : (double)(this.getHeight() + 1.0F);
			double w = death ? (double)this.getWidth() : (double)(this.getWidth() * 1.5F);
			double tempX = (flag ? this.posX : (double)this.getTeleportPosition().getX()) + (this.rand.nextDouble() - 0.5D) * w;
			double tempY = (flag ? this.posY : (double)this.getTeleportPosition().getY()) + (this.rand.nextDouble() - 0.5D) * h + (double)(death ? 1.5F : 0.5F);
			double tempZ = (flag ? this.posZ : (double)this.getTeleportPosition().getZ()) + (this.rand.nextDouble() - 0.5D) * w;
			this.world.addParticle(MBParticleTypes.LARGE_PORTAL, tempX, tempY, tempZ, (double)f, (double)f1, (double)f2);
		}
	}

	private void createClone(double x, double y, double z) {
		MutantEndermanEntity clone = MBEntityType.MUTANT_ENDERMAN.create(this.world);
		clone.setPosition(this.posX, this.posY, this.posZ);
		clone.cloner = this;
		clone.experienceValue = this.rand.nextInt(2);
		clone.stepHeight = 1.0F;
		EntityUtil.copyNBT(this, clone, false);
		clone.setCloneState(2);
		clone.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		if (clone.teleportTo(x, y, z)) {
			this.world.addEntity(clone);
			this.cloneList.add(clone);
		}
	}

	private MutantEndermanEntity getRandomClone() {
		if (this.cloneList.isEmpty()) {
			return this;
		} else {
			return this.cloneList.get(this.rand.nextInt(this.cloneList.size()));
		}
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return false;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	protected void constructKnockBackVector(LivingEntity livingEntity) {
		if (!this.isClone()) {
			livingEntity.applyEntityCollision(this);
			livingEntity.velocityChanged = true;
		}
	}

	@Override
	public Team getTeam() {
		return this.isDecoyClone() && this.cloner != null ? this.cloner.getTeam() : super.getTeam();
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (this.isDecoyClone()) {
			if (entityIn == this.cloner) {
				return true;
			}

			if (this.cloner != null) {
				return this.cloner.isOnSameTeam(entityIn);
			}
		}

		return super.isOnSameTeam(entityIn);
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (this.isDecoyClone()) {
			return;
		}

		if (!this.dead) {
			this.dead = true;
			EntityUtil.onDeath(this, cause);
		}

		if (!this.world.isRemote) {
			this.deathCause = cause;
			if (this.recentlyHit > 0) {
				this.recentlyHit += MAX_DEATH_TIME;
			}
		}
	}

	@Override
	protected void onDeathUpdate() {
		if (this.isDecoyClone()) {
			this.spawnTeleportParticles(true);
			EntityUtil.dropExperience(this, this.recentlyHit, this::getExperiencePoints, this.attackingPlayer);
			this.remove();
		} else {
			++this.deathTime;
			this.setMotion(0.0D, Math.min(this.getMotion().y, 0.0D), 0.0D);

			if (this.attackID != DEATH_ATTACK) {
				this.setAttackID(DEATH_ATTACK);
			}

			if (this.deathTime == 80) {
				this.playSound(MBSoundEvents.ENTITY_MUTANT_ENDERMAN_DEATH, 5.0F, this.getSoundPitch());
			}

			if (!this.world.isRemote) {
				if (this.deathTime >= 60 && this.deathTime < 80 && this.deathEntities == null) {
					this.deathEntities = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().grow(10.0D, 8.0D, 10.0D), EndersoulFragmentEntity.IS_VALID_TARGET);
				}

				if (this.deathTime >= 60 && this.rand.nextInt(3) != 0) {
					EndersoulFragmentEntity orb = new EndersoulFragmentEntity(this.world, this);
					orb.setPosition(this.posX, this.posY + 3.8D, this.posZ);
					orb.setMotion((double)((this.rand.nextFloat() - 0.5F) * 1.5F), (double)((this.rand.nextFloat() - 0.5F) * 1.5F), (double)((this.rand.nextFloat() - 0.5F) * 1.5F));
					this.world.addEntity(orb);
				}

				if (this.deathTime >= 80 && this.deathTime < MAX_DEATH_TIME - 20 && this.deathEntities != null) {
					for (int i = 0; i < this.deathEntities.size(); i++) {
						Entity entity = this.deathEntities.get(i);
						if (entity.fallDistance > 4.5F) {
							entity.fallDistance = 4.5F;
						}

						if (this.getDistanceSq(entity) > 64.0D) {
							if (EndersoulFragmentEntity.isProtected(entity) || !entity.isAlive()) {
								this.deathEntities.remove(entity);
							} else {
								double x = this.posX - entity.posX;
								double y = entity.getMotion().y;
								double z = this.posZ - entity.posZ;
								double d = Math.sqrt(x * x + z * z);

								if (this.posY + 4.0D > entity.posY) {
									y = Math.max(entity.getMotion().y, 0.4000000059604645D);
								}

								entity.setMotion(0.800000011920929D * x / d, y, 0.800000011920929D * z / d);
								EntityUtil.sendPlayerVelocityPacket(entity);
							}
						}
					}
				}

				if (this.deathTime >= 100 && this.deathTime < 150 && this.deathTime % 6 == 0) {
					this.spawnDrops(this.deathCause);
				}
			}

			if (this.deathTime >= MAX_DEATH_TIME) {
				EntityUtil.dropExperience(this, this.recentlyHit, this::getExperiencePoints, this.attackingPlayer);
				this.remove();
			}
		}
	}

	@Override
	public ItemEntity entityDropItem(ItemStack stack) {
		return this.entityDropItem(stack, this.deathTime > 0 ? 3.84F : 0.0F);
	}

	@Override
	public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
		if (this.rand.nextInt(3) == 0) {
			return false;
		} else if (worldIn.getDimension().getType() == DimensionType.THE_END && this.rand.nextInt(2600) != 0) {
			return false;
		} else {
			return super.canSpawn(worldIn, spawnReasonIn);
		}
	}

	private void setAttackID(int id) {
		this.attackID = id;
		this.attackTick = 0;
		this.world.setEntityState(this, (byte)id);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putByte("CloneState", this.dataManager.get(CLONE_STATE));
		compound.putShort("ScreamDelay", (short)this.screamDelayTick);
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		byte b0 = compound.getByte("CloneState");
		if (b0 < 2) {
			this.setCloneState(0);
		} else {
			this.remove();
		}

		this.screamDelayTick = compound.getShort("ScreamDelay");
	}

	@Override
	public int getTalkInterval() {
		return 200;
	}

	@Override
	public void playAmbientSound() {
		if (this.attackID != SCREAM_ATTACK && !this.isClone()) {
			this.playSound(this.getAmbientSound(), 1.5F, this.getSoundPitch());
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_MUTANT_ENDERMAN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return this.isDecoyClone() ? null : MBSoundEvents.ENTITY_MUTANT_ENDERMAN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	public void sendHoldBlock(int blockIndex, int blockId) {
		if (!this.world.isRemote) {
			this.heldBlock[blockIndex] = blockId;
			this.heldBlockTick[blockIndex] = 0;
			PacketHandler.sendToAllTracking(new HeldBlockPacket(this, blockId, blockIndex), this);
		}
	}

	private boolean isBeingLookedAt(LivingEntity target) {
		if (!EndersoulFragmentEntity.IS_VALID_TARGET.test(target) || target instanceof MobEntity && ((MobEntity)target).getAttackTarget() != this) {
			return false;
		}

		Vec3d playerVec = target.getLook(1.0F).normalize();
		Vec3d targetVec = new Vec3d(this.posX - target.posX, this.getBoundingBox().minY + (double)this.getEyeHeight() - (target.posY + (double)target.getEyeHeight()), this.posZ - target.posZ);
		double length = targetVec.length();
		targetVec = targetVec.normalize();
		double d = playerVec.dotProduct(targetVec);
		return d > 1.0D - 0.08D / length ? target.canEntityBeSeen(this) : false;
	}

	class TargetLookerGoal extends NearestAttackableTargetGoal<LivingEntity> {
		public TargetLookerGoal(MutantEndermanEntity mutantEndermanEntity) {
			super(mutantEndermanEntity, LivingEntity.class, 10, false, false, mutantEndermanEntity::isBeingLookedAt);
		}

		@Override
		public boolean shouldExecute() {
			return !isClone() && getAttackTarget() == null && super.shouldExecute();
		}

		@Override
		public void startExecuting() {
			super.startExecuting();
			setAttackID(STARE_ATTACK);
		}
	}

	class ForcedLookGoal extends Goal {
		private LivingEntity attackTarget;

		public ForcedLookGoal() {
			setMutexFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return attackID == STARE_ATTACK;
		}

		@Override
		public void startExecuting() {
			this.attackTarget = getAttackTarget();
			navigator.clearPath();
			if (!isSilent()) {
				world.playMovingSound(null, MutantEndermanEntity.this, MBSoundEvents.ENTITY_MUTANT_ENDERMAN_STARE, getSoundCategory(), 2.5F, 0.7F + rand.nextFloat() * 0.2F);
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			if (this.attackTarget == null || !this.attackTarget.isAlive()) {
				return false;
			} else {
				return attackTick < 100 && getDistanceSq(this.attackTarget) > 9.0D && isBeingLookedAt(this.attackTarget);
			}
		}

		@Override
		public void tick() {
			lookController.setLookPositionWithEntity(this.attackTarget, 45.0F, 45.0F);
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.attackTarget.attackEntityFrom(DamageSource.causeMobDamage(MutantEndermanEntity.this).setDamageBypassesArmor().setDamageIsAbsolute(), 2.0F);
			this.attackTarget.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 160 + rand.nextInt(140)));
			double x = posX - this.attackTarget.posX;
			double z = posZ - this.attackTarget.posZ;
			this.attackTarget.setMotion(x * 0.10000000149011612D, 0.30000001192092896D, z * 0.10000000149011612D);
			EntityUtil.sendPlayerVelocityPacket(this.attackTarget);
			this.attackTarget = null;
		}
	}

	class MeleeGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			return attackID == MELEE_ATTACK;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 10;
		}

		@Override
		public void tick() {
			if (attackTick == 3) {
				for (Entity entity : world.getEntitiesInAABBexcluding(MutantEndermanEntity.this, getBoundingBox().grow(4.0D), EndersoulFragmentEntity.IS_VALID_TARGET)) {
					double dist = (double)getDistance(entity);
					if (getBoundingBox().minY <= entity.getBoundingBox().maxY && dist <= 4.0D) {
						double x = posX - entity.posX;
						double z = posZ - entity.posZ;

						if (EntityUtil.isFacing(MutantEndermanEntity.this, x, z, 3.0F + (1.0F - (float)dist / 4.0F) * 40.0F)) {
							boolean lower = getActiveArm() >= 3;
							float attackDamage = (float)getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
							float damage = attackDamage > 0.0F ? attackDamage + (lower ? 1.0F : 3.0F) : 0.0F;
							entity.attackEntityFrom(DamageSource.causeMobDamage(MutantEndermanEntity.this), damage);
							float power = 0.4F + rand.nextFloat() * 0.2F;
							if (!lower) {
								power += 0.2F;
							}

							entity.setMotion(-x / dist * (double)power, (double)(power * 0.6F), -z / dist * (double)power);
						}
					}
				}
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
		}
	}

	class CloneGoal extends Goal {
		private LivingEntity attackTarget;

		@Override
		public boolean shouldExecute() {
			if (isDecoyClone() || getAttackTarget() == null) {
				return false;
			} else if (heldBlock[1] == 0 && heldBlock[2] == 0) {
				if (isClone()) {
					return true;
				} else {
					return attackID == 0 && rand.nextInt(300) == 0;
				}
			} else {
				return false;
			}
		}

		@Override
		public void startExecuting() {
			setCloneState(1);
			this.attackTarget = getAttackTarget();
			extinguish();
			clearActivePotions();
			spawnTeleportParticles(true);
			double x = this.attackTarget.posX + (double)((rand.nextFloat() - 0.5F) * 24.0F);
			double z = this.attackTarget.posZ + (double)((rand.nextFloat() - 0.5F) * 24.0F);
			double y = this.attackTarget.posY + 8.0D;

			for (int i = 0; i < 7; ++i) {
				x = this.attackTarget.posX + (double)((rand.nextFloat() - 0.5F) * 24.0F);
				z = this.attackTarget.posZ + (double)((rand.nextFloat() - 0.5F) * 24.0F);
				y = this.attackTarget.posY + 8.0D;
				createClone(x, y, z);
			}

			createClone(prevPosX, prevPosY, prevPosZ);
			teleportTo(x, y, z);
			EntityUtil.divertAttackers(MutantEndermanEntity.this, getRandomClone());
		}

		@Override
		public boolean shouldContinueExecuting() {
			return getAttackTarget() != null && getAttackTarget().isAlive() && !cloneList.isEmpty() && isClone() && attackTick < 600;
		}

		@Override
		public void tick() {
			for (int i = cloneList.size() - 1; i >= 0; --i) {
				MutantEndermanEntity clone = cloneList.get(i);
				if (!clone.isAlive()) {
					EntityUtil.divertAttackers(cloneList.remove(i), getRandomClone());
				}
			}
		}

		@Override
		public void resetTask() {
			setCloneState(0);
			this.attackTarget = null;
		}
	}

	class ScreamGoal extends Goal {
		public ScreamGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			if (getAttackTarget() != null && attackID == 0) {
				return screamDelayTick > 0 ? false : rand.nextInt(isWet() ? 400 : 1200) == 0;
			} else {
				return false;
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 165;
		}

		@Override
		public void startExecuting() {
			setAttackID(SCREAM_ATTACK);
			navigator.clearPath();
		}

		@Override
		public void tick() {
			if (attackTick == 40) {
				world.getWorldInfo().setRaining(false);
				spawnTeleportParticles(false);
				playSound(MBSoundEvents.ENTITY_MUTANT_ENDERMAN_SCREAM, 5.0F, 0.7F + rand.nextFloat() * 0.2F);
				List<Entity> screamEntities = world.getEntitiesInAABBexcluding(MutantEndermanEntity.this, getBoundingBox().grow(20.0D, 12.0D, 20.0D), EndersoulFragmentEntity.IS_VALID_TARGET);

				for (int i = 0; i < screamEntities.size(); ++i) {
					Entity entity = (Entity)screamEntities.get(i);
					double dist = getDistanceSq(entity);
					if (dist > 400.0D) {
						screamEntities.remove(i);
						--i;
					} else {
						entity.attackEntityFrom(DamageSource.causeMobDamage(MutantEndermanEntity.this).setDamageBypassesArmor().setDamageIsAbsolute(), 4.0F);
						if (entity instanceof MobEntity) {
							MobEntity living = (MobEntity)entity;
							living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 120, 3));
							if (rand.nextInt(2) != 0) {
								living.addPotionEffect(new EffectInstance(Effects.POISON, 120 + rand.nextInt(180), rand.nextInt(2)));
							}

							if (rand.nextInt(4) != 0) {
								living.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 300 + rand.nextInt(300), rand.nextInt(2)));
							}

							if (rand.nextInt(3) != 0) {
								living.addPotionEffect(new EffectInstance(Effects.HUNGER, 120 + rand.nextInt(60), 10 + rand.nextInt(2)));
							}

							if (rand.nextInt(4) != 0) {
								living.addPotionEffect(new EffectInstance(Effects.NAUSEA, 120 + rand.nextInt(400), 0));
							}
						}
					}
				}
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			screamDelayTick = 600;
		}
	}

	class TeleportGoal extends Goal {
		public TeleportGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return attackID == TELEPORT_ATTACK;
		}

		@Override
		public void startExecuting() {
			navigator.clearPath();
			stopRiding();

			if (getAttackTarget() != null) {
				getLookController().setLookPositionWithEntity(getAttackTarget(), 30.0F, 30.0F);
			}

			teleportAttack(MutantEndermanEntity.this);
			setPosition((double)getTeleportPosition().getX() + 0.5D, (double)getTeleportPosition().getY(), (double)getTeleportPosition().getZ() + 0.5D);
			world.playSound(null, prevPosX, prevPosY + (double)getHeight() / 2.0D, prevPosZ, MBSoundEvents.ENTITY_MUTANT_ENDERMAN_TELEPORT, getSoundCategory(), 1.0F, 1.0F);
			playSound(MBSoundEvents.ENTITY_MUTANT_ENDERMAN_TELEPORT, 1.0F, 1.0F);
			teleportAttack(MutantEndermanEntity.this);
			setPosition(prevPosX, prevPosY, prevPosZ);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.shouldExecute() && attackTick < 10;
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			setPosition((double)getTeleportPosition().getX() + 0.5D, (double)getTeleportPosition().getY(), (double)getTeleportPosition().getZ() + 0.5D);
			setTeleportPosition(null);
			prevPosX = posX;
			prevPosY = posY;
			prevPosZ = posZ;
		}
	}

	class TeleSmashGoal extends Goal {
		private LivingEntity attackTarget;

		public TeleSmashGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return attackTarget != null && attackID == TELESMASH_ATTACK;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackID == TELESMASH_ATTACK && attackTick < 30;
		}

		@Override
		public void startExecuting() {
			this.attackTarget.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20, 5));
			this.attackTarget.addPotionEffect(new EffectInstance(Effects.NAUSEA, 160 + this.attackTarget.getRNG().nextInt(160), 0));
		}

		@Override
		public void tick() {
			if (attackTick < 20) {
				getLookController().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			if (attackTick == 17)
				this.attackTarget.stopRiding();

			if (attackTick == 18) {
				double x = this.attackTarget.posX + (double)((this.attackTarget.getRNG().nextFloat() - 0.5F) * 14.0F);
				double y = this.attackTarget.posY + (double)this.attackTarget.getRNG().nextFloat() + (this.attackTarget instanceof PlayerEntity ? 13.0D : 7.0D);
				double z = this.attackTarget.posZ + (double)((this.attackTarget.getRNG().nextFloat() - 0.5F) * 14.0F);
				world.playSound(null, this.attackTarget.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, this.attackTarget.getSoundCategory(), 1.2F, 0.9F + this.attackTarget.getRNG().nextFloat() * 0.2F);
				this.attackTarget.setPositionAndUpdate(x, y, z);
				this.attackTarget.attackEntityFrom(DamageSource.causeMobDamage(MutantEndermanEntity.this).setDamageBypassesArmor(), 6.0F);
				EntityUtil.spawnLargePortalParticles(this.attackTarget, 256, 1.8F, true);
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.attackTarget = null;
		}
	}

	class ThrowHeldBlockGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			if (attackID != 0) {
				return false;
			} else if (!triggerThrowBlock && getRNG().nextInt(28) != 0) {
				return false;
			} else {
				if (getAttackTarget() != null && !canEntityBeSeen(getAttackTarget())) {
					return false;
				}
				int id = getThrowingHand();
				if (id == -1) {
					return false;
				} else {
					setActiveArm(id);
					return true;
				}
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackID == THROW_ATTACK && attackTick < 14;
		}

		@Override
		public void startExecuting() {
			setAttackID(THROW_ATTACK);
			int id = getActiveArm();
			ThrowableBlockEntity block = new ThrowableBlockEntity(world, MutantEndermanEntity.this, id);
			world.addEntity(block);
			sendHoldBlock(id, 0);
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			setActiveArm(0);
			triggerThrowBlock = false;
		}
	}
}