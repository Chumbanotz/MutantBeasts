package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.client.animationapi.IAnimatedEntity;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.ZombieChunk;
import chumbanotz.mutantbeasts.util.ZombieResurrect;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MutantZombieEntity extends ZombieEntity implements IAnimatedEntity {
	// private static final UUID GROUND_MELEE_DAMAGE_MODIFIER =
	// UUID.fromString("51a6ea78-ce90-11e9-a32f-2a2ae2dbcce4");
	private static final DataParameter<Integer> LIVES = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> THROW_ATTACK_STATE = EntityDataManager.createKey(MutantZombieEntity.class, DataSerializers.BYTE);
	public static final int MAX_DOWN_TIME = 140;
	public static final int MAX_VANISH_TIME = 100;
	private int currentAttackID;
	private int animTick;
	public int throwHitTick = -1;
	public int throwFinishTick = -1;
	public int downTime;
	public int vanishTime;
	private final List<ZombieChunk> meleeGroundList = new ArrayList<>();
	private final List<ZombieResurrect> resurrectList = new ArrayList<>();
	private LivingEntity lastAttacker;

	public MutantZombieEntity(EntityType<? extends MutantZombieEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.5F;
		this.experienceValue = 30;
		this.ignoreFrustumCheck = true;
		this.navigator = new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MutantZombieEntity.AttackGoal());
		this.goalSelector.addGoal(1, new MutantZombieEntity.MeleeGoal());
		this.goalSelector.addGoal(1, new MutantZombieEntity.RoarGoal());
		this.goalSelector.addGoal(1, new MutantZombieEntity.ThrowAttackGoal());
		this.goalSelector.addGoal(2, new MBMeleeAttackGoal(this, 1.2D, true).setMaxAttackTick(0));
		this.goalSelector.addGoal(4, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this, MutantZombieEntity.class).setCallsForHelp(ZombiePigmanEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 60, true, true, TurtleEntity.TARGET_DRY_BABY));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0D);
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

	public void decrementLives() {
		this.dataManager.set(LIVES, this.getLives() - 1);
	}

	public boolean getThrowAttackHit() {
		return (this.dataManager.get(THROW_ATTACK_STATE) & 1) != 0;
	}

	public void setThrowAttackHit(boolean flag) {
		byte b0 = this.dataManager.get(THROW_ATTACK_STATE);
		this.dataManager.set(THROW_ATTACK_STATE, flag ? (byte)(b0 | 1) : (byte)(b0 & -2));
	}

	public boolean getThrowAttackFinish() {
		return (this.dataManager.get(THROW_ATTACK_STATE) & 2) != 0;
	}

	public void setThrowAttackFinish(boolean flag) {
		byte b0 = this.dataManager.get(THROW_ATTACK_STATE);
		this.dataManager.set(THROW_ATTACK_STATE, flag ? (byte)(b0 | 2) : (byte)(b0 & -3));
	}

	@Override
	public int getAnimationID() {
		return this.currentAttackID;
	}

	@Override
	public void setAnimationID(int id) {
		this.currentAttackID = id;
	}

	@Override
	public int getAnimationTick() {
		return this.animTick;
	}

	@Override
	public void setAnimationTick(int tick) {
		this.animTick = tick;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.8F;
	}

	@Override
	protected BodyController createBodyController() {
		return new BodyController(this) {
			@Override
			public void updateRenderAngles() {
				if (downTime == 0) {
					super.updateRenderAngles();
				}
			}
		};
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public int getMaxFallHeight() {
		return 100;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	public void attackEntityAtDistSq(LivingEntity living, float f) {
		if (!this.world.isRemote) {
			if (this.currentAttackID == 0 && this.onGround && this.rand.nextInt(20) == 0) {
				this.sendPacket(1);
			}

			if (this.currentAttackID == 0 && f < 1.0F && this.rand.nextInt(125) == 0 && this.canEntityBeSeen(living)) {
				this.sendPacket(3);
			}
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (!this.world.isRemote) {
			if (this.currentAttackID == 0 && this.rand.nextInt(5) == 0 && this.canEntityBeSeen(entityIn)) {
				this.sendPacket(3);
			}

			if (this.currentAttackID == 0 && this.onGround) {
				this.sendPacket(1);
			}
		}

		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isProjectile() && !(source.getImmediateSource() instanceof MutantArrowEntity)) {
			amount *= 0.5;
		}

		Entity entity = source.getTrueSource();
		return entity != null && (this.ignoresEntity(entity) || this.currentAttackID == 3 && entity == this.getAttackTarget()) ? false : super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	public void setChild(boolean childZombie) {
	}

	@Override
	protected boolean shouldDrown() {
		return false;
	}

	@Override
	protected boolean shouldBurnInDay() {
		return false;
	}

	@Override
	public void setBreakDoorsAItask(boolean enabled) {
	}

	@Override
	protected ItemStack getSkullDrop() {
		return ItemStack.EMPTY;
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	public void tick() {
		super.tick();
		this.fixRotation();
		this.updateAnimation();
		this.deathTime = 0;
		this.setPathPriority(PathNodeType.LEAVES, this.moveController.getSpeed() > 1.0D && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? 0.0F : -1.0F);
		if (this.getAttackTarget() != null && !this.getAttackTarget().isAlive()) {
			this.setAttackTarget(null);
		}

		if (this.downTime == 0 && this.ticksExisted % 100 == 0 && !this.world.isDaytime() && this.getHealth() < this.getMaxHealth()) {
			this.heal(2.0F);
		}

		this.updateMeleeGrounds();

		for (int i = this.resurrectList.size() - 1; i >= 0; --i) {
			ZombieResurrect zr = this.resurrectList.get(i);

			if (!zr.update(this)) {
				this.resurrectList.remove(zr);
			}
		}

		if (this.getHealth() > 0.0F) {
			this.downTime = 0;
			this.vanishTime = 0;
		}

		if (this.isAlive() && this.moveController.getSpeed() > 1.0D && this.collided && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(0.2D);

			for (BlockPos blockpos : BlockPos.getAllInBoxMutable(MathHelper.floor(axisalignedbb.minX), MathHelper.floor(axisalignedbb.minY), MathHelper.floor(axisalignedbb.minZ), MathHelper.floor(axisalignedbb.maxX), MathHelper.floor(axisalignedbb.maxY), MathHelper.floor(axisalignedbb.maxZ))) {
				BlockState blockstate = this.world.getBlockState(blockpos);

				if (!this.world.isRemote && (blockstate.getBlock() instanceof LeavesBlock || blockstate.getBlock() instanceof AbstractGlassBlock || blockstate.getBlock() instanceof CropsBlock)) {
					this.world.destroyBlock(blockpos, false);
				}
			}
		}
	}

	protected void fixRotation() {
		float f;

		for (f = this.rotationYawHead - this.renderYawOffset; f < -180.0F; f += 360.0F) {
			;
		}

		while (f >= 180.0F) {
			f -= 360.0F;
		}

		float f1 = 0.1F;

		if (this.currentAttackID == 1) {
			f1 = 0.2F;
		}

		this.renderYawOffset += f * f1;
	}

	protected void updateAnimation() {
		if (this.currentAttackID != 0) {
			++this.animTick;
		}

		if (this.currentAttackID == 3) {
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
		if (!this.meleeGroundList.isEmpty()) {
			ZombieChunk chunk = this.meleeGroundList.remove(0);
			chunk.handleBlocks(this.world, this);
			AxisAlignedBB box = new AxisAlignedBB((double)chunk.getX(), (double)(chunk.getY() + 1), (double)chunk.getZ(), (double)(chunk.getX() + 3), (double)(chunk.getY() + 2), (double)(chunk.getZ() + 3));

			if (chunk.first) {
				double addScale = this.rand.nextDouble() * 0.75D;
				box.grow(0.25D + addScale, 0.25D + addScale * 0.5D, 0.25D + addScale);
			}

			for (Entity entity : EntityUtil.getCollidingEntities(this, this.world, box)) {
				if (!this.ignoresEntity(entity)) {
					entity.attackEntityFrom(DamageSource.causeMobDamage(this), (float)(6 + this.rand.nextInt(3)));
					double x = entity.posX - posX;
					double z = entity.posZ - posZ;
					double d = Math.sqrt(x * x + z * z);
					entity.setMotion(x / d * 0.4D, 0.0D, z / d * 0.4D);
					if (entity instanceof LivingEntity) {
						if (this.rand.nextInt(5) == 0 && !EntityUtil.canBlockDamageSource((LivingEntity)entity, DamageSource.causeMobDamage(this))) {
							((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 160, 1));
						}
					}
				}
			}
		} else {
			// this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(GROUND_MELEE_DAMAGE_MODIFIER);
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
	protected void func_213371_e(LivingEntity p_213371_1_) {
		if (this.currentAttackID == 1) {
			p_213371_1_.applyEntityCollision(this);
			p_213371_1_.velocityChanged = true;
		}
	}

	@Override
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
		return spawnDataIn;
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (this.getLives() <= 0) {
			super.onDeath(cause);
		}
	}

	@Override
	protected void onDeathUpdate() {
		++this.downTime;
		this.stepHeight = 0.0F;

		if (this.isBurning()) {
			++this.vanishTime;
		} else {
			this.vanishTime = Math.max(0, this.vanishTime - 1);
		}

		if (this.downTime >= MAX_DOWN_TIME) {
			float f = (float)(this.downTime - this.vanishTime);
			int i = (int)(40.0F * f / (float)MAX_DOWN_TIME);
			this.downTime = 0;
			this.vanishTime = 0;
			this.extinguish();
			this.stepHeight = 1.5F;

			if (!this.world.isRemote) {
				this.decrementLives();

				if (this.lastAttacker != null) {
					this.setRevengeTarget(this.lastAttacker);
				}
			}

			this.setHealth((float)i);
		}

		if (this.vanishTime >= MAX_VANISH_TIME || this.getLives() <= 0 && this.downTime > 25) {
			if (!this.world.isRemote) {
				if (this.recentlyHit > 0 && this.canDropLoot() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
					int i = this.getExperiencePoints(this.attackingPlayer);
					i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.attackingPlayer, i);

					while (i > 0) {
						int j = ExperienceOrbEntity.getXPSplit(i);
						i -= j;
						this.world.addEntity(new ExperienceOrbEntity(this.world, this.posX, this.posY, this.posZ, j));
					}
				}

				this.entityDropItem(MBItems.HULK_HAMMER);
			}

			this.remove();

			for (int k = 0; k < 30; ++k) {
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				this.world.addParticle(ParticleTypes.FLAME, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
			}
		}
	}

	@Override
	public void onKillCommand() {
		super.onKillCommand();
		this.dataManager.set(LIVES, 0);
	}

	@Override
	public void setMotionMultiplier(BlockState p_213295_1_, Vec3d p_213295_2_) {
		super.setMotionMultiplier(p_213295_1_, p_213295_2_.scale(5.0D));
	}

	private boolean ignoresEntity(Entity entity) {
		return SummonableCapability.isEntityEligible(entity.getType()) || entity instanceof MutantZombieEntity;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("Lives", this.getLives());
		compound.putShort("DownTime", (short)this.downTime);
		compound.putShort("VanishTime", (short)this.vanishTime);

		if (!this.resurrectList.isEmpty()) {
			ListNBT listnbt = new ListNBT();
			for (ZombieResurrect resurrect : this.resurrectList) {
				CompoundNBT compound1 = NBTUtil.writeBlockPos(resurrect.getPosition());
				compound1.putInt("Tick", resurrect.tick);
				listnbt.add(compound1);
			}

			compound.put("Resurrections", listnbt);
		}

		if (this.lastAttacker != null) {
			compound.putUniqueId("LastAttackerUUID", this.lastAttacker.getUniqueID());
		}

		for (String s : new String[] {"IsBaby", "CanBreakDoors", "InWaterTime", "DrownedConversionTime"}) {
			compound.remove(s);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.dataManager.set(LIVES, compound.getInt("Lives"));
		this.downTime = compound.getShort("DownTime");
		this.vanishTime = compound.getShort("VanishTime");
		ListNBT listnbt1 = compound.getList("Resurrections", 10);

		for (int i = 0; i < listnbt1.size(); i++) {
			CompoundNBT compound1 = listnbt1.getCompound(i);
			this.resurrectList.add(i, new ZombieResurrect(this.world, NBTUtil.readBlockPos(compound1), compound1.getInt("Tick")));
		}

		if (compound.contains("LastAttackerUUID") && this.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(compound.getUniqueId("LastAttackerUUID"));
			this.lastAttacker = entity instanceof LivingEntity ? (LivingEntity)entity : null;
			this.setRevengeTarget(this.lastAttacker);
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
		return MBSoundEvents.ENTITY_MUTANT_ZOMBIE_HURT;
	}

	class AttackGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			if (getAttackTarget() != null) {
				double x = Math.max(0.0D, getAttackTarget().posX - posX - (double)((getAttackTarget().getWidth() + getWidth()) / 2.0F));
				double y = Math.max(0.0D, getAttackTarget().posY - posY - (double)((getAttackTarget().getHeight() + getHeight()) / 2.0F));
				double z = Math.max(0.0D, getAttackTarget().posZ - posZ - (double)((getAttackTarget().getWidth() + getWidth()) / 2.0F));
				return getDistanceSq(x * x, y * y, z * z) <= 9.0D;
			}

			return false;
		}

		@Override
		public void startExecuting() {
			attackEntityAtDistSq(getAttackTarget(), (float)9.0D);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}
	}

	class MeleeGoal extends Goal {
		private LivingEntity attackTarget;
		private double dirX = -1.0D;
		private double dirZ = -1.0D;

		public MeleeGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && onGround ? currentAttackID == 1 : false;
		}

		@Override
		public void startExecuting() {
			animTick = 0;
			navigator.clearPath();
			livingSoundTime = -getTalkInterval();
			playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ATTACK, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
//			getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier(GROUND_MELEE_DAMAGE_MODIFIER, "Ground melee damage", (double)(-4 - rand.nextInt(3)), AttributeModifier.Operation.ADDITION).setSaved(false));
		}

		@Override
		public boolean shouldContinueExecuting() {
			return downTime <= 0 && animTick < 25;
		}

		@Override
		public void tick() {
			if (animTick < 8) {
				lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			if (animTick == 8) {
				double x = this.attackTarget.posX - posX;
				double z = this.attackTarget.posZ - posZ;
				double d = Math.sqrt(x * x + z * z);
				this.dirX = x / d;
				this.dirZ = z / d;
			}

			if (animTick == 12) {
				int x = MathHelper.floor(posX + this.dirX * 2.0D);
				int y = MathHelper.floor(getBoundingBox().minY);
				int z = MathHelper.floor(posZ + this.dirZ * 2.0D);
				int x1 = MathHelper.floor(posX + this.dirX * 8.0D);
				int z1 = MathHelper.floor(posZ + this.dirZ * 8.0D);
				ZombieChunk.addLinePositions(world, meleeGroundList, x, z, x1, z1, y);
				world.playSound(null, x1, y, z1, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, 0.8F + rand.nextFloat() * 0.4F);
			}
		}

		@Override
		public void resetTask() {
			sendPacket(0);
			this.attackTarget = null;
			this.dirX = -1.0D;
			this.dirZ = -1.0D;
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
		public void startExecuting() {
			animTick = 0;
			sendPacket(2);
			livingSoundTime = -getTalkInterval();
			setInvulnerable(true);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return downTime <= 0 && animTick < 120;
		}

		@Override
		public void tick() {
			if (animTick < 75) {
				lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			}

			if (animTick == 10) {
				playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_ROAR, 3.0F, 0.7F + rand.nextFloat() * 0.2F);

				for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(MutantZombieEntity.this, getBoundingBox().grow(12.0D, 8.0D, 12.0D))) {
					if (!ignoresEntity(entity) && getDistanceSq(entity) <= 196.0D) {
						double x = entity.posX - posX;
						double z = entity.posZ - posZ;
						double d = Math.sqrt(x * x + z * z);
						entity.setMotion(x / d * 0.699999988079071D, 0.30000001192092896D, z / d * 0.699999988079071D);

						if (entity instanceof LivingEntity) {
							entity.attackEntityFrom(DamageSource.causeMobDamage(MutantZombieEntity.this).setDamageBypassesArmor().setDamageIsAbsolute(), (float)(2 + rand.nextInt(2)));
						}
					}
				}
			}

			if (animTick >= 20 && animTick < 80 && animTick % 10 == 0) {
				int x = MathHelper.floor(posX);
				int y = MathHelper.floor(getBoundingBox().minY);
				int z = MathHelper.floor(posZ);
				x += (1 + rand.nextInt(8)) * (rand.nextBoolean() ? 1 : -1);
				z += (1 + rand.nextInt(8)) * (rand.nextBoolean() ? 1 : -1);
				y = ZombieResurrect.getSuitableGround(world, x, y - 1, z);

				if (y != -1) {
					resurrectList.add(new ZombieResurrect(world, x, y, z));
				}
			}

			navigator.clearPath();
		}

		@Override
		public void resetTask() {
			this.attackTarget = null;
			sendPacket(0);
			setInvulnerable(false);
		}
	}

	class ThrowAttackGoal extends Goal {
		private LivingEntity attackTarget;
		private int hit = -1;
		private int finish = -1;

		public ThrowAttackGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			this.attackTarget = getAttackTarget();
			return this.attackTarget != null && currentAttackID == 3;
		}

		@Override
		public void startExecuting() {
			animTick = 0;
			navigator.clearPath();
			this.attackTarget.stopRiding();
			double x = this.attackTarget.posX - posX;
			double z = this.attackTarget.posZ - posZ;
			double d = Math.sqrt(x * x + z * z);
			this.attackTarget.setMotion(x / d * 0.800000011920929D, 1.600000023841858D, z / d * 0.800000011920929D);

			if (this.attackTarget instanceof ServerPlayerEntity) {
				this.attackTarget.velocityChanged = true;
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return currentAttackID == 3 && downTime <= 0 && this.finish < 10;
		}

		@Override
		public void tick() {
			lookController.setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
			double d1;
			double d2;
			double x;
			double z;

			if (animTick == 15) {
				d1 = this.attackTarget.posX - posX;
				d2 = this.attackTarget.posY - posY;
				x = this.attackTarget.posZ - posZ;
				z = Math.sqrt(d1 * d1 + d2 * d2 + x * x);
				setMotion(d1 / z * 3.4000000953674316D, d2 / z * 1.399999976158142D, x / z * 3.4000000953674316D);
			} else if (animTick > 15) {
				d1 = (double)(getWidth() * 2.0F * getWidth() * 2.0F);
				d2 = getDistanceSq(this.attackTarget.posX, this.attackTarget.getBoundingBox().minY, this.attackTarget.posZ);

				if (d2 < d1 && this.hit == -1) {
					this.hit = 0;
					setThrowAttackHit(true);
					this.attackTarget.attackEntityFrom(DamageSource.causeMobDamage(MutantZombieEntity.this), (float)getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue());
					x = this.attackTarget.posX - posX;
					z = this.attackTarget.posZ - posZ;
					double d = Math.sqrt(x * x + z * z);
					this.attackTarget.setMotion(x / d * 0.6000000238418579D, -1.2000000476837158D, z / d * 0.6000000238418579D);
					this.attackTarget.hurtResistantTime = 10;

					if (this.attackTarget instanceof ServerPlayerEntity) {
						this.attackTarget.velocityChanged = true;
						EntityUtil.disableShield(this.attackTarget, DamageSource.causeMobDamage(MutantZombieEntity.this), 150);
					}

					playSound(MBSoundEvents.ENTITY_MUTANT_ZOMBIE_GRUNT, 0.3F, 0.8F + rand.nextFloat() * 0.4F);
				}

				if (this.hit >= 0) {
					++this.hit;
				}

				if ((onGround || isInWater() || isInLava()) && this.finish == -1) {
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
			sendPacket(0);
			this.attackTarget = null;
			this.hit = -1;
			this.finish = -1;
			setThrowAttackHit(false);
			setThrowAttackFinish(false);
		}
	}
}