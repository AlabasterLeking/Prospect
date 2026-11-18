package alabaster.prospect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LeadKnotRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/lead_knot.png");
    private static final double KNOT_Y = 0.375;

    private static LeashKnotModel<Entity> model;

    private LeadKnotRenderer() {}

    public static RenderType renderType() {
        return model().renderType(TEXTURE);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 origin, BlockPos fence) {
        poseStack.pushPose();
        poseStack.translate(fence.getX() + 0.5 - origin.x, fence.getY() + KNOT_Y - origin.y, fence.getZ() + 0.5 - origin.z);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        model().renderToBuffer(poseStack, buffer.getBuffer(renderType()), LevelRenderer.getLightColor(level, fence), OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static LeashKnotModel<Entity> model() {
        if (model == null) {
            model = new LeashKnotModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.LEASH_KNOT));
        }
        return model;
    }
}
