package chumbanotz.mutantbeasts.packet;

import java.util.function.Supplier;

import chumbanotz.mutantbeasts.entity.projectile.ThrowableBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class EndersoulHandPacket {
	private int entityId;
	private int posX;
	private int posY;
	private int posZ;

	public EndersoulHandPacket(ThrowableBlockEntity eblock) {
		this.entityId = eblock.getEntityId();
		this.posX = (int)(eblock.posX * 10000.0D);
		this.posY = (int)(eblock.posY * 10000.0D);
		this.posZ = (int)(eblock.posZ * 10000.0D);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(this.posX);
		buffer.writeInt(this.posY);
		buffer.writeInt(this.posZ);
	}

	EndersoulHandPacket(PacketBuffer buffer) {
		this.entityId = buffer.readInt();
		this.posX = buffer.readInt();
		this.posY = buffer.readInt();
		this.posZ = buffer.readInt();
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		double x = this.posX / 10000.0D, y = this.posY / 10000.0D, z = this.posZ / 10000.0D;
		Entity entity = context.get().getSender().world.getEntityByID(this.entityId);

		if (entity != null) {
			entity.setPosition(x, y, z);
		}
	}
}