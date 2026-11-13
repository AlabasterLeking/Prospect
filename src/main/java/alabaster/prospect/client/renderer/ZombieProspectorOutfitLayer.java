package alabaster.prospect.client.renderer;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.prospector.ZombieProspector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class ZombieProspectorOutfitLayer extends RenderLayer<ZombieProspector, ZombieVillagerModel<ZombieProspector>> {

    private static final ResourceLocation OUTFIT = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "textures/entity/zombie_prospector_outfit.png");

    public ZombieProspectorOutfitLayer(RenderLayerParent<ZombieProspector, ZombieVillagerModel<ZombieProspector>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ZombieProspector entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        renderColoredCutoutModel(getParentModel(), OUTFIT, poseStack, buffer, packedLight, entity, -1);
    }
}