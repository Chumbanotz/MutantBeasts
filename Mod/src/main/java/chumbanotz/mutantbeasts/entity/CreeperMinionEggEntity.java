package chumbanotz.mutantbeasts.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class CreeperMinionEggEntity extends Entity {
	public int health = this.getMaxHealth();
	public int age;
	public float rotationRoll;
	protected int recentlyHit;
	@OnlyIn(Dist.CLIENT)
	private double velX;
	@OnlyIn(Dist.CLIENT)
	private double velY;
	@OnlyIn(Dist.CLIENT)
	private double velZ;

	public CreeperMinionEggEntity(World world) {
		super(null, world);
		int minToTick = 1200;
		this.age = (60 + this.rand.nextInt(40)) * minToTick;
		this.preventEntitySpawning = true;
		// this.setSize(0.5625F, 0.75F);
	}

	@Override
	protected void registerData() {
	}

	@Override
	public double getYOffset() {
		return this.getRidingEntity() != null && this.getRidingEntity() instanceof PlayerEntity ? -1.0625D : (double)this.getYOffset();
	}

	@Override
	public double getMountedYOffset() {
		return (double)this.getHeight();
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.getBoundingBox();
	}

	public AxisAlignedBB getBoundingBox() {
		return this.getRidingEntity() != null ? null : this.getBoundingBox();
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive();
	}

	@Override
	public boolean canBePushed() {
		return this.isAlive();
	}

	public int getMaxHealth() {
		return 8;
	}

	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int i) {
		// super.setPositionAndRotation2(x, y, z, yaw, pitch, i);
		// this.motionX = this.velX;
		// this.motionY = this.velY;
		// this.motionZ = this.velZ;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double x, double y, double z) {
		super.setVelocity(x, y, z);
		this.velX = x;
		this.velY = y;
		this.velZ = z;
	}

	protected void move() {
		if (this.getRidingEntity() == null) {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.setMotion(this.getMotion().subtract(0.0D, 0.03999999910593033D, 0.0D));
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().mul(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

			if (this.onGround) {
				this.setMotion(this.getMotion().mul(0.699999988079071D, 0.0D, 0.699999988079071D));
			}
		}
	}

	public void hatchEgg() {
		CreeperMinionEntity minion = new CreeperMinionEntity(null, this.world);
		// minion.setOwner(this.getOwner());
		minion.setSitting(true);
		minion.setHealth(minion.getMaxHealth());
		minion.setPosition(this.posX, this.posY, this.posZ);
		this.world.addEntity(minion);
		// this.world.playSoundAtEntity(minion,
		// "MutantCreatures:mutantcreeper.egghatch", 0.7F, 0.9F + this.rand.nextFloat()
		// * 0.1F);
		// int x = MathHelper.floor(this.posX);
		// int y = MathHelper.floor(this.getBoundingBox().minY);
		// int z = MathHelper.floor(this.posZ);
		this.remove();
	}

	@Override
	public void tick() {
		super.tick();
		this.move();

		if (!this.world.isRemote) {
			--this.age;

			if (this.health < this.getMaxHealth() && this.ticksExisted - this.recentlyHit > 80 && this.ticksExisted % 20 == 0) {
				++this.health;
			}

			if (this.age <= 0 && this.getRidingEntity() == null) {
				this.hatchEgg();
			}
		}

	}

	public static void entityMountEntity(Entity entity, Entity entity1) {
		// if (entity != null && !entity.world.isRemote)
		// {
		// entity.mountEntity(entity1);
		// }
	}

	protected void removeTopEgg() {
		// if (this.riddenByEntity != null && this.riddenByEntity instanceof
		// CreeperMinionEggEntity)
		// {
		// CreeperMinionEggEntity egg = (CreeperMinionEggEntity)this.riddenByEntity;
		// egg.removeTopEgg();
		// }
		// else
		// {
		// this.mountEntity((Entity)null);
		// }
	}

	protected void mountEgg(CreeperMinionEggEntity egg) {
		// if (egg.riddenByEntity == null)
		// {
		// this.mountEntity(egg);
		// }
		// else if (!(egg.riddenByEntity instanceof CreeperMinionEgg))
		// {
		// entityMountEntity(egg.riddenByEntity, (Entity) null);
		// this.mountEntity(egg);
		// }
		// else
		// {
		// CreeperMinionEgg eggRiding = (CreeperMinionEgg) egg.riddenByEntity;
		// this.mountEgg(eggRiding);
		// }
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		boolean mount = false;
		Entity entity = player.getRidingEntity();

		if (!this.world.isRemote) {
			if (entity == null) {
				this.startRiding(player);
				mount = true;
			} else if (entity instanceof CreeperMinionEggEntity) {
				if (entity == this) {
					this.removeTopEgg();
				} else {
					this.mountEgg((CreeperMinionEggEntity)entity);
					mount = true;
				}
			}

			this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.7F, (mount ? 0.6F : 0.3F) + this.rand.nextFloat() * 0.1F);
		}

		return super.processInitialInteract(player, hand);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float f) {
		if (this.getRidingEntity() != null) {
			return false;
		} else {
			Entity entity = source.getTrueSource();

			if (entity == null || !(entity instanceof CreeperEntity)) {
				if (source.isExplosion()) {
					if (!this.world.isRemote) {
						this.age = (int)((float)this.age - f * 80.0F);
					}

					if (!this.world.isRemote) {
						// MCHandler.spawnHeartsAtEntity(this, (int) (f / 2.0F));
					}

					return false;
				} else {
					this.recentlyHit = this.ticksExisted;
					this.setMotion(0.0D, 0.2D, 0.0D);

					if (!this.world.isRemote) {
						this.health = (int)((float)this.health - f);
					}

					if (this.health <= 0) {
						if (!this.world.isRemote) {
							this.world.createExplosion(null, source, this.posX, this.posY, this.posZ, 0.0F, true, Explosion.Mode.BREAK);

							if (this.rand.nextInt(3) == 0) {
								// this.dropItem(MutantCreatures.creeperShard, 1);
							} else {
								for (int j = 5 + this.rand.nextInt(6); j > 0; --j) {
									// this.dropItem(Items.gunpowder, 1);
								}
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
	protected void readAdditional(CompoundNBT compound) {
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}