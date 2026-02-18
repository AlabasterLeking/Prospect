package alabaster.prospect.client.event;


import alabaster.prospect.client.renderer.SparkleNodeRenderer;
import alabaster.prospect.common.registry.ProspectModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "prospect", bus = EventBusSubscriber.Bus.MOD, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class ClientSetupEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ProspectModEntities.SPARKLE_NODE.get(),
                SparkleNodeRenderer::new
        );
    }
}
