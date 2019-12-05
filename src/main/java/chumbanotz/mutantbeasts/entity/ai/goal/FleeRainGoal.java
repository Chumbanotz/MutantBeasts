package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.Random;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FleeRainGoal extends FleeSunGoal {
	public FleeRainGoal(CreatureEntity theCreatureIn, double movementSpeedIn) {
		super(theCreatureIn, movementSpeedIn);
	}

	@Override
	public boolean shouldExecute() {
		if (this.creature.getAttackTarget() != null) {
			return false;
		} else if (!this.creature.world.isRainingAt(new BlockPos(this.creature.posX, this.creature.getBoundingBox().minY, this.creature.posZ))) {
			return false;
		} else {
			return this.func_220702_g();
		}
	}

	@Override
	protected Vec3d findPossibleShelter() {
		Random random = this.creature.getRNG();
		BlockPos blockpos = new BlockPos(this.creature.posX, this.creature.getBoundingBox().minY, this.creature.posZ);

		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!this.creature.world.isRainingAt(blockpos1) && !this.creature.world.hasWater(blockpos1)) {
				return new Vec3d((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
			}
		}

		return null;
	}
}