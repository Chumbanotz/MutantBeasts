package chumbanotz.mutantbeasts.packet;

import java.util.Optional;
import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class HeldBlockPacket {
	private final int entityId;
	private final int blockId;
	private final byte blockIndex;

	public HeldBlockPacket(MutantEndermanEntity enderman, int bId, int index) {
		this.entityId = enderman.getEntityId();
		this.blockId = bId;
		this.blockIndex = (byte)index;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(this.blockId);
		buffer.writeByte(this.blockIndex);
	}

	HeldBlockPacket(PacketBuffer buffer) {
		this.entityId = buffer.readInt();
		this.blockId = buffer.readInt();
		this.blockIndex = buffer.readByte();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
			optionalWorld.filter(ClientWorld.class::isInstance).ifPresent(world -> {
				Entity entity = world.getEntityByID(this.entityId);
				if (entity instanceof MutantEndermanEntity && this.blockIndex > 0 && this.blockId != -1) {
					((MutantEndermanEntity)entity).sendHoldBlock(this.blockIndex, this.blockId);
				}
			});
		});

		context.get().setPacketHandled(true);
	}
}