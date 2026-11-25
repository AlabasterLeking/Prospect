package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.prospecting.PanningTables;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PanningTablesPayload(List<PanningTables.Table> tables) implements CustomPacketPayload {

    public static final Type<PanningTablesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "panning_tables"));

    public static final StreamCodec<ByteBuf, PanningTablesPayload> STREAM_CODEC =
            PanningTables.Table.STREAM_CODEC.apply(ByteBufCodecs.list()).map(PanningTablesPayload::new, PanningTablesPayload::tables);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
