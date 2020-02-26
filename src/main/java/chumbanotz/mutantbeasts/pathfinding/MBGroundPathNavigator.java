package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

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
	protected void pathFollow() {
//		Vec3d vec3d = this.getEntityPosition();
//		this.maxDistanceToWaypoint = this.entity.getWidth() > 0.75F ? this.entity.getWidth() / 2.0F : 0.75F - this.entity.getWidth() / 2.0F;
//		Vec3d vec3d1 = this.currentPath.getCurrentPos();
//		if (Math.abs(this.entity.posX - (vec3d1.x + 0.5D)) < (double)this.maxDistanceToWaypoint && Math.abs(this.entity.posZ - (vec3d1.z + 0.5D)) < (double)this.maxDistanceToWaypoint && Math.abs(this.entity.posY - vec3d1.y) < 1.0D) {
//			this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
//		}
//
//		this.checkForStuck(vec3d);
		super.pathFollow();
	}

	@Override
	protected void trimPath() {
		super.trimPath();
		if (this.shouldAvoidRain && this.world.isRaining() && this.world.getBiome(this.entity.getPosition()).getPrecipitation() == Biome.RainType.RAIN) {
			if (this.world.isRainingAt(new BlockPos(this.entity.posX, this.entity.getBoundingBox().minY + 0.5D, this.entity.posZ))) {
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
				if (this.entity.getPathPriority(PathNodeType.LAVA) <= -1.0F) {
					this.entity.setPathPriority(PathNodeType.LAVA, 8.0F);
				}
			} else if (this.entity.getPathPriority(PathNodeType.LAVA) != -1.0F) {
				this.entity.setPathPriority(PathNodeType.LAVA, -1.0F);
			}
		}
	}

	public MBGroundPathNavigator setAvoidRain(boolean avoidRain) {
		this.shouldAvoidRain = avoidRain;
		return this;
	}
}