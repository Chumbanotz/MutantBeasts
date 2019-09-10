package chumbanotz.mutantbeasts.entity.projectile;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class ThrowableBlockEntity extends ThrowableEntity {
	private static final DataParameter<Optional<BlockState>> BLOCK_STATE = EntityDataManager.createKey(ThrowableBlockEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	private UUID ownerUUID;

	protected ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(type, worldIn);
	}

	protected ThrowableBlockEntity(EntityType<? extends ThrowableBlockEntity> type, LivingEntity livingEntityIn, World worldIn) {
		super(type, livingEntityIn, worldIn);
		this.ownerUUID = livingEntityIn.getUniqueID();
	}

	@Override
	protected void registerData() {
		this.dataManager.register(BLOCK_STATE, Optional.of(this.getDefaultBlockState()));
	}

	protected abstract BlockState getDefaultBlockState();

	public BlockState getBlockState() {
		return this.dataManager.get(BLOCK_STATE).orElse(this.getDefaultBlockState());
	}

	public void setBlockState(BlockState state) {
		this.dataManager.set(BLOCK_STATE, Optional.of(state));
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
		RayTraceResult raytraceresult = ProjectileHelper.func_221266_a(this, true, this.ticksExisted >= 25, this.owner, RayTraceContext.BlockMode.COLLIDER);

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