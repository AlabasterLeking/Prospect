package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.prospector.WanderingProspector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ProspectorHelmetLayer extends RenderLayer<WanderingProspector, VillagerModel<WanderingProspector>> {

    private static final float HELMET_DROP = 0.2F;

    private final ItemInHandRenderer itemInHandRenderer;

    public ProspectorHelmetLayer(RenderLayerParent<WanderingProspector, VillagerModel<WanderingProspector>> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WanderingProspector entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return;

        poseStack.pushPose();
        getParentModel().getHead().translateAndRotate(poseStack);
        CustomHeadLayer.translateToHead(poseStack, true);
        poseStack.translate(0.0F, -HELMET_DROP, 0.0F);
        itemInHandRenderer.renderItem(entity, helmet, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}