package chumbanotz.mutantbeasts.entity.mutant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import chumbanotz.mutantbeasts.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MutantEndermanEntity extends EndermanEntity {
	public int currentAttackID;
	public int animTick;
	public int hasTargetTick = this.preTargetTick = 0;
	public int preTargetTick;
	public int hasTarget;
	public int teleX;
	public int teleY;
	public int teleZ;
	public int screamDelayTick;
	public int deathTick;
	public int[] heldBlock = new int[5];
	public int[] heldBlockData = new int[5];
	public int[] heldBlockTick = new int[5];
	public boolean triggerThrowBlock;
	protected int blockFrenzy;
	protected IAttributeInstance moveSpeed;
	protected List<Entity> screamEntities;
	protected List<Entity> deathEntities;
	// protected MCAIAttackOnCollide aiAttackPlayer;
	// protected MCAIAttackOnCollide aiAttack;
	// protected MCAIEnderClone aiClone;
	private int dirty = -1;
	private long preTargetA;
	private long preTargetB;
	//private static ArrayList<Integer> carriableBlocks = new ArrayList<>();

	public MutantEndermanEntity(EntityType<? extends MutantEndermanEntity> type, World worldIn) {
		super(type, worldIn);
		this.experienceValue = 40;
		this.stepHeight = 1.4F;
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(96.0D);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
	}

	@Override
	protected void registerData() {
		super.registerData();
	}

	public int getThrownBlock() {
		return 0;
	}

	public void setThrownBlock(int index) {
	}

	public int getMeleeArm() {
		return 0;
	}

	public void setMeleeArm(int id) {
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return sizeIn.height;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public void setAttackTarget(LivingEntity entitylivingbaseIn) {
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0"));
		super.setAttackTarget(entitylivingbaseIn);
	}

	protected void updateTargetTick() {
		this.preTargetTick = this.hasTargetTick;
		// if (this.isAggressive()) {
		// this.hasTarget = 20;
		// }
		//
		// boolean emptyHanded = true;
		//
		// int i;
		// for (i = 1; i < this.heldBlock.length; ++i) {
		// if (this.heldBlock[i] > 0) {
		// emptyHanded = false;
		// }
		//
		// if (this.hasTarget > 0) {
		// if (this.heldBlock[i] > 0) {
		// this.heldBlockTick[i] = Math.min(10, this.heldBlockTick[i] + 1);
		// }
		// } else {
		// this.heldBlockTick[i] = Math.max(0, this.heldBlockTick[i] - 1);
		// }
		// }
		//
		// if (this.hasTarget > 0) {
		// this.hasTargetTick = Math.min(10, this.hasTargetTick + 1);
		// } else if (emptyHanded) {
		// this.hasTargetTick = Math.max(0, this.hasTargetTick - 1);
		// } else if (!this.world.isRemote) {
		// for (i = 1; i < this.heldBlock.length; ++i) {
		// if (this.heldBlock[i] != 0 && this.heldBlockTick[i] == 0) {
		// int x = MathHelper.floor(this.posX - 1.5D + this.rand.nextDouble() * 4.0D);
		// int y = MathHelper.floor(this.posY - 0.5D + this.rand.nextDouble() * 2.5D);
		// int z = MathHelper.floor(this.posZ - 1.5D + this.rand.nextDouble() * 4.0D);
		// Block block = this.world.getBlock(x, y, z);
		// Block block1 = this.world.getBlock(x, y - 1, z);
		// if (block == Blocks.AIR && block1 != Blocks.AIR &&
		// block1.renderAsNormalBlock()) {
		// this.world.setBlock(x, y, z, Block.getBlockById(this.heldBlock[i]),
		// this.heldBlockData[i], 3);
		// this.sendHoldBlock(i, 0, 0);
		// } else if (this.rand.nextInt(50) == 0) {
		// this.sendHoldBlock(i, 0, 0);
		// }
		// }
		// }
		// }

		this.hasTarget = Math.max(0, this.hasTarget - 1);
	}

	protected void updateScreamEntities() {
		this.screamDelayTick = Math.max(0, this.screamDelayTick - 1);
		if (this.currentAttackID == 5 && this.animTick >= 40 && this.animTick <= 160) {
			if (this.animTick == 160) {
				this.screamEntities = null;
			} else {
				if (this.screamEntities == null) {
					this.screamEntities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().expand(20.0D, 12.0D, 20.0D));
					List<Entity> list = new ArrayList<>();
					list.addAll(this.screamEntities);
					this.screamEntities = list;
				}

				for (int i = 0; i < this.screamEntities.size(); ++i) {
					Entity entity = (Entity)this.screamEntities.get(i);
					if (this.getDistanceSq(entity) > 400.0D) {
						this.screamEntities.remove(i);
						--i;
					} else {
						entity.rotationPitch += (this.rand.nextFloat() - 0.3F) * 6.0F;
					}
				}
			}
		}
	}

	@Override
	public void playEndermanSound() {
	}

	@Override
	public boolean isScreaming() {
		return false;
	}

	@Override
	public void livingTick() {
		super.livingTick();

		if (this.currentAttackID != 0) {
			++this.animTick;
		}

		this.updateTargetTick();
		this.updateScreamEntities();
		double h = this.currentAttackID != 10 ? (double)this.getHeight() : (double)(this.getHeight() + 1.0F);
		double w = this.currentAttackID != 10 ? (double)this.getWidth() : (double)(this.getWidth() * 1.5F);
		boolean targetBlind = this.getAttackTarget() != null && this.getAttackTarget().getActivePotionEffect(Effects.BLINDNESS) != null;
		if (!targetBlind) {
			for (int i = 0; i < 3; ++i) {
				double x = this.posX + (this.rand.nextDouble() - 0.5D) * w;
				double y = this.posY + this.rand.nextDouble() * h - 0.25D;
				double z = this.posZ + (this.rand.nextDouble() - 0.5D) * w;
				this.world.addParticle(ParticleTypes.PORTAL, x, y, z, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
			}
		}
	}

	private void updateDirtyHands() {
		if (this.dirty >= 0) {
			++this.dirty;
		}

		if (this.dirty >= 8) {
			this.dirty = -1;

			for (int i = 1; i < this.heldBlock.length; ++i) {
				if (this.heldBlock[i] > 0) {
					this.sendHoldBlock(i, this.heldBlock[i], this.heldBlockData[i]);
				}
			}

			if (this.preTargetA != 0L && this.preTargetB != 0L) {
				// List list = this.world.loadedEntityList;
				//
				// for (int i = 0; i < list.size(); ++i) {
				// Entity entity = (Entity)list.get(i);
				// if (entity instanceof LivingEntity) {
				// LivingEntity living = (LivingEntity)entity;
				// if (living.getPersistentID() != null &&
				// living.getPersistentID().getLeastSignificantBits() == this.preTargetA &&
				// living.getPersistentID().getMostSignificantBits() == this.preTargetB) {
				// this.setRevengeTarget(living);
				// break;
				// }
				// }
				// }
				//
				// this.preTargetA = 0L;
				// this.preTargetB = 0L;
			}
		}
	}

	protected void updateBlockFrenzy() {
		this.blockFrenzy = Math.max(0, this.blockFrenzy - 1);
		if (this.getAttackTarget() != null && this.currentAttackID == 0) {
			if (this.blockFrenzy == 0 && this.rand.nextInt(600) == 0) {
				this.blockFrenzy = 200 + this.rand.nextInt(80);
			}

			if (this.blockFrenzy > 0 && this.rand.nextInt(8) == 0) {
				// int x = MathHelper.floor(this.posX - 2.5D + this.rand.nextDouble() * 5.0D);
				// int y = MathHelper.floor(this.posY - 0.5D + this.rand.nextDouble() * 3.0D);
				// int z = MathHelper.floor(this.posZ - 2.5D + this.rand.nextDouble() * 5.0D);
				// int id = Block.getIdFromBlock(this.world.getBlock(x, y, z));
				// int index = this.getFavorableHand();
				// if (index != -1 && carriableBlocks.contains(id)) {
				// this.sendHoldBlock(index, id, this.world.getBlockMetadata(x, y, z));
				// if (this.world.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
				// this.world.setBlock(x, y, z, Blocks.air, 0, 3);
				// }
				// }
			}
		}

	}

	protected void updateTeleport() {
		Entity entity = this.getAttackTarget();
		this.teleportByChance(entity == null ? 1600 : 800, entity);
		if (entity != null) {
			double d = this.getDistanceSq(entity);
			if (d > 1024.0D) {
				this.teleportByChance(10, entity);
			}
		}

	}

	protected void updateClone() {
		// if (this.currentAttackID == 6) {
		// this.aiAttackPlayer.moveSpeed = 1.0F;
		// this.aiAttack.moveSpeed = 1.0F;
		// } else {
		// this.aiAttackPlayer.moveSpeed = 1.2F;
		// this.aiAttack.moveSpeed = 1.2F;
		// }
	}

	@Override
	protected void updateAITasks() {
		if (this.isInWaterRainOrBubbleColumn() && this.ticksExisted % 100 == 0) {
			this.attackEntityFrom(DamageSource.DROWN, 1.0F);
		}

		this.updateDirtyHands();
		this.updateBlockFrenzy();
		this.updateTeleport();
		this.updateClone();
	}

	public int getAvailableHand() {
		List<Integer> list = new ArrayList<>();

		for (int i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] == 0) {
				list.add(i);
			}
		}

		if (list.isEmpty()) {
			return -1;
		} else {
			return (Integer)list.get(this.rand.nextInt(list.size()));
		}
	}

	public int getFavorableHand() {
		List<Integer> outer = new ArrayList<>();
		List<Integer> inner = new ArrayList<>();

		for (int i = 1; i < this.heldBlock.length; ++i) {
			if (this.heldBlock[i] == 0) {
				if (i <= 2) {
					outer.add(i);
				} else {
					inner.add(i);
				}
			}
		}

		if (outer.isEmpty() && inner.isEmpty()) {
			return -1;
		} else if (!outer.isEmpty()) {
			return (Integer)outer.get(this.rand.nextInt(outer.size()));
		} else {
			return (Integer)inner.get(this.rand.nextInt(inner.size()));
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (!this.world.isRemote && this.currentAttackID == 0) {
			int i = this.getAvailableHand();
			if (!this.teleportByChance(6, entityIn)) {
				if (i != -1) {
					boolean allHandsFree = this.heldBlock[1] == 0 && this.heldBlock[2] == 0;
					if (allHandsFree && this.rand.nextInt(10) == 0) {
						this.sendAttackPacket(6);
					} else if (allHandsFree && this.rand.nextInt(7) == 0) {
						this.sendAttackPacket(7);
					} else {
						this.setMeleeArm(i);
						this.sendAttackPacket(1);
					}
				} else {
					this.triggerThrowBlock = true;
				}
			}
		}

		if (this.currentAttackID == 6) {
			boolean flag = super.attackEntityAsMob(entityIn);
			if (!this.world.isRemote && this.rand.nextInt(2) == 0) {
				double x = entityIn.posX + (double)((this.rand.nextFloat() - 0.5F) * 24.0F);
				double z = entityIn.posZ + (double)((this.rand.nextFloat() - 0.5F) * 24.0F);
				double y = entityIn.posY + (double)this.rand.nextInt(5) + 4.0D;
				EntityUtil.teleportTo(this, x, y, z);
			}

			if (flag) {
				this.heal(2.0F);
			}

			return flag;
		} else {
			return true;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		Entity entity = source.getTrueSource();
		if (entity != null && entity instanceof EnderDragonEntity) {
			return false;
		} else if (this.currentAttackID != 4 && this.currentAttackID != 5) {
			if (!this.world.isRemote) {
				if (this.currentAttackID == 6) {
					// this.aiClone.resetTask();
				}

				boolean betterDodge = entity == null;
				if (source.isProjectile()) {
					betterDodge = true;
				}

				if (this.teleportByChance(betterDodge ? 3 : 6, entity)) {
					if (entity != null && entity instanceof LivingEntity) {
						this.setRevengeTarget((LivingEntity)entity);
					}

					return false;
				}

				boolean betterTeleport = false;
				if (source == DamageSource.DROWN) {
					betterTeleport = true;
				}

				this.teleportByChance(betterTeleport ? 3 : 5, entity);
			}

			return super.attackEntityFrom(source, amount);
		} else {
			return false;
		}
	}

	public boolean teleportByChance(int chance, Entity entity) {
		if (this.currentAttackID != 0) {
			return false;
		} else {
			chance = Math.max(1, chance);
			if (this.rand.nextInt(chance) == 0) {
				return entity == null ? this.teleportRandomly() : this.teleportToEntity(entity);
			} else {
				return false;
			}
		}
	}

	public boolean teleportRandomly() {
		if (this.currentAttackID != 0) {
			return false;
		} else {
			double radius = 24.0D;
			double x = this.posX + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			double y = this.posY + (double)this.rand.nextInt((int)radius * 2) - radius;
			double z = this.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			return this.sendTeleportPacket(x, y, z);
		}
	}

	public boolean teleportToEntity(Entity entity) {
		if (this.currentAttackID != 0) {
			return false;
		} else {
			double d = this.getDistanceSq(entity);
			double x = 0.0D;
			double y = 0.0D;
			double z = 0.0D;
			double radius = 16.0D;
			if (d < 100.0D) {
				x = entity.posX + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
				y = entity.posY + this.rand.nextDouble() * radius;
				z = entity.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D * radius;
			} else {
				Vec3d vec = new Vec3d(this.posX - entity.posX, this.getBoundingBox().minY + (double)this.getHeight() / 2.0D - entity.posY + (double)entity.getEyeHeight(), this.posZ - entity.posZ);
				vec = vec.normalize();
				x = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec.x * radius;
				y = this.posY + (double)this.rand.nextInt(8) - vec.y * radius;
				z = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec.z * radius;
			}

			return this.sendTeleportPacket(x, y, z);
		}
	}

	public int maxDeathTick() {
		return 280;
	}

	@Override
	protected void onDeathUpdate() {
		super.onDeathUpdate();
		// ++this.deathTick;
		// this.motionX = 0.0D;
		// this.motionY = Math.min(this.motionY, 0.0D);
		// this.motionZ = 0.0D;
		// if (this.currentAttackID != 10) {
		// this.sendAttackPacket(10);
		// }
		//
		// if (this.deathTick == 80) {
		// this.playSound("MutantCreatures:mutantenderman.death", this.getSoundVolume(),
		// this.getSoundPitch());
		// }
		//
		// int i;
		// if (!this.world.isRemote) {
		// if (this.deathTick >= 60 && this.deathTick < 80 && this.deathEntities ==
		// null) {
		// this.deathEntities = this.world.getEntitiesWithinAABBExcludingEntity(this,
		// this.boundingBox.expand(10.0D, 8.0D, 10.0D));
		// List temp = new ArrayList();
		// temp.addAll(this.deathEntities);
		// this.deathEntities = temp;
		// }
		//
		// if (this.deathTick >= 60 && this.rand.nextInt(3) != 0) {
		// EndersoulFragment orb = new EndersoulFragment(this.world);
		// orb.setPosition(this.posX, this.posY + (double)this.getEyeHeight() - 1.0D,
		// this.posZ);
		// orb.motionX = (double)((this.rand.nextFloat() - 0.5F) * 1.5F);
		// orb.motionY = (double)((this.rand.nextFloat() - 0.5F) * 1.5F);
		// orb.motionZ = (double)((this.rand.nextFloat() - 0.5F) * 1.5F);
		// this.world.spawnEntityInWorld(orb);
		// }
		//
		// if (this.deathTick >= 80 && this.deathTick < this.maxDeathTick() - 20 &&
		// this.deathEntities != null) {
		// for (i = 0; i < this.deathEntities.size(); ++i) {
		// Entity entity = (Entity)this.deathEntities.get(i);
		// if (!(entity instanceof EndersoulFragment) && !(entity instanceof EntityItem)
		// && !(entity instanceof MutantEnderman)) {
		// if (entity.fallDistance > 4.5F) {
		// entity.fallDistance = 4.5F;
		// }
		//
		// if (this.getDistanceSq(entity) > 64.0D) {
		// boolean protectedPlayer = EndersoulFragment.isProtectedPlayer(entity);
		// if (protectedPlayer) {
		// this.deathEntities.remove(i);
		// --i;
		// } else {
		// double x = this.posX - entity.posX;
		// double z = this.posZ - entity.posZ;
		// double d = Math.sqrt(x * x + z * z);
		// entity.motionX = 0.800000011920929D * x / d;
		// if (this.posY + 4.0D > entity.posY) {
		// entity.motionY = Math.max(entity.motionY, 0.4000000059604645D);
		// }
		//
		// entity.motionZ = 0.800000011920929D * z / d;
		// if (entity instanceof EntityPlayerMP) {
		// EntityPlayerMP player = (EntityPlayerMP)entity;
		// MutantCreatures.sendPacketToAll(player, new S12PacketEntityVelocity(player));
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// if (this.deathTick >= 100 && this.deathTick < 150 && this.deathTick % 6 == 0)
		// {
		// Item item = Items.ENDER_PEARL;
		// if (this.rand.nextBoolean()) {
		// item = Items.ENDER_EYE;
		// }
		//
		// EntityItem itemEntity = new EntityItem(this.world, this.posX, this.posY +
		// (double)(this.getEyeHeight() * 0.8F), this.posZ, new ItemStack(item, 1, 0));
		// this.world.spawnEntityInWorld(itemEntity);
		// }
		// }
		//
		// if (this.deathTick >= this.maxDeathTick()) {
		// if (!this.world.isRemote && (this.recentlyHit > 0 || this.isPlayer()) &&
		// !this.isChild()) {
		// i = this.getExperiencePoints(this.attackingPlayer);
		//
		// while (i > 0) {
		// int k = EntityXPOrb.getXPSplit(i);
		// i -= k;
		// this.world.spawnEntityInWorld(new EntityXPOrb(this.world, this.posX,
		// this.posY, this.posZ, k));
		// }
		// }
		//
		// this.remove();
		// }
	}

	@Override
	public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
		if (this.rand.nextInt(3) == 0) {
			return false;
		} else if (worldIn.getDimension().getType().getId() == 1 && this.rand.nextInt(2600) != 0) {
			return false;
		} else {
			return super.canSpawn(worldIn, spawnReasonIn);// && MutantCreatures.getRandomSpawnChance();
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

		for (int i = 1; i < this.heldBlock.length; ++i) {
			compound.putInt("heldBlockID_" + i, this.heldBlock[i]);
			compound.putInt("heldBlockData_" + i, this.heldBlockData[i]);
		}

		LivingEntity target = this.getAttackTarget();
		if (target != null) {
			compound.putLong("targetA", target.getUniqueID().getLeastSignificantBits());
			compound.putLong("targetB", target.getUniqueID().getMostSignificantBits());
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

		for (int i = 1; i < this.heldBlock.length; ++i) {
			this.heldBlock[i] = compound.getInt("heldBlockID_" + i);
			this.heldBlockData[i] = compound.getInt("heldBlockData_" + i);
			this.dirty = 0;
		}

		if (compound.contains("targetA") && compound.contains("targetB")) {
			this.preTargetA = compound.getLong("targetA");
			this.preTargetB = compound.getLong("targetB");
		}
	}

	@Override
	public int getTalkInterval() {
		return 200;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return super.getAmbientSound();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return super.getHurtSound(damageSourceIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	public void sendAttackPacket(int id) {
		this.currentAttackID = id;
	}

	public void sendHoldBlock(int blockIndex, int blockId, int blockData) {
		// if (!MutantCreatures.isEffectiveClient()) {
		// this.heldBlock[blockIndex] = blockId;
		// this.heldBlockData[blockIndex] = blockData;
		// this.heldBlockTick[blockIndex] = 0;
		// //MutantCreatures.wrapper.sendToAll(new PacketEnderBlock(this, blockId,
		// blockIndex, blockData));
		// }
	}

	public boolean sendTeleportPacket(double targetX, double targetY, double targetZ) {
		if (this.currentAttackID == 0) {
			this.currentAttackID = 4;
			double oldX = this.posX;
			double oldY = this.posY;
			double oldZ = this.posZ;
			this.teleX = MathHelper.floor(targetX);
			this.teleY = MathHelper.floor(targetY);
			this.teleZ = MathHelper.floor(targetZ);
			this.posX = (double)this.teleX + 0.5D;
			this.posY = (double)this.teleY;
			this.posZ = (double)this.teleZ + 0.5D;
			boolean success = false;
			if (this.world.isBlockPresent(new BlockPos(this.teleX, this.teleY, this.teleZ))) {
				boolean temp = false;

				while (true) {
					while (!temp && this.teleY > 0) {
						Block block = this.world.getBlockState(new BlockPos(this.teleX, this.teleY - 1, this.teleZ)).getBlock();
						if (block != Blocks.AIR && this.world.getBlockState(new BlockPos(this.teleX, this.teleY - 1, this.teleZ)).getMaterial().blocksMovement()) {
							temp = true;
						} else {
							--this.posY;
							--this.teleY;
						}
					}

					if (temp) {
						this.setPosition(this.posX, this.posY, this.posZ);
						if (this.world.isCollisionBoxesEmpty(this, this.getBoundingBox()) && !this.world.containsAnyLiquid(this.getBoundingBox())) {
							success = true;
						}
					}
					break;
				}
			}

			this.setPosition(oldX, oldY, oldZ);
			if (!success) {
				this.currentAttackID = 0;
				return false;
			} else {
				// MutantCreatures.wrapper.sendToAll(new PacketEnderTeleport(this, this.teleX,
				// this.teleY, this.teleZ));
				return true;
			}
		} else {
			return false;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void handleTeleport(int x, int y, int z) {
		this.currentAttackID = 4;
		this.animTick = 0;
		this.teleX = x;
		this.teleY = y;
		this.teleZ = z;
		this.spawnBigParticles();
	}

	@OnlyIn(Dist.CLIENT)
	public void spawnBigParticles() {
		this.spawnBigParticles(256, 1.8F);
	}

	@OnlyIn(Dist.CLIENT)
	public void spawnBigParticles(int temp, float speed) {
		// EffectRenderer renderer =
		// FMLClientHandler.instance().getClient().effectRenderer;
		// if (this.currentAttackID == 4) {
		// temp *= 2;
		// }
		//
		// for (int i = 0; i < temp; ++i) {
		// float f = (this.rand.nextFloat() - 0.5F) * speed;
		// float f1 = (this.rand.nextFloat() - 0.5F) * speed;
		// float f2 = (this.rand.nextFloat() - 0.5F) * speed;
		// boolean flag = i < temp / 2;
		// if (this.currentAttackID != 4) {
		// flag = true;
		// }
		//
		// boolean death = this.currentAttackID != 10;
		// double h = death ? (double)this.height : (double)(this.height + 1.0F);
		// double w = death ? (double)this.width : (double)(this.width * 1.5F);
		// double tempX = (flag ? this.posX : (double)this.teleX) +
		// (this.rand.nextDouble() - 0.5D) * w;
		// double tempY = (flag ? this.posY : (double)this.teleY) +
		// (this.rand.nextDouble() - 0.5D) * h + (double)(death ? 1.5F : 0.5F);
		// double tempZ = (flag ? this.posZ : (double)this.teleZ) +
		// (this.rand.nextDouble() - 0.5D) * w;
		// renderer.addEffect(new FXEnder(this.worldObj, tempX, tempY, tempZ, (double)f,
		// (double)f1, (double)f2, true));
		// }
	}
}