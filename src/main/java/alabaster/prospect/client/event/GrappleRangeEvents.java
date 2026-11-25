package alabaster.prospect.client.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.client.ClientConfig;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class GrappleRangeEvents {

    private static final float THROW_SPEED = 1.7F;
    private static final float CROSSBOW_SPEED = 3.15F;
    private static final double DRAG = 0.99;
    private static final double GRAVITY = 0.05;
    private static final int MAX_FLIGHT_TICKS = 100;
    private static final int BRACKET_GAP = 5;
    private static final int BRACKET_LENGTH = 3;
    private static final int BRACKET_COLOR = 0xD0FFFFFF;
    private static final int CROSSHAIR_SIZE = 15;

    private static boolean inRange;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        inRange = false;
        if (!ClientConfig.RANGE_INDICATOR.get() || player == null || minecraft.level == null || player.isSpectator()) return;
        float speed = launchSpeed(player);
        if (speed <= 0.0F) return;
        double maxLength = (speed == THROW_SPEED ? Config.GRAPPLE_THROWN_MAX_LENGTH : Config.GRAPPLE_MAX_LENGTH).get();
        inRange = landsWithin(player, speed, maxLength);
    }

    private static float launchSpeed(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ProspectModItems.GRAPPLING_HOOK.get())) {
                return player.getCooldowns().isOnCooldown(held.getItem()) ? 0.0F : THROW_SPEED;
            }
            if (held.getItem() instanceof CrossbowItem && held.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).contains(ProspectModItems.GRAPPLING_HOOK.get())) {
                return CROSSBOW_SPEED;
            }
        }
        return 0.0F;
    }

    private static boolean landsWithin(Player player, float speed, double maxLength) {
        Vec3 start = player.getEyePosition();
        Vec3 position = start;
        Vec3 velocity = player.getLookAngle().scale(speed);
        double maxLengthSqr = maxLength * maxLength;
        for (int tick = 0; tick < MAX_FLIGHT_TICKS; tick++) {
            Vec3 next = position.add(velocity);
            BlockHitResult hit = player.level().clip(new ClipContext(position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) {
                return hit.getLocation().distanceToSqr(start) <= maxLengthSqr;
            }
            if (next.distanceToSqr(start) > maxLengthSqr) return false;
            position = next;
            velocity = velocity.scale(DRAG).subtract(0.0, GRAVITY, 0.0);
        }
        return false;
    }

    @SubscribeEvent
    public static void onRenderCrosshair(RenderGuiLayerEvent.Post event) {
        if (!inRange || !event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.getCameraType().isFirstPerson() || minecraft.options.hideGui) return;
        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = (graphics.guiWidth() - CROSSHAIR_SIZE) / 2 + CROSSHAIR_SIZE / 2;
        int centerY = (graphics.guiHeight() - CROSSHAIR_SIZE) / 2 + CROSSHAIR_SIZE / 2;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                int cornerX = centerX + sx * BRACKET_GAP;
                int cornerY = centerY + sy * BRACKET_GAP;
                graphics.fill(Math.min(cornerX, cornerX - sx * BRACKET_LENGTH), cornerY, Math.max(cornerX, cornerX - sx * BRACKET_LENGTH) + 1, cornerY + 1, BRACKET_COLOR);
                graphics.fill(cornerX, Math.min(cornerY, cornerY - sy * BRACKET_LENGTH), cornerX + 1, Math.max(cornerY, cornerY - sy * BRACKET_LENGTH) + 1, BRACKET_COLOR);
            }
        }
    }
}
