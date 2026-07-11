package alabaster.prospect.common.prospecting;

import java.util.List;
import java.util.Map;


public class DiamondGemEffect implements ProspectingGemEffect {

    private static final Map<String, Integer> RARITY = Map.ofEntries(
            Map.entry("minecraft:coal_ores", 0),
            Map.entry("minecraft:copper_ores", 0),
            Map.entry("minecraft:iron_ores", 1),
            Map.entry("minecraft:gold_ores", 1),
            Map.entry("minecraft:redstone_ores", 2),
            Map.entry("minecraft:lapis_ores", 2),
            Map.entry("minecraft:diamond_ores", 2),
            Map.entry("minecraft:emerald_ores", 2),
            Map.entry("prospect:ruby_ores", 2),
            Map.entry("prospect:sapphire_ores", 2),
            Map.entry("prospect:topaz_ores", 2),
            Map.entry("minecraft:netherite_ores", 3)
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