package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.network.TopazGlintPayload;
import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

public class TopazGemEffect implements ProspectingGemEffect {

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

        ProspectingSockets sockets = ctx.stack().getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        boolean hasEmerald = sockets.has(ProspectingGems.EMERALD);

        if (hasEmerald) {
            TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(hit.oreTagId()));
            Set<BlockPos> vein = VeinFinder.findVein(ctx.level(), hit.pos(), tag);
            for (BlockPos pos : vein) {
                PacketDistributor.sendToPlayer(serverPlayer, new TopazGlintPayload(pos));
            }
        } else {
            PacketDistributor.sendToPlayer(serverPlayer, new TopazGlintPayload(hit.pos()));
        }
    }
}