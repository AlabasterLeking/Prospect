package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SlimeChunkPayload(boolean inSlimeChunk) implements CustomPacketPayload {

    public static final Type<SlimeChunkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "slime_chunk"));

    public static final StreamCodec<ByteBuf, SlimeChunkPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SlimeChunkPayload::inSlimeChunk,
            SlimeChunkPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}