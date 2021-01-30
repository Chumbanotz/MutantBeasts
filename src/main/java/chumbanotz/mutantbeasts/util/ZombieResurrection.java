package chumbanotz.mutantbeasts.util;

import chumbanotz.mutantbeasts.entity.ai.goal.TrackSummonerGoal;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.tags.FluidTags;
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

		BlockPos abovePos = this.up();
		if (mutantZombie.getRNG().nextInt(15) == 0) {
			this.world.playEvent(2001, abovePos, Block.getStateId(this.world.getBlockState(abovePos)));
		}

		if (--this.tick <= 0) {
			if (!this.world.isRemote) {
				ZombieEntity zombieEntity = getZombieByLocation(this.world, abovePos).create(this.world);
				zombieEntity.onInitialSpawn(this.world, this.world.getDifficultyForLocation(this), SpawnReason.MOB_SUMMONED, null, null);
				zombieEntity.setHealth(zombieEntity.getMaxHealth() * (0.6F + 0.4F * zombieEntity.getRNG().nextFloat()));
				zombieEntity.playAmbientSound();
				zombieEntity.stopRiding(); //Chicken jockeys seem to cause problems
				this.world.playEvent(2001, abovePos, Block.getStateId(this.world.getBlockState(abovePos)));
				zombieEntity.moveToBlockPosAndAngles(abovePos, mutantZombie.rotationYaw, 0.0F);
				zombieEntity.goalSelector.addGoal(0, new TrackSummonerGoal(zombieEntity, mutantZombie));
				zombieEntity.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(zombieEntity, 1.0D));
				this.world.addEntity(zombieEntity);
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

	public static EntityType<? extends ZombieEntity> getZombieByLocation(World world, BlockPos pos) {
		Biome biome = world.getBiome(pos);
		int chance = world.rand.nextInt(100);

		if (biome.getCategory() == Biome.Category.DESERT) {
			return chance < 80 && world.isSkyLightMax(pos) ? EntityType.HUSK : chance < 1 ? EntityType.ZOMBIE_VILLAGER : EntityType.ZOMBIE;
		} else if ((biome.getCategory() == Biome.Category.OCEAN || biome.getCategory() == Biome.Category.RIVER) && world.hasWater(pos)) {
			return EntityType.DROWNED;
		} else {
			return chance < 95 ? EntityType.ZOMBIE : EntityType.ZOMBIE_VILLAGER;
		}
	}
}