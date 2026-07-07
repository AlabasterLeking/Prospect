package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.network.ToggleMiningHelmetLightPayload;
import alabaster.prospect.common.registry.ProspectKeyBindings;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class MiningHelmetInputEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ProspectKeyBindings.TOGGLE_MINING_HELMET_LIGHT.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleMiningHelmetLightPayload());
        }
    }
}