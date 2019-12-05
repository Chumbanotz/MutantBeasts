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

		int id = 0;
		channel.registerMessage(++id, CreeperMinionTrackerPacket.class, CreeperMinionTrackerPacket::encode, CreeperMinionTrackerPacket::new, CreeperMinionTrackerPacket::handle);
		channel.registerMessage(++id, TeleportPacket.class, TeleportPacket::encode, TeleportPacket::new, TeleportPacket::handle);
		channel.registerMessage(++id, HeldBlockPacket.class, HeldBlockPacket::encode, HeldBlockPacket::new, HeldBlockPacket::handle);
		channel.registerMessage(++id, EndersoulHandPacket.class, EndersoulHandPacket::encode, EndersoulHandPacket::new, EndersoulHandPacket::handle);
	}

	/** <p>{@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}</p> {@link PacketDistributor#TRACKING_ENTITY} */
	public static <MSG> void sendToAllTracking(MSG message, Entity entity) {
		channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
	}

	public static <MSG> void sendToServer(MSG message) {
		channel.sendToServer(message);
	}
}