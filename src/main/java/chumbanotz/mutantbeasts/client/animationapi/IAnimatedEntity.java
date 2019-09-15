package chumbanotz.mutantbeasts.client.animationapi;

import chumbanotz.mutantbeasts.packet.AnimationPacket;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import net.minecraft.entity.Entity;

public interface IAnimatedEntity {
	int getAnimationID();

	void setAnimationID(int id);

	int getAnimationTick();

	void setAnimationTick(int tick);

	default void sendPacket(int animId) {
		this.setAnimationID(animId);
		PacketHandler.INSTANCE.sendToServer(new AnimationPacket(animId, ((Entity)this).getEntityId()));
	}
}