package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.particle.GlowSporeOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ProspectParticleTypes {
    private ProspectParticleTypes() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Prospect.MODID);

    public static final Supplier<SimpleParticleType> TOPAZ_GLINT =
            PARTICLE_TYPES.register("topaz_glint", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> SPARK =
            PARTICLE_TYPES.register("spark", () -> new SimpleParticleType(false));

    public static final Supplier<ParticleType<GlowSporeOptions>> GLOW_SPORE =
            PARTICLE_TYPES.register("glow_spore", () -> new ParticleType<GlowSporeOptions>(false) {
                @Override
                public MapCodec<GlowSporeOptions> codec() {
                    return GlowSporeOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, GlowSporeOptions> streamCodec() {
                    return GlowSporeOptions.STREAM_CODEC;
                }
            });
}