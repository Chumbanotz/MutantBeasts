package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.SeismicWave;
import chumbanotz.mutantbeasts.util.ZombieResurrection;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantZombieEntity extends MonsterEntity {
	private static final DataParameter<Integer> LIVES = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> THROW_ATTACK_STATE = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.BYTE);
	public static final int MAX_DEATH_TIME = 140, MAX_VANISH_TIME = 100;
	public static final byte MELEE_ATTACK = 4, THROW_ATTACK = 5, ROAR_ATTACK = 6;
	private int attackID;
	private int attackTick;
	public int throwHitTick = -1;
	public int throwFinishTick = -1;
	public int vanishTime;
	private final List<SeismicWave> seismicWaveList = new ArrayList<>();
	private final List<ZombieResurrection> resurrectionList = new ArrayList<>();
	private LivingEntity killer;
	private DamageSource deathCause = DamageSource.GENERIC;

	public MutantZombieEntity(EntityType<? extends MutantZombieEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.5F;
		this.experienceValue = 30;
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantZombieEntity.SlamGroundGoal());
		this.goalSelector.addGoal(0, new MutantZombieEntity.RoarGoal());
		this.goalSelector.addGoal(0, new MutantZombieEntity.ThrowAttackGoal());
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.2D).setMaxAttackTick(0));
		this.goalSelector.addGoal(2, new AvoidDamageGoal(this, 1.2D));
		this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this).setCallsForHelp());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(SWIM_SPEED).setBaseValue(4.0D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(LIVES, 3);
		this.dataManager.register(THROW_ATTACK_STATE, (byte)0);
	}

	public int getLives() {
		return this.dataManager.get(LIVES);
	}

	private void setLives(int lives) {
		this.dataManager.set(LIVES, lives);
	}

	public boolean getThrowAttackHit() {
		return (this.dataManager.get(THROW_ATTACK_STATE) & 1) != 0;
	}

	private void setThrowAttackHit(boolean flag) {
		byte b0 = this.dataManager.get(THROW_ATTACK_STATE);
		this.dataManager.set(THROW_ATTACK_STATE, flag ? (byte)(b0 | 1) : (byte)(b0 & -2));
	}

	public boolean getThrowAttackFinish() {
		return (this.dataManager.get(THROW_ATTACK_STATE) & 2) != 0;
	}

	private void setThrowAttackFinish(boolean flag) {
		byte b0 = this.dataManager.get(THROW_ATTACK_STATE);
		this.dataManager.set(THROW_ATTACK_STATE, flag ? (byte)(b0 | 2) : (byte)(b0 & -3));
	}

	public int getAttackID() {
		return this.attackID;
	}

	private void setAttackID(int attackID) {
		this.attackID = attackID;
		this.attackTick = 0;
		this.world.setEntityState(this, (byte)attackID);
	}

	public int getAttackTick() {
		return this.attackTick;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.8F;
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.UNDEAD;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	protected float updateDistance(float renderYawOffset, float distance) {
		return this.isAlive() ? super.updateDistance(renderYawOffset, distance) : distance;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public int getMaxFallHeight() {
		return 16;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
		if (player.getHeldItem(hand).getItem() == Items.FLINT_AND_STEEL && this.deathTime > 0 && !this.isBurning() && !this.isWet()) {
			this.setFire(8);
			player.swingArm(hand);
			player.getHeldItem(hand).damageItem(1, player, livingEntity -> livingEntity.sendBreakAnimation(hand));
			this.world.playSound(player, this.getPosition(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (this.attackID == THROW_ATTACK) {
			return super.attackEntityAsMob(entityIn);
		} else {
			if (!this.world.isRemote) {
				if (this.attackID == 0 && this.rand.nextInt(5) == 0 && this.canEntityBeSeen(entityIn)) {
					this.setAttackID(THROW_ATTACK);
				}

				if (this.attackID == 0 && this.onGround) {
					this.setAttackID(MELEE_ATTACK);
				}
			}

			return true;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			if (source.isProjectile() && source.getDamageLocation() != null) {
				amount *= 0.5F;
			}

			if (this.attackID == ROAR_ATTACK && source != DamageSource.OUT_OF_WORLD) {
				if (this.attackTick <= 10) {
					return false;
				} else {
					amount *= 0.15F;
				}
			}

			Entity entity = source.getTrueSource();
			return entity != null && (!this.canHarm(entity) || this.attackID == THROW_ATTACK && entity == this.getAttackTarget()) ? false : super.attackEntityFrom(source, amount);
		}
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			EntityUtil.spawnParticlesAtEntity(this, ParticleTypes.FLAME, 30);
		} else if (id == 0 || id >= 4 && id <= 6) {
			this.attackID = id;
			this.attackTick = 0;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.getAttackTarget() != null && this.getDistanceSq(this.getAttackTarget()) <= 49.0D) {
			int chance = !this.hasPath() || !this.canEntityBeSeen(this.getAttackTarget()) || this.getLastDamageSource() != null && this.getLastDamageSource().isProjectile() ? 5 : 20;
			if (this.attackID == 0 && this.onGround && this.rand.nextInt(chance) == 0) {
				this.setAttackID(MELEE_ATTACK);
			}

			if (this.attackID == 0 && this.getDistanceSq(this.getAttackTarget()) < 1.0F && this.rand.nextInt(125) == 0 && this.canEntityBeSeen(this.getAttackTarget())) {
				this.setAttackID(THROW_ATTACK);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.fixRotation();
		this.updateAnimation();
		this.updateMeleeGrounds();
		this.setPathPriority(PathNodeType.LEAVES, net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? 0.0F : -1.0F);

		if (this.isAlive() && this.ticksExisted % 100 == 0 && !this.world.isDaytime() && this.getHealth() < this.getMaxHealth()) {
			this.heal(2.0F);
		}

		for (int i = this.resurrectionList.size() - 1; i >= 0; --i) {
			ZombieResurrection zr = this.resurrectionList.get(i);

			if (!zr.update(this)) {
				this.resurrectionList.remove(zr);
			}
		}

		if (this.getHealth() > 0.0F) {
			this.deathTime = 0;
			this.vanishTime = 0;
		}

		if (this.isAlive() && this.collidedHorizontally && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			AxisAlignedBB bb = this.getBoundingBox().grow(0.2D);
			for (BlockPos blockpos : BlockPos.getAllInBoxMutable(MathHelper.floor(bb.minX), MathHelper.floor(bb.minY), MathHelper.floor(bb.minZ), MathHelper.floor(bb.maxX), MathHelper.floor(bb.maxY), MathHelper.floor(bb.maxZ))) {
				Block block = this.world.getBlockState(blockpos).getBlock();
				if (!this.world.isRemote && (block instanceof BambooBlock || net.minecraft.tags.BlockTags.LEAVES.contains(block) || net.minecraftforge.common.Tags.Blocks.GLASS.contains(block))) {
					this.world.destroyBlock(blockpos, false);
				}
			}
		}
	}

	private void fixRotation() {
		float yaw;

		for (yaw = this.rotationYawHead - this.renderYawOffset; yaw < -180.0F; yaw += 360.0F) {
			;
		}

		while (yaw >= 180.0F) {
			yaw -= 360.0F;
		}

		float offset = 0.1F;

		if (this.attackID == MELEE_ATTACK) {
			offset = 0.2F;
		}

		this.renderYawOffset += yaw * offset;
	}

	protected void updateAnimation() {
		if (this.attackID != 0) {
			++this.attackTick;
		}

		if (this.attackID == THROW_ATTACK) {
			if (this.getThrowAttackHit()) {
				if (this.throwHitTick == -1) {
					this.throwHitTick = 0;
				}

				++this.throwHitTick;
			}

			if (this.getThrowAttackFinish()) {
				if (this.throwFinishTick == -1) {
					this.throwFinishTick = 0;
				}

				++this.throwFinishTick;
			}
		} else {
			this.throwHitTick = -1;
			this.throwFinishTick = -1;
		}
	}

	protected void updateMeleeGrounds() {
		if (!this.seismicWaveList.isEmpty()) {
			SeismicWave wave = this.seismicWaveList.remove(0);
			wave.affectBlocks(this.world, this);
			AxisAlignedBB box = new AxisAlignedBB((double)wave.getX(), (double)(wave.getY() + 1), (double)wave.getZ(), (double)(wave.getX() + 3), (double)(wave.getY() + 2), (double)(wave.getZ() + 3));

			if (wave.isFirst()) {
				double addScale = this.rand.nextDouble() * 0.75D;
				box.grow(0.25D + addScale, 0.25D + addScale * 0.5D, 0.25D + addScale);
			}

			for (Entity entity : this.world.getEntitiesInAABBexcluding(this, box, EntityPredicates.CAN_AI_TARGET.and(this::canHarm))) {
				entity.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageIsAbsolute(), wave.isFirst() ? (float)(9 + this.rand.nextInt(4)) : (float)(6 + this.rand.nextInt(3)));
				double x = entity.posX - posX;
				double z = entity.posZ - posZ;
				double d = Math.sqrt(x * x + z * z);
				entity.setMotion(x / d * 0.3D, 0.04D, z / d * 0.3D);
				entity.isAirBorne = true;
				EntityUtil.sendPlayerVelocityPacket(entity);
				if (entity instanceof LivingEntity) {
					if (this.rand.nextInt(5) == 0 && !EntityUtil.canBlockDamageSource((LivingEntity)entity, DamageSource.causeMobDamage(this))) {
						((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 160, 1));
					}
				}
			}
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
		livingEntity.applyEntityCollision(this);
		livingEntity.velocityChanged = true;
	}

	@Override
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
		return spawnDataIn;
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote) {
			this.deathCause = cause;
			EntityUtil.alertOthers(this);

			if (cause.getTrueSource() instanceof LivingEntity) {
				this.killer = (LivingEntity)cause.getTrueSource();
			}

			if (this.recentlyHit > 0) {
				this.recentlyHit += MAX_DEATH_TIME;
			}
		}
	}

	@Override
	protected void onDeathUpdate() {
		if (this.isBurning()) {
			++this.vanishTime;
		} else if (this.vanishTime > 0) {
			--this.vanishTime;
		}

		if (++this.deathTime >= MAX_DEATH_TIME) {
			this.deathTime = 0;
			this.vanishTime = 0;

			if (!this.world.isRemote) {
				this.setLives(this.getLives() - 1);

				if (this.killer != null) {
					this.setRevengeTarget(this.killer);
					this.killer.setRevengeTarget(this);
				}
			}

			this.setHealth((float)Math.round(this.getMaxHealth() / 3.75F));
		}

		if (this.vanishTime >= MAX_VANISH_TIME || this.getLives() <= 0 && this.deathTime > 25) {
			EntityUtil.dropExperience(this, this.recentlyHit, this::getExperiencePoints, this.attackingPlayer);
			super.onDeath(this.deathCause);
			this.remove();
		}
	}

	@Override
	public void onKillCommand() {
		super.onKillCommand();
		this.setLives(0);
	}

	@Override
	public void setMotionMultiplier(BlockState blockState, Vec3d motionMultiplier) {
		super.setMotionMultiplier(blockState, motionMultiplier.scale(5.0D));
	}

	private boolean canHarm(Entity entity) {
		return !SummonableCapability.isEntityEligible(entity.getType()) && !(entity instanceof MutantZombieEntity);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("Lives", this.getLives());
		compound.putShort("VanishTime", (short)this.vanishTime);

		if (!this.resurrectionList.isEmpty()) {
			ListNBT listnbt = new ListNBT();
			for (ZombieResurrection resurrect : this.resurrectionList) {
				CompoundNBT compound1 = NBTUtil.writeBlockPos(resurrect.getPosition());
				compound1.putInt("Tick", resurrect.getTick());
				listnbt.add(compound1);
			}

			compound.put("Resurrections", listnbt);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("Lives")) {
			this.setLives(compound.getInt("Lives"));
		}

		this.vanishTime = compound.getShort("VanishTime");
		ListNBT listNBT = compound.getList("Resurrections", 10);
		for (int i = 0; i < listNBT.size(); i++) {
			CompoundNBT compound1 = listNBT.getCompound(i);
			this.resurrectionList.add(i, new ZombieResurrection(this.world, NBTUtil.readBlockPos(compound1), compound1.getInt("Tick")));
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_MUTANT_ZOMBIE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_MUTANT_ZOMBIE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_MUTANT_ZOMBIE_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
	}

	class SlamGroundGoal extends Goal {
		private double dirX = -1.0D;
		private double dirZ = -1.0D;
		private LivingEntity attackTarget;

		public SlamGroundGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && attackID == MELEE_ATTACK;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 25;
		}

		@Override
		public void startExecuting() {
			attackTick = 0;
			livingSoundTime = -getTalkInterval();
			playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ATTACK, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
		}

		@Override
		public void tick() {
			if (attackTick < 8) {
				lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			if (attackTick == 8) {
				double x = this.attackTarget.posX - posX;
				double z = this.attackTarget.posZ - posZ;
				double d = Math.sqrt(x * x + z * z);
				this.dirX = x / d;
				this.dirZ = z / d;
			}

			if (attackTick == 12) {
				int x = MathHelper.floor(posX + this.dirX * 2.0D);
				int y = MathHelper.floor(getBoundingBox().minY);
				int z = MathHelper.floor(posZ + this.dirZ * 2.0D);
				int x1 = MathHelper.floor(posX + this.dirX * 8.0D);
				int z1 = MathHelper.floor(posZ + this.dirZ * 8.0D);
				SeismicWave.createWaves(world, seismicWaveList, x, z, x1, z1, y);
				world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, 0.8F + rand.nextFloat() * 0.4F);
			}

			navigator.clearPath();
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.dirX = -1.0D;
			this.dirZ = -1.0D;
			this.attackTarget = null;
		}
	}

	class RoarGoal extends Goal {
		private LivingEntity attackTarget;

		public RoarGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && onGround && getDistanceSq(this.attackTarget) > 16.0D ? rand.nextFloat() * 100.0F < 0.35F : false;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 120;
		}

		@Override
		public void startExecuting() {
			setAttackID(ROAR_ATTACK);
			navigator.clearPath();
			idleTime = 0;
			livingSoundTime = -getTalkInterval();
		}

		@Override
		public void tick() {
			if (attackTick < 75) {
				lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			if (attackTick == 10) {
				playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ROAR, 3.0F, 0.7F + rand.nextFloat() * 0.2F);

				for (LivingEntity livingEntity : world.getEntitiesWithinAABB(LivingEntity.class, getBoundingBox().grow(12.0D, 8.0D, 12.0D))) {
					if (getDistanceSq(livingEntity) <= 196.0D) {
						if (SummonableCapability.getLazy(livingEntity).isPresent() && !SummonableCapability.get(livingEntity).isSpawnedBySummoner() && SummonableCapability.get(livingEntity).getSummoner() == null) {
							SummonableCapability.get(livingEntity).setSummoner(MutantZombieEntity.this);
						} else if (canHarm(livingEntity)) {
							double x = livingEntity.posX - posX;
							double z = livingEntity.posZ - posZ;
							double d = Math.sqrt(x * x + z * z);
							livingEntity.setMotion(x / d * 0.699999988079071D, 0.30000001192092896D, z / d * 0.699999988079071D);
							livingEntity.attackEntityFrom(DamageSource.causeMobDamage(MutantZombieEntity.this).setDamageBypassesArmor().setDamageIsAbsolute(), (float)(2 + rand.nextInt(2)));
						}
					}
				}
			}

			if (world.dimension.isSurfaceWorld() && attackTick >= 20 && attackTick < 80 && attackTick % 10 == 0) {
				int x = MathHelper.floor(posX);
				int y = MathHelper.floor(getBoundingBox().minY);
				int z = MathHelper.floor(posZ);
				x += (1 + rand.nextInt(8)) * (rand.nextBoolean() ? 1 : -1);
				z += (1 + rand.nextInt(8)) * (rand.nextBoolean() ? 1 : -1);
				y = ZombieResurrection.getSuitableGround(world, x, y - 1, z);

				if (y != -1) {
					resurrectionList.add(new ZombieResurrection(world, x, y, z));
				}
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
		}
	}

	class ThrowAttackGoal extends Goal {
		private int hit = -1;
		private int finish = -1;
		private LivingEntity attackTarget;

		public ThrowAttackGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && attackID == THROW_ATTACK;
		}

		@Override
		public void startExecuting() {
			attackTick = 0;
			navigator.clearPath();
			this.attackTarget.stopRiding();
			double x = this.attackTarget.posX - posX;
			double z = this.attackTarget.posZ - posZ;
			double d = Math.sqrt(x * x + z * z);
			this.attackTarget.setMotion(x / d * 0.800000011920929D, 1.600000023841858D, z / d * 0.800000011920929D);
			this.attackTarget.velocityChanged = true;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackID == THROW_ATTACK && this.finish < 10;
		}

		@Override
		public void tick() {
			lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			double d1;
			double d2;
			double x;
			double z;

			if (attackTick == 15) {
				d1 = this.attackTarget.posX - posX;
				d2 = this.attackTarget.posY - posY;
				x = this.attackTarget.posZ - posZ;
				z = Math.sqrt(d1 * d1 + d2 * d2 + x * x);
				setMotion(d1 / z * 3.4000000953674316D, d2 / z * 1.399999976158142D, x / z * 3.4000000953674316D);
			} else if (attackTick > 15) {
				d1 = (double)(getWidth() * 2.0F * getWidth() * 2.0F);
				d2 = getDistanceSq(this.attackTarget.posX, this.attackTarget.getBoundingBox().minY, this.attackTarget.posZ);

				if (d2 < d1 && this.hit == -1) {
					this.hit = 0;
					setThrowAttackHit(true);
					attackEntityAsMob(this.attackTarget);
					x = this.attackTarget.posX - posX;
					z = this.attackTarget.posZ - posZ;
					double d = Math.sqrt(x * x + z * z);
					this.attackTarget.setMotion(x / d * 0.6000000238418579D, -1.2000000476837158D, z / d * 0.6000000238418579D);
					this.attackTarget.velocityChanged = true;
					this.attackTarget.hurtResistantTime = 10;
					EntityUtil.disableShield(this.attackTarget, DamageSource.causeMobDamage(MutantZombieEntity.this), 150);
					playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_GRUNT, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
				}

				if (this.hit >= 0) {
					++this.hit;
				}

				if ((onGround || world.containsAnyLiquid(getBoundingBox())) && this.finish == -1) {
					this.finish = 0;
					setThrowAttackFinish(true);
				}

				if (this.finish >= 0) {
					++this.finish;
				}
			}
		}

		@Override
		public void resetTask() {
			setAttackID(0);
			this.hit = -1;
			this.finish = -1;
			setThrowAttackHit(false);
			setThrowAttackFinish(false);
			this.attackTarget = null;
		}
	}
}