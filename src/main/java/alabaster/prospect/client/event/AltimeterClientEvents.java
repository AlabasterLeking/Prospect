package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.trinket.AltimeterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

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
}