package chumbanotz.mutantbeasts.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.world.World;

public class ChemicalXEntity extends PotionEntity {

	public ChemicalXEntity(World p_i50150_1_, LivingEntity p_i50150_2_) {
		super(p_i50150_1_, p_i50150_2_);
	}

	public ChemicalXEntity(World p_i50151_1_, double p_i50151_2_, double p_i50151_4_, double p_i50151_6_) {
		super(p_i50151_1_, p_i50151_2_, p_i50151_4_, p_i50151_6_);
	}

	public ChemicalXEntity(EntityType<? extends PotionEntity> p_i50149_1_, World p_i50149_2_) {
		super(p_i50149_1_, p_i50149_2_);
	}
}