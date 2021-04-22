package chumbanotz.mutantbeasts.entity;

import java.util.function.Predicate;

import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class EndersoulFragmentEntity extends Entity {
	public static final Predicate<Entity> IS_VALID_TARGET = EntityPredicates.CAN_AI_TARGET.and(entity -> {
		EntityType<?> type = entity.getType();
		return type != EntityType.ITEM && type != EntityType.EXPERIENCE_ORB && type != EntityType.END_CRYSTAL && type != MBEntityType.ENDERSOUL_CLONE && type != MBEntityType.ENDERSOUL_FRAGMENT && type != MBEntityType.MUTANT_ENDERMAN && type != EntityType.ENDER_DRAGON && type != EntityType.ENDERMAN;
	});
	private static final DataParameter<Boolean> TAMED = EntityDataManager.createKey(EndersoulFragmentEntity.class, DataSerializers.BOOLEAN);
	private int explodeTick = 20 + this.rand.nextInt(20);
	public final float[][] stickRotations = new float[8][3];
	private PlayerEntity owner;

	public EndersoulFragmentEntity(EntityType<? extends EndersoulFragmentEntity> type, World world) {
		super(type, world);
		this.preventEntitySpawning = true;
		for (int i = 0; i < this.stickRotations.length; ++i) {
			for (int j = 0; j < this.stickRotations[i].length; ++j) {
				this.stickRotations[i][j] = this.rand.nextFloat() * 2.0F * (float)Math.PI;
			}
		}
	}

	public EndersoulFragmentEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.ENDERSOUL_FRAGMENT, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(TAMED, false);
	}

	public boolean isTamed() {
		return this.dataManager.get(TAMED);
	}

	public void setTamed(boolean tamed) {
		this.dataManager.set(TAMED, tamed);
	}

	public PlayerEntity getOwner() {
		return this.owner;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive();
	}

	@Override
	public boolean canBePushed() {
		return this.isAlive();
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			EntityUtil.spawnEndersoulParticles(this, 64, 0.8F);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		Vec3d vec3d = this.getMotion();
		if (this.owner == null && vec3d.y > -0.05000000074505806D && !this.hasNoGravity()) {
			this.setMotion(vec3d.x, Math.max(-0.05000000074505806D, vec3d.y - 0.10000000149011612D), vec3d.z);
		}

		this.move(MoverType.SELF, this.getMotion());
		this.setMotion(this.getMotion().scale(0.9D));

		if (this.owner != null && (!this.owner.isAlive() || !this.owner.isAddedToWorld() || this.owner.isSpectator())) {
			this.owner = null;
		}

		if (!this.world.isRemote) {
			if (!this.isTamed() && --this.explodeTick == 0) {
				this.explode();
			}

			if (this.owner != null && this.getDistanceSq(this.owner) > 9.0D) {
				float scale = 0.05F;
				this.addVelocity((this.owner.posX - this.posX) * (double)scale, (this.owner.posY + (double)(this.owner.getHeight() / 3.0F) - this.posY) * (double)scale, (this.owner.posZ - this.posZ) * (double)scale);
			}
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (this.isTamed()) {
			if (this.owner == null && !player.isSneaking()) {
				this.owner = player;
				this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
				return true;
			}

			if (this.owner == player && player.isSneaking()) {
				this.owner = null;
				this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
				return true;
			}

			return false;
		} else {
			if (!this.world.isRemote) {
				this.setTamed(true);
			}

			this.owner = player;
			this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
			return true;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			if (!this.world.isRemote && this.isAlive() && this.ticksExisted > 0) {
				this.explode();
			}

			return true;
		}
	}

	private void explode() {
		this.playSound(MBSoundEvents.ENTITY_ENDERSOUL_FRAGMENT_EXPLODE, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		this.world.setEntityState(this, (byte)3);
		for (Entity entity : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().grow(5.0D), IS_VALID_TARGET)) {
			if (this.getDistanceSq(entity) <= 25.0D) {
				boolean hitChance = this.rand.nextInt(3) != 0;
				if (isProtected(entity)) {
					hitChance = this.rand.nextInt(3) == 0;
				} else {
					double x = entity.posX - this.posX;
					double z = entity.posZ - this.posZ;
					double d = Math.sqrt(x * x + z * z);
					entity.setMotion(0.800000011920929D * x / d, (double)(this.rand.nextFloat() * 0.6F - 0.1F), 0.800000011920929D * z / d);
					EntityUtil.sendPlayerVelocityPacket(entity);
				}

				if (hitChance) {
					entity.attackEntityFrom(DamageSource.MAGIC, 1.0F);
				}
			}
		}

		this.remove();
	}

	public static boolean isProtected(Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		} else {
			return EntityUtil.isHolding((LivingEntity)entity, MBItems.ENDERSOUL_HAND);
		}
	}

	@Override
	public SoundCategory getSoundCategory() {
		return this.isTamed() ? SoundCategory.NEUTRAL : SoundCategory.HOSTILE;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putBoolean("Tamed", this.isTamed());
		compound.putInt("ExplodeTick", this.explodeTick);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.setTamed(compound.getBoolean("Collected") || compound.getBoolean("Tamed"));
		if (compound.contains("ExplodeTick")) {
			this.explodeTick = compound.getInt("ExplodeTick");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}