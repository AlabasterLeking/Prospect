package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.grapple.ZiplineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ZiplineRenderer extends EntityRenderer<ZiplineEntity> {

    public ZiplineRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ZiplineEntity line, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Vec3 start = line.getStart();
        Vec3 end = line.getEnd();
        long seed = line.getUUID().getLeastSignificantBits();
        Vec3 ropeEnd = line.isHooked() ? end.add(HookHeadRenderer.ropeOffset(line.getHookFace(), seed)) : end;
        if (line.isChain()) {
            ChainRenderer.render(poseStack, buffer, line.level(), start, ropeEnd, 0.0F);
            ChainKnotRenderer.render(poseStack, buffer, line.level(), start, BlockPos.containing(start));
            ChainKnotRenderer.render(poseStack, buffer, line.level(), start, BlockPos.containing(end));
            super.render(line, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        RopeRenderer.render(poseStack, buffer, line.level(), start, ropeEnd, 0.0F);

        LeadKnotRenderer.render(poseStack, buffer, line.level(), start, BlockPos.containing(start));
        if (line.isHooked()) {
            Vec3 offset = end.subtract(start);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            HookHeadRenderer.renderLanded(poseStack, buffer, packedLight, line.getHookFace(), seed);
            poseStack.popPose();
        } else {
            LeadKnotRenderer.render(poseStack, buffer, line.level(), start, BlockPos.containing(end));
        }
        super.render(line, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplineEntity line) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}