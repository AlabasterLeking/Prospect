package alabaster.prospect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class RopeRenderer {

    private static final int SEGMENTS = 24;
    private static final float WIDTH = 0.03F;

    private RopeRenderer() {}

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 originWorld, Vec3 targetWorld, float sag) {
        render(poseStack, buffer, level, originWorld, targetWorld, sag, false);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 originWorld, Vec3 targetWorld, float sag, boolean warn) {
        Vec3 delta = targetWorld.subtract(originWorld);
        float dx = (float) delta.x;
        float dy = (float) delta.y;
        float dz = (float) delta.z;

        float horizontal = Mth.sqrt(dx * dx + dz * dz);
        float sideX = horizontal < 1.0E-3F ? WIDTH / 2.0F : dz / horizontal * WIDTH / 2.0F;
        float sideZ = horizontal < 1.0E-3F ? 0.0F : dx / horizontal * WIDTH / 2.0F;

        BlockPos startPos = BlockPos.containing(originWorld);
        BlockPos endPos = BlockPos.containing(targetWorld);
        int startBlock = level.getBrightness(LightLayer.BLOCK, startPos);
        int endBlock = level.getBrightness(LightLayer.BLOCK, endPos);
        int startSky = level.getBrightness(LightLayer.SKY, startPos);
        int endSky = level.getBrightness(LightLayer.SKY, endPos);

        VertexConsumer consumer = buffer.getBuffer(RenderType.leash());
        Matrix4f pose = poseStack.last().pose();
        for (int i = 0; i <= SEGMENTS; i++) {
            vertexPair(consumer, pose, dx, dy, dz, sag, startBlock, endBlock, startSky, endSky, WIDTH, WIDTH, sideX, sideZ, i, false, warn);
        }
        for (int i = SEGMENTS; i >= 0; i--) {
            vertexPair(consumer, pose, dx, dy, dz, sag, startBlock, endBlock, startSky, endSky, WIDTH, 0.0F, sideX, sideZ, i, true, warn);
        }
    }

    private static void vertexPair(VertexConsumer consumer, Matrix4f pose, float dx, float dy, float dz, float sag, int startBlock, int endBlock, int startSky, int endSky, float thickness, float lift, float sideX, float sideZ, int index, boolean reverse, boolean warn) {
        float progress = (float) index / SEGMENTS;
        int light = LightTexture.pack((int) Mth.lerp(progress, startBlock, endBlock), (int) Mth.lerp(progress, startSky, endSky));
        float shade = index % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float red = (warn ? 0.8F : 0.45F) * shade;
        float green = (warn ? 0.18F : 0.33F) * shade;
        float blue = (warn ? 0.12F : 0.2F) * shade;
        float x = dx * progress;
        float y = dy * progress - sag * 4.0F * progress * (1.0F - progress);
        float z = dz * progress;
        consumer.addVertex(pose, x - sideX, y + lift, z + sideZ).setColor(red, green, blue, 1.0F).setLight(light);
        consumer.addVertex(pose, x + sideX, y + thickness - lift, z - sideZ).setColor(red, green, blue, 1.0F).setLight(light);
    }
}