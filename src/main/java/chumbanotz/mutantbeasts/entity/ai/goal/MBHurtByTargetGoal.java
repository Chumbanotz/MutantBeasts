package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class MBHurtByTargetGoal extends TargetGoal {
	private static final EntityPredicate PREDICATE = new EntityPredicate().setLineOfSiteRequired().setUseInvisibilityCheck();
	private boolean entityCallsForHelp;
	private final Class<?>[] excludedTargetTypes;
	private Class<?>[] excludedReinforcementTypes;
	private final List<LivingEntity> targetList = new ArrayList<>();

	public MBHurtByTargetGoal(CreatureEntity creatureIn, Class<?>... excludedTargetTypes) {
		super(creatureIn, true);
		this.excludedTargetTypes = excludedTargetTypes;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean shouldExecute() {
		if (this.goalOwner.getAttackTarget() != null && !this.goalOwner.getAttackTarget().isAlive()) {
			this.goalOwner.setAttackTarget(null);
		}

		LivingEntity revengeTarget = this.goalOwner.getRevengeTarget();

		if (revengeTarget != null) {
			if (revengeTarget instanceof WitherEntity && this.goalOwner.isEntityUndead()) {
				this.goalOwner.setRevengeTarget(null);
				return false;
			} else {
				for (Class<?> oclass : this.excludedTargetTypes) {
					if (oclass.isAssignableFrom(revengeTarget.getClass())) {
						this.goalOwner.setRevengeTarget(null);
						return false;
					}
				}

				this.target = revengeTarget;
			}
		} else if (!this.targetList.isEmpty()) {
			this.targetList.removeIf(e -> !e.isAddedToWorld() || !this.goalOwner.canAttack(e) || !this.goalOwner.canAttack(e.getType()));
			if (this.targetList.size() > 0) {
				this.targetList.sort(this::compareDistance);
				this.target = this.targetList.get(0);
			}
		}

		return this.isSuitableTarget(this.target, PREDICATE);
	}

	@Override
	public void startExecuting() {
		this.goalOwner.setAttackTarget(this.target);
		this.unseenMemoryTicks = 300;

		if (this.entityCallsForHelp) {
			alertOthers(this.goalOwner, this.excludedReinforcementTypes);
		}

		super.startExecuting();
	}

	@Override
	public boolean shouldContinueExecuting() {
		LivingEntity revengeTarget = this.goalOwner.getRevengeTarget();

		if (this.isSuitableTarget(revengeTarget, PREDICATE) && revengeTarget != this.target && this.compareDistance(revengeTarget, this.target) < 0) {
			return false;
		}

		return super.shouldContinueExecuting();
	}

	@Override
	public void tick() {
		if (this.target != null && this.targetList.size() < 10 && !this.targetList.contains(this.target) && (!(this.target instanceof MobEntity) || ((MobEntity)this.target).getAttackTarget() == this.goalOwner)) {
			this.targetList.add(this.target);
		}
	}

	public MBHurtByTargetGoal setCallsForHelp(Class<?>... excludedReinforcementTypes) {
		this.entityCallsForHelp = true;
		this.excludedReinforcementTypes = excludedReinforcementTypes;
		return this;
	}

	private int compareDistance(LivingEntity first, LivingEntity second) {
		return Double.compare(this.goalOwner.getDistanceSq(first), this.goalOwner.getDistanceSq(second));
	}

	public static void alertOthers(MobEntity alertingMob, Class<?>... excludedReinforcementTypes) {
		if (alertingMob.isAIDisabled() || alertingMob.getRevengeTarget() == null) {
			return;
		}

		double d0 = (double)alertingMob.getNavigator().getPathSearchRange();
		for (MobEntity otherMob : alertingMob.world.getEntitiesWithinAABB(alertingMob.getClass(), new AxisAlignedBB(alertingMob.posX, alertingMob.posY, alertingMob.posZ, alertingMob.posX + 1.0D, alertingMob.posY + 1.0D, alertingMob.posZ + 1.0D).grow(d0, 10.0D, d0))) {
			if (otherMob != null && alertingMob != otherMob && (!(alertingMob instanceof TameableEntity) || ((TameableEntity)alertingMob).getOwner() == ((TameableEntity)otherMob).getOwner()) && !otherMob.isOnSameTeam(alertingMob.getRevengeTarget())) {
				boolean flag = false;
				for (Class<?> oclass : excludedReinforcementTypes) {
					if (otherMob.getClass() == oclass) {
						flag = true;
						break;
					}
				}

				if (!flag) {
					otherMob.setRevengeTarget(alertingMob.getRevengeTarget());
				}
			}
		}
	}
}