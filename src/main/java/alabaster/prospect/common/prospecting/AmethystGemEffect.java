package alabaster.prospect.common.prospecting;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class AmethystGemEffect implements ProspectingGemEffect {

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        if (ctx.player() instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }
}