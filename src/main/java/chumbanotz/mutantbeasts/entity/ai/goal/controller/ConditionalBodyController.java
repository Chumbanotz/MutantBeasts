package chumbanotz.mutantbeasts.entity.ai.goal.controller;

import java.util.function.BooleanSupplier;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.BodyController;

public class ConditionalBodyController extends BodyController {
	private final BooleanSupplier condition;

	public ConditionalBodyController(MobEntity mob, BooleanSupplier condition) {
		super(mob);
		this.condition = condition;
	}

	@Override
	public void updateRenderAngles() {
		if (this.condition.getAsBoolean()) {
			super.updateRenderAngles();
		}
	}	
}