package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.trinket.AltimeterItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class AltimeterClientEvents {

    private static final int REFRESH_TICKS = 5;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) return;
        if (minecraft.level.getGameTime() % REFRESH_TICKS != 0) return;

        boolean holding = false;
        for (InteractionHand hand : InteractionHand.values()) {
            if (minecraft.player.getItemInHand(hand).getItem() instanceof AltimeterItem) {
                holding = true;
            }
        }
        if (holding) {
            minecraft.player.displayClientMessage(AltimeterItem.reading(minecraft.level, minecraft.player), true);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null || !(event.getItemStack().getItem() instanceof AltimeterItem)) return;
        List<Component> tooltip = event.getToolTip();
        int index = Math.min(1, tooltip.size());
        tooltip.add(index, AltimeterItem.reading(player.level(), player).copy().withStyle(ChatFormatting.GRAY));
        Component layer = AltimeterItem.layer(player.level(), player);
        if (layer != null) {
            tooltip.add(index + 1, layer.copy().withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
