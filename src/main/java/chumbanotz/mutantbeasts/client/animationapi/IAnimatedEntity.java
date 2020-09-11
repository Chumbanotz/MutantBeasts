package chumbanotz.mutantbeasts.client.animationapi;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public interface IAnimatedEntity extends IEntityAdditionalSpawnData {
	int getAnimationID();

	void setAnimationID(int id);

	int getAnimationTick();

	void setAnimationTick(int tick);

	@Override
	default void writeSpawnData(PacketBuffer buffer) {
		buffer.writeVarInt(this.getAnimationID());
		buffer.writeVarInt(this.getAnimationTick());
	}

	@Override
	default void readSpawnData(PacketBuffer additionalData) {
		this.setAnimationID(additionalData.readVarInt());
		this.setAnimationTick(additionalData.readVarInt());
	}
}