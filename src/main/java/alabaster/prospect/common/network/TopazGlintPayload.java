package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TopazGlintPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<TopazGlintPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "topaz_glint"));

    public static final StreamCodec<ByteBuf, TopazGlintPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TopazGlintPayload::pos,
            TopazGlintPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}