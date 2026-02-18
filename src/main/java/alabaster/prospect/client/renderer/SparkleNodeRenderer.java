package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SparkleNodeRenderer extends EntityRenderer<SparkleNodeEntity> {

    public SparkleNodeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(
            SparkleNodeEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        // Intentionally empty — invisible
    }

    @Override
    public ResourceLocation getTextureLocation(SparkleNodeEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(SparkleNodeEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }
}