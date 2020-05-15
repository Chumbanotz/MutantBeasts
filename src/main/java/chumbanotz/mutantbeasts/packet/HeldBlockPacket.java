package chumbanotz.mutantbeasts.packet;

import java.util.Optional;
import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.mutant.MutantEndermanEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class HeldBlockPacket {
	private int entityId;
	private short blockId;
	private byte blockIndex;

	public HeldBlockPacket(MutantEndermanEntity enderman, int bId, int index) {
		this.entityId = enderman.getEntityId();
		this.blockId = (short)bId;
		this.blockIndex = (byte)index;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeShort(this.blockId);
		buffer.writeByte(this.blockIndex);
	}

	HeldBlockPacket(PacketBuffer buffer) {
		this.entityId = buffer.readInt();
		this.blockId = buffer.readShort();
		this.blockIndex = buffer.readByte();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
			optionalWorld.filter(ClientWorld.class::isInstance).ifPresent(world -> {
				MutantEndermanEntity enderman = (MutantEndermanEntity)world.getEntityByID(this.entityId);
				if (enderman != null && this.blockIndex > 0 && this.blockId != -1) {
					enderman.heldBlock[this.blockIndex] = this.blockId;
					enderman.heldBlockTick[this.blockIndex] = 0;
				}
			});
		});

		context.get().setPacketHandled(true);
	}
}