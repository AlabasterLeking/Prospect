package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DiamondGemEffect implements ProspectingGemEffect {

    private static final Map<String, Integer> RARITY = Map.ofEntries(
            Map.entry("c:ores/coal", 0),
            Map.entry("c:ores/copper", 0),
            Map.entry("c:ores/iron", 1),
            Map.entry("c:ores/gold", 1),
            Map.entry("c:ores/zinc", 1),
            Map.entry("c:ores/redstone", 2),
            Map.entry("c:ores/lapis", 2),
            Map.entry("c:ores/diamond", 2),
            Map.entry("c:ores/emerald", 2),
            Map.entry("c:ores/quartz", 2),
            Map.entry("c:ores/ruby", 2),
            Map.entry("c:ores/sapphire", 2),
            Map.entry("c:ores/topaz", 2),
            Map.entry("c:ores/netherite_scrap", 3)
    );

    private static final double DISTANCE_WEIGHT_PER_RANK = 6.0;
    private static final double DISTANCE_WEIGHT_PER_VEIN_BLOCK = 2.0;
    private static final int TOP_K_FOR_VEIN_CHECK = 5;

    @Override
    public OreCandidate selectCandidate(List<OreCandidate> candidates, OreCandidate currentBest, ProspectingContext ctx) {
        ProspectingSockets sockets = ctx.stack().getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        boolean hasQuartz = sockets.has(ProspectingGems.QUARTZ);
        boolean hasEmerald = sockets.has(ProspectingGems.EMERALD);

        if (hasQuartz) {
            return selectByVeinSize(candidates, currentBest, ctx);
        }

        OreCandidate best = currentBest;
        double bestScore = best == null ? Double.MAX_VALUE : rarityScore(best);
        for (OreCandidate candidate : candidates) {
            double s = rarityScore(candidate);
            if (s < bestScore) {
                bestScore = s;
                best = candidate;
            }
        }

        if (hasEmerald && best != null) {
            best = refineByVeinSize(candidates, best, ctx);
        }

        return best;
    }

    private static OreCandidate selectByVeinSize(List<OreCandidate> candidates, OreCandidate currentBest, ProspectingContext ctx) {
        OreCandidate best = currentBest;
        double bestScore = best == null ? Double.MAX_VALUE : veinScore(best, ctx);
        for (OreCandidate candidate : candidates) {
            double s = veinScore(candidate, ctx);
            if (s < bestScore) {
                bestScore = s;
                best = candidate;
            }
        }
        return best;
    }

    private static OreCandidate refineByVeinSize(List<OreCandidate> candidates, OreCandidate rarityBest, ProspectingContext ctx) {
        List<OreCandidate> topK = candidates.stream()
                .sorted(Comparator.comparingDouble(DiamondGemEffect::rarityScore))
                .limit(TOP_K_FOR_VEIN_CHECK)
                .toList();

        OreCandidate best = rarityBest;
        double bestScore = combinedScore(rarityBest, ctx);
        for (OreCandidate candidate : topK) {
            double s = combinedScore(candidate, ctx);
            if (s < bestScore) {
                bestScore = s;
                best = candidate;
            }
        }
        return best;
    }

    private static double rarityScore(OreCandidate candidate) {
        int rarity = RARITY.getOrDefault(candidate.oreTagId(), 0);
        return candidate.distanceFromOrigin() - rarity * DISTANCE_WEIGHT_PER_RANK;
    }

    private static double veinScore(OreCandidate candidate, ProspectingContext ctx) {
        int veinSize = veinSizeOf(candidate, ctx);
        return candidate.distanceFromOrigin() - veinSize * DISTANCE_WEIGHT_PER_VEIN_BLOCK;
    }

    private static double combinedScore(OreCandidate candidate, ProspectingContext ctx) {
        int rarity = RARITY.getOrDefault(candidate.oreTagId(), 0);
        int veinSize = veinSizeOf(candidate, ctx);
        return candidate.distanceFromOrigin() - rarity * DISTANCE_WEIGHT_PER_RANK - veinSize * DISTANCE_WEIGHT_PER_VEIN_BLOCK;
    }

    private static int veinSizeOf(OreCandidate candidate, ProspectingContext ctx) {
        TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(candidate.oreTagId()));
        return VeinFinder.findVein(ctx.level(), candidate.pos(), tag).size();
    }
}