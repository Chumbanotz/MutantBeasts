package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
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

	@Override
	public void tick() {
		super.tick();
		if (this.entity.isInLava()) {
			if (this.entity.getPathPriority(PathNodeType.LAVA) <= -1) {
				this.entity.setPathPriority(PathNodeType.LAVA, 8.0F);
			}
		} else if (this.entity.getPathPriority(PathNodeType.LAVA) != -1) {
			this.entity.setPathPriority(PathNodeType.LAVA, -1.0F);
		}
	}
}