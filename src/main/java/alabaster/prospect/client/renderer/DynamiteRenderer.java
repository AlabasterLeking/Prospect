package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class DynamiteRenderer extends EntityRenderer<DynamiteEntity> {

    private static final float SCALE = 0.5F;
    private static final float REST_HEIGHT = 1.5F / 16.0F * SCALE;
    private static final float MODEL_CENTER_Y = 6.0F / 16.0F;

    private final ItemRenderer itemRenderer;

    public DynamiteRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(DynamiteEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, REST_HEIGHT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getSpin(partialTick)));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0.0F, 0.5F - MODEL_CENTER_Y, 0.0F);
        itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DynamiteEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}