package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.prospector.ZombieProspector;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class ZombieProspectorRenderer extends HumanoidMobRenderer<ZombieProspector, ZombieVillagerModel<ZombieProspector>> {

    private static final ResourceLocation ZOMBIE_VILLAGER_BASE = ResourceLocation.withDefaultNamespace("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieProspectorRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieVillagerModel<>(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), 0.5F);
        addLayer(new ZombieProspectorOutfitLayer(this));
        addLayer(new HumanoidArmorLayer<>(this,
                new ZombieVillagerModel<>(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)),
                new ZombieVillagerModel<>(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieProspector entity) {
        return ZOMBIE_VILLAGER_BASE;
    }

    @Override
    protected boolean isShaking(ZombieProspector entity) {
        return super.isShaking(entity) || entity.isConverting();
    }
}