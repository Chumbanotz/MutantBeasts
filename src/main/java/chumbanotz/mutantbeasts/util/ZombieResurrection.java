package chumbanotz.mutantbeasts.util;

import java.util.ArrayList;
import java.util.List;

import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
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

					if (!world.isAirBlock(pos) && world.isAirBlock(pos.up()) && blockState.getCollisionShape(world, pos).isEmpty()) {
						--i;
					} else if (!world.isAirBlock(pos) && !world.isAirBlock(pos.up()) && !blockState.getCollisionShape(world, pos.up()).isEmpty()) {
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

	public static EntityType<?> getZombieByLocation(World world, BlockPos pos) {
		List<Biome.SpawnListEntry> entries = new ArrayList<>();
		for (Biome.SpawnListEntry entry : world.getBiome(pos).getSpawns(EntityClassification.MONSTER)) {
			if (isEligible(entry)) {
				entries.add(entry);
				System.out.println(entry);
			}
		}

		return entries.isEmpty() ? EntityType.ZOMBIE : WeightedRandom.getRandomItem(world.rand, entries).entityType;
	}

	private static boolean isEligible(Biome.SpawnListEntry entry) {
		EntityType<?> type = entry.entityType;
		return type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.HUSK || type == EntityType.DROWNED;
	}
}