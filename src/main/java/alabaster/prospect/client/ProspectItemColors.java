package alabaster.prospect.client;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ProspectItemColors {

    private static final int DEFAULT_SHELL_COLOR = 0xEAEAF2;

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) return -1;
            DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
            int rgb = dyed != null ? dyed.rgb() : DEFAULT_SHELL_COLOR;
            return 0xFF000000 | rgb;
        }, ProspectModItems.MINING_HELMET.get());
    }
}