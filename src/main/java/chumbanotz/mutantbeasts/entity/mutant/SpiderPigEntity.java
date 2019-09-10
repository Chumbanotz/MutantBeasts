package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.ai.goal.AvoidDamageGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBHurtByTargetGoal;
import chumbanotz.mutantbeasts.entity.ai.goal.MBMeleeAttackGoal;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NonTamedTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpiderPigEntity extends TameableEntity {
	private static final Ingredient TEMPTATION_ITEMS = Ingredient.fromItems(Items.CARROT, Items.POTATO, Items.BEETROOT, Items.PORKCHOP, Items.SPIDER_EYE);
	public int lastJumpTick;
	public int jumpTick;
	public int chargingTick;
	public int exhaustAmount;
	public boolean prevPlayerJumping;
	public boolean chargeExhausted;
	public boolean jumping;
	private final List<SpiderPigEntity.BlockCoord> webList = new ArrayList<>();

	public SpiderPigEntity(EntityType<? extends SpiderPigEntity> type, World worldIn) {
		super(type, worldIn);
		this.setTamed(false);
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.1D, false));
		this.goalSelector.addGoal(2, new SpiderPigEntity.JumpAttackGoal());
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.1D, this::isChild));
		this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0D, 10.0F, 5.0F));
		this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, false, TEMPTATION_ITEMS));
		this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.1D));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(9, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new MBHurtByTargetGoal(this).setCallsForHelp());
		this.targetSelector.addGoal(4, new NonTamedTargetGoal<>(this, CreeperMinionEntity.class, true, CreeperMinionEntity.IS_TAMED));
		this.targetSelector.addGoal(5, new NonTamedTargetGoal<>(this, MobEntity.class, true, this::isPigOrSpider));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	public boolean isClimbing() {
		return (this.dataManager.get(TAMED) & 2) != 0;
	}

	public void setClimbing(boolean climbing) {
		byte b0 = this.dataManager.get(TAMED);
		this.dataManager.set(TAMED, climbing ? (byte)(b0 | 2) : (byte)(b0 & -3));
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.ARTHROPOD;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return sizeIn.height * 0.75F;
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		return new ClimberPathNavigator(this, worldIn);
	}

	@Override
	public boolean isPotionApplicable(EffectInstance potioneffectIn) {
		return potioneffectIn.getPotion() != Effects.POISON && super.isPotionApplicable(potioneffectIn);
	}

	@Override
	public boolean canBeLeashedTo(PlayerEntity player) {
		return super.canBeLeashedTo(player) && this.isTamed();
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return TEMPTATION_ITEMS.test(stack);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isRemote) {
			this.setClimbing(this.collidedHorizontally);
			this.lastJumpTick = Math.max(0, this.lastJumpTick - 1);

			if (this.jumpTick > 10 && this.onGround) {
				this.jumping = false;
			}

			this.updateWebList(false);
			this.updateChargeState();
		}
	}

	protected void updateWebList(boolean onlyCheckSize) {
		SpiderPigEntity.BlockCoord first;

		if (!onlyCheckSize) {
			for (int i = 0; i < this.webList.size(); ++i) {
				SpiderPigEntity.BlockCoord coord = this.webList.get(i);
				BlockState block = this.world.getBlockState(coord);

				if (block.getMaterial() != Material.WEB) {
					this.webList.remove(i);
					--i;
				} else {
					--coord.timeLeft;
				}
			}

			if (!this.webList.isEmpty()) {
				first = this.webList.get(0);

				if (first.timeLeft < 0) {
					this.webList.remove(0);
					this.world.playEvent(2001, first, Block.getStateId(Blocks.COBWEB.getDefaultState()));
					this.world.setBlockState(first, Blocks.AIR.getDefaultState());
				}
			}
		}

		while (this.webList.size() > 12) {
			first = this.webList.remove(0);
			this.world.playEvent(2001, first, Block.getStateId(Blocks.COBWEB.getDefaultState()));
			this.world.setBlockState(first, Blocks.AIR.getDefaultState());
		}
	}

	protected void updateChargeState() {
		if (this.exhaustAmount >= 120) {
			this.chargeExhausted = true;
		}

		if (this.exhaustAmount <= 0) {
			this.chargeExhausted = false;
		}

		this.exhaustAmount = Math.max(0, this.exhaustAmount - 1);

		if (this.chargingTick > 0) {
			for (Entity entity : EntityUtil.getCollidingEntities(this, this.world, this.getBoundingBox())) {
				if (entity != this.getRidingEntity()) {
					entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, (LivingEntity)this.getRidingEntity()), (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue() * 0.5F);
				}
			}
		}

		this.chargingTick = Math.max(0, this.chargingTick - 1);
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		if (this.isTamed()) {
			if (itemstack.isFood() && TEMPTATION_ITEMS.test(itemstack) && this.getHealth() < this.getMaxHealth()) {
				this.heal((float)itemstack.getItem().getFood().getHealing());
				this.consumeItemFromStack(player, itemstack);
				return true;
			}
		}

		return super.processInteract(player, hand);
	}

	@Override
	public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
		if (!(target instanceof CreeperEntity)) {
			if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
				return false;
			} else if (target instanceof IronGolemEntity && ((IronGolemEntity)target).isPlayerCreated() || target instanceof SnowGolemEntity) {
				return false;
			} else if (target instanceof TameableEntity && ((TameableEntity)target).getOwner() == owner) {
				return false;
			} else {
				return !(target instanceof AbstractHorseEntity) || !((AbstractHorseEntity)target).isTame();
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		this.jumping = false;
		boolean dealDamage = true;
		boolean spiderType = entityIn instanceof SpiderEntity || entityIn instanceof SpiderPigEntity;

		if (this.rand.nextInt(2) == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			double dx = entityIn.posX - entityIn.prevPosX;
			double dz = entityIn.posZ - entityIn.prevPosZ;
			int x = (int)(entityIn.posX + dx * 0.5D);
			int y = MathHelper.floor(this.getBoundingBox().minY);
			int z = (int)(entityIn.posZ + dz * 0.5D);
			Material material = this.world.getBlockState(new BlockPos(x, y, z)).getMaterial();

			if (!material.isSolid() && !material.isLiquid() && material != Material.WEB && !spiderType) {
				this.world.setBlockState(new BlockPos(x, y, z), Blocks.COBWEB.getDefaultState());
				this.webList.add(new SpiderPigEntity.BlockCoord(x, y, z));
				this.updateWebList(true);
				this.setMotion(0.0D, Math.max(0.25D, this.getMotion().y), 0.0D);
				this.fallDistance = 0.0F;
			} else {
				dealDamage = true;
			}
		}

		if (dealDamage) {
			float damage = (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();

			if (entityIn.world.getBlockState(entityIn.getPosition()).getMaterial() == Material.WEB && !spiderType) {
				damage += 4.0F;
			}

			entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
		}

		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		Entity entity = source.getTrueSource();
		return this.isBeingRidden() && entity != null && this.isRidingOrBeingRiddenBy(entity) ? false : super.attackEntityFrom(source, amount);
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
	}

	@Override
	public boolean canBeSteered() {
		return this.getControllingPassenger() instanceof LivingEntity;
	}

	@Override
	public void travel(Vec3d p_213352_1_) {
		if (this.isAlive()) {
			if (this.isBeingRidden() && this.canBeSteered()) {
				LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
				this.rotationYaw = livingentity.rotationYaw;
				this.prevRotationYaw = this.rotationYaw;
				this.rotationPitch = livingentity.rotationPitch * 0.5F;
				this.setRotation(this.rotationYaw, this.rotationPitch);
				this.renderYawOffset = this.rotationYaw;
				this.rotationYawHead = this.renderYawOffset;
				float f = livingentity.moveStrafing * 0.8F;
				float f1 = livingentity.moveForward * 0.6F;
				this.stepHeight = 1.0F;

				this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
				if (this.canPassengerSteer()) {
					this.setAIMoveSpeed((float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
					super.travel(new Vec3d((double)f, p_213352_1_.y, (double)f1));
				} else if (livingentity instanceof PlayerEntity) {
					this.setMotion(Vec3d.ZERO);
				}

				this.prevLimbSwingAmount = this.limbSwingAmount;
				double d2 = this.posX - this.prevPosX;
				double d3 = this.posZ - this.prevPosZ;
				float f4 = MathHelper.sqrt(d2 * d2 + d3 * d3) * 4.0F;
				if (f4 > 1.0F) {
					f4 = 1.0F;
				}

				this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
				this.limbSwing += this.limbSwingAmount;
			} else {
				this.stepHeight = 0.6F;
				this.jumpMovementFactor = 0.02F;
				super.travel(p_213352_1_);
			}
		}
	}

	@Override
	public void onKillEntity(LivingEntity entityLivingIn) {
		if (!this.world.isRemote) {
			if (entityLivingIn instanceof CreeperMinionEntity) {
				CreeperMinionEntity minion = (CreeperMinionEntity)entityLivingIn;

				if (minion.isTamed() && this.getHealth() <= 8.0F && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, (PlayerEntity)minion.getOwner())) {
					this.playTameEffect(true);
					this.world.setEntityState(this, (byte)7);
					this.setTamedBy((PlayerEntity)minion.getOwner());
				}

				minion.remove();
			}

			if (this.isPigOrSpider(entityLivingIn)) {
				SpiderPigEntity spiderPigEntity = MBEntityType.SPIDER_PIG.create(entityLivingIn.world);
				EntityUtil.copyNBT(entityLivingIn, spiderPigEntity);
				entityLivingIn.remove();
				this.world.addEntity(spiderPigEntity);
			}
		}
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return !this.isTamed();
	}

	@Override
	public boolean isOnLadder() {
		return this.isClimbing();
	}

	@Override
	public void setMotionMultiplier(BlockState p_213295_1_, Vec3d p_213295_2_) {
		if (p_213295_1_.getBlock() != Blocks.COBWEB) {
			super.setMotionMultiplier(p_213295_1_, p_213295_2_);
		}
	}

	@Override
	public boolean canMateWith(AnimalEntity otherAnimal) {
		return this.isTamed() && otherAnimal instanceof SpiderPigEntity && ((SpiderPigEntity)otherAnimal).isTamed() && super.canMateWith(otherAnimal);
	}

	@Override
	public AgeableEntity createChild(AgeableEntity ageable) {
		SpiderPigEntity spiderPigEntity = MBEntityType.SPIDER_PIG.create(this.world);
		UUID uuid = this.getOwnerId();

		if (uuid != null) {
			spiderPigEntity.setOwnerId(uuid);
			spiderPigEntity.setTamed(true);
		}

		return spiderPigEntity;
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();

		if (!this.world.isRemote && !this.webList.isEmpty()) {
			for (SpiderPigEntity.BlockCoord coord : this.webList) {
				BlockState block = this.world.getBlockState(coord);

				if (block.getMaterial() == Material.WEB) {
					this.world.playEvent(2001, coord, Block.getStateId(Blocks.COBWEB.getDefaultState()));
					this.world.setBlockState(coord, Blocks.AIR.getDefaultState());
				}
			}
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

		if (!this.webList.isEmpty()) {
			ListNBT listnbt = new ListNBT();

			for (SpiderPigEntity.BlockCoord coord : this.webList) {
				CompoundNBT compound1 = NBTUtil.writeBlockPos(coord);
				compound1.putInt("TimeLeft", coord.timeLeft);
				listnbt.add(compound1);
			}

			compound.put("Webs", listnbt);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		ListNBT listnbt = compound.getList("Webs", 10);

		for (int i = 0; i < listnbt.size(); i++) {
			CompoundNBT compound1 = listnbt.getCompound(i);
			this.webList.add(i, new SpiderPigEntity.BlockCoord(NBTUtil.readBlockPos(compound1), compound1.getInt("TimeLeft")));
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_PIG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_PIG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_PIG_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
		this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
	}

	private boolean isPigOrSpider(LivingEntity livingEntity) {
		return livingEntity.getType() == EntityType.PIG || livingEntity.getType() == EntityType.SPIDER;
	}

	class JumpAttackGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			LivingEntity target = getAttackTarget();
			return target != null && lastJumpTick <= 0 && (onGround || isInWater()) && (getDistanceSq(target) < 64.0D && getRNG().nextInt(8) == 0 || getDistanceSq(target) < 6.25D);
		}

		@Override
		public void startExecuting() {
			jumping = true;
			lastJumpTick = 15;
			LivingEntity target = getAttackTarget();
			double x = target.posX - posX;
			double y = target.posY - posY;
			double z = target.posZ - posZ;
			double d = (double)MathHelper.sqrt(x * x + y * y + z * z);
			double scale = (double)(2.0F + 0.2F * getRNG().nextFloat() * getRNG().nextFloat());
			setMotion(x / d * scale, y / d * scale * 0.5D + 0.3D, z / d * scale);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return jumping && jumpTick < 40;
		}

		@Override
		public void tick() {
			++jumpTick;
		}

		@Override
		public void resetTask() {
			jumpTick = 0;
		}
	}

	static class BlockCoord extends BlockPos {
		public int timeLeft;

		public BlockCoord(int x, int y, int z) {
			super(x, y, z);
			this.timeLeft = 1200;
		}

		public BlockCoord(BlockPos pos, int timeLeft) {
			super(pos);
			this.timeLeft = timeLeft;
		}
	}
}