package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.World;

public class MBGroundPathNavigator extends GroundPathNavigator {
	public MBGroundPathNavigator(MobEntity entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
	}

	@Override
	protected PathFinder getPathFinder(int i) {
		this.nodeProcessor = new MBWalkNodeProcessor();
		this.nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(this.nodeProcessor, i);
	}
}