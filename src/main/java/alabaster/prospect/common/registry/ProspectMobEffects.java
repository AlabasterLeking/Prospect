package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.effect.GlimmerEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ProspectMobEffects {
    private ProspectMobEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Prospect.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> GLIMMER = MOB_EFFECTS.register("glimmer", GlimmerEffect::new);
}
