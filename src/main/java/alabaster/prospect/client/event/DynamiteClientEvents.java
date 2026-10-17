package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.client.sound.DynamiteFuseSound;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class DynamiteClientEvents {

    private static final Map<UUID, DynamiteFuseSound> HELD_FUSES = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof DynamiteEntity dynamite) {
            Minecraft.getInstance().getSoundManager().play(new DynamiteFuseSound(dynamite, false));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        HELD_FUSES.values().removeIf(DynamiteFuseSound::isStopped);
        if (mc.level == null) {
            HELD_FUSES.clear();
            return;
        }

        for (Player player : mc.level.players()) {
            if (!DynamiteFuseSound.isHoldingLit(player) || HELD_FUSES.containsKey(player.getUUID())) continue;
            DynamiteFuseSound sound = new DynamiteFuseSound(player, true);
            HELD_FUSES.put(player.getUUID(), sound);
            mc.getSoundManager().play(sound);
        }
    }
}