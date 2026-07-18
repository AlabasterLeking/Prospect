package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ProspectModNetworking {

    private static final Map<Integer, Boolean> REMOTE_JUMPING = new ConcurrentHashMap<>();

    public static boolean isRemoteJumping(int entityId) {
        return REMOTE_JUMPING.getOrDefault(entityId, false);
    }

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

                    MiningHelmetItem.toggleLight(helmet);
                })
        );

        registrar.playToClient(
                MinecartJumpingPayload.TYPE,
                MinecartJumpingPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (payload.jumping()) {
                        REMOTE_JUMPING.put(payload.entityId(), true);
                    } else {
                        REMOTE_JUMPING.remove(payload.entityId());
                    }
                })
        );

        registrar.playToClient(
                TopazGlintPayload.TYPE,
                TopazGlintPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.level == null) return;
                    net.minecraft.core.BlockPos pos = payload.pos();
                    for (int i = 0; i < 6; i++) {
                        double x = pos.getX() + mc.level.random.nextDouble();
                        double y = pos.getY() + mc.level.random.nextDouble();
                        double z = pos.getZ() + mc.level.random.nextDouble();
                        mc.level.addParticle(alabaster.prospect.common.registry.ProspectParticleTypes.TOPAZ_GLINT.get(),
                                x, y, z, 0, 0, 0);
                    }
                })
        );
    }
}