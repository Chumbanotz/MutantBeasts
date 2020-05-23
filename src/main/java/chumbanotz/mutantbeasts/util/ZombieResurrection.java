package chumbanotz.mutantbeasts.util;

import java.util.ArrayList;
import java.util.List;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.state.properties.Half;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ZombieResurrection extends BlockPos {
	private int tick;
	private final World world;

	public ZombieResurrection(World world, int x, int y, int z) {
		super(x, y, z);
		this.world = world;
		this.tick = 100 + world.rand.nextInt(40);
	}

	public ZombieResurrection(World world, BlockPos pos, int tick) {
		super(pos);
		this.world = world;
		this.tick = tick;
	}

	public int getTick() {
		return tick;
	}

	public boolean update(MutantZombieEntity mutantZombie) {
		if (this.world.isAirBlock(this)) {
			return false;
		}

		if (mutantZombie.getRNG().nextInt(15) == 0) {
			this.world.playEvent(2001, this.up(), Block.getStateId(this.world.getBlockState(this.up())));
		}

		if (--this.tick <= 0) {
			Entity entity = getZombieByLocation(this.world, this).create(this.world);
			if (entity instanceof MobEntity) {
				MobEntity mobEntity = (MobEntity)entity;
				ILivingEntityData livingEntityData = null;
				livingEntityData = mobEntity.onInitialSpawn(this.world, this.world.getDifficultyForLocation(this), SpawnReason.MOB_SUMMONED, livingEntityData, null);
				mobEntity.setHealth(mobEntity.getMaxHealth() * (0.6F + 0.4F * mobEntity.getRNG().nextFloat()));
				mobEntity.playAmbientSound();
			}

			this.world.playEvent(2001, this.up(), Block.getStateId(this.world.getBlockState(this.up())));

			if (!this.world.isRemote) {
				SummonableCapability.getLazy(entity).ifPresent(summonable -> {
					summonable.setSummonerUUID(mutantZombie.getUniqueID());
					summonable.setSpawnedBySummoner(true);
				});

				entity.setPosition((double)this.getX() + 0.5D, (double)this.getY() + 1.0D, (double)this.getZ() + 0.5D);
				this.world.addEntity(entity);
			}

			return false;
		} else {
			return true;
		}
	}

	public static int getSuitableGround(World world, int x, int y, int z) {
		return getSuitableGround(world, x, y, z, 4, true);
	}

	public static int getSuitableGround(World world, int x, int y, int z, int range, boolean checkDay) {
		int i = y;

		while (Math.abs(y - i) <= range) {
			BlockPos pos = new BlockPos(x, i, z);
			BlockState blockState = world.getBlockState(pos);
	
			if (blockState.getBlock() != Blocks.FIRE) {
				if (checkDay && !world.getFluidState(pos).isTagged(FluidTags.LAVA) || world.getFluidState(pos).isEmpty()) {
					if (world.isAirBlock(pos)) {
						--i;
						continue;
					}

					if (!world.isAirBlock(pos) && world.isAirBlock(pos.up()) && shouldIgnoreBlock(world, blockState, pos)) {
						--i;
					} else if (!world.isAirBlock(pos) && !world.isAirBlock(pos.up()) && !shouldIgnoreBlock(world, world.getBlockState(pos.up()), pos.up())) {
						++i;
						continue;
					}
				}

				if (checkDay && world.isDaytime()) {
					BlockPos pos1 = new BlockPos(x, y + 1, z);
					float f = world.getBrightness(pos1);

					if (f > 0.5F && world.canBlockSeeSky(pos1) && world.rand.nextInt(3) != 0) {
						return -1;
					}
				}

				return i;
			}

			return -1;
		}

		return -1;
	}

	private static boolean shouldIgnoreBlock(World world, BlockState blockState, BlockPos blockPos) {
		if (blockState.getBlock() instanceof TrapDoorBlock && blockState.get(TrapDoorBlock.HALF) == Half.TOP) {
			return false;
		} else if (blockState.getBlock() instanceof DoorBlock) {
			return true;
		} else {
			return blockState.getCollisionShape(world, blockPos).isEmpty();
		}
	}

	public static EntityType<?> getZombieByLocation(World world, BlockPos pos) {
		List<Biome.SpawnListEntry> entries = new ArrayList<>(world.getBiome(pos).getSpawns(EntityClassification.MONSTER));
		entries.removeIf(entry -> !SummonableCapability.isEntityEligible(entry.entityType));
		return entries.isEmpty() ? EntityType.ZOMBIE : WeightedRandom.getRandomItem(world.rand, entries).entityType;
	}
}