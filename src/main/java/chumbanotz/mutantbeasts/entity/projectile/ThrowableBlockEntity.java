package chumbanotz.mutantbeasts.entity.projectile;

import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class ThrowableBlockEntity extends ThrowableEntity implements IEntityAdditionalSpawnData {
	private static final DataParameter<OptionalInt> THROWER_ENTITY_ID = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.OPTIONAL_VARINT);
	private static final DataParameter<Boolean> HELD = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.BOOLEAN);
	private BlockState blockState = Blocks.GRASS_BLOCK.getDefaultState();
	private UUID ownerUUID;

	public ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, LivingEntity livingEntityIn, World worldIn) {
		super(MBEntityType.THROWABLE_BLOCK, livingEntityIn.posX, livingEntityIn.posY + (double)livingEntityIn.getEyeHeight() - (double)0.1F, livingEntityIn.posZ, worldIn);
		this.setThrower(livingEntityIn);
	}

	public ThrowableBlockEntity(World world, MutantEndermanEntity enderman, int id) {
		this(MBEntityType.THROWABLE_BLOCK, enderman, world);
		this.blockState = Block.getStateById(enderman.heldBlock[id]);
		boolean outer = (id <= 2);
		boolean right = (id & 1) == 1;
		LivingEntity attackTarget = enderman.getAttackTarget();
		Vec3d forward = EntityUtil.getDirVector(this.rotationYaw, outer ? 2.7F : 1.4F);
		Vec3d strafe = EntityUtil.getDirVector(this.rotationYaw + (right ? 90.0F : -90.0F), outer ? 2.2F : 2.0F);
		this.posX += forward.x + strafe.x;
		this.posY += (outer ? 2.8F : 1.1F) - 4.8D;
		this.posZ += forward.z + strafe.z;
		if (attackTarget != null) {
			this.shoot(attackTarget.posX - this.posX, attackTarget.posY + (double)attackTarget.getEyeHeight() - this.posY, attackTarget.posZ - this.posZ, 1.4F, 1.0F);
		} else if (enderman.isAlive()) {
			this.throwBlock(enderman);
		}
	}

	public ThrowableBlockEntity(MutantSnowGolemEntity mutantSnowGolem, World worldIn) {
		this(MBEntityType.THROWABLE_BLOCK, mutantSnowGolem, worldIn);
		this.rotationYaw = mutantSnowGolem.rotationYaw;
		this.blockState = Blocks.ICE.getDefaultState();
	}

	public ThrowableBlockEntity(World world, PlayerEntity player, BlockState blockState, BlockPos pos) {
		this(MBEntityType.THROWABLE_BLOCK, player, world);
		this.blockState = blockState;
		this.setPosition((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D);
		this.setHeld(true);
	}

	public ThrowableBlockEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(MBEntityType.THROWABLE_BLOCK, worldIn);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(THROWER_ENTITY_ID, OptionalInt.empty());
		this.dataManager.register(HELD, false);
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	@Override
	@Nullable
	public LivingEntity getThrower() {
		if (this.owner == null && this.ownerUUID != null && this.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(this.ownerUUID);
			if (entity instanceof LivingEntity) {
				this.setThrower((LivingEntity)entity);
			}

			this.ownerUUID = null;
		}

		return this.owner;
	}

	private void setThrower(LivingEntity thrower) {
		this.owner = thrower;
		this.ownerUUID = thrower.getUniqueID();
		this.dataManager.set(THROWER_ENTITY_ID, OptionalInt.of(thrower.getEntityId()));
	}

	public boolean isHeld() {
		return this.dataManager.get(HELD);
	}

	private void setHeld(boolean held) {
		this.dataManager.set(HELD, held);
	}

	@Override
	protected float getGravityVelocity() {
		if (this.owner instanceof PlayerEntity) {
			return 0.04F;
		} else if (this.owner instanceof MutantSnowGolemEntity) {
			return 0.06F;
		} else {
			return 0.01F;
		}
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive() && !(this.owner instanceof MutantSnowGolemEntity);
	}

	@Override
	public boolean canBePushed() {
		return this.isHeld() && this.isAlive();
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (entityIn != this.owner) {
			super.applyEntityCollision(entityIn);
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			for (int i = 0; i < 60; ++i) {
				double x = this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
				double y = this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight());
				double z = this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
				double motx = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
				double moty = (double)(0.5F + this.rand.nextFloat() * 2.0F);
				double motz = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
				this.world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, this.blockState), x, y, z, motx, moty, motz);
			}
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (THROWER_ENTITY_ID.equals(key)) {
			OptionalInt optionalInt = this.dataManager.get(THROWER_ENTITY_ID);
			if (optionalInt.isPresent()) {
				Entity entity = this.world.getEntityByID(optionalInt.getAsInt());
				this.owner = entity instanceof LivingEntity ? (LivingEntity)entity : null;
			} else {
				this.owner = null;
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

		if (this.isHeld()) {
			if (this.owner == null) {
				this.setHeld(false);
				return;
			}

			Vec3d vec = this.owner.getLookVec();
			double x = this.owner.posX + vec.x * 1.6D - this.posX;
			double y = this.owner.posY + this.owner.getEyeHeight() + vec.y * 1.6D - this.posY;
			double z = this.owner.posZ + vec.z * 1.6D - this.posZ;
			float offset = 0.6F;
			this.setMotion(x * offset, y * offset, z * offset);
			this.move(MoverType.SELF, this.getMotion());
			if (!this.world.isRemote && (!this.owner.isAlive() || this.owner.isSpectator() || !this.owner.canEntityBeSeen(this) || !EntityUtil.isHolding(this.owner, MBItems.ENDERSOUL_HAND))) {
				this.setHeld(false);
			}
		} else {
			RayTraceResult raytraceresult = ProjectileHelper.rayTrace(this, true, false, this.owner, RayTraceContext.BlockMode.COLLIDER);

			if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				this.onImpact(raytraceresult);
			}

			Vec3d motion = this.getMotion();
			this.posX += motion.x;
			this.posY += motion.y;
			this.posZ += motion.z;
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			float f;

			if (this.isInWater()) {
				for (int i = 0; i < 4; ++i) {
					this.world.addParticle(ParticleTypes.BUBBLE, this.posX - motion.x * 0.25D, this.posY - motion.y * 0.25D, this.posZ - motion.z * 0.25D, motion.x, motion.y, motion.z);
				}

				f = 0.8F;
			} else {
				f = 0.99F;
			}

			this.setMotion(motion.scale((double)f));

			if (!this.hasNoGravity()) {
				Vec3d vec3d1 = this.getMotion();
				this.setMotion(vec3d1.x, vec3d1.y - (double)this.getGravityVelocity(), vec3d1.z);
			}

			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (player.isSneaking()) {
			return false;
		} else if (player.getHeldItem(hand).getItem() != MBItems.ENDERSOUL_HAND) {
			return false;
		} else if (this.isHeld()) {
			if (this.owner == player) {
				if (!this.world.isRemote) {
					this.setHeld(false);
					this.throwBlock(player);
				}

				player.swingArm(hand);
				player.getHeldItem(hand).damageItem(1, player, e -> e.sendBreakAnimation(hand));
				return true;
			}

			return false;
		} else {
			this.setHeld(true);
			this.setThrower(player);
			return true;
		}
	}

	private void throwBlock(LivingEntity thrower) {
		this.rotationYaw = thrower.rotationYaw;
		this.rotationPitch = thrower.rotationPitch;
		float f = 0.4F;
		float PI = 3.1415927F;
		this.setMotion((double)(-MathHelper.sin(this.rotationYaw / 180.0F * PI) * MathHelper.cos(this.rotationPitch / 180.0F * PI) * f), (double)(-MathHelper.sin(this.rotationPitch / 180.0F * PI) * f), (double)(MathHelper.cos(this.rotationYaw / 180.0F * PI) * MathHelper.cos(this.rotationPitch / 180.0F * PI) * f));
		this.shoot(this.getMotion().x, this.getMotion().y, this.getMotion().z, 1.4F, 1.0F);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (this.owner instanceof MutantSnowGolemEntity) {
			for (LivingEntity livingEntity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(2.5D, 2.0D, 2.5D))) {
				if (MutantSnowGolemEntity.canHarm(this.owner, livingEntity) && this.getDistanceSq(livingEntity) <= 6.25D) {
					livingEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.owner), 4.0F + (float)(this.rand.nextInt(3)));
				}
			}

			if (result.getType() == RayTraceResult.Type.ENTITY) {
				Entity entity = ((EntityRayTraceResult)result).getEntity();
				if (MutantSnowGolemEntity.canHarm(this.owner, entity)) {
					entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner), 4.0F);
				}
			}

			if (!this.world.isRemote) {
				this.world.setEntityState(this, (byte)3);
				this.remove();
			}

			this.playSound(this.blockState.getSoundType().getBreakSound(), 0.8F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.8F);
		} else {
			BlockPos pos = this.getPosition();
			if (result.getType() == RayTraceResult.Type.BLOCK) {
				if (!this.world.isRemote) {
					if (canPlaceBlock(this.world, this.blockState, pos, this.owner)) {
						SoundType soundType = this.blockState.getSoundType(this.world, pos, this.owner);
						this.playSound(soundType.getPlaceSound(), (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
					} else {
						this.world.playEvent(2001, pos, Block.getStateId(this.blockState));
						if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
							Block.spawnDrops(this.blockState, this.world, pos);
						}
					}
				}
			} else if (result.getType() == RayTraceResult.Type.ENTITY && !this.world.isRemote) {
				((EntityRayTraceResult)result).getEntity().attackEntityFrom(DamageSource.causeThrownDamage(this, this.owner), 4.0F);
				this.world.playEvent(2001, pos, Block.getStateId(this.blockState));
				if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
					Block.spawnDrops(this.blockState, this.world, pos);
				}
			}

			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(2.0D))) {
				if (!entity.isEntityEqual(this.owner) && entity.canBeCollidedWith() && this.getDistanceSq(entity) <= 4.0D) {
					double x = entity.posX - this.posX;
					double z = entity.posZ - this.posZ;
					double d = Math.sqrt(x * x + z * z);
					entity.setMotion(x / d * 0.6000000238418579D, 0.20000000298023224D, z / d * 0.6000000238418579D);
					entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner), (float)(6 + this.rand.nextInt(3)));
				}
			}

			if (!this.world.isRemote) {
				this.remove();
			}
		}
	}

	private static boolean canPlaceBlock(World world, BlockState blockState, BlockPos pos, @Nullable LivingEntity thrower) {
		if (thrower instanceof PlayerEntity && !((PlayerEntity)thrower).isAllowEdit()) {
			return false;
		} else if (thrower instanceof MobEntity && !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, thrower)) {
			return false;
		} else {
			return blockState.isValidPosition(world, pos) && world.setBlockState(pos, blockState);
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.put("BlockState", NBTUtil.writeBlockState(this.blockState));
		compound.putBoolean("Held", this.isHeld());
		if (this.ownerUUID != null) {
			compound.putUniqueId("OwnerUUID", this.ownerUUID);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		if (compound.contains("BlockState", 10)) {
			this.blockState = NBTUtil.readBlockState(compound.getCompound("BlockState"));
		}

		this.setHeld(compound.getBoolean("Held"));
		if (compound.hasUniqueId("OwnerUUID")) {
			this.ownerUUID = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeInt(Block.getStateId(this.blockState));
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.blockState = Block.getStateById(additionalData.readInt());
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}