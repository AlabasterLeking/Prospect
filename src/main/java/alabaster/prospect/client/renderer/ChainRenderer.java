package alabaster.prospect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public final class ChainRenderer {

    private static final ResourceLocation CHAIN = ResourceLocation.withDefaultNamespace("block/chain");
    private static final int SEGMENTS = 24;
    private static final double HALF_WIDTH = 1.5 / 16.0;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    private ChainRenderer() {}

    public static RenderType renderType() {
        return RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 originWorld, Vec3 targetWorld, float sag) {
        render(poseStack, buffer, level, originWorld, targetWorld, sag, false);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 originWorld, Vec3 targetWorld, float sag, boolean warn) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(CHAIN);
        VertexConsumer consumer = buffer.getBuffer(renderType());
        PoseStack.Pose pose = poseStack.last();
        Vec3 delta = targetWorld.subtract(originWorld);

        BlockPos startPos = BlockPos.containing(originWorld);
        BlockPos endPos = BlockPos.containing(targetWorld);
        int startBlock = level.getBrightness(LightLayer.BLOCK, startPos);
        int endBlock = level.getBrightness(LightLayer.BLOCK, endPos);
        int startSky = level.getBrightness(LightLayer.SKY, startPos);
        int endSky = level.getBrightness(LightLayer.SKY, endPos);
        float green = warn ? 0.35F : 1.0F;
        float blue = warn ? 0.35F : 1.0F;

        Vec3[] points = new Vec3[SEGMENTS + 1];
        for (int i = 0; i <= SEGMENTS; i++) {
            double t = (double) i / SEGMENTS;
            points[i] = new Vec3(delta.x * t, delta.y * t - sag * 4.0 * t * (1.0 - t), delta.z * t);
        }

        double travelled = 0.0;
        for (int i = 0; i < SEGMENTS; i++) {
            Vec3 from = points[i];
            Vec3 to = points[i + 1];
            double length = from.distanceTo(to);
            if (length < 1.0E-6) continue;
            Vec3 direction = to.subtract(from).scale(1.0 / length);
            Vec3 side = direction.cross(UP);
            side = side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
            Vec3 normal = side.cross(direction).normalize();
            float progress = (i + 0.5F) / SEGMENTS;
            int light = LightTexture.pack((int) Mth.lerp(progress, startBlock, endBlock), (int) Mth.lerp(progress, startSky, endSky));

            double done = 0.0;
            while (done < length - 1.0E-6) {
                double offset = (travelled + done) % 1.0;
                double step = Math.min(length - done, 1.0 - offset);
                Vec3 a = from.add(direction.scale(done));
                Vec3 b = from.add(direction.scale(done + step));
                float v0 = sprite.getV((float) offset);
                float v1 = sprite.getV((float) (offset + step));
                strip(consumer, pose, a, b, side.scale(HALF_WIDTH), normal, sprite.getU(0.0F), sprite.getU(3.0F / 16.0F), v0, v1, light, green, blue);
                strip(consumer, pose, a, b, normal.scale(HALF_WIDTH), side, sprite.getU(3.0F / 16.0F), sprite.getU(6.0F / 16.0F), v0, v1, light, green, blue);
                done += step;
            }
            travelled += length;
        }
    }

    private static void strip(VertexConsumer consumer, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 offset, Vec3 normal, float u0, float u1, float v0, float v1, int light, float green, float blue) {
        vertex(consumer, pose, a.subtract(offset), u0, v0, light, normal, green, blue);
        vertex(consumer, pose, a.add(offset), u1, v0, light, normal, green, blue);
        vertex(consumer, pose, b.add(offset), u1, v1, light, normal, green, blue);
        vertex(consumer, pose, b.subtract(offset), u0, v1, light, normal, green, blue);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 point, float u, float v, int light, Vec3 normal, float green, float blue) {
        consumer.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(1.0F, green, blue, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
