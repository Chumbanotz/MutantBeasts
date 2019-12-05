package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.entity.projectile.ChemicalXEntity;
import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class SkullSpiritEntity extends Entity {
	private static final DataParameter<Boolean> ATTACHED = EntityDataManager.createKey(SkullSpiritEntity.class, DataSerializers.BOOLEAN);
	private MobEntity target;
	private int startTick = 15;
	private int attachedTick = 80 + this.rand.nextInt(40);

	public SkullSpiritEntity(EntityType<? extends SkullSpiritEntity> type, World worldIn) {
		super(type, worldIn);
		this.noClip = true;
	}

	public SkullSpiritEntity(World worldIn, MobEntity target) {
		this(MBEntityType.SKULL_SPIRIT, worldIn);
		this.target = target;
	}

	public SkullSpiritEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		this(MBEntityType.SKULL_SPIRIT, worldIn);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(ATTACHED, false);
	}

	private boolean isAttached() {
		return this.dataManager.get(ATTACHED);
	}

	private void attach(boolean flag) {
		this.dataManager.set(ATTACHED, flag);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.target != null && this.target.isAlive()) {
			if (this.isAttached()) {
				if (!this.world.isRemote) {
					this.target.setMotion((double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), this.target.getMotion().y, (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F));

					if (--this.attachedTick <= 0) {
						this.target.setPosition(this.posX, 0.0D, this.posZ);
						this.target.remove();
						this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0F, false, Explosion.Mode.NONE);
						this.dropTargetEquipment();
						MobEntity mutant = ChemicalXEntity.getMutantOf(this.target);
						if (mutant != null) {
							if (this.rand.nextFloat() < 0.75F) {
								mutant.enablePersistence();
								mutant.setPosition(this.posX, this.posY, this.posZ);
								this.world.addEntity(mutant);
								AxisAlignedBB bb = mutant.getBoundingBox().grow(1.0D);
								for (BlockPos pos : BlockPos.getAllInBoxMutable(MathHelper.floor(bb.minX), MathHelper.floor(mutant.posY), MathHelper.floor(bb.minZ), MathHelper.floor(bb.maxX), MathHelper.floor(bb.maxY), MathHelper.floor(bb.maxZ))) {
									BlockState blockState = this.world.getBlockState(pos);
									if (blockState.getMaterial().isSolid() && !net.minecraft.tags.BlockTags.WITHER_IMMUNE.contains(blockState.getBlock())) {
										this.world.removeBlock(pos, false);
									}
								}
							}
						}

						this.remove();
					}
				}

				this.setPosition(this.target.posX, this.target.posY, this.target.posZ);

				if (this.rand.nextInt(8) == 0) {
					this.target.attackEntityFrom(DamageSource.MAGIC, 0.0F);
				}

				if (this.world instanceof ServerWorld) {
					for (int i = 0; i < 3; i++) {
						double posX = this.target.posX + (this.rand.nextFloat() * this.target.getWidth() * 2.0F) - this.target.getWidth();
						double posY = this.target.posY + 0.5D + (this.rand.nextFloat() * this.target.getHeight());
						double posZ = this.target.posZ + (this.rand.nextFloat() * this.target.getWidth() * 2.0F) - this.target.getWidth();
						double x = this.rand.nextGaussian() * 0.02D;
						double y = this.rand.nextGaussian() * 0.02D;
						double z = this.rand.nextGaussian() * 0.02D;
						((ServerWorld)this.world).spawnParticle(MBParticleTypes.SKULL_SPIRIT, posX, posY, posZ, 0, x, y, z, 1.0D);
					}
				}
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
					this.attach(true);
				}

				if (this.world instanceof ServerWorld) {
					for (int i = 0; i < 16; i++) {
						float xx = (this.rand.nextFloat() - 0.5F) * 1.2F;
						float yy = (this.rand.nextFloat() - 0.5F) * 1.2F;
						float zz = (this.rand.nextFloat() - 0.5F) * 1.2F;
						((ServerWorld)this.world).spawnParticle(MBParticleTypes.SKULL_SPIRIT, this.posX + xx, this.posY + yy, this.posZ + zz, 0, 0.0D, 0.0D, 0.0D, 1.0D);
					}
				}
			}
		} else {
			this.remove();
		}
	}

	private void dropTargetEquipment() {
		if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			return;
		}

		if (this.target.getType() == EntityType.SKELETON) {
			ItemStack main = this.target.getHeldItemMainhand();
			ItemStack off = this.target.getHeldItemOffhand();
			if (!(main.getItem() instanceof BowItem) && !(main.getItem() instanceof CrossbowItem)) {
				this.entityDropItem(main);
			} else if (!(off.getItem() instanceof BowItem) && !(off.getItem() instanceof CrossbowItem)) {
				this.entityDropItem(off);
			}

			this.target.getArmorInventoryList().forEach(this::entityDropItem);
		} else {
			this.target.getEquipmentAndArmor().forEach(this::entityDropItem);
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putBoolean("Attached", this.isAttached());
		compound.putInt("AttachedTick", this.attachedTick);
		if (this.target != null) {
			compound.putUniqueId("TargetUUID", this.target.getUniqueID());
		}
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.attach(compound.getBoolean("Attached"));
		this.attachedTick = compound.getInt("AttachedTick");
		if (compound.hasUniqueId("TargetUUID") && this.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(compound.getUniqueId("TargetUUID"));
			if (entity instanceof MobEntity) {
				this.target = (MobEntity)entity;
			}
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}