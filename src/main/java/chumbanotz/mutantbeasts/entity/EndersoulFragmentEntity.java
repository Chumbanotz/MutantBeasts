package chumbanotz.mutantbeasts.entity;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.util.EntityUtil;
import chumbanotz.mutantbeasts.util.MBSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EndermanEntity;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class EndersoulFragmentEntity extends Entity {
	public static final Predicate<Entity> IS_VALID_TARGET = entity -> EntityPredicates.CAN_AI_TARGET.test(entity) && entity.canBeCollidedWith() && !(entity instanceof EndersoulFragmentEntity) && !(entity instanceof MutantEndermanEntity) && !(entity instanceof EndermanEntity);
	private static final DataParameter<Boolean> COLLECTED = EntityDataManager.createKey(EndersoulFragmentEntity.class, DataSerializers.BOOLEAN);
	private int explodeTick = 20 + this.rand.nextInt(20);
	public final float[][] stickRotations = new float[8][3];
	private WeakReference<MutantEndermanEntity> owner;
	private PlayerEntity collector;

	public EndersoulFragmentEntity(EntityType<? extends EndersoulFragmentEntity> type, World world) {
		super(type, world);
		for (int i = 0; i < this.stickRotations.length; ++i) {
			for (int j = 0; j < this.stickRotations[i].length; ++j) {
				this.stickRotations[i][j] = this.rand.nextFloat() * 2.0F * 3.1415927F;
			}
		}
	}

	public EndersoulFragmentEntity(World world, MutantEndermanEntity owner) {
		this(MBEntityType.ENDERSOUL_FRAGMENT, world);
		this.owner = new WeakReference<>(owner);
	}

	public EndersoulFragmentEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(MBEntityType.ENDERSOUL_FRAGMENT, world);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(COLLECTED, false);
	}

	public boolean isCollected() {
		return this.dataManager.get(COLLECTED);
	}

	public void setCollected(boolean collected) {
		this.dataManager.set(COLLECTED, collected);
	}

	public PlayerEntity getCollector() {
		return this.collector;
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
	public boolean canBeAttackedWithItem() {
		return !this.isCollected();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			EntityUtil.spawnLargePortalParticles(this, 64, 0.8F, false);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		Vec3d vec3d = this.getMotion();
		if (this.collector == null && vec3d.y > -0.05000000074505806D) {
			this.setMotion(vec3d.x, Math.max(-0.05000000074505806D, vec3d.y - 0.10000000149011612D), vec3d.z);
		}

		this.move(MoverType.SELF, this.getMotion());
		this.setMotion(this.getMotion().scale(0.9D));

		if (this.collector != null) {
			if (!this.collector.isAlive() || !this.collector.isAddedToWorld() || this.collector.isSpectator()) {
				this.collector = null;
			}

			if (!this.world.isRemote && this.getDistanceSq(this.collector) > 9.0D) {
				float scale = 0.05F;
				this.addVelocity((this.collector.posX - this.posX) * (double)scale, (this.collector.posY + (double)(this.collector.getHeight() / 3.0F) - this.posY) * (double)scale, (this.collector.posZ - this.posZ) * (double)scale);
			}
		}

		if (!this.world.isRemote && !this.isCollected() && --this.explodeTick == 0) {
			this.explode();
		}
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (this.isCollected()) {
			if (this.collector == null && !player.isSneaking()) {
				this.collector = player;
				this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
				return true;
			}

			if (this.collector == player && player.isSneaking()) {
				this.collector = null;
				this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
				return true;
			}

			return false;
		} else {
			if (!this.world.isRemote) {
				this.setCollected(true);
			}

			this.collector = player;
			this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
			return true;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			if (!this.world.isRemote && this.isAlive()) {
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
					entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.owner == null ? this : this.owner.get()).setDamageBypassesArmor(), 1.0F);
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
		return this.isCollected() ? SoundCategory.NEUTRAL : SoundCategory.HOSTILE;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putBoolean("Collected", this.isCollected());
		compound.putInt("ExplodeTick", this.explodeTick);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.setCollected(compound.getBoolean("Collected"));
		if (compound.contains("ExplodeTick")) {
			this.explodeTick = compound.getInt("ExplodeTick");
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}