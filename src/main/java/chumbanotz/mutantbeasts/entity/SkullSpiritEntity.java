package chumbanotz.mutantbeasts.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class SkullSpiritEntity extends Entity {
	private static final DataParameter<Boolean> GRABBED = EntityDataManager.createKey(SkullSpiritEntity.class, DataSerializers.BOOLEAN);
	public MobEntity target;
	private int startTick;
	private int grabTick;

	public SkullSpiritEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(null, worldIn);
	}

	public SkullSpiritEntity(World worldIn, MobEntity target) {
		super(null, worldIn);
		this.startTick = 15;
		this.grabTick = 80 + this.rand.nextInt(40);
		this.target = target;
		this.noClip = true;
		// this.setSize(0.1F, 0.1F);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(GRABBED, false);
	}

	protected boolean getGrabbed() {
		return this.dataManager.get(GRABBED);
	}

	protected void setGrabbed(boolean flag) {
		this.dataManager.set(GRABBED, flag);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.target != null && this.target.isAlive()) {
			if (this.getGrabbed()) {
				if (!this.world.isRemote) {
					--this.grabTick;
					double d0 = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
					this.target.setMotion(d0, 0.0D, d0);

					if (this.grabTick <= 0) {
						// float yaw = this.target.rotationYaw;
						this.target.setPosition(this.posX, 0.0D, this.posZ);
						this.target.remove();
						this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0F, false, Explosion.Mode.NONE);
						// MobEntity living = null;//ChemicalX.getMutantOf(this.target);
						//
						// if (living != null && this.rand.nextInt(2) == 0)
						// {
						// living.rotationYaw = yaw;
						// living.setPosition(this.posX, this.posY, this.posZ);
						// this.world.addEntity(living);
						// }

						this.remove();
					}
				}

				this.setPosition(this.target.posX, this.target.posY, this.target.posZ);

				if (this.rand.nextInt(8) == 0) {
					this.target.attackEntityFrom(DamageSource.MAGIC, 0.0F);
				}

				this.spawnSkullParticles(false);
			} else {
				this.prevPosX = this.posX;
				this.prevPosY = this.posY;
				this.prevPosZ = this.posZ;
				this.setMotion(0.0D, 0.0D, 0.0D);

				if (this.startTick-- >= 0) {
					this.setMotion(this.getMotion().add(0.0D, (double)(0.3F * (float)this.startTick / 15.0F), 0.0D));
				}

				double x = this.target.posX - this.posX;
				double y = this.target.posY - this.posY;
				double z = this.target.posZ - this.posZ;
				double d = Math.sqrt(x * x + y * y + z * z);
				this.setMotion(this.getMotion().add(x / d * 0.20000000298023224D, y / d * 0.20000000298023224D, z / d * 0.20000000298023224D));
				this.move(MoverType.SELF, this.getMotion());

				if (!this.world.isRemote && this.getDistanceSq(this.target) < 1.0D) {
					this.setGrabbed(true);
				}

				this.spawnSkullParticles(true);
			}
		} else {
			this.remove();
		}
	}

	public void spawnSkullParticles(boolean flag) {
		int i;
		if (flag) {
			for (i = 0; i < 16; ++i) {
				float xx = (this.rand.nextFloat() - 0.5F) * 1.2F;
				float yy = (this.rand.nextFloat() - 0.5F) * 1.2F;
				float zz = (this.rand.nextFloat() - 0.5F) * 1.2F;
				this.world.addParticle(null, this.posX + (double)xx, this.posY + (double)yy, this.posZ + (double)zz, 0.0D, 0.0D, 0.0D);
			}
		} else {
			for (i = 0; i < 3; ++i) {
				double posX = this.target.posX + (double)(this.rand.nextFloat() * this.target.getWidth() * 2.0F) - (double)this.target.getWidth();
				double posY = this.target.posY + 0.5D + (double)(this.rand.nextFloat() * this.target.getHeight());
				double posZ = this.target.posZ + (double)(this.rand.nextFloat() * this.target.getWidth() * 2.0F) - (double)this.target.getWidth();
				double x = this.rand.nextGaussian() * 0.02D;
				double y = this.rand.nextGaussian() * 0.02D;
				double z = this.rand.nextGaussian() * 0.02D;
				this.world.addParticle(null, posX, posY, posZ, x, y, z);
			}
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}