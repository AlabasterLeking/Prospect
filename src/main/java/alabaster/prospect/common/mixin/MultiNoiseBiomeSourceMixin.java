package alabaster.prospect.common.mixin;

import alabaster.prospect.common.registry.ProspectBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {

    @Unique
    private static final int PROSPECT_GROVE_MAX_Y = 0;

    @Unique
    private Climate.ParameterList<Holder<Biome>> prospect$withoutGrove;

    @Shadow
    private Climate.ParameterList<Holder<Biome>> parameters() {
        throw new AssertionError();
    }

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    private void prospect$keepGroveDeep(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (QuartPos.toBlock(y) <= PROSPECT_GROVE_MAX_Y || !cir.getReturnValue().is(ProspectBiomes.GLOWSHROOM_GROVE)) return;
        if (prospect$withoutGrove == null) {
            prospect$withoutGrove = new Climate.ParameterList<>(parameters().values().stream().filter(entry -> !entry.getSecond().is(ProspectBiomes.GLOWSHROOM_GROVE)).toList());
        }
        cir.setReturnValue(prospect$withoutGrove.findValue(sampler.sample(x, y, z)));
    }
}
