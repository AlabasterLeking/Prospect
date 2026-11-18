package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GrappleReelPayload(int direction) implements CustomPacketPayload {

    public static final Type<GrappleReelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "grapple_reel"));

    public static final StreamCodec<ByteBuf, GrappleReelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GrappleReelPayload::direction,
            GrappleReelPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}