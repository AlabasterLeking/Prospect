package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ZiplineRidePayload(int ziplineId) implements CustomPacketPayload {

    public static final Type<ZiplineRidePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "zipline_ride"));

    public static final StreamCodec<ByteBuf, ZiplineRidePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ZiplineRidePayload::ziplineId,
            ZiplineRidePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}