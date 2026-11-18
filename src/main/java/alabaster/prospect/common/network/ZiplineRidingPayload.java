package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ZiplineRidingPayload(int playerId, boolean riding) implements CustomPacketPayload {

    public static final Type<ZiplineRidingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "zipline_riding"));

    public static final StreamCodec<ByteBuf, ZiplineRidingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ZiplineRidingPayload::playerId,
            ByteBufCodecs.BOOL, ZiplineRidingPayload::riding,
            ZiplineRidingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
