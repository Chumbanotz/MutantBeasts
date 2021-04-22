package chumbanotz.mutantbeasts.packet;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class FluidParticlePacket {
	private final BlockState blockState;
	private final BlockPos blockPos;

	public FluidParticlePacket(BlockState blockState, BlockPos blockPos) {
		this.blockState = blockState;
		this.blockPos = blockPos;
	}

	void encode(PacketBuffer buffer) {
		buffer.writeInt(Block.getStateId(this.blockState));
		buffer.writeBlockPos(this.blockPos);
	}

	FluidParticlePacket(PacketBuffer buffer) {
		this.blockState = Block.getStateById(buffer.readInt());
		this.blockPos = buffer.readBlockPos();
	}

	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
			optionalWorld.filter(ClientWorld.class::isInstance).ifPresent(world -> {
				this.blockState.getFluidState().getShape(world, this.blockPos).forEachBox((p_228348_3_, p_228348_5_, p_228348_7_, p_228348_9_, p_228348_11_, p_228348_13_) -> {
					double d1 = Math.min(1.0D, p_228348_9_ - p_228348_3_);
					double d2 = Math.min(1.0D, p_228348_11_ - p_228348_5_);
					double d3 = Math.min(1.0D, p_228348_13_ - p_228348_7_);
					int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
					int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
					int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

					for (int l = 0; l < i; ++l) {
						for (int i1 = 0; i1 < j; ++i1) {
							for (int j1 = 0; j1 < k; ++j1) {
								double d4 = (l + 0.5D) / i;
								double d5 = (i1 + 0.5D) / j;
								double d6 = (j1 + 0.5D) / k;
								double d7 = d4 * d1 + p_228348_3_;
								double d8 = d5 * d2 + p_228348_5_;
								double d9 = d6 * d3 + p_228348_7_;
								world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, this.blockState).setPos(this.blockPos), true, this.blockPos.getX() + d7, this.blockPos.up().getY() + d8, this.blockPos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D);
							}
						}
					}
				});
			});
		});

		context.get().setPacketHandled(true);
	}
}