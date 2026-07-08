package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecartJumpingPayload(int entityId, boolean jumping) implements CustomPacketPayload {

    public static final Type<MinecartJumpingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "minecart_jumping"));

    public static final StreamCodec<ByteBuf, MinecartJumpingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MinecartJumpingPayload::entityId,
            ByteBufCodecs.BOOL, MinecartJumpingPayload::jumping,
            MinecartJumpingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}