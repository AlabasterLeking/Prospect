package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.network.PanningTablesPayload;
import alabaster.prospect.common.prospecting.PanningTables;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Prospect.MODID)
public class PanningSyncEvents {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        PanningTablesPayload payload = new PanningTablesPayload(PanningTables.collect(event.getPlayerList().getServer()));
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}
