package alabaster.prospect.common.particle;

import alabaster.prospect.common.block.GlowColor;
import alabaster.prospect.common.registry.ProspectParticleTypes;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record GlowSporeOptions(Optional<GlowColor> color) implements ParticleOptions {

    public static final MapCodec<GlowSporeOptions> CODEC =
            GlowColor.CODEC.optionalFieldOf("color").xmap(GlowSporeOptions::new, GlowSporeOptions::color);

    public static final StreamCodec<ByteBuf, GlowSporeOptions> STREAM_CODEC =
            ByteBufCodecs.optional(ByteBufCodecs.idMapper(id -> GlowColor.values()[id], GlowColor::ordinal))
                    .map(GlowSporeOptions::new, GlowSporeOptions::color);

    public static GlowSporeOptions of(GlowColor color) {
        return new GlowSporeOptions(Optional.of(color));
    }

    @Override
    public ParticleType<?> getType() {
        return ProspectParticleTypes.GLOW_SPORE.get();
    }
}
