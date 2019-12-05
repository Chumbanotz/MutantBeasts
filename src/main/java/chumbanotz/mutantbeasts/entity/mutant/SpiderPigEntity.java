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
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IJumpingMount;
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
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpiderPigEntity extends TameableEntity implements IJumpingMount {
	private static final DataParameter<Boolean> CLIMBING = EntityDataManager.createKey(SpiderPigEntity.class, DataSerializers.BOOLEAN);
	private static final Ingredient TEMPTATION_ITEMS = Ingredient.fromItems(Items.CARROT, Items.POTATO, Items.BEETROOT, Items.PORKCHOP, Items.SPIDER_EYE);
	private int lastJumpTick;
	private int jumpTick;
	private int chargingTick;
	private int exhaustAmount;
	private boolean chargeExhausted;
	private float jumpPower;
	private boolean jumping;
	private final List<SpiderPigEntity.WebPos> webList = new ArrayList<>(12);

	public SpiderPigEntity(EntityType<? extends SpiderPigEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new MBMeleeAttackGoal(this, 1.1D).setMaxAttackTick(16));
		this.goalSelector.addGoal(2, new SpiderPigEntity.JumpAttackGoal());
		this.goalSelector.addGoal(3, new AvoidDamageGoal(this, 1.1D, this::isChild));
		this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0D, 10.0F, 5.0F));
		this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, false, Ingredient.fromItems(Items.CARROT_ON_A_STICK)));
		this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, false, TEMPTATION_ITEMS));
		this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.1D));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(9, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new MBHurtByTargetGoal(this).setCallsForHelp());
		this.targetSelector.addGoal(4, new NonTamedTargetGoal<>(this, MobEntity.class, true, SpiderPigEntity::isPigOrSpider));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(CLIMBING, false);
	}

	public boolean isBesideClimbableBlock() {
		return this.dataManager.get(CLIMBING);
	}

	private void setBesideClimbableBlock(boolean climbing) {
		this.dataManager.set(CLIMBING, climbing);
	}

	public boolean isSaddled() {
		return (this.dataManager.get(TAMED) & 2) != 0;
	}

	private void setSaddled(boolean saddled) {
		byte b0 = this.dataManager.get(TAMED);
		this.dataManager.set(TAMED, saddled ? (byte)(b0 | 2) : (byte)(b0 & -3));
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

		if (this.exhaustAmount >= 120) {
			this.chargeExhausted = true;
		}

		if (this.exhaustAmount <= 0) {
			this.chargeExhausted = false;
		}

		this.exhaustAmount = Math.max(0, this.exhaustAmount - 1);
		if (!this.world.isRemote) {
			this.targetSelector.setFlag(Goal.Flag.TARGET, !this.isChild());
			this.setBesideClimbableBlock(this.collidedHorizontally);
			this.lastJumpTick = Math.max(0, this.lastJumpTick - 1);

			if (this.jumpTick > 10 && this.onGround) {
				this.jumping = false;
			}

			if (this.jumping) {
				this.fallDistance = 0.0F;
			}

			this.updateWebList(false);
			this.updateChargeState();

			if (this.isAlive() && this.isTamed() && this.ticksExisted % 600 == 0) {
				this.heal(1.0F);
			}
		}
	}

	private void updateWebList(boolean onlyCheckSize) {
		SpiderPigEntity.WebPos first;

		if (!onlyCheckSize) {
			for (int i = 0; i < this.webList.size(); ++i) {
				SpiderPigEntity.WebPos coord = this.webList.get(i);

				if (this.world.getBlockState(coord) != Blocks.COBWEB.getDefaultState()) {
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
					this.removeWeb(first);
				}
			}
		}

		while (this.webList.size() > 12) {
			first = this.webList.remove(0);
			this.removeWeb(first);
		}
	}

	private void removeWeb(BlockPos pos) {
		if (this.world.getBlockState(pos) == Blocks.COBWEB.getDefaultState()) {
			this.world.playEvent(2001, pos, Block.getStateId(Blocks.COBWEB.getDefaultState()));
			this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	private void updateChargeState() {
		if (this.chargingTick > 0) {
			for (LivingEntity livingEntity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox())) {
				if (livingEntity != this && livingEntity != this.getControllingPassenger() && livingEntity != this.getOwner() && livingEntity.attackable()) {
					this.attackEntityAsMob(livingEntity);
				}
			}
		}

		this.chargingTick = Math.max(0, this.chargingTick - 1);
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (this.isTamed()) {
			if (itemstack.isFood() && this.isBreedingItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
				this.heal((float)itemstack.getItem().getFood().getHealing());
				this.consumeItemFromStack(player, itemstack);
				return true;
			}

			if (itemstack.getItem() == Items.SADDLE) {
				if (!player.isSneaking() && !this.isSaddled() && !this.isChild()) {
					this.setSaddled(true);
					this.world.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
					this.consumeItemFromStack(player, itemstack);
					return true;
				}
			} else if (this.isSaddled() && !this.isBeingRidden()) {
				if (!player.isSneaking()) {
					if (!this.world.isRemote) {
						player.startRiding(this);
						this.navigator.clearPath();
					}

					return true;
				} else if (this.isOwner(player)) {
					this.setSaddled(false);
					this.world.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
					if (!this.world.isRemote) {
						this.entityDropItem(Items.SADDLE);
					}

					return true;
				}
			}
		}

		return super.processInteract(player, hand);
	}

	@Override
	public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
		return EntityUtil.shouldAttackEntity(target, owner, false);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		float damage = (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
		boolean spiderType = entityIn instanceof SpiderEntity || entityIn instanceof SpiderPigEntity;

		if ((!this.isBeingRidden() || flag) && this.rand.nextInt(2) == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
			double dx = entityIn.posX - entityIn.prevPosX;
			double dz = entityIn.posZ - entityIn.prevPosZ;
			BlockPos pos = new BlockPos((int)(entityIn.posX + dx * 0.5D), MathHelper.floor(this.getBoundingBox().minY), (int)(entityIn.posZ + dz * 0.5D));
			Material material = this.world.getBlockState(pos).getMaterial();

			if (!material.isSolid() && !material.isLiquid() && material != Material.WEB && !spiderType) {
				this.world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
				this.webList.add(new SpiderPigEntity.WebPos(pos, this.isBeingRidden() ? 600 : 1200));
				this.updateWebList(true);
				this.setMotion(0.0D, Math.max(0.25D, this.getMotion().y), 0.0D);
				this.fallDistance = 0.0F;
			}
		}

		if (entityIn.world.getBlockState(entityIn.getPosition()).getMaterial() == Material.WEB && !spiderType) {
			damage += 4.0F;
		}

		return flag;
	}

	@Override
	public boolean canJump() {
		return this.isSaddled() && !chargeExhausted;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setJumpPower(int jumpPowerIn) {
		exhaustAmount += 50 * jumpPowerIn / 100;
		System.out.println("exhaustAmount = " + exhaustAmount);
		this.jumpPower = 1.0F * (float)jumpPowerIn / 100.0F;
		System.out.println("jumpPower = " + jumpPower);
	}

	@Override
	public void handleStartJump(int jumpPowerIn) {
		chargingTick = 8 * jumpPowerIn / 100;
	}

	@Override
	public void handleStopJump() {
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked() || this.isBeingRidden() && this.isSaddled();
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
	public void travel(Vec3d vec3d) {
		if (this.isBeingRidden() && this.canBeSteered()) {
			LivingEntity passenger = (LivingEntity)this.getControllingPassenger();
			this.stepHeight = 1.0F;
			this.prevRotationYaw = this.rotationYaw = this.rotationYawHead = passenger.rotationYaw;
			this.prevRotationPitch = this.rotationPitch = passenger.rotationPitch * 0.4F;
			this.setRotation(this.rotationYaw, this.rotationPitch);

			while (this.renderYawOffset > this.rotationYawHead + 180.0F) {
				this.renderYawOffset -= 360.0F;
			}

			while (this.renderYawOffset < this.rotationYawHead - 180.0F) {
				this.renderYawOffset += 360.0F;
			}

			if (!chargeExhausted && jumpPower > 0 && (this.onGround || this.world.containsAnyLiquid(getBoundingBox()))) {
				float pitch = rotationPitch;
				rotationPitch = 0.0F;
				rotationPitch = pitch;
				double power = 1.600000023841858D * (double)jumpPower;
				setMotion(getLookVec().x * power, 0.30000001192092896D, getLookVec().z * power);
				this.isAirBorne = true;
				jumpPower = 0;
			}

			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
			if (this.canPassengerSteer()) {
				float strafe = passenger.moveStrafing * 0.8F;
				float forward = passenger.moveForward * 0.6F;
				this.setAIMoveSpeed((float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
				super.travel(new Vec3d((double)strafe, vec3d.y, (double)forward));
			} else if (passenger instanceof PlayerEntity) {
				this.setMotion(Vec3d.ZERO);
			}
		} else {
			this.stepHeight = 0.6F;
			this.jumpMovementFactor = 0.02F;
			super.travel(vec3d);
		}
	}

	@Override
	public void onKillEntity(LivingEntity entityLivingIn) {
		if (!this.world.isRemote) {
			if (entityLivingIn instanceof CreeperMinionEntity && ((CreeperMinionEntity)entityLivingIn).getOwner() instanceof PlayerEntity && !this.isTamed()) {
				CreeperMinionEntity minion = (CreeperMinionEntity)entityLivingIn;
				if (minion.isTamed() && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, (PlayerEntity)minion.getOwner())) {
					this.playTameEffect(true);
					this.world.setEntityState(this, (byte)7);
					this.setTamedBy((PlayerEntity)minion.getOwner());
				}

				minion.remove();
			}

			if (isPigOrSpider(entityLivingIn)) {
				SpiderPigEntity spiderPigEntity = MBEntityType.SPIDER_PIG.create(this.world);
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
		return this.isBesideClimbableBlock();
	}

	@Override
	public void setMotionMultiplier(BlockState blockState, Vec3d motionMultiplier) {
		if (blockState.getBlock() != Blocks.COBWEB) {
			super.setMotionMultiplier(blockState, motionMultiplier);
		}
	}

	@Override
	public AgeableEntity createChild(AgeableEntity ageable) {
		if (this.rand.nextInt(20) == 0) {
			return EntityType.PIG.create(this.world);
		} else {
			SpiderPigEntity spiderPig = MBEntityType.SPIDER_PIG.create(this.world);
			UUID uuid = this.getOwnerId();
			if (uuid != null) {
				spiderPig.setOwnerId(uuid);
				spiderPig.setTamed(true);
			}
			return spiderPig;
		}
	}

	@Override
	protected void dropInventory() {
		if (this.isSaddled()) {
			this.entityDropItem(Items.SADDLE);
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		MBHurtByTargetGoal.alertOthers(this);
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (!this.world.isRemote && !this.webList.isEmpty()) {
			this.webList.forEach(this::removeWeb);
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putBoolean("Saddled", this.isSaddled());

		if (!this.webList.isEmpty()) {
			ListNBT listnbt = new ListNBT();
			for (SpiderPigEntity.WebPos coord : this.webList) {
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
		this.setSaddled(compound.getBoolean("Saddled"));
		ListNBT listnbt = compound.getList("Webs", 10);
		for (int i = 0; i < listnbt.size(); i++) {
			CompoundNBT compound1 = listnbt.getCompound(i);
			this.webList.add(i, new SpiderPigEntity.WebPos(NBTUtil.readBlockPos(compound1), compound1.getInt("TimeLeft")));
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MBSoundEvents.ENTITY_SPIDER_PIG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MBSoundEvents.ENTITY_SPIDER_PIG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MBSoundEvents.ENTITY_SPIDER_PIG_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
		this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
	}

	private static boolean isPigOrSpider(LivingEntity livingEntity) {
		return livingEntity.getType() == EntityType.PIG || livingEntity.getType() == EntityType.SPIDER;
	}

	class JumpAttackGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			LivingEntity target = getAttackTarget();
			return target != null && lastJumpTick <= 0 && (onGround || isInWater()) && (getDistanceSq(target) < 64.0D && rand.nextInt(8) == 0 || getDistanceSq(target) < 6.25D);
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
			double scale = (double)(2.0F + 0.2F * rand.nextFloat() * rand.nextFloat());
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

	static class WebPos extends BlockPos {
		private int timeLeft;

		public WebPos(BlockPos pos, int timeLeft) {
			super(pos);
			this.timeLeft = timeLeft;
		}
	}
}