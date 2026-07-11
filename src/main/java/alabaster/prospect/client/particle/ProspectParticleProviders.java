package alabaster.prospect.client.particle;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ProspectParticleProviders {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ProspectParticleTypes.TOPAZ_GLINT.get(), TopazGlintParticleProvider::new);
    }
}