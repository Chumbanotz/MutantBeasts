package chumbanotz.mutantbeasts.packet;

import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.mutant.MutantSnowGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SimpleAnimationPacket extends AnimationPacket {
	private final boolean isAnimating;

	public SimpleAnimationPacket(int animId, int entityId, boolean isAnimating) {
		super(animId, entityId);
		this.isAnimating = isAnimating;
	}

	@Override
	void encode(PacketBuffer buffer) {
		super.encode(buffer);
		buffer.writeBoolean(this.isAnimating);
	}

	/** decode */
	SimpleAnimationPacket(PacketBuffer buffer) {
		super(buffer);
		this.isAnimating = buffer.readBoolean();
	}

	@Override
	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			MutantSnowGolemEntity entity = (MutantSnowGolemEntity)Minecraft.getInstance().world.getEntityByID(this.entityId);
			if (entity != null && this.animId == 0) {
				entity.isThrowing = this.isAnimating;
				if (!this.isAnimating) {
					entity.throwTick = 0;
				}
			}
		});

		context.get().setPacketHandled(true);
	}
}