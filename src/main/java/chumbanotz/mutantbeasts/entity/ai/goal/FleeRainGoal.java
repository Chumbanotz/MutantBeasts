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
		} else if (!this.creature.world.isRainingAt(this.creature.getPosition())) {
			return false;
		} else {
			return this.func_220702_g();
		}
	}

	@Override
	protected Vec3d findPossibleShelter() {
		Random random = this.creature.getRNG();
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos().setPos(this.creature);

		for (int i = 0; i < 10; ++i) {
			blockpos.setPos(this.creature).move(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!this.creature.world.isRainingAt(blockpos) && !this.creature.world.hasWater(blockpos)) {
				return new Vec3d(blockpos);
			}
		}

		return null;
	}
}