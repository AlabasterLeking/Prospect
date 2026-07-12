package alabaster.prospect.common.prospecting;

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

    @Override
    public OreCandidate selectCandidate(List<OreCandidate> candidates, OreCandidate currentBest, ProspectingContext ctx) {
        OreCandidate best = currentBest;
        double bestScore = best == null ? Double.MAX_VALUE : score(best);

        for (OreCandidate candidate : candidates) {
            double s = score(candidate);
            if (s < bestScore) {
                bestScore = s;
                best = candidate;
            }
        }
        return best;
    }

    private static double score(OreCandidate candidate) {
        int rarity = RARITY.getOrDefault(candidate.oreTagId(), 0);
        return candidate.distanceFromOrigin() - rarity * DISTANCE_WEIGHT_PER_RANK;
    }
}