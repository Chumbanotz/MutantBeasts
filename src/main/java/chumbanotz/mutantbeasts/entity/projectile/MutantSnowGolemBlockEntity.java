package chumbanotz.mutantbeasts.entity.projectile;

import chumbanotz.mutantbeasts.entity.MBEntityType;
import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;

public class MutantSnowGolemBlockEntity extends ThrowableBlockEntity {
	public MutantSnowGolemBlockEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		super(MBEntityType.MUTANT_SNOW_GOLEM_BLOCK, packet, worldIn);
	}

	public MutantSnowGolemBlockEntity(MutantSnowGolemEntity mutantSnowGolem, World worldIn) {
		super(MBEntityType.MUTANT_SNOW_GOLEM_BLOCK, mutantSnowGolem, worldIn);
		this.rotationYaw = mutantSnowGolem.rotationYaw;

		if (this.getBlockState() != mutantSnowGolem.getIceBlock()) {
			this.setBlockState(mutantSnowGolem.getIceBlock());
		}
	}

	@Override
	protected BlockState getDefaultBlockState() {
		return Blocks.ICE.getDefaultState();
	}

	@Override
	protected float getGravityVelocity() {
		return this.getBlockState().getBlock() == Blocks.BLUE_ICE ? 0.08F : 0.06F;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			this.spawnIceParticles();
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(2.5D, 2.0D, 2.5D))) {
			if (!this.shouldIgnore(entity) && this.getDistanceSq(entity) <= 6.25D) {
				entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.getThrower()), this.getDamageByBlock() + (float)(this.rand.nextInt(3)));
			}
		}

		if (result.getType() == RayTraceResult.Type.ENTITY && !this.shouldIgnore(((EntityRayTraceResult)result).getEntity())) {
			((EntityRayTraceResult)result).getEntity().attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), this.getDamageByBlock());
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = ((BlockRayTraceResult)result).getPos();
			BlockState state = this.world.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof FireBlock) {
				this.world.extinguishFire(null, pos, ((BlockRayTraceResult)result).getFace());
			} else if (block instanceof CampfireBlock && state.get(CampfireBlock.LIT)) {
				this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);

				if (this.rand.nextInt(10 - (int)this.getDamageByBlock()) == 0) {
					this.world.setBlockState(pos, state.with(BlockStateProperties.LIT, Boolean.valueOf(false)), 11);
				}
			}
		}

		if (!this.world.isRemote) {
			this.world.setEntityState(this, (byte)3);
			this.remove();
		}

		this.playSound(this.getBlockState().getSoundType().getBreakSound(), 0.8F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.8F);
	}

	private void spawnIceParticles() {
		for (int i = 0; i < 60; ++i) {
			double x = this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
			double y = this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight());
			double z = this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth();
			double motx = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
			double moty = (double)(0.5F + this.rand.nextFloat() * 2.0F);
			double motz = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 3.0F);
			this.world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, this.getBlockState()), x, y, z, motx, moty, motz);
		}
	}

	private float getDamageByBlock() {
		return this.getBlockState().getBlock() == Blocks.PACKED_ICE ? 5.0F : this.getBlockState().getBlock() == Blocks.BLUE_ICE ? 6.0F : 4.0F;
	}

	private boolean shouldIgnore(Entity entity) {
		return entity == this.owner || !(entity instanceof IMob);
	}
}