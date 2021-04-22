package chumbanotz.mutantbeasts.packet;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import chumbanotz.mutantbeasts.particles.MBParticleTypes;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class SpawnParticlePacket {
	private final IParticleData particle;
	private final double posX;
	private final double posY;
	private final double posZ;
	private final double motionX;
	private final double motionY;
	private final double motionZ;
	private final int amount;
	private final Random random = new Random();

	public SpawnParticlePacket(IParticleData particle, double posX, double posY, double posZ, double motionX, double motionY, double motionZ, int amount) {
		this.particle = particle;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.amount = amount;
	}

	@SuppressWarnings("deprecation")
	void encode(PacketBuffer buffer) {
		buffer.writeInt(Registry.PARTICLE_TYPE.getId(this.particle.getType()));
		this.particle.write(buffer);
		buffer.writeDouble(this.posX);
		buffer.writeDouble(this.posY);
		buffer.writeDouble(this.posZ);
		buffer.writeDouble(this.motionX);
		buffer.writeDouble(this.motionY);
		buffer.writeDouble(this.motionZ);
		buffer.writeInt(this.amount);
	}

	@SuppressWarnings("deprecation")
	SpawnParticlePacket(PacketBuffer buffer) {
		ParticleType<?> particletype = Registry.PARTICLE_TYPE.getByValue(buffer.readInt());
		if (particletype == null) {
			particletype = ParticleTypes.BARRIER;
		}

		this.particle = this.readParticle(buffer, particletype);
		this.posX = buffer.readDouble();
		this.posY = buffer.readDouble();
		this.posZ = buffer.readDouble();
		this.motionX = buffer.readDouble();
		this.motionY = buffer.readDouble();
		this.motionZ = buffer.readDouble();
		this.amount = buffer.readInt();
	}

	private <T extends IParticleData> T readParticle(PacketBuffer packetBuffer, ParticleType<T> particleType) {
		return particleType.getDeserializer().read(particleType, packetBuffer);
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
			optionalWorld.filter(ClientWorld.class::isInstance).ifPresent(world -> {
				for (int i = 0; i < this.amount; i++) {
					if (this.particle == MBParticleTypes.ENDERSOUL) {
						float f = (this.random.nextFloat() - 0.5F) * 1.8F;
						float f1 = (this.random.nextFloat() - 0.5F) * 1.8F;
						float f2 = (this.random.nextFloat() - 0.5F) * 1.8F;
						double tempX = this.posX + ((this.random.nextFloat() - 0.5F) * this.motionX);
						double tempY = this.posY + ((this.random.nextFloat() - 0.5F) * this.motionY) + 0.5D;
						double tempZ = this.posZ + ((this.random.nextFloat() - 0.5F) * this.motionZ);
						world.addOptionalParticle(this.particle, true, tempX, tempY, tempZ, (double)f, (double)f1, (double)f2);
					} else {
						double posX = this.posX + (this.random.nextFloat() * this.motionX * 2.0F) - this.motionX;
						double posY = this.posY + 0.5D + (this.random.nextFloat() * this.motionY);
						double posZ = this.posZ + (this.random.nextFloat() * this.motionZ * 2.0F) - this.motionZ;
						double x = this.random.nextGaussian() * 0.02D;
						double y = this.random.nextGaussian() * 0.02D;
						double z = this.random.nextGaussian() * 0.02D;
						world.addParticle(this.particle, posX, posY, posZ, x, y, z);
					}
				}
			});
		});

		context.get().setPacketHandled(true);
	}
}