package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.network.MinecartJumpRequestPayload;
import alabaster.prospect.common.utilities.ClientJumpingMinecart;
import alabaster.prospect.common.utilities.MinecartJumpConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class MinecartJumpbarEvents {

    private static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
    private static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");

    private static int jumpCharge = 0;
    private static float jumpScale = 0.0f;
    private static float prevJumpScale = 0.0f;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        prevJumpScale = jumpScale;

        if (mc.player == null || !(mc.player.getVehicle() instanceof AbstractMinecart cart)) {
            jumpCharge = 0;
            jumpScale = 0.0f;
            prevJumpScale = 0.0f;
            return;
        }

        boolean airborne = cart instanceof ClientJumpingMinecart jumpingCart && jumpingCart.prospect$isClientAirborne();
        boolean jumping = mc.options.keyJump.isDown();

        if (jumping && !airborne) {
            jumpCharge = Math.min(jumpCharge + MinecartJumpConstants.CHARGE_RATE, 100);
        } else {
            if (!jumping && !airborne && jumpCharge > 0) {
                PacketDistributor.sendToServer(new MinecartJumpRequestPayload(jumpCharge));
            }
            jumpCharge = 0;
        }

        jumpScale = jumpCharge / 100.0f;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !(mc.player.getVehicle() instanceof AbstractMinecart)) return;

        float scale = Mth.lerp(event.getPartialTick().getGameTimeDeltaPartialTick(false), prevJumpScale, jumpScale);
        if (scale <= 0.0f) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = guiGraphics.guiWidth() / 2 - 91;
        int y = guiGraphics.guiHeight() - 32 + 3;
        int width = (int) (scale * 183.0f);

        guiGraphics.blitSprite(JUMP_BAR_BACKGROUND_SPRITE, x, y, 182, 5);
        if (width > 0) {
            guiGraphics.blitSprite(JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, x, y, width, 5);
        }
    }
}