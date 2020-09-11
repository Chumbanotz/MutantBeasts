package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.function.BooleanSupplier;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AvoidDamageGoal extends PanicGoal {
	private final BooleanSupplier avoidsAttacker;

	public AvoidDamageGoal(CreatureEntity creature, double speed) {
		this(creature, speed, () -> false);
	}

	public AvoidDamageGoal(CreatureEntity creature, double speed, BooleanSupplier avoidsAttacker) {
		super(creature, speed);
		this.avoidsAttacker = avoidsAttacker;
	}

	@Override
	public boolean shouldExecute() {
		if (this.creature.isBurning()) {
			if (this.creature.world.isRaining()) {
				for (int i = 0; i < 10; ++i) {
					BlockPos blockpos1 = this.creature.getPosition().add(this.creature.getRNG().nextInt(20) - 10, this.creature.getRNG().nextInt(6) - 3, this.creature.getRNG().nextInt(20) - 10);
					if (this.creature.world.isRainingAt(blockpos1) && this.creature.getBlockPathWeight(blockpos1) >= 0.0F) {
			            return this.hasPosition(new Vec3d(blockpos1));
					}
				}
			}

			BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 25, 8);
			return blockpos != null && this.creature.getNavigator().func_179680_a(blockpos, 0) != null && this.hasPosition(new Vec3d(blockpos)) || this.findRandomPosition();
		} else if (this.avoidsAttacker.getAsBoolean() && this.creature.getRevengeTarget() != null) {
			return this.hasPosition(RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 10, 9, this.creature.getRevengeTarget().getPositionVec()));
		} else if (this.creature.getLastDamageSource() != null && this.shouldAvoidDamage(this.creature.getLastDamageSource())) {
			Vec3d damageVec = this.creature.getLastDamageSource().getDamageLocation();
			if (damageVec != null) {
				return this.hasPosition(RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 4, 2, damageVec));
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

	protected boolean shouldAvoidDamage(DamageSource source) {
		if (source.getTrueSource() != null) {
			return false;
		} else if (source.isMagicDamage() && source.getImmediateSource() == null) {
			return false;
		} else {
			return source != DamageSource.DROWN && source != DamageSource.FALL && source != DamageSource.STARVE && source != DamageSource.OUT_OF_WORLD;
		}
	}
}