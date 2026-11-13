package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.Set;

public class BellMushroomFeature extends GlowingHugeMushroomFeature {

    @Override
    protected int stemHeight(RandomSource random, int radius) {
        return radius * 2 + 1 + random.nextInt(3);
    }

    @Override
    protected void buildCap(Set<BlockPos> cap, BlockPos top, int radius, RandomSource random) {
        ring(cap, top.below(2), radius - 0.5, radius + 0.5);
        ring(cap, top.below(), radius - 0.5, radius + 0.5);
        ring(cap, top, 0.0, radius + 0.5);
        ring(cap, top.above(), 0.0, Math.max(1.0, radius - 0.1));
        ring(cap, top.above(2), 0.0, Math.max(0.5, radius - 1.0));
        if (radius >= 3) ring(cap, top.above(3), 0.0, radius - 2.0);
    }
}