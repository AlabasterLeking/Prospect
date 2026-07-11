package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ProspectParticleTypes {
    private ProspectParticleTypes() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Prospect.MODID);

    public static final Supplier<SimpleParticleType> TOPAZ_GLINT =
            PARTICLE_TYPES.register("topaz_glint", () -> new SimpleParticleType(true));
}