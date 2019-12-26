package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.function.BooleanSupplier;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AvoidDamageGoal extends PanicGoal {
	protected final BooleanSupplier avoidsAttacker;

	public AvoidDamageGoal(CreatureEntity creature, double speed) {
		this(creature, speed, () -> false);
	}

	public AvoidDamageGoal(CreatureEntity creature, double speed, BooleanSupplier avoidsAttacker) {
		super(creature, speed);
		this.avoidsAttacker = avoidsAttacker;
	}

	@Override
	public boolean shouldExecute() {
		Vec3d vec3d = null;
		if (this.creature.isBurning() || this.creature.getLastDamageSource() != null && this.creature.getLastDamageSource().isFireDamage() && this.creature.hurtResistantTime > 10) {
			BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 25, 8);
			vec3d = blockpos != null && !this.creature.isInWater() ? new Vec3d(blockpos) : RandomPositionGenerator.findRandomTarget(this.creature, 8, 4);
		}

		if (this.creature.getLastDamageSource() != null && this.shouldAvoidDamage(this.creature.getLastDamageSource()) && this.creature.hurtResistantTime > 10) {
			vec3d = this.creature.getLastDamageSource() == DamageSource.DROWN && this.creature.isInWater() ? RandomPositionGenerator.getLandPos(this.creature, 15, 15) : RandomPositionGenerator.findRandomTarget(this.creature, 4, 4);
		}

		if (this.avoidsAttacker.getAsBoolean() && this.creature.getRevengeTarget() != null) {
			vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 10, 9, this.creature.getRevengeTarget().getPositionVector());
		}

		if (vec3d == null) {
			return false;
		} else {
			this.randPosX = vec3d.x;
			this.randPosY = vec3d.y;
			this.randPosZ = vec3d.z;
			return true;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.creature.isBurning() || this.avoidsAttacker.getAsBoolean() && this.creature.getRevengeTarget() != null) {
			return super.shouldContinueExecuting();
		} else {
			return this.creature.getLastDamageSource() != null && this.creature.hurtResistantTime > 10;
		}
	}

	@Override
	public void resetTask() {
		this.creature.getNavigator().clearPath();
	}

	protected boolean shouldAvoidDamage(DamageSource source) {
		if (source.getImmediateSource() != null && (source.getTrueSource() == null || !this.creature.canEntityBeSeen(source.getTrueSource()))) {
			return true;
		} else {
			return source.getTrueSource() == null && !source.isFireDamage() && source != DamageSource.FALL && source != DamageSource.STARVE && source != DamageSource.OUT_OF_WORLD;
		}
	}
}