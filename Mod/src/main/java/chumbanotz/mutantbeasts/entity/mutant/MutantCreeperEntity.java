package chumbanotz.mutantbeasts.entity.mutant;

import java.util.EnumSet;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.pathfinding.MBGroundPathNavigator;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantCreeperEntity extends CreeperEntity {
	private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(MutantCreeperEntity.class, DataSerializers.BYTE);
	public static final int MAX_CHARGE_TIME = 100;
	public static final int MAX_DEATH_TIME = 100;
	private int chargeTime;
	private int chargeHits;
	private boolean canSummonLightning;
	private MBMeleeAttackGoal meleeAttackGoal;

	public MutantCreeperEntity(EntityType<? extends MutantCreeperEntity> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 1.5F;
		this.experienceValue = 30;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MutantCreeperEntity.JumpAttackGoal());
		this.goalSelector.addGoal(1, new MutantCreeperEntity.SpawnMinionsGoal());
		this.goalSelector.addGoal(1, new MutantCreeperEntity.ChargeAttackGoal());
		this.goalSelector.addGoal(2, this.meleeAttackGoal = new MBMeleeAttackGoal(this, 1.3D, false));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new MBHurtByTargetGoal(this, CreeperEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AnimalEntity.class, 60, true, true, EntityUtil::isMobFeline));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_KNOCKBACK).setBaseValue(2.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(SWIM_SPEED).setBaseValue(4.5D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(STATUS, (byte)0);
	}

	@Override
	public boolean getPowered() {
		return (this.dataManager.get(STATUS) & 1) != 0;
	}

	private void setPowered(boolean powered) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, powered ? (byte)(b0 | 1) : (byte)(b0 & -2));
	}

	public boolean isJumpAttacking() {
		return (this.dataManager.get(STATUS) & 2) != 0;
	}

	private void setJumpAttacking(boolean jumping) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, jumping ? (byte)(b0 | 2) : (byte)(b0 & -3));
	}

	public boolean isCharging() {
		return (this.dataManager.get(STATUS) & 4) != 0;
	}

	private void setCharging(boolean flag) {
		byte b0 = this.dataManager.get(STATUS);
		this.dataManager.set(STATUS, flag ? (byte)(b0 | 4) : (byte)(b0 & -5));
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 2.6F;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new MBGroundPathNavigator(this, worldIn);
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)(this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
		float f = (float)this.getAttribute(SharedMonsterAttributes.ATTACK_KNOCKBACK).getValue();

		if (flag) {
			if (f > 0.0F && entityIn instanceof LivingEntity) {
				entityIn.stopRiding();
				((LivingEntity)entityIn).knockBack(this, f * 0.5F, (double)MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F))));
				this.setMotion(this.getMotion().mul(0.6D, 1.0D, 0.6D));
			}

			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.getPowered() && source.isFireDamage()) {
			this.extinguish();
			return false;
		}

		if (source.isExplosion()) {
			float f = amount / 2.0F;

			if (!(source.getTrueSource() instanceof MutantCreeperEntity) && this.isAlive() && this.getHealth() < this.getMaxHealth()) {
				this.heal(f);
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				((ServerWorld)this.world).spawnParticle(ParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), (int)(f / 2.0F), d0, d1, d2, 0.0D);
			}

			return true;
		} else if (this.isCharging()) {
			if (!source.isMagicDamage() && source.getImmediateSource() instanceof LivingEntity) {
				source.getImmediateSource().attackEntityFrom(DamageSource.causeThornsDamage(this), 2.0F);
			}

			if (!this.world.isRemote && amount > 0.0F && source.getImmediateSource() != null && this.hurtResistantTime > 10) {
				--this.chargeHits;
				MutantBeasts.LOGGER.debug("Charge hits left = " + chargeHits);
			}
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity lightningBolt) {
		this.setPowered(true);
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand) {
		return false;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	protected void func_213623_ec() {
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 6) {
			for (int i = 0; i < 15; ++i) {
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				this.world.addParticle(ParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
			}
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isRemote && this.isJumpAttacking()) {
			if (this.onGround) {
				this.setJumpAttacking(false);
				Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
				this.world.createExplosion(this, DamageSource.causeMobDamage(this).setExplosion(), this.posX, this.posY, this.posZ, this.getPowered() ? 6.0F : 4.0F, false, explosion$mode);
			}

			this.meleeAttackGoal.resetAttackTick();
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
		if (!this.isJumpAttacking() && this.deathTime == 0) {
			p_213371_1_.applyEntityCollision(this);
			p_213371_1_.velocityChanged = true;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getExplosionColor() {
		float f = (float)this.deathTime / MAX_DEATH_TIME;

		if (this.isCharging()) {
			int i = this.ticksExisted % 20;
			f = i < 10 ? 0.6F : 0.0F;
		}

		return (int)(f * 255.0F);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		if (!this.isSilent()) {
			this.world.playMovingSound(null, this, MBSoundEvents.ENTITY_MUTANT_CREEPER_DEATH, this.getSoundCategory(), 2.0F, 1.0F);
		}

		Entity entity = cause.getTrueSource();

		if (entity != null && entity instanceof PlayerEntity) {
			this.recentlyHit = Integer.MAX_VALUE;
			this.attackingPlayer = (PlayerEntity)entity;
		}
	}

	@Override
	protected void onDeathUpdate() {
		++this.deathTime;
		float f = this.getPowered() ? 12.0F : 8.0F;
		float f1 = f * 1.5F;

		for (Entity entity : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().grow((double)f1), EntityPredicates.CAN_AI_TARGET)) {
			double x = this.posX - entity.posX;
			double y = this.posY - entity.posY;
			double z = this.posZ - entity.posZ;
			double d = Math.sqrt(x * x + y * y + z * z);
			float f2 = (float)this.deathTime / (float)MAX_DEATH_TIME;
			entity.setMotion(entity.getMotion().add(x / d * (double)f2 * 0.09D, y / d * (double)f2 * 0.09D, z / d * (double)f2 * 0.09D));
		}

		this.setPosition(this.posX + (double)(this.rand.nextFloat() * 0.2F) - 0.10000000149011612D, this.posY, this.posZ + (double)(this.rand.nextFloat() * 0.2F) - 0.10000000149011612D);

		if (!this.world.areCollisionShapesEmpty(this.getBoundingBox())) {
			this.pushOutOfBlocks(this.posX, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.posZ);
		}

		if (this.deathTime >= MAX_DEATH_TIME) {
			if (!this.world.isRemote) {
				Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
				this.world.createExplosion(this, DamageSource.causeMobDamage(this).setExplosion(), this.posX, this.posY, this.posZ, f, this.isBurning(), explosion$mode);
				EntityUtil.spawnLingeringCloud(this);

				if (this.recentlyHit > 0 && this.canDropLoot() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
					int i = this.getExperiencePoints(this.attackingPlayer);
					i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.attackingPlayer, i);

					while (i > 0) {
						int j = ExperienceOrbEntity.getXPSplit(i);
						i -= j;
						this.world.addEntity(new ExperienceOrbEntity(this.world, this.posX, this.posY, this.posZ, j));
					}
				}

				if (this.attackingPlayer != null) {
					// CreeperMinionEggEntity egg = new CreeperMinionEggEntity(this.world);
					// egg.setOwner(this.killerName);
					// egg.setPosition(this.posX, this.posY, this.posZ);
					// this.world.addEntity(egg);
				}
			}

			this.remove();
		}
	}

	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
	}

	@Override
	public void setMotionMultiplier(BlockState p_213295_1_, Vec3d p_213295_2_) {
		super.setMotionMultiplier(p_213295_1_, p_213295_2_.scale(6.0D));
	}

	@Override
	public boolean ableToCauseSkullDrop() {
		return this.getPowered();
	}

	@Override
	public boolean hasIgnited() {
		return false;
	}

	@Override
	public int getCreeperState() {
		return -1;
	}

	@Override
	protected void handleFluidJump(Tag<Fluid> fluidTag) {
		this.setMotion(this.getMotion().add(0.0D, 0.04D, 0.0D));
	}

	@Override
	public float getExplosionResistance(Explosion explosionIn, IBlockReader worldIn, BlockPos pos, BlockState blockStateIn, IFluidState p_180428_5_, float p_180428_6_) {
		return this.getPowered() && blockStateIn.canEntityDestroy(worldIn, pos, this) ? Math.min(0.8F, p_180428_6_) : p_180428_6_;
	}

	@Override
	public void playAmbientSound() {
		if (this.getAttackTarget() == null) {
			super.playAmbientSound();
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_MUTANT_CREEPER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_MUTANT_CREEPER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("JumpAttacking", this.isJumpAttacking());
		compound.putBoolean("Charging", this.isCharging());
		compound.putInt("ChargeTime", this.chargeTime);
		compound.putInt("ChargeHits", this.chargeHits);
		compound.putBoolean("SummonLightning", this.canSummonLightning);

		if (this.getPowered()) {
			compound.putBoolean("Powered", true);
		}

		if (this.deathTime > 0 && this.attackingPlayer != null) {
			compound.putUniqueId("KillerUUID", this.attackingPlayer.getUniqueID());
		}

		for (String s : new String[] {"powered", "Fuse", "ExplosionRadius", "ignited"}) {
			compound.remove(s);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setPowered(compound.getBoolean("Powered"));
		this.setJumpAttacking(compound.getBoolean("JumpAttacking"));
		this.setCharging(compound.getBoolean("Charging"));
		this.chargeTime = compound.getInt("ChargeTime");
		this.chargeHits = compound.getInt("ChargeHits");
		this.canSummonLightning = compound.getBoolean("SummonLightning");

		if (compound.contains("KillerUUID")) {
			this.recentlyHit = Integer.MAX_VALUE;
			this.attackingPlayer = this.world.getPlayerByUuid(compound.getUniqueId("KillerUUID"));
		}
	}

	class SpawnMinionsGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			float chance = !hasPath() ? 1.5F : 0.6F;
			return getAttackTarget() != null && getDistanceSq(getAttackTarget()) <= 1024.0D && !isCharging() ? rand.nextFloat() * 100.0F < chance : false;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			int maxSpawn = world.getDifficulty().getId() * 2;
			for (int i = (int)Math.ceil((double)getHealth() / getMaxHealth() * (double)maxSpawn); i > 0; --i) {
				CreeperMinionEntity creeper = MBEntityType.CREEPER_MINION.create(world);
				double x = posX + (double)(rand.nextFloat() - rand.nextFloat());
				double y = posY + (double)(rand.nextFloat() * 0.5F);
				double z = posZ + (double)(rand.nextFloat() - rand.nextFloat());
				double xx = getAttackTarget().posX - posX;
				double yy = getAttackTarget().posY - posY;
				double zz = getAttackTarget().posZ - posZ;
				double d0 = 0.15D + (double)(rand.nextFloat() * 0.05F);
				creeper.setMotion(xx * d0, yy * d0, zz * d0);
				creeper.setPosition(x, y, z);
				creeper.setOwnerUniqueId(entityUniqueID);
				creeper.setAttackTarget(getAttackTarget());
				creeper.setPowered(getPowered());
				world.addEntity(creeper);
			}
		}
	}

	class ChargeAttackGoal extends Goal {
		public ChargeAttackGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
		}

		private boolean shouldAttemptToHeal() {
			return !(getMaxHealth() - getHealth() < 20.0F);
		}

		@Override
		public boolean shouldExecute() {
			LivingEntity target = getAttackTarget();
			return target != null && onGround && this.shouldAttemptToHeal() && getDistanceSq(target) >= 25.0D && getDistanceSq(target) <= 1024.0D ? rand.nextFloat() * 100.0F < 0.7F : false;
		}

		@Override
		public boolean shouldContinueExecuting() {
			if (canSummonLightning && getAttackTarget() != null && getDistanceSq(getAttackTarget()) < 25.0D) {
				return false;
			}

			return chargeTime < MAX_CHARGE_TIME && chargeHits > 0;
		}

		@Override
		public void startExecuting() {
			setCharging(true);
			getNavigator().clearPath();

			if (chargeHits == 0) {
				chargeHits = 3 + rand.nextInt(3);
			}

			if (rand.nextInt(world.isThundering() ? 2 : 6) == 0 && !getPowered()) {
				canSummonLightning = true;
			}
		}

		@Override
		public void tick() {
			int i = chargeTime % 20;

			if (i == 0 || i == 20) {
				playSound(MBSoundEvents.ENTITY_MUTANT_CREEPER_CHARGE, 0.6F, 0.7F + rand.nextFloat() * 0.6F);
			}

			++chargeTime;
		}

		@Override
		public void resetTask() {
			if (canSummonLightning && getAttackTarget() != null && getDistanceSq(getAttackTarget()) < 25.0D && world.canBlockSeeSky(getPosition())) {
				((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, false));
			} else {
				if (chargeTime >= MAX_CHARGE_TIME) {
					heal(30.0F);
					world.setEntityState(MutantCreeperEntity.this, (byte)6);
				}
			}

			chargeTime = 0;
			chargeHits = 4 + rand.nextInt(3);
			setCharging(false);
			canSummonLightning = false;
		}
	}

	class JumpAttackGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			LivingEntity target = getAttackTarget();
			return target != null && getDistanceSq(target) <= 1024.0D && onGround && !isCharging() ? rand.nextFloat() * 100.0F < 0.9F : false;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			setJumpAttacking(true);
			setMotion((getAttackTarget().posX - posX) * 0.2D, 1.4D, (getAttackTarget().posZ - posZ) * 0.2D);
		}
	}
}