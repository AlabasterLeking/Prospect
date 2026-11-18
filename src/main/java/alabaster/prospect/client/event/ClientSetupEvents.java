package alabaster.prospect.client.event;

import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import alabaster.prospect.common.item.SlimeBucketItem;
import alabaster.prospect.Prospect;
import alabaster.prospect.client.model.ChainKnotModel;
import alabaster.prospect.client.renderer.DynamiteRenderer;
import alabaster.prospect.client.renderer.GrapplingHookRenderer;
import alabaster.prospect.client.renderer.ZiplineRenderer;
import alabaster.prospect.client.renderer.SparkleNodeRenderer;
import alabaster.prospect.client.renderer.WanderingProspectorRenderer;
import alabaster.prospect.client.renderer.ZombieProspectorRenderer;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.item.trinket.AltimeterItem;
import alabaster.prospect.common.item.trinket.PortableSmelterItem;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import alabaster.prospect.common.registry.ProspectKeyBindings;
import alabaster.prospect.common.registry.ProspectModEntities;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetupEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ProspectModEntities.SPARKLE_NODE.get(),
                SparkleNodeRenderer::new
        );
        event.registerEntityRenderer(
                ProspectModEntities.WANDERING_PROSPECTOR.get(),
                WanderingProspectorRenderer::new
        );
        event.registerEntityRenderer(
                ProspectModEntities.ZOMBIE_PROSPECTOR.get(),
                ZombieProspectorRenderer::new
        );
        event.registerEntityRenderer(
                ProspectModEntities.DYNAMITE.get(),
                DynamiteRenderer::new
        );
        event.registerEntityRenderer(
                ProspectModEntities.GRAPPLING_HOOK.get(),
                GrapplingHookRenderer::new
        );
        event.registerEntityRenderer(
                ProspectModEntities.ZIPLINE.get(),
                ZiplineRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChainKnotModel.LAYER, ChainKnotModel::createBodyLayer);
    }

    // Custom Item Rendering for Pans
    private static final ResourceLocation PANNING_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "panning");
    private static final ResourceLocation PANNING_LAVA_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "panning_lava");
    private static final ResourceLocation ELEVATION_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "elevation");
    private static final ResourceLocation LIT_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "lit");
    private static final ResourceLocation DANCE_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "dance");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ProspectModItems.ALTIMETER.get(), ELEVATION_PROPERTY,
                    (stack, level, entity, seed) -> {
                        Entity holder = entity != null ? entity : stack.getEntityRepresentation();
                        if (holder == null || holder.level() == null) return 0.5F;
                        return holder instanceof LivingEntity living ? AltimeterItem.needle(holder.level(), living) : 0.5F;
                    });
            ItemProperties.register(ProspectModItems.SLIME_BUCKET.get(), DANCE_PROPERTY,
                    (stack, level, entity, seed) -> {
                        if (!SlimeBucketItem.isDancing()) return 0.0F;
                        Level world = level != null ? level : entity != null ? entity.level() : Minecraft.getInstance().level;
                        if (world == null) return 0.0F;
                        long step = world.getGameTime() / SlimeBucketItem.DANCE_TICKS_PER_FRAME;
                        return (1 + step % SlimeBucketItem.DANCE_FRAMES) / 10.0F;
                    });
            ItemProperties.register(ProspectModItems.PORTABLE_SMELTER.get(), LIT_PROPERTY,
                    (stack, level, entity, seed) -> PortableSmelterItem.readState(stack).isLit() ? 1.0F : 0.0F);
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof PanItem)
                    .forEach(item -> {
                        ItemProperties.register(
                                item,
                                PANNING_PROPERTY,
                                (stack, level, entity, seed) ->
                                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                                                ? 1.0f : 0.0f
                        );
                        ItemProperties.register(
                                item,
                                PANNING_LAVA_PROPERTY,
                                (stack, level, entity, seed) ->
                                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                                                && SparkleNodeEntity.isClientHarvestingLava(entity.getId())
                                                ? 1.0f : 0.0f
                        );
                    });
        });
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ProspectKeyBindings.TOGGLE_MINING_HELMET_LIGHT);
        event.register(ProspectKeyBindings.GRAPPLE_REEL_IN);
        event.register(ProspectKeyBindings.GRAPPLE_LET_OUT);
    }
}