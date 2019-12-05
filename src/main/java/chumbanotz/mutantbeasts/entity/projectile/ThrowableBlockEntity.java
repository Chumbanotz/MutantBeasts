package chumbanotz.mutantbeasts.entity.projectile;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.IndirectDamageSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class ThrowableBlockEntity extends ThrowableEntity {
	private static final DataParameter<Optional<BlockState>> BLOCK_STATE = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	private static final DataParameter<Byte> BALLISTICS = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.BYTE);
	private static final DataParameter<Boolean> HELD = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.BOOLEAN);
	private UUID ownerUUID;

	public ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, LivingEntity livingEntityIn, World worldIn) {
		super(MBEntityType.THROWABLE_BLOCK, livingEntityIn, worldIn);
		this.ownerUUID = livingEntityIn.getUniqueID();
	}

	public ThrowableBlockEntity(World world, MutantEndermanEntity enderman, int id) {
		this(MBEntityType.THROWABLE_BLOCK, enderman, world);
		this.setBlockState(Block.getStateById(enderman.heldBlock[id]));
		boolean outer = (id <= 2);
		boolean right = (id & 1) == 1;
		LivingEntity living = enderman.getAttackTarget();
		Vec3d forward = EntityUtil.getDirVector(this.rotationYaw, outer ? 2.7F : 1.4F);
		Vec3d strafe = EntityUtil.getDirVector(this.rotationYaw + (right ? 90.0F : -90.0F), outer ? 2.2F : 2.0F);
		this.posX += forward.x + strafe.x;
		this.posY += (outer ? 2.8F : 1.1F) - enderman.getEyeHeight();
		this.posZ += forward.z + strafe.z;
		if (living != null)
			this.shoot(living.posX - this.posX, living.posY - this.posY, living.posZ - this.posZ, 1.4F, 1.0F);
	}

	public ThrowableBlockEntity(MutantSnowGolemEntity mutantSnowGolem, World worldIn) {
		super(MBEntityType.THROWABLE_BLOCK, mutantSnowGolem, worldIn);
		this.rotationYaw = mutantSnowGolem.rotationYaw;
		this.setBlockState(mutantSnowGolem.getIceBlock());
		this.setBallistics(1);
	}

	public ThrowableBlockEntity(World world, PlayerEntity player, BlockState blockState, int data) {
		this(MBEntityType.THROWABLE_BLOCK, player, world);
		this.setBlockState(Block.getStateById(data));
		this.setPosition(this.posX, this.posY, this.posZ);
		this.setHeld(true);
	}

	public ThrowableBlockEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(MBEntityType.THROWABLE_BLOCK, worldIn);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(BLOCK_STATE, Optional.of(Blocks.GRASS_BLOCK.getDefaultState()));
		this.dataManager.register(BALLISTICS, (byte)0);
		this.dataManager.register(HELD, false);
	}

	public BlockState getBlockState() {
		return this.dataManager.get(BLOCK_STATE).orElse(Blocks.GRASS_BLOCK.getDefaultState());
	}

	public void setBlockState(BlockState state) {
		this.dataManager.set(BLOCK_STATE, Optional.of(state));
	}

	public byte getBallistics() {
		return this.dataManager.get(BALLISTICS);
	}

	private void setBallistics(int id) {
		this.dataManager.set(BALLISTICS, (byte)id);
	}

	public boolean isBeingHeld() {
		return this.dataManager.get(HELD);
	}

	private void setHeld(boolean held) {
		this.dataManager.set(HELD, held);
	}

	@Override
	protected float getGravityVelocity() {
		if (this.isBeingHeld()) {
			return 0.0F;
		} else {
			if (this.getThrower() != null && this.getThrower() instanceof PlayerEntity) {
				return 0.04F;
			}

			return this.getBallistics() == 1 ? 0.06F : 0.01F;
		}
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isBeingHeld() && this.isAlive();
	}

	@Override
	public boolean canBePushed() {
		return this.isBeingHeld() && this.isAlive();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			for (int i = 0; i < 60; ++i) {
				double x = this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
				double y = this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight());
				double z = this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
				double motx = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
				double moty = (double)(0.5F + this.rand.nextFloat() * 2.0F);
				double motz = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
				this.world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, this.getBlockState()), x, y, z, motx, moty, motz);
			}
		}
	}

	@Override
	public void tick() {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;

		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.baseTick();
		RayTraceResult raytraceresult = ProjectileHelper.rayTrace(this, true, this.ticksExisted >= 25, this.owner, RayTraceContext.BlockMode.COLLIDER);

		if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
			this.onImpact(raytraceresult);
		}

		Vec3d vec3d = this.getMotion();
		this.posX += vec3d.x;
		this.posY += vec3d.y;
		this.posZ += vec3d.z;
		ProjectileHelper.rotateTowardsMovement(this, 0.2F);
		float f;

		if (this.isInWater()) {
			for (int i = 0; i < 4; ++i) {
				this.world.addParticle(ParticleTypes.BUBBLE, this.posX - vec3d.x * 0.25D, this.posY - vec3d.y * 0.25D, this.posZ - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
			}

			f = 0.8F;
		} else {
			f = 0.99F;
		}

		this.setMotion(vec3d.scale((double)f));

		if (!this.hasNoGravity()) {
			Vec3d vec3d1 = this.getMotion();
			this.setMotion(vec3d1.x, vec3d1.y - (double)this.getGravityVelocity(), vec3d1.z);
		}

		this.setPosition(this.posX, this.posY, this.posZ);
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (!this.world.isRemote && this.isBeingHeld() && this.getThrower() == player) {
			if (player.isSneaking()) {
				return false;
			}

			this.setHeld(false);
			this.moveTowardsHeading(player);
			ItemStack stack = player.getHeldItem(hand);
			if (stack != null && stack.getItem() == MBItems.ENDERSOUL_HAND) {
				stack.damageItem(1, player, e -> e.sendBreakAnimation(player.getActiveHand()));
			}

			player.swingArm(hand);
		}

		return false;
	}

	private void moveTowardsHeading(LivingEntity living) {
		this.rotationYaw = living.rotationYaw;
		this.rotationPitch = living.rotationPitch;
		float f = 0.4F;
		float PI = 3.1415927F;
		this.setMotion((double)(-MathHelper.sin(this.rotationYaw / 180.0F * PI) * MathHelper.cos(this.rotationPitch / 180.0F * PI) * f), (double)(-MathHelper.sin(this.rotationPitch / 180.0F * PI) * f), (double)(MathHelper.cos(this.rotationYaw / 180.0F * PI) * MathHelper.cos(this.rotationPitch / 180.0F * PI) * f));
		this.shoot(this.getMotion().x, this.getMotion().y, this.getMotion().z, 1.4F, 1.0F);
	}

	@Override
	@Nullable
	public LivingEntity getThrower() {
		if (this.owner == null && this.ownerUUID != null && this.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(this.ownerUUID);
			if (entity instanceof LivingEntity) {
				this.owner = (LivingEntity)entity;
			} else {
				this.ownerUUID = null;
			}
		}

		return this.owner;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (this.getBallistics() == 1) {
			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(2.5D, 2.0D, 2.5D))) {
				if (this.canHarm(entity) && this.getDistanceSq(entity) <= 6.25D) {
					entity.attackEntityFrom(new IndirectDamageSource("mob", this, this.getThrower()), this.getDamageByBlock() + (float)(this.rand.nextInt(3)));
				}
			}

			if (result.getType() == RayTraceResult.Type.ENTITY) {
				Entity entity = ((EntityRayTraceResult)result).getEntity();
				if (this.canHarm(entity)) {
					entity.attackEntityFrom(new IndirectDamageSource("thrown", this, this.getThrower()), this.getDamageByBlock());
				}
			}

			if (!this.world.isRemote) {
				this.world.setEntityState(this, (byte)3);
				this.remove();
			}

			this.playSound(this.getBlockState().getSoundType().getBreakSound(), 0.8F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.8F);
		} else {
			if (this.isBeingHeld()) {
				return;
			}

			LivingEntity thrower = this.getThrower();
			if (result.getType() == RayTraceResult.Type.BLOCK) {
				BlockState blockstate = this.world.getBlockState(((BlockRayTraceResult)result).getPos());
				if (blockstate.getCollisionShape(this.world, ((BlockRayTraceResult)result).getPos()).isEmpty()) {
					return;
				}
			}

			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(2.0D))) {
				if (entity != this.getThrower() && this.getDistanceSq(entity) <= 4.0D) {
					double x = entity.posX - this.posX;
					double z = entity.posZ - this.posZ;
					double d = Math.sqrt(x * x + z * z);
					entity.setMotion(x / d * 0.6000000238418579D, 0.20000000298023224D, z / d * 0.6000000238418579D);
					entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)(6 + this.rand.nextInt(3)));
				}
			}

			boolean hitEntity = result.getType() == RayTraceResult.Type.ENTITY;
			if (hitEntity) {
				((EntityRayTraceResult)result).getEntity().attackEntityFrom(DamageSource.causeMobDamage(thrower), 4.0F);
			}

			BlockPos pos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
			boolean grief = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, thrower);
			if (thrower != null && thrower instanceof PlayerEntity) {
				grief = true;
			}

			boolean play = true;
			Block block = this.getBlockState().getBlock();
			boolean fail = false;
			if (block == Blocks.AIR) {
				fail = true;
			} else if (this.world.getBlockState(pos).isSolid()) {
				if (!this.world.isRemote && grief && !hitEntity) {
					this.world.setBlockState(pos, this.getBlockState());
				}
			} else if (this.world.getBlockState(pos.up()).isSolid()) {
				if (!this.world.isRemote && grief && !hitEntity) {
					this.world.setBlockState(pos.up(), this.getBlockState());
				}

				this.world.playEvent(2001, pos.up(), Block.getStateId(this.getBlockState()));
				play = false;
			}

			if (!this.world.isRemote && fail) {
				this.entityDropItem(block);
			}

			if (play) {
				this.world.playEvent(2001, pos, Block.getStateId(this.getBlockState()));
			}

			if (!hitEntity) {
				this.remove();
			}
		}
	}

	private float getDamageByBlock() {
		Block block = this.getBlockState().getBlock();
		return block == Blocks.PACKED_ICE ? 5.0F : block == Blocks.BLUE_ICE ? 6.0F : 4.0F;
	}

	private boolean canHarm(Entity entity) {
		if (entity == this.owner) {
			return false;
		} else {
			return entity instanceof MobEntity && entity instanceof IMob;
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.put("BlockState", NBTUtil.writeBlockState(this.getBlockState()));
		if (this.ownerUUID != null) {
			compound.putUniqueId("OwnerUUID", this.ownerUUID);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		this.setBlockState(NBTUtil.readBlockState(compound.getCompound("BlockState")));
		if (compound.hasUniqueId("OwnerUUID")) {
			this.ownerUUID = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}