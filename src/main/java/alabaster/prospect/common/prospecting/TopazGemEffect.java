package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.network.TopazGlintPayload;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.server.level.ServerPlayer;

public class TopazGemEffect implements ProspectingGemEffect {

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        if (ctx.player() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new TopazGlintPayload(hit.pos()));
        }
    }
}