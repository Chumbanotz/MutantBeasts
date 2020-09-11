package chumbanotz.mutantbeasts.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SkullSpiritParticle extends SpriteTexturedParticle {
	private SkullSpiritParticle(World world, double x, double y, double z, double xx, double yy, double zz) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= 0.10000000149011612D;
		this.motionY *= 0.10000000149011612D;
		this.motionZ *= 0.10000000149011612D;
		this.motionX += xx;
		this.motionY += yy;
		this.motionZ += zz;
		float color = 1.0F - (float)(Math.random() * 0.2D);
		this.setColor(color, color, color);
		float scale = 0.4F + this.rand.nextFloat() * 0.6F;
		this.particleScale *= scale;
		this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
		this.maxAge = (int)((float)this.maxAge * scale);
		this.canCollide = false;
	}

	@Override
	public float getScale(float partialTicks) {
		float timeScale = ((float)this.age + partialTicks) / (float)this.maxAge * 32.0F;
		timeScale = MathHelper.clamp(timeScale, 0.0F, 1.0F);
		return this.particleScale * timeScale;
	}

	@Override
	public void tick() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.age++ >= this.maxAge) {
			this.setExpired();
		}

		this.motionY += 0.002D;
		this.move(this.motionX, this.motionY, this.motionZ);

		if (this.posY == this.prevPosY) {
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	public static class Factory implements IParticleFactory<BasicParticleType> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			SkullSpiritParticle skullSpiritParticle = new SkullSpiritParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
			skullSpiritParticle.selectSpriteRandomly(this.spriteSet);
			return skullSpiritParticle;
		}
	}
}