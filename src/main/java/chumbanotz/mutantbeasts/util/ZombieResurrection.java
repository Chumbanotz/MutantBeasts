package chumbanotz.mutantbeasts.util;

import chumbanotz.mutantbeasts.capability.SummonableCapability;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.GrassPathBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.state.properties.Half;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ZombieResurrection {
	private final int posX;
	private final int posY;
	private final int posZ;
	private int tick;
	private final World world;

	public ZombieResurrection(World world, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = world;
		this.tick = 100 + world.rand.nextInt(40);
	}

	public ZombieResurrection(World world, BlockPos pos, int tick) {
		this.posX = pos.getX();
		this.posY = pos.getY();
		this.posZ = pos.getZ();
		this.world = world;
		this.tick = tick;
	}

	public int getTick() {
		return tick;
	}

	public boolean update(MutantZombieEntity mutantZombie) {
		BlockPos pos = this.getPosition();
		if (this.world.isAirBlock(pos.down())) {
			return false;
		}

		if (mutantZombie.getRNG().nextInt(15) == 0) {
			this.world.playEvent(2001, pos, Block.getStateId(this.world.getBlockState(pos.down())));
			Block block = world.getBlockState(pos.down()).getBlock();
			if (block instanceof SnowyDirtBlock || block instanceof GrassPathBlock || block instanceof FarmlandBlock) {
				world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 2);
			}
		}

		if (--this.tick <= 0) {
			ZombieEntity zombie = getZombieByLocation(this.world, pos).create(this.world);
			zombie.onInitialSpawn(this.world, this.world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
			zombie.setHealth(zombie.getMaxHealth() * (0.6F + 0.4F * zombie.getRNG().nextFloat()));
			zombie.playAmbientSound();
			if (zombie.getRidingEntity() != null) {
				zombie.stopRiding();
			}
			this.world.playEvent(2001, pos, Block.getStateId(this.world.getBlockState(pos.down())));
			if (world.getBlockState(pos).getBlockHardness(world, pos) <= 1.0F && !net.minecraft.tags.BlockTags.WITHER_IMMUNE.contains(world.getBlockState(pos).getBlock()) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, zombie)) {
				world.destroyBlock(pos, false);
			}

			if (!this.world.isRemote) {
				SummonableCapability.get(zombie).setSummoner(mutantZombie);
				SummonableCapability.get(zombie).setSpawnedBySummoner(true);
				zombie.setPosition((double)this.posX + 0.5D, (double)this.posY + 1.0D, (double)this.posZ + 0.5D);
				this.world.addEntity(zombie);
			}

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

	public static EntityType<? extends ZombieEntity> getZombieByLocation(World world, BlockPos pos) {
		int chance = world.rand.nextInt(100);
		Biome biome = world.getBiome(pos);

		if (biome.getCategory() == Biome.Category.DESERT) {
			return chance < 80 && world.isSkyLightMax(pos) ? EntityType.HUSK : chance < 1 ? EntityType.ZOMBIE_VILLAGER : EntityType.ZOMBIE;
		} else if ((biome.getCategory() == Biome.Category.OCEAN || biome.getCategory() == Biome.Category.RIVER) && world.hasWater(pos)) {
			return EntityType.DROWNED;
		} else {
			return chance < 95 ? EntityType.ZOMBIE : EntityType.ZOMBIE_VILLAGER;
		}
	}
}