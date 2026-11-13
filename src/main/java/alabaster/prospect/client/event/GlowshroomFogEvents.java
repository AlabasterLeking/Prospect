package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.minecraft.client.renderer.FogRenderer;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class GlowshroomFogEvents {

    private static final float FAR_PLANE = 72.0F;
    private static final float NEAR_PLANE = 24.0F;
    private static final float FADE_PER_TICK = 0.02F;

    private static float strength;
    private static float previousStrength;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        previousStrength = strength;
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || minecraft.level == null) {
            strength = 0.0F;
            return;
        }
        boolean inGrove = minecraft.level.getBiome(BlockPos.containing(camera.getEyePosition())).is(ProspectBiomes.GLOWSHROOM_GROVE);
        strength = Mth.clamp(strength + (inGrove ? FADE_PER_TICK : -FADE_PER_TICK), 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        float blend = Mth.lerp((float) event.getPartialTick(), previousStrength, strength);
        if (blend <= 0.0F) return;
        float far = event.getFarPlaneDistance();
        if (far <= FAR_PLANE) return;
        event.setFarPlaneDistance(Mth.lerp(blend, far, FAR_PLANE));
        event.setNearPlaneDistance(Mth.lerp(blend, event.getNearPlaneDistance(), NEAR_PLANE));
        event.setCanceled(true);
    }
}