package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.controller.FixedBodyController;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.HurtByNearestTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.SeismicWave;
import chumbanotz.mutantbeasts.util.ZombieResurrection;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class MutantZombieEntity extends MonsterEntity implements IEntityAdditionalSpawnData {
	private static final DataParameter<Integer> LIVES = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> THROW_ATTACK_STATE = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.BYTE);
	public static final int MAX_DEATH_TIME = 140;
	public static final int MAX_VANISH_TIME = 100;
	public static final byte MELEE_ATTACK = 1;
	public static final byte THROW_ATTACK = 2;
	public static final byte ROAR_ATTACK = 3;
	private int attackID;
	private int attackTick;
	public int throwHitTick = -1;
	public int throwFinishTick = -1;
	public int vanishTime;
	private final List<SeismicWave> seismicWaveList = new ArrayList<>();
	private final List<ZombieResurrection> resurrectionList = new ArrayList<>();
	private DamageSource deathCause;
	public int deathTime;

	public MutantZombieEntity(EntityType<? extends MutantZombieEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.0F;
		this.experienceValue = 30;
	}

	public MutantZombieEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.MUTANT_ZOMBIE, world);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantZombieEntity.SlamGroundGoal());
		this.goalSelector.addGoal(0, new MutantZombieEntity.RoarGoal());
		this.goalSelector.addGoal(0, new MutantZombieEntity.ThrowAttackGoal());
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.2D).setMaxAttackTick(0));
		this.goalSelector.addGoal(2, new AvoidDamageGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> false));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(0, new HurtByNearestTargetGoal(this).setCallsForHelp());
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(100));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, true));
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
		this.world.setEntityState(this, (byte)-attackID);
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
	protected BodyController createBodyController() {
		return new FixedBodyController(this);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public int getMaxFallHeight() {
		return this.getAttackTarget() != null ? (int)this.getDistance(this.getAttackTarget()) : 3;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
		ItemStack itemStack = player.getHeldItem(hand);
		if (itemStack.getItem() == Items.FLINT_AND_STEEL && !this.isAlive() && !this.isBurning() && !this.isWet()) {
			this.setFire(8);
			player.swingArm(hand);
			player.addStat(Stats.ITEM_USED.get(itemStack.getItem()));
			itemStack.damageItem(1, player, livingEntity -> livingEntity.sendBreakAnimation(hand));
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
				if (this.attackID == 0 && this.rand.nextInt(5) == 0 && this.getEntitySenses().canSee(entityIn)) {
					this.attackID = THROW_ATTACK;
				}

				if (this.attackID == 0 && (this.onGround || !this.getBlockState().getFluidState().isEmpty())) {
					this.attackID = MELEE_ATTACK;
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
			if (this.attackID == ROAR_ATTACK && source != DamageSource.OUT_OF_WORLD) {
				if (this.attackTick <= 10) {
					return false;
				}

				amount *= 0.15F;
			}

			Entity entity = source.getTrueSource();
			return entity != null && this.attackID == THROW_ATTACK && entity == this.getAttackTarget() ? false : super.attackEntityFrom(source, amount);
		}
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id <= 0) {
			this.attackID = Math.abs(id);
			this.attackTick = 0;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.getAttackTarget() != null && this.getDistanceSq(this.getAttackTarget()) <= 49.0D) {
			if (this.attackID == 0 && this.onGround && Math.abs(this.posY - this.getAttackTarget().posY) <= 4.0D && this.rand.nextInt(20) == 0) {
				this.attackID = MELEE_ATTACK;
			}

			if (this.attackID == 0 && this.getDistanceSq(this.getAttackTarget()) < 1.0F && this.rand.nextInt(125) == 0) {
				this.attackID = THROW_ATTACK;
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.fixRotation();
		this.updateAnimation();
		this.updateMeleeGrounds();

		if (!this.world.isDaytime() && this.ticksExisted % 100 == 0 && this.isAlive() && this.getHealth() < this.getMaxHealth()) {
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

	private void updateAnimation() {
		if (this.attackID != 0) {
			++this.attackTick;
		}

		if (this.world.isRemote) {
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
	}

	protected void updateMeleeGrounds() {
		if (!this.seismicWaveList.isEmpty()) {
			SeismicWave wave = this.seismicWaveList.remove(0);
			wave.affectBlocks(this.world, this);
			AxisAlignedBB box = new AxisAlignedBB((double)wave.getX(), (double)(wave.getY() + 1), (double)wave.getZ(), (double)(wave.getX() + 1), (double)(wave.getY() + 2), (double)(wave.getZ() + 1));

			if (wave.isFirst()) {
				double addScale = this.rand.nextDouble() * 0.75D;
				box = box.grow(0.25D + addScale, 0.25D + addScale * 0.5D, 0.25D + addScale);
			}

			for (Entity entity : this.world.getEntitiesInAABBexcluding(this, box, EntityPredicates.CAN_AI_TARGET)) {
				if (!this.isOnSameTeam(entity) && entity.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageIsAbsolute(), wave.isFirst() ? (float)(9 + this.rand.nextInt(4)) : (float)(6 + this.rand.nextInt(3))) && entity instanceof LivingEntity && this.rand.nextInt(5) == 0) {
					((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 160, 1));
				}
	
				double x = entity.posX - posX;
				double z = entity.posZ - posZ;
				double d = Math.sqrt(x * x + z * z);
				entity.setMotion(x / d * 0.3D, 0.04D, z / d * 0.3D);
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
		livingEntity.velocityChanged = true;
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote) {
			if (cause == DamageSource.OUT_OF_WORLD) {
				this.setLives(0);
			}

			this.deathCause = cause;
			this.goalSelector.getRunningGoals().forEach(PrioritizedGoal::resetTask);
			this.setLastAttackedEntity(this.getRevengeTarget());
			this.world.setEntityState(this, (byte)3);

			if (this.recentlyHit > 0) {
				this.recentlyHit += MAX_DEATH_TIME;
			}
		}
	}

	@Override
	protected void onDeathUpdate() {
		if (this.deathTime <= 25 || !this.isBurning() || this.deathTime >= 100) {
			++this.deathTime;
		}

		if (this.getLives() <= 0) {
			super.deathTime = this.deathTime;
		}

		if (this.isBurning()) {
			++this.vanishTime;
		} else if (this.vanishTime > 0) {
			--this.vanishTime;
		}

		if (this.deathTime >= MAX_DEATH_TIME) {
			this.deathTime = 0;
			this.vanishTime = 0;
			this.deathCause = null;
			this.setLives(this.getLives() - 1);
			if (this.getLastAttackedEntity() != null) {
				this.getLastAttackedEntity().setRevengeTarget(this);
			}

			this.setHealth((float)Math.round(this.getMaxHealth() / 3.75F));
		}

		if (this.vanishTime >= MAX_VANISH_TIME || this.getLives() <= 0 && this.deathTime > 25) {
			if (!this.world.isRemote) {
				super.onDeath(this.deathCause != null ? this.deathCause : DamageSource.GENERIC);
			}

			EntityUtil.dropExperience(this, this.recentlyHit, this.experienceValue, this.attackingPlayer);
			EntityUtil.spawnParticlesAtEntity(this, this.isBurning() ? ParticleTypes.FLAME : ParticleTypes.POOF, 30);
			this.remove();
		}
	}

	@Override
	public void onKillEntity(LivingEntity livingEntity) {
		if ((this.world.getDifficulty() == Difficulty.NORMAL && this.rand.nextBoolean() || this.world.getDifficulty() == Difficulty.HARD) && livingEntity instanceof VillagerEntity) {
			EntityUtil.convertMobWithNBT(livingEntity, EntityType.ZOMBIE_VILLAGER, false);
			if (!livingEntity.isSilent()) {
				this.world.playEvent(null, 1026, livingEntity.getPosition(), 0);	
			}
		}
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (this.getType() == entityIn.getType() || super.isOnSameTeam(entityIn)) {
			return true;
		} else if (ZombieResurrection.canBeResurrected(entityIn.getType())) {
			return this.getAttackTarget() != entityIn && this.getTeam() == null && entityIn.getTeam() == null;
		} else {
			return false;
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("Lives", this.getLives());
		compound.putShort("VanishTime", (short)this.vanishTime);
		compound.putShort("DeathTime", (short)this.deathTime);

		if (!this.resurrectionList.isEmpty()) {
			ListNBT listnbt = new ListNBT();
			for (ZombieResurrection resurrection : this.resurrectionList) {
				CompoundNBT compound1 = NBTUtil.writeBlockPos(resurrection);
				compound1.putInt("Tick", resurrection.getTick());
				listnbt.add(compound1);
			}

			compound.put("Resurrections", listnbt);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.deathTime = super.deathTime;
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
		if (this.deathTime == 0) {
			this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
		}
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeVarInt(this.attackID);
		buffer.writeVarInt(this.attackTick);
		buffer.writeVarInt(this.deathTime);
		buffer.writeVarInt(this.vanishTime);
		buffer.writeVarInt(this.throwHitTick);
		buffer.writeVarInt(this.throwFinishTick);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.attackID = additionalData.readVarInt();
		this.attackTick = additionalData.readVarInt();
		this.deathTime = additionalData.readVarInt();
		this.vanishTime = additionalData.readVarInt();
		this.throwHitTick = additionalData.readVarInt();
		this.throwFinishTick = additionalData.readVarInt();
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
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
			setAttackID(MELEE_ATTACK);
			livingSoundTime = -getTalkInterval();
			playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ATTACK, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
		}

		@Override
		public void tick() {
			getNavigator().clearPath();
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
		public RoarGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return ticksExisted % 3 == 0 && getAttackTarget()!= null && onGround && getDistanceSq(getAttackTarget().posX, getAttackTarget().posY, getAttackTarget().posZ) > 16.0D && rand.nextFloat() * 100.0F < 0.35F;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackTick < 120;
		}

		@Override
		public void startExecuting() {
			setAttackID(ROAR_ATTACK);
			livingSoundTime = -getTalkInterval();
		}

		@Override
		public void tick() {
			getNavigator().clearPath();
			if (attackTick < 75 && getAttackTarget() != null) {
				lookController.setLookPositionWithEntity(getAttackTarget(), 30.0F, 30.0F);
			}

			if (attackTick == 10) {
				playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ROAR, 3.0F, 0.7F + rand.nextFloat() * 0.2F);

				for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(MutantZombieEntity.this, getBoundingBox().grow(12.0D, 8.0D, 12.0D))) {
					if (entity.canBeCollidedWith() && !isOnSameTeam(entity) && getDistanceSq(entity.posX, entity.posY, entity.posZ) <= 196.0D) {
						double x = entity.posX - posX;
						double z = entity.posZ - posZ;
						double d = Math.sqrt(x * x + z * z);
						entity.setMotion(x / d * 0.699999988079071D, 0.30000001192092896D, z / d * 0.699999988079071D);
						EntityUtil.sendPlayerVelocityPacket(entity);
						entity.attackEntityFrom(DamageSource.causeMobDamage(MutantZombieEntity.this), (float)(2 + rand.nextInt(2)));
					}
				}
			}

			if (attackTick >= 20 && attackTick < 80 && attackTick % 10 == 0) {
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
			setAttackID(THROW_ATTACK);
			this.attackTarget.stopRiding();
			double x = this.attackTarget.posX - posX;
			double z = this.attackTarget.posZ - posZ;
			double d = Math.sqrt(x * x + z * z);
			this.attackTarget.setMotion(x / d * 0.800000011920929D, 1.600000023841858D, z / d * 0.800000011920929D);
			EntityUtil.sendPlayerVelocityPacket(this.attackTarget);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackID == THROW_ATTACK && this.finish < 10;
		}

		@Override
		public void tick() {
			getNavigator().clearPath();
			if (!getThrowAttackFinish()) {
				lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			double d1;
			double d2;
			double x;
			double z;

			if (attackTick == 15) {
				d1 = this.attackTarget.posX - posX;
				d2 = this.attackTarget.posY - posY;
				x = this.attackTarget.posZ - posZ;
				z = Math.sqrt(d1 * d1 + d2 * d2 + x * x);
				motionMultiplier = Vec3d.ZERO;
				setMotion(d1 / z * 3.4000000953674316D, d2 / z * 1.399999976158142D, x / z * 3.4000000953674316D);
			} else if (attackTick > 15) {
				d1 = (double)(getWidth() * 2.0F * getWidth() * 2.0F);
				d2 = getDistanceSq(this.attackTarget.posX, this.attackTarget.getBoundingBox().minY, this.attackTarget.posZ);

				if (!getThrowAttackHit() && d2 < d1) {
					setThrowAttackHit(true);
					if (!attackEntityAsMob(this.attackTarget)) {
						EntityUtil.disableShield(this.attackTarget, 150);
					}

					x = this.attackTarget.posX - posX;
					z = this.attackTarget.posZ - posZ;
					double d = Math.sqrt(x * x + z * z);
					this.attackTarget.hurtResistantTime = 0;
					this.attackTarget.setMotion(x / d * 0.6000000238418579D, -1.2000000476837158D, z / d * 0.6000000238418579D);
					EntityUtil.stunRavager(this.attackTarget);
					EntityUtil.sendPlayerVelocityPacket(this.attackTarget);
					playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_GRUNT, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
				}

				if ((onGround || !getBlockState().getFluidState().isEmpty()) && this.finish == -1) {
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
			this.finish = -1;
			setThrowAttackHit(false);
			setThrowAttackFinish(false);
			this.attackTarget = null;
		}
	}
}