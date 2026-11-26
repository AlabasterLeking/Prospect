package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.client.InstrumentReadings;
import alabaster.prospect.common.item.trinket.AltimeterItem;
import alabaster.prospect.integration.curios.CuriosCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class AltimeterClientEvents {

    private static final int REFRESH_TICKS = 5;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) return;
        if (minecraft.level.getGameTime() % REFRESH_TICKS != 0) return;

        for (InteractionHand hand : InteractionHand.values()) {
            Component reading = reading(minecraft.level, minecraft.player, minecraft.player.getItemInHand(hand));
            if (reading != null) {
                minecraft.player.displayClientMessage(reading, true);
                return;
            }
        }
        if (ModList.get().isLoaded(CuriosCompat.MODID)) {
            ItemStack worn = CuriosCompat.find(minecraft.player, stack -> stack.getItem() instanceof AltimeterItem);
            if (!worn.isEmpty()) {
                minecraft.player.displayClientMessage(AltimeterItem.reading(minecraft.level, minecraft.player), true);
            }
        }
    }

    @Nullable
    private static Component reading(Level level, Player player, ItemStack stack) {
        if (stack.getItem() instanceof AltimeterItem) return AltimeterItem.reading(level, player);
        Component clock = InstrumentReadings.clock(level, stack);
        if (clock != null) return clock;
        List<Component> compass = InstrumentReadings.compass(level, player, stack);
        return compass.isEmpty() ? null : compass.get(0);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        ItemStack stack = event.getItemStack();
        Level level = player.level();
        List<Component> lines;
        if (stack.getItem() instanceof AltimeterItem) {
            Component layer = AltimeterItem.layer(level, player);
            lines = layer == null ? List.of(AltimeterItem.reading(level, player)) : List.of(AltimeterItem.reading(level, player), layer);
        } else {
            Component clock = InstrumentReadings.clock(level, stack);
            lines = clock != null ? List.of(clock) : InstrumentReadings.compass(level, player, stack, true);
        }
        List<Component> tooltip = event.getToolTip();
        int index = Math.min(1, tooltip.size());
        for (int i = 0; i < lines.size(); i++) {
            tooltip.add(index + i, lines.get(i).copy().withStyle(i == 0 ? ChatFormatting.GRAY : ChatFormatting.DARK_AQUA));
        }
    }
}
