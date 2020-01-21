package chumbanotz.mutantbeasts.packet;

import chumbanotz.mutantbeasts.MutantBeasts;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	private static SimpleChannel channel;

	public static void register() {
		channel = NetworkRegistry.newSimpleChannel(
				MutantBeasts.prefix("main"),
				() -> PROTOCOL_VERSION,
				PROTOCOL_VERSION::equals,
				PROTOCOL_VERSION::equals);

		channel.registerMessage(0, CreeperMinionTrackerPacket.class, CreeperMinionTrackerPacket::encode, CreeperMinionTrackerPacket::new, CreeperMinionTrackerPacket::handle);
		channel.registerMessage(1, HeldBlockPacket.class, HeldBlockPacket::encode, HeldBlockPacket::new, HeldBlockPacket::handle);
	}

	/** <p>{@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}</p> {@link PacketDistributor#TRACKING_ENTITY} */
	public static <MSG> void sendToAllTracking(MSG message, Entity entity) {
		channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
	}

	public static <MSG> void sendToServer(MSG message) {
		channel.sendToServer(message);
	}
}