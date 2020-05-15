package chumbanotz.mutantbeasts.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndersoulParticle extends SpriteTexturedParticle {
	private EndersoulParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
		this.maxAge = (int)(Math.random() * 15.0D) + 10;
		this.motionX *= 0.10000000149011612D;
		this.motionY *= 0.10000000149011612D;
		this.motionZ *= 0.10000000149011612D;
		this.motionX += xSpeedIn;
		this.motionY += ySpeedIn;
		this.motionZ += zSpeedIn;
		this.particleScale = 0.1F * (this.rand.nextFloat() * 0.4F + 2.4F);
		float color = this.rand.nextFloat() * 0.6F + 0.4F;
		this.particleRed = color * 0.9F;
		this.particleGreen = color * 0.3F;
		this.particleBlue = color;
		this.canCollide = false;
	}

	@Override
	public float getScale(float partialTicks) {
		float scale = 1.0F - (this.age + partialTicks) / this.maxAge;
		scale *= scale;
		scale = 1.0F - scale;
		return this.particleScale * scale;
	}

	@Override
	protected int getBrightnessForRender(float partialTick) {
		return 15728880;
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

		this.motionX *= 0.9D;
		this.motionY *= 0.9D;
		this.motionZ *= 0.9D;
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			EndersoulParticle endersoulParticle = new EndersoulParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
			endersoulParticle.selectSpriteRandomly(this.spriteSet);
			return endersoulParticle;
		}
	}
}