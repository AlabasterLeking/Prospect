package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class AmethystGemEffect implements ProspectingGemEffect {

    private static final double PROXIMITY_RANGE = 32.0;
    private static final double VEIN_SATURATION = 8.0; // vein size at which the tone-deepening maxes out

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

        ProspectingSockets sockets = ctx.stack().getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);

        float pitch = 1.2f;
        float volume = 1.0f;

        if (sockets.has(ProspectingGems.RUBY)) {
            double dist = Math.sqrt(ctx.player().distanceToSqr(
                    hit.pos().getX() + 0.5, hit.pos().getY() + 0.5, hit.pos().getZ() + 0.5));
            float t = (float) Math.min(dist / PROXIMITY_RANGE, 1.0);
            pitch = 2.0f - 1.2f * t; // closer = higher pitch
        } else if (sockets.has(ProspectingGems.EMERALD)) {
            TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(hit.oreTagId()));
            int veinSize = VeinFinder.findVein(ctx.level(), hit.pos(), tag).size();
            float t = (float) Math.min(veinSize / VEIN_SATURATION, 1.0);
            pitch = 1.2f - 0.4f * t;  // bigger vein = deeper tone
            volume = 1.0f + 0.5f * t; // and a bit louder
        }

        serverPlayer.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, volume, pitch);
    }
}