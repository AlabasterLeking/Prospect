package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.client.renderer.ChainRenderer;
import alabaster.prospect.client.renderer.RopeHand;
import alabaster.prospect.common.event.MinecartLinkEvents;
import alabaster.prospect.common.tag.ProspectModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class MinecartLinkClientEvents {

    private static final double ATTACH_HEIGHT = 0.45;
    private static final double END_OFFSET = 0.45;
    private static final float SAG = 0.06F;
    private static final float PENDING_SAG = 0.15F;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;
        boolean mainHand = player.getMainHandItem().is(ProspectModTags.CHAINS);
        if (MinecartLinkEvents.clientPending != -1 && !mainHand && !player.getOffhandItem().is(ProspectModTags.CHAINS)) {
            MinecartLinkEvents.clientPending = -1;
        }
        if (MinecartLinkEvents.CLIENT_LINKS.isEmpty() && MinecartLinkEvents.clientPending == -1) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (Map.Entry<Integer, Set<Integer>> entry : MinecartLinkEvents.CLIENT_LINKS.entrySet()) {
            if (!(level.getEntity(entry.getKey()) instanceof AbstractMinecart first)) continue;
            for (int otherId : entry.getValue()) {
                if (otherId < entry.getKey() || !(level.getEntity(otherId) instanceof AbstractMinecart second)) continue;
                Vec3 firstPos = first.getPosition(partialTick);
                Vec3 secondPos = second.getPosition(partialTick);
                draw(poseStack, buffer, level, camera, attach(firstPos, secondPos), attach(secondPos, firstPos), SAG, false);
            }
        }

        if (level.getEntity(MinecartLinkEvents.clientPending) instanceof AbstractMinecart cart) {
            Vec3 hand = RopeHand.position(player, mainHand, partialTick);
            Vec3 start = attach(cart.getPosition(partialTick), hand);
            draw(poseStack, buffer, level, camera, start, hand, PENDING_SAG, false);
        }

        buffer.endBatch(ChainRenderer.renderType());
    }

    private static Vec3 attach(Vec3 from, Vec3 toward) {
        double dx = toward.x - from.x;
        double dz = toward.z - from.z;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4) return from.add(0.0, ATTACH_HEIGHT, 0.0);
        double reach = Math.min(END_OFFSET, length * 0.5);
        return from.add(dx / length * reach, ATTACH_HEIGHT, dz / length * reach);
    }

    private static void draw(PoseStack poseStack, MultiBufferSource buffer, ClientLevel level, Vec3 camera, Vec3 start, Vec3 end, float sag, boolean warn) {
        poseStack.pushPose();
        poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
        ChainRenderer.render(poseStack, buffer, level, start, end, sag, warn);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        MinecartLinkEvents.CLIENT_LINKS.clear();
        MinecartLinkEvents.clientPending = -1;
    }
}
