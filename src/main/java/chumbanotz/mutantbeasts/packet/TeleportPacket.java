package chumbanotz.mutantbeasts.packet;

import java.util.Optional;
import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class TeleportPacket {
	private int entityId;
	private int teleX;
	private int teleY;
	private int teleZ;

	public TeleportPacket(MutantEndermanEntity enderman, int x, int y, int z) {
		this.entityId = enderman.getEntityId();
		this.teleX = x;
		this.teleY = y;
		this.teleZ = z;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(this.teleX);
		buffer.writeInt(this.teleY);
		buffer.writeInt(this.teleZ);
	}

	/** decode */
	TeleportPacket(PacketBuffer buffer) {
		this.entityId = buffer.readInt();
		this.teleX = buffer.readInt();
		this.teleY = buffer.readInt();
		this.teleZ = buffer.readInt();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
			optionalWorld.filter(ClientWorld.class::isInstance).ifPresent(world -> {
				MutantEndermanEntity enderman = (MutantEndermanEntity)world.getEntityByID(this.entityId);
				if (enderman != null) {
					enderman.handleTeleport(this.teleX, this.teleY, this.teleZ);
				}
			});
		});

		context.get().setPacketHandled(true);
	}
}