package chumbanotz.mutantbeasts.packet;

import java.util.function.Supplier;

import chumbanotz.mutantbeasts.client.animationapi.IAnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AnimationPacket {
	protected final byte animId;
	protected final int entityId;

	public AnimationPacket(int animId, int entityId) {
		this.animId = (byte)animId;
		this.entityId = entityId;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeByte(this.animId);
		buffer.writeInt(this.entityId);
	}

	/** decode */
	AnimationPacket(PacketBuffer buffer) {
		this.animId = buffer.readByte();
		this.entityId = buffer.readInt();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			IAnimatedEntity entity = (IAnimatedEntity)Minecraft.getInstance().world.getEntityByID(this.entityId);
			if (entity != null && this.animId != -1) {
				entity.setAnimationID(this.animId);
				if (this.animId == 0) {
					entity.setAnimationTick(0);
				}
			}
		});

		context.get().setPacketHandled(true);
	}
}