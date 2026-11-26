package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecartLinkPayload(int first, int second, boolean linked) implements CustomPacketPayload {

    public static final Type<MinecartLinkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "minecart_link"));

    public static final StreamCodec<ByteBuf, MinecartLinkPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MinecartLinkPayload::first,
            ByteBufCodecs.VAR_INT, MinecartLinkPayload::second,
            ByteBufCodecs.BOOL, MinecartLinkPayload::linked,
            MinecartLinkPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
