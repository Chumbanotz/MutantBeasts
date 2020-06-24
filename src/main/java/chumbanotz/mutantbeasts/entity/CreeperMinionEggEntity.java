package chumbanotz.mutantbeasts.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.MutatedExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class CreeperMinionEggEntity extends Entity {
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(CreeperMinionEggEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Boolean> CHARGED = EntityDataManager.createKey(CreeperMinionEggEntity.class, DataSerializers.BOOLEAN);
	private int health = 8;
	private int age = (60 + this.rand.nextInt(40)) * 1200;
	private int recentlyHit;
	private double velocityX;
	private double velocityY;
	private double velocityZ;

	public CreeperMinionEggEntity(EntityType<? extends CreeperMinionEggEntity> type, World world) {
		super(type, world);
		this.preventEntitySpawning = true;
	}

	public CreeperMinionEggEntity(World world, Entity owner) {
		this(MBEntityType.CREEPER_MINION_EGG, world);
		this.setOwnerUniqueId(owner.getUniqueID());
	}

	public CreeperMinionEggEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.CREEPER_MINION_EGG, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.empty());
		this.dataManager.register(CHARGED, false);
	}

	@Nullable
	public UUID getOwnerUniqueId() {
		return this.dataManager.get(OWNER_UNIQUE_ID).orElse(null);
	}

	public void setOwnerUniqueId(@Nullable UUID uuid) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.ofNullable(uuid));
	}

	public boolean isCharged() {
		return this.dataManager.get(CHARGED);
	}

	public void setCharged(boolean charged) {
		this.dataManager.set(CHARGED, charged);
	}

	@Override
	public double getYOffset() {
		return this.isPassenger() ? (double)this.getHeight() - (this.getRidingEntity().getPose() == Pose.SNEAKING ? 0.35D : 0.2D) : 0.0F;
	}

	@Override
	public double getMountedYOffset() {
		return (double)this.getHeight();
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.canBePushed() ? entity.getBoundingBox() : null;
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
	public boolean canRiderInteract() {
		return true;
	}

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
		this.setMotion(this.velocityX, this.velocityY, this.velocityZ);
	}

	@Override
	public void setVelocity(double x, double y, double z) {
		super.setVelocity(x, y, z);
		this.velocityX = x;
		this.velocityY = y;
		this.velocityZ = z;
	}

	private void hatch() {
		CreeperMinionEntity minion = MBEntityType.CREEPER_MINION.create(this.world);
		UUID uuid = this.getOwnerUniqueId();

		if (uuid != null) {
			PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
			if (playerEntity != null) {
				minion.setTamedBy(playerEntity);
				minion.getAISit().setSitting(true);
			} else {
				minion.setOwnerId(uuid);
			}
		}

		minion.setPowered(this.isCharged());
		minion.setPosition(this.posX, this.posY, this.posZ);
		this.world.addEntity(minion);
		this.playSound(MBSoundEvents.ENTITY_CREEPER_MINION_EGG_HATCH, 0.7F, 0.9F + this.rand.nextFloat() * 0.1F);
		this.remove();
	}

	@Override
	public void tick() {
		super.tick();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (!this.hasNoGravity()) {
			this.setMotion(this.getMotion().subtract(0.0D, 0.04, 0.0D));
		}

		this.move(MoverType.SELF, this.getMotion());
		this.setMotion(this.getMotion().scale(0.98D));
		if (this.onGround) {
			this.setMotion(this.getMotion().mul(0.7D, 0.0D, 0.7D));
		}

		if (this.isPassenger() && (this.isEntityInsideOpaqueBlock() || this.getRidingEntity().getPose() != Pose.STANDING && this.getRidingEntity().getPose() != Pose.SNEAKING || this.getRidingEntity().isSpectator())) {
			this.stopRiding();
			this.playMountSound(false);
		}

		if (!this.world.isRemote) {
			if (this.health < 8 && this.ticksExisted - this.recentlyHit > 80 && this.ticksExisted % 20 == 0) {
				++this.health;
			}

			if (--this.age <= 0 && !this.isPassenger()) {
				this.hatch();
			}
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (this.isPassenger() && player == this.getRidingEntity()) {
			this.stopRiding();
			this.playMountSound(false);
			return true;
		} else if (!player.isBeingRidden() && (player.getPose() == Pose.STANDING || player.getPose() == Pose.SNEAKING)) {
			this.startRiding(player, true);
			this.playMountSound(true);
			return true;
		} else {
			return false;
		}
	}

	private void playMountSound(boolean mount) {
		this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.7F, (mount ? 0.6F : 0.3F) + this.rand.nextFloat() * 0.1F);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source) || source.getTrueSource() == this.getRidingEntity()) {
			return false;
		} else {
			Entity entity = source.getTrueSource();
			if (entity == null || !(entity instanceof CreeperEntity) && !(entity instanceof CreeperMinionEntity)) {
				this.markVelocityChanged();

				if (source.isExplosion()) {
					if (!this.world.isRemote) {
						this.age = (int)((float)this.age - amount * 80.0F);
					}

					if (this.world instanceof ServerWorld) {
						for (int i = 0; i < (int)(amount / 2.0F); i++) {
							double d0 = this.rand.nextGaussian() * 0.02D;
							double d1 = this.rand.nextGaussian() * 0.02D;
							double d2 = this.rand.nextGaussian() * 0.02D;
							((ServerWorld)this.world).spawnParticle(ParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), 0, d0, d1, d2, 1.0D);
						}
					}

					return false;
				} else {
					this.recentlyHit = this.ticksExisted;
					this.setMotion(0.0D, 0.2D, 0.0D);

					if (!this.world.isRemote) {
						this.health = (int)((float)this.health - amount);
					}

					if (this.health <= 0) {
						MutatedExplosion.create(this, this.isCharged() ? 2.0F : 0.0F, false, MutatedExplosion.Mode.DESTROY);
						if (!this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
							if (this.rand.nextInt(this.isCharged() ? 2 : 3) == 0) {
								this.entityDropItem(MBItems.CREEPER_SHARD);
							} else for (int j = 5 + this.rand.nextInt(6); j > 0; --j) {
								this.entityDropItem(Items.GUNPOWDER);
							}
						}

						this.remove();
					}

					return true;
				}
			} else {
				return false;
			}
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putInt("Health", this.health);
		compound.putInt("Age", this.age);
		compound.putInt("RecentlyHit", this.recentlyHit);
		if (this.isCharged()) {
			compound.putBoolean("Charged", true);
		}

		if (this.getOwnerUniqueId() != null) {
			compound.putUniqueId("OwnerUUID", this.getOwnerUniqueId());
		}
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.health = compound.getInt("Health");
		if (compound.contains("Age")) {
			this.age = compound.getInt("Age");
		}

		this.recentlyHit = compound.getInt("RecentlyHit");
		this.setCharged(compound.getBoolean("Charged"));
		if (compound.hasUniqueId("OwnerUUID")) {
			this.setOwnerUniqueId(compound.getUniqueId("OwnerUUID"));
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}