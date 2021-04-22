package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MBGroundPathNavigator extends GroundPathNavigator {
	private boolean shouldAvoidRain;

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
	protected void trimPath() {
		super.trimPath();
		if (this.shouldAvoidRain && this.world.isRaining()) {
			if (this.world.isRainingAt(this.entity.getPosition())) {
				return;
			}

			for (int i = 0; i < this.currentPath.getCurrentPathLength(); ++i) {
				PathPoint pathpoint = this.currentPath.getPathPointFromIndex(i);
				if (this.world.isRainingAt(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z))) {
					this.currentPath.setCurrentPathLength(i);
					return;
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.entity.isImmuneToFire()) {
			if (this.entity.isInLava()) {
				if (this.entity.getPathPriority(PathNodeType.LAVA) < 8.0F) {
					this.entity.setPathPriority(PathNodeType.LAVA, 8.0F);
				}
			} else if (this.entity.getPathPriority(PathNodeType.LAVA) > -1.0F) {
				this.entity.setPathPriority(PathNodeType.LAVA, -1.0F);
			}
		}
	}

	public MBGroundPathNavigator setAvoidRain(boolean avoidRain) {
		this.shouldAvoidRain = avoidRain;
		return this;
	}
}