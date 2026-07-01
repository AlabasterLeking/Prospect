package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class MiningHelmetOverlayEvents {

    private static final int LAYER_COUNT = 16;
    private static final int OUTER_ALPHA = 0;
    private static final int CENTER_ALPHA = 2;
    private static final int GLOW_RGB = 0xFFF3CE;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.is(ProspectModTags.MINING_HELMET_LIGHT)) return;

        BlockPos pos = mc.player.blockPosition();
        if (mc.level.canSeeSky(pos)) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int maxDim = Math.max(width, height);
        int centerX = width / 2;
        int centerY = height / 2;

        for (int i = 0; i < LAYER_COUNT; i++) {
            double t = i / (double) (LAYER_COUNT - 1);
            int radius = (int) (maxDim * (1.0 - t * 0.95));
            int alpha = OUTER_ALPHA + (int) Math.round((CENTER_ALPHA - OUTER_ALPHA) * t);
            int argb = (alpha << 24) | GLOW_RGB;
            guiGraphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY + radius, argb);
        }
    }
}