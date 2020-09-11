package chumbanotz.mutantbeasts.packet;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class MBPacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			MutantBeasts.prefix("main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public static void register() {
		INSTANCE.registerMessage(0, CreeperMinionTrackerPacket.class, CreeperMinionTrackerPacket::encode, CreeperMinionTrackerPacket::new, CreeperMinionTrackerPacket::handle);
		INSTANCE.registerMessage(1, HeldBlockPacket.class, HeldBlockPacket::encode, HeldBlockPacket::new, HeldBlockPacket::handle);
		INSTANCE.registerMessage(2, FluidParticlePacket.class, FluidParticlePacket::encode, FluidParticlePacket::new, FluidParticlePacket::handle);
		INSTANCE.registerMessage(3, SpawnParticlePacket.class, SpawnParticlePacket::encode, SpawnParticlePacket::new, SpawnParticlePacket::handle);
	}
}