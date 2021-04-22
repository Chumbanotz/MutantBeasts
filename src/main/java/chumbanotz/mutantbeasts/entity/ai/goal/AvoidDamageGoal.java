package chumbanotz.mutantbeasts.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AvoidDamageGoal extends PanicGoal {
	public AvoidDamageGoal(CreatureEntity creature, double speed) {
		super(creature, speed);
	}

	@Override
	public boolean shouldExecute() {
		if (this.creature.isBurning()) {
			if (this.creature.world.isRaining()) {
				BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos().setPos(this.creature);
				for (int i = 0; i < 10; ++i) {
					blockPos.move(this.creature.getRNG().nextInt(20) - 10, this.creature.getRNG().nextInt(6) - 3, this.creature.getRNG().nextInt(20) - 10);
					if (this.creature.world.isRainingAt(blockPos) && this.creature.getBlockPathWeight(blockPos) >= 0.0F) {
			            return this.hasPosition(new Vec3d(blockPos));
					}
				}
			}

			BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 25, 8);
			return blockpos != null && this.creature.getNavigator().func_179680_a(blockpos, 0) != null && this.hasPosition(new Vec3d(blockpos)) || this.findRandomPosition();
		} else if (this.creature.isChild() && this.creature.getRevengeTarget() != null) {
			return this.hasPosition(RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, this.creature.getRevengeTarget().getPositionVec()));
		} else if (this.creature.getLastDamageSource() != null && this.shouldAvoidDamage(this.creature.getLastDamageSource())) {
			Vec3d damageVec = this.creature.getLastDamageSource().getDamageLocation();
			if (damageVec != null) {
				return this.hasPosition(RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, damageVec));
			} else {
				return this.findRandomPosition();
			}
		} else {
			return false;
		}
	}

	private boolean hasPosition(Vec3d vec3d) {
		if (vec3d == null) {
			return false;
		} else {
			this.randPosX = vec3d.x;
			this.randPosY = vec3d.y;
			this.randPosZ = vec3d.z;
			return true;
		}
	}

	private boolean shouldAvoidDamage(DamageSource source) {
		if (source.getTrueSource() != null) {
			return false;
		} else if (source.isMagicDamage() && source.getImmediateSource() == null) {
			return false;
		} else {
			return source != DamageSource.DROWN && source != DamageSource.FALL && source != DamageSource.STARVE && source != DamageSource.OUT_OF_WORLD;
		}
	}
}