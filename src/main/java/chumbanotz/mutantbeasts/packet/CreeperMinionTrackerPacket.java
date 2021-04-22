package chumbanotz.mutantbeasts.packet;

import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreeperMinionTrackerPacket {
	private final int entityId;
	private final byte optionsId;
	private final boolean setOption;

	public CreeperMinionTrackerPacket(CreeperMinionEntity creeperMinion, int optionsId, boolean setOption) {
		this.entityId = creeperMinion.getEntityId();
		this.optionsId = (byte)optionsId;
		this.setOption = setOption;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeByte(this.optionsId);
		buffer.writeBoolean(this.setOption);
	}

	CreeperMinionTrackerPacket(PacketBuffer buffer) {
		this.entityId = buffer.readInt();
		this.optionsId = buffer.readByte();
		this.setOption = buffer.readBoolean();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			CreeperMinionEntity creeperMinion = (CreeperMinionEntity)context.get().getSender().world.getEntityByID(this.entityId);
			if (this.optionsId == 0) {
				creeperMinion.setDestroyBlocks(this.setOption);
			} else if (this.optionsId == 1) {
				creeperMinion.setCustomNameVisible(this.setOption);
			} else if (this.optionsId == 2) {
				creeperMinion.setCanRideOnShoulder(this.setOption);
			}
		});

		context.get().setPacketHandled(true);
	}
}