package chumbanotz.mutantbeasts.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import chumbanotz.mutantbeasts.util.MutatedExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
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
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class CreeperMinionEggEntity extends Entity {
	private static final DataParameter<Boolean> POWERED = EntityDataManager.createKey(CreeperMinionEggEntity.class, DataSerializers.BOOLEAN);
	private int health = 8;
	private int age = (60 + this.rand.nextInt(40)) * 1200;
	private int recentlyHit;
	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private UUID ownerUUID;

	public CreeperMinionEggEntity(EntityType<? extends CreeperMinionEggEntity> type, World world) {
		super(type, world);
		this.preventEntitySpawning = true;
	}

	public CreeperMinionEggEntity(World world, Entity owner) {
		this(MBEntityType.CREEPER_MINION_EGG, world);
		this.ownerUUID = owner.getUniqueID();
	}

	public CreeperMinionEggEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.CREEPER_MINION_EGG, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(POWERED, false);
	}

	public boolean isPowered() {
		return this.dataManager.get(POWERED);
	}

	public void setPowered(boolean powered) {
		this.dataManager.set(POWERED, powered);
	}

	@Override
	public double getYOffset() {
		if (this.getRidingEntity() instanceof PlayerEntity) {
			return this.getHeight() - (this.getRidingEntity().getPose() == Pose.SNEAKING ? 0.35D : 0.2D);
		}

		return 0.0F;
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
		if (this.ownerUUID != null) {
			PlayerEntity playerEntity = this.world.getPlayerByUuid(this.ownerUUID);
			if (playerEntity != null && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(minion, playerEntity)) {
				minion.setTamedBy(playerEntity);
				minion.getAISit().setSitting(true);
			}
		}

		if (this.isPowered()) {
			minion.setPowered(true);
		}

		minion.setPosition(this.posX, this.posY, this.posZ);
		this.world.addEntity(minion);
		this.playSound(MBSoundEvents.ENTITY_CREEPER_MINION_EGG_HATCH, 0.7F, 0.9F + this.rand.nextFloat() * 0.1F);
		this.remove();
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity lightningBolt) {
		super.onStruckByLightning(lightningBolt);
		this.setPowered(true);
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

			if (--this.age <= 0) {
				this.hatch();
			}
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (this.getRidingEntity() == player) {
			this.getTopPassenger(player).stopRiding();
			this.playMountSound(false);
			return true;
		} else if (player.getPose() == Pose.STANDING || player.getPose() == Pose.SNEAKING) {
			this.startRiding(this.getTopPassenger(player), true);
			this.playMountSound(true);
			return true;
		} else {
			return false;
		}
	}

	private Entity getTopPassenger(Entity entity) {
		List<Entity> list = entity.getPassengers();
		return !list.isEmpty() ? this.getTopPassenger(list.get(0)) : entity;
	}

	private void playMountSound(boolean mount) {
		this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.7F, (mount ? 0.6F : 0.3F) + this.rand.nextFloat() * 0.1F);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source) || source.getTrueSource() == this.getRidingEntity()) {
			return false;
		} else if (!this.world.isRemote && this.isAlive()) {
			this.markVelocityChanged();
			if (source.isExplosion()) {
				this.age = (int)((float)this.age - amount * 80.0F);
				EntityUtil.sendParticlePacket(this, ParticleTypes.HEART, (int)(amount / 2.0F));
				return false;
			} else {
				this.recentlyHit = this.ticksExisted;
				this.health = (int)((float)this.health - amount);

				if (this.health <= 0) {
					MutatedExplosion.create(this, this.isPowered() ? 2.0F : 0.0F, false, MutatedExplosion.Mode.BREAK);
					if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
						if (this.isPowered() || this.rand.nextInt(3) == 0) {
							this.entityDropItem(MBItems.CREEPER_SHARD);
						} else for (int i = 5 + this.rand.nextInt(6); i > 0; --i) {
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

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putInt("Health", this.health);
		compound.putInt("Age", this.age);
		compound.putInt("RecentlyHit", this.recentlyHit);
		if (this.isPowered()) {
			compound.putBoolean("Powered", true);
		}

		if (this.ownerUUID != null) {
			compound.putUniqueId("OwnerUUID", this.ownerUUID);
		}
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		if (compound.contains("Health")) {
			this.health = compound.getInt("Health");
		}

		if (compound.contains("Age")) {
			this.age = compound.getInt("Age");
		}

		this.recentlyHit = compound.getInt("RecentlyHit");
		this.setPowered(compound.getBoolean("Charged") || compound.getBoolean("Powered"));
		if (compound.hasUniqueId("OwnerUUID")) {
			this.ownerUUID = compound.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}