package chumbanotz.mutantbeasts.entity.projectile;

import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.entity.EndersoulFragmentEntity;
import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class ThrowableBlockEntity extends ThrowableEntity implements IEntityAdditionalSpawnData {
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
				this.owner = (LivingEntity)entity;
			} else {
				this.ownerUUID = null;
			}
		}

		return this.owner;
	}

	private void setThrower(LivingEntity thrower) {
		this.owner = thrower;
		this.ownerUUID = thrower.getUniqueID();
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
		return this.isHeld() && this.isAlive();
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
	public void tick() {
		if (this.isHeld()) {
			this.lastTickPosX = this.posX;
			this.lastTickPosY = this.posY;
			this.lastTickPosZ = this.posZ;

			if (!this.world.isRemote) {
				this.setFlag(6, this.isGlowing());
			}

			this.baseTick();

			if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator() || !EndersoulFragmentEntity.isProtected(this.owner)) {
				this.setHeld(false);
			} else {
				Vec3d vec = this.owner.getLookVec();
				double x = this.owner.posX + vec.x * 1.6D - this.posX;
				double y = this.owner.posY + this.owner.getEyeHeight() + vec.y * 1.6D - this.posY;
				double z = this.owner.posZ + vec.z * 1.6D - this.posZ;
				float offset = 0.6F;
				this.setMotion(x * offset, y * offset, z * offset);
				this.move(MoverType.SELF, this.getMotion());
			}
		} else {
			super.tick();
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (player.isSneaking()) {
			return false;
		}

		ItemStack itemStack = player.getHeldItem(hand);
		if (itemStack.getItem() != MBItems.ENDERSOUL_HAND) {
			return false;
		} else {
			if (this.isHeld() && this.owner == player) {
				if (!this.world.isRemote) {
					this.setHeld(false);
					this.throwBlock(player);
				}

				player.swingArm(hand);
				itemStack.damageItem(1, player, e -> e.sendBreakAnimation(hand));
				return true;
			}

			return false;
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
		if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockRayTraceResult)result).getPos();
			if (this.world.getBlockState(blockPos).getCollisionShape(this.world, blockPos).isEmpty()) {
				return;
			}
		}

		if (this.world.isRemote) {
			return;
		}

		if (this.owner instanceof MutantSnowGolemEntity) {
			if (result.getType() == RayTraceResult.Type.ENTITY) {
				Entity entity = ((EntityRayTraceResult)result).getEntity();
				if (!MutantSnowGolemEntity.canHarm(this.owner, entity) || entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.owner), 4.0F) && entity instanceof EndermanEntity) {
					return;
				}
			}

			for (LivingEntity livingEntity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(2.5D, 2.0D, 2.5D))) {
				if (MutantSnowGolemEntity.canHarm(this.owner, livingEntity) && this.getDistanceSq(livingEntity) <= 6.25D) {
					livingEntity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner), 4.0F + (float)(this.rand.nextInt(3)));
				}
			}

			this.world.setEntityState(this, (byte)3);
			this.remove();
			this.playSound(this.blockState.getSoundType().getBreakSound(), 0.8F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.8F);
		} else {
			Item item = this.blockState.getBlock().asItem();
			boolean canOwnerGrief = this.owner == null || !(this.owner instanceof MobEntity) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.owner);
			if (result.getType() == RayTraceResult.Type.BLOCK) {
				this.remove();
				if (canOwnerGrief) {
					BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult)result;
					Direction direction = blockRayTraceResult.getFace();
					DirectionalPlaceContext context = new DirectionalPlaceContext(this.world, blockRayTraceResult.getPos().offset(direction), direction, new ItemStack(item), direction.getOpposite());
					if (item instanceof BlockItem && ((BlockItem)item).tryPlace(context) == ActionResultType.SUCCESS) {
						return;
					}					
				}

				this.world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, this.getPosition(), Block.getStateId(this.blockState));
				if (canOwnerGrief && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
					this.entityDropItem(item);
				}
			} else if (result.getType() == RayTraceResult.Type.ENTITY) {
				Entity entity = ((EntityRayTraceResult)result).getEntity();
				if (entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.owner), 4.0F)) {
					if (entity instanceof EndermanEntity) {
						return;
					}

					if (this.owner != null) {
						this.applyEnchantments(this.owner, entity);	
					}
				}

				this.world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, this.getPosition(), Block.getStateId(this.blockState));
				if (canOwnerGrief && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
					this.entityDropItem(item);
				}

				this.remove();
			}

			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(2.0D))) {
				if (entity.canBeCollidedWith() && !entity.isEntityEqual(this.owner) && this.getDistanceSq(entity) <= 4.0D) {
					entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner), (float)(6 + this.rand.nextInt(3)));
				}
			}
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
		if (compound.contains("BlockState", Constants.NBT.TAG_COMPOUND)) {
			this.blockState = NBTUtil.readBlockState(compound.getCompound("BlockState"));
		}

		this.setHeld(compound.getBoolean("Held"));
		if (compound.hasUniqueId("OwnerUUID")) {
			this.ownerUUID = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeVarInt(Block.getStateId(this.blockState));
		buffer.writeVarInt(this.owner == null ? 0 : this.owner.getEntityId());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.blockState = Block.getStateById(additionalData.readVarInt());
		Entity entity = this.world.getEntityByID(additionalData.readVarInt());
		this.owner = entity instanceof LivingEntity ? (LivingEntity)entity : null;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}