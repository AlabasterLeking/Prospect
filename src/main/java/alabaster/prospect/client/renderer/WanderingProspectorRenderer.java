package alabaster.prospect.client.renderer;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.prospector.WanderingProspector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.resources.ResourceLocation;

public class WanderingProspectorRenderer extends MobRenderer<WanderingProspector, VillagerModel<WanderingProspector>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "textures/entity/wandering_prospector.png");

    public WanderingProspectorRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        addLayer(new ProspectorHelmetLayer(this, context.getItemInHandRenderer()));
        addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(WanderingProspector entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(WanderingProspector entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}