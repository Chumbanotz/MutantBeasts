package chumbanotz.mutantbeasts.packet;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(MutantBeasts.createResource("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static void register() {
		int id = 0;
		INSTANCE.registerMessage(++id, AnimationPacket.class, AnimationPacket::encode, AnimationPacket::new, AnimationPacket::handle);
		INSTANCE.registerMessage(++id, SimpleAnimationPacket.class, SimpleAnimationPacket::encode, SimpleAnimationPacket::new, SimpleAnimationPacket::handle);
	}
}