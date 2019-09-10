package chumbanotz.mutantbeasts.util;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ZombieResurrect {
	private int posX;
	private int posY;
	private int posZ;
	public int tick;
	private World world;

	public ZombieResurrect(World world, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = world;
		this.tick = 100 + world.rand.nextInt(40);
	}

	public ZombieResurrect(World world, BlockPos pos, int tick) {
		this.posX = pos.getX();
		this.posY = pos.getY();
		this.posZ = pos.getZ();
		this.world = world;
		this.tick = tick;
	}

	public boolean update(MutantZombieEntity mutantZombie) {
		BlockPos pos = this.getPosition();

		if (mutantZombie.getRNG().nextInt(15) == 0) {
			this.world.playEvent(2001, pos, Block.getStateId(this.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ))));
		}

		--this.tick;

		if (!this.world.isRemote && this.tick <= 0) {
			ZombieEntity zombie = getZombieByLocation(this.world, pos).create(this.world);
			zombie.onInitialSpawn(this.world, this.world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
			zombie.setHealth(zombie.getMaxHealth() * (0.6F + 0.4F * zombie.getRNG().nextFloat()));
			zombie.playAmbientSound();
			SummonableCapability.get(zombie).setSummoner(mutantZombie);
			zombie.setPosition((double)this.posX + 0.5D, (double)this.posY + 1.0D, (double)this.posZ + 0.5D);
			this.world.addEntity(zombie);
			return false;
		} else {
			return true;
		}
	}

	public BlockPos getPosition() {
		return new BlockPos(this.posX, this.posY + 1, this.posZ);
	}

	public static int getSuitableGround(World world, int x, int y, int z) {
		return getSuitableGround(world, x, y, z, 4, true);
	}

	public static int getSuitableGround(World world, int x, int y, int z, int range, boolean checkDay) {
		int i = y;

		while (Math.abs(y - i) <= range) {
			Block block = world.getBlockState(new BlockPos(x, i, z)).getBlock();
			Block block1 = world.getBlockState(new BlockPos(x, i + 1, z)).getBlock();

			if (block != Blocks.LAVA && block != Blocks.FIRE) {
				if (block != Blocks.WATER) {
					if (block == Blocks.AIR) {
						--i;
						continue;
					}

					if (block != Blocks.AIR && block1 == Blocks.AIR && block.getDefaultState().getCollisionShape(world, new BlockPos(x, i, z)).isEmpty()) {
						--i;
					} else if (block != Blocks.AIR && block1 != Blocks.AIR && !block1.getDefaultState().getCollisionShape(world, new BlockPos(x, i + 1, z)).isEmpty()) {
						++i;
						continue;
					}
				}

				if (checkDay && world.isDaytime()) {
					float f = world.getBrightness(new BlockPos(x, y + 1, z));

					if (f > 0.5F && world.canBlockSeeSky(new BlockPos(x, y + 1, z)) && world.rand.nextInt(3) != 0) {
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
		int chance = world.rand.nextInt(100);
		Biome biome = world.getBiome(pos);

		if (biome.getCategory() == Biome.Category.DESERT) {
			return chance < 80 && world.isSkyLightMax(pos) ? EntityType.HUSK : chance < 1 ? EntityType.ZOMBIE_VILLAGER : EntityType.ZOMBIE;
		} else if ((biome.getCategory() == Biome.Category.OCEAN || biome.getCategory() == Biome.Category.RIVER) && world.getFluidState(pos).isTagged(FluidTags.WATER)) {
			return EntityType.DROWNED;
		} else {
			return chance < 95 ? EntityType.ZOMBIE : EntityType.ZOMBIE_VILLAGER;
		}
	}
}