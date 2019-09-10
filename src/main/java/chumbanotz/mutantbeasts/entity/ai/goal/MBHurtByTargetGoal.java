package chumbanotz.mutantbeasts.entity.ai.goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.capability.SummonableCapability;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class MBHurtByTargetGoal extends TargetGoal {
	private static final EntityPredicate PREDICATE = new EntityPredicate().setLineOfSiteRequired().setUseInvisibilityCheck();
	private boolean entityCallsForHelp;
	private final Class<?>[] excludedAttackerTypes;
	private Class<?>[] excludedReinforcementTypes;
	private final List<LivingEntity> attackerList = new ArrayList<>();

	public MBHurtByTargetGoal(CreatureEntity creatureIn, Class<?>... excludedAttackerTypes) {
		super(creatureIn, true);
		this.excludedAttackerTypes = excludedAttackerTypes;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean shouldExecute() {
		LivingEntity revengetarget = this.goalOwner.getRevengeTarget();
		boolean shouldTarget = true;

		if (revengetarget != null) {
			if (revengetarget instanceof MobEntity && SummonableCapability.get((MobEntity)revengetarget).getSummoner() == this.goalOwner) {
				shouldTarget = false;
				return false;
			}

			for (Class<?> oclass : this.excludedAttackerTypes) {
				if (oclass.isAssignableFrom(revengetarget.getClass())) {
					shouldTarget = false;
					return false;
				}
			}
		} else if (!this.attackerList.isEmpty()) {
			this.attackerList.removeIf(e -> e.deathTime > 0 || !e.isAddedToWorld());
			if (this.attackerList.size() > 0) {
				revengetarget = this.attackerList.get(this.attackerList.size() - 1);
				MutantBeasts.LOGGER.debug(this.goalOwner.getName().getString() + " has previous attacker " + revengetarget.getName().getString());
			}
		} else if (this.entityCallsForHelp) {
			double d0 = this.getTargetDistance();

			for (MobEntity mobentity : this.goalOwner.world.getEntitiesWithinAABB(this.goalOwner.getClass(), new AxisAlignedBB(this.goalOwner.posX, this.goalOwner.posY, this.goalOwner.posZ, this.goalOwner.posX + 1.0D, this.goalOwner.posY + 1.0D, this.goalOwner.posZ + 1.0D).grow(d0, 10.0D, d0))) {
				if (mobentity != null && this.goalOwner != mobentity && !mobentity.isAIDisabled() && mobentity.getRevengeTarget() != null && (!(this.goalOwner instanceof TameableEntity) || ((TameableEntity)this.goalOwner).getOwner() == ((TameableEntity)mobentity).getOwner()) && !mobentity.isOnSameTeam(mobentity.getRevengeTarget())) {
					revengetarget = mobentity.getRevengeTarget();

					for (Class<?> oclass : this.excludedAttackerTypes) {
						if (oclass.isAssignableFrom(revengetarget.getClass())) {
							break;
						}
					}

					if (this.excludedReinforcementTypes == null) {
						break;
					}

					for (Class<?> oclass : this.excludedReinforcementTypes) {
						if (mobentity.getClass() == oclass) {
							shouldTarget = false;
							break;
						}
					}
				}
			}
		}

		if (shouldTarget) {
			this.target = revengetarget;
		} else {
			this.goalOwner.setRevengeTarget(null);
		}

		return this.target != null && PREDICATE.canTarget(this.goalOwner, this.target);
	}

	@Override
	public void startExecuting() {
		this.goalOwner.setAttackTarget(this.target);
		this.unseenMemoryTicks = 300;
		super.startExecuting();
	}

	@Override
	public boolean shouldContinueExecuting() {
		LivingEntity revengeTarget = this.goalOwner.getRevengeTarget();

		if (revengeTarget != null && revengeTarget != this.target && PREDICATE.canTarget(this.goalOwner, revengeTarget)) {
			if (this.goalOwner.getDistanceSq(revengeTarget) < this.goalOwner.getDistanceSq(this.target)) {
				this.resetTask();
				MutantBeasts.LOGGER.debug(this.goalOwner.getName().getString() + " is changing target");
				return false;
			}
		}

		return super.shouldContinueExecuting();
	}

	@Override
	public void tick() {
		if (!this.attackerList.contains(this.target)) {
			MutantBeasts.LOGGER.debug(this.goalOwner.getName().getString() + " has committed " + this.target.getName().getString() + " to memory");
			this.attackerList.add(this.target);
		}
	}

	public MBHurtByTargetGoal setCallsForHelp(Class<?>... excludedReinforcementTypes) {
		this.entityCallsForHelp = true;
		this.excludedReinforcementTypes = excludedReinforcementTypes;
		return this;
	}
}