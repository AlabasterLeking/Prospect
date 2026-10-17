package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecartJumpRequestPayload(int charge) implements CustomPacketPayload {

    public static final Type<MinecartJumpRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "minecart_jump_request"));

    public static final StreamCodec<ByteBuf, MinecartJumpRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MinecartJumpRequestPayload::charge,
            MinecartJumpRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}