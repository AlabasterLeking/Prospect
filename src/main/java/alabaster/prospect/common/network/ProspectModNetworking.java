package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ProspectModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ToggleMiningHelmetLightPayload.TYPE,
                ToggleMiningHelmetLightPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

                    ItemStack helmet = serverPlayer.getItemBySlot(EquipmentSlot.HEAD);
                    if (!helmet.is(ProspectModTags.MINING_HELMET_LIGHT)) return;

                    boolean nowOn = MiningHelmetItem.toggleLight(helmet);
                    serverPlayer.displayClientMessage(
                            Component.translatable(nowOn
                                    ? "tooltip.prospect.mining_helmet.light_on"
                                    : "tooltip.prospect.mining_helmet.light_off"),
                            true);
                })
        );
    }
}