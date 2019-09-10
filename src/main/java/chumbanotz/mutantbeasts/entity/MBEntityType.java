package chumbanotz.mutantbeasts.entity;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.mutant.MutantCreeperEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSkeletonPartEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import chumbanotz.mutantbeasts.entity.mutant.MutantZombieEntity;
import chumbanotz.mutantbeasts.entity.mutant.SpiderPigEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantArrowEntity;
import chumbanotz.mutantbeasts.entity.projectile.MutantSnowGolemBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MutantBeasts.MOD_ID)
public class MBEntityType {
	public static final EntityType<CreeperMinionEntity> CREEPER_MINION = null;
	public static final EntityType<EndermanCloneEntity> ENDERMAN_CLONE = null;
	public static final EntityType<MutantCreeperEntity> MUTANT_CREEPER = null;
	public static final EntityType<MutantSnowGolemEntity> MUTANT_SNOW_GOLEM = null;
	public static final EntityType<MutantSnowGolemBlockEntity> MUTANT_SNOW_GOLEM_BLOCK = null;
	public static final EntityType<MutantSkeletonEntity> MUTANT_SKELETON = null;
	public static final EntityType<MutantArrowEntity> MUTANT_SKELETON_ARROW = null;
	public static final EntityType<MutantSkeletonPartEntity> MUTANT_SKELETON_PART = null;
	public static final EntityType<MutantZombieEntity> MUTANT_ZOMBIE = null;
	public static final EntityType<SpiderPigEntity> SPIDER_PIG = null;
}