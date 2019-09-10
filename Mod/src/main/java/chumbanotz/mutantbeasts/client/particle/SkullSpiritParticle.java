package chumbanotz.mutantbeasts.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullSpiritParticle extends SpriteTexturedParticle {
	private float skullScale;
	private boolean up;
	private static final ResourceLocation particleTexture = new ResourceLocation("mutantbeasts:textures/particles.png");
	private static final ResourceLocation defaultParticleTexture = new ResourceLocation("textures/particle/particles.png");

	public SkullSpiritParticle(World p_i50999_1_, double p_i50999_2_, double p_i50999_4_, double p_i50999_6_, double p_i50999_8_, double p_i50999_10_, double p_i50999_12_) {
		super(p_i50999_1_, p_i50999_2_, p_i50999_4_, p_i50999_6_, p_i50999_8_, p_i50999_10_, p_i50999_12_);
		this.motionX *= 0.10000000149011612D;
		this.motionY *= 0.10000000149011612D;
		this.motionZ *= 0.10000000149011612D;
		this.motionX += p_i50999_8_;
		this.motionY += p_i50999_10_;
		this.motionZ += p_i50999_12_;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F - (float)(Math.random() * 0.2D);
		this.particleScale *= 1.0F;
		float scale = 0.4F + this.rand.nextFloat() * 0.6F;
		this.particleScale *= scale;
		this.skullScale = this.particleScale;
		this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
		this.maxAge = (int)((float)this.maxAge * scale);
		this.canCollide = false;
		// this.up = moveUp;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, ActiveRenderInfo entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float timeScale = ((float)this.age + partialTicks) / (float)this.maxAge * 32.0F;
		if (timeScale < 0.0F) {
			timeScale = 0.0F;
		}

		if (timeScale > 1.0F) {
			timeScale = 1.0F;
		}

		this.particleScale = this.skullScale * timeScale;
		TextureManager renderer = Minecraft.getInstance().textureManager;
		//buffer.draw();
		renderer.bindTexture(particleTexture);
		//buffer.startDrawingQuads();
		//buffer.setBrightness(this.getBrightnessForRender(partialTicks));
		//buffer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		//buffer.draw();
		renderer.bindTexture(defaultParticleTexture);
		//buffer.startDrawingQuads();
	}

	@Override
	public void tick() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.age++ >= this.maxAge) {
			this.setExpired();
		}

		if (this.up)
		{
			this.motionY += 0.002D;
		}

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
}