package alabaster.prospect.common.prospecting;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ProspectingSockets(Set<ResourceLocation> gemIds) {

    public static final ProspectingSockets EMPTY = new ProspectingSockets(Set.of());

    public static final Codec<ProspectingSockets> CODEC = ResourceLocation.CODEC
            .listOf()
            .xmap(list -> new ProspectingSockets(new LinkedHashSet<>(list)), sockets -> List.copyOf(sockets.gemIds()));

    public static final StreamCodec<ByteBuf, ProspectingSockets> STREAM_CODEC = StreamCodec.of(
            (buf, sockets) -> {
                ByteBufCodecs.VAR_INT.encode(buf, sockets.gemIds().size());
                for (ResourceLocation id : sockets.gemIds()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, id.toString());
                }
            },
            buf -> {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                Set<ResourceLocation> ids = new LinkedHashSet<>();
                for (int i = 0; i < size; i++) {
                    ids.add(ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf)));
                }
                return new ProspectingSockets(ids);
            }
    );

    public boolean has(ResourceLocation gemId) {
        return gemIds.contains(gemId);
    }

    public ProspectingSockets with(ResourceLocation gemId) {
        Set<ResourceLocation> copy = new LinkedHashSet<>(gemIds);
        copy.add(gemId);
        return new ProspectingSockets(copy);
    }
}