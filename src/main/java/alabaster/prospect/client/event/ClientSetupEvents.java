package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.client.renderer.SparkleNodeRenderer;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.registry.ProspectModEntities;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "prospect", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetupEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ProspectModEntities.SPARKLE_NODE.get(),
                SparkleNodeRenderer::new
        );
    }

    // Custom Item Rendering for Pans
    private static final ResourceLocation PANNING_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "panning");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof PanItem)
                    .forEach(item -> ItemProperties.register(
                            item,
                            PANNING_PROPERTY,
                            (stack, level, entity, seed) ->
                                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                                            ? 1.0f : 0.0f
                    ));
        });
    }
}
