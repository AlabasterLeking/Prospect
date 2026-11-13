package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.Set;

public class DomeMushroomFeature extends GlowingHugeMushroomFeature {

    @Override
    protected int stemHeight(RandomSource random, int radius) {
        return radius + 1 + random.nextInt(3);
    }

    @Override
    protected void buildCap(Set<BlockPos> cap, BlockPos top, int radius, RandomSource random) {
        ring(cap, top.below(), radius - 0.4, radius + 0.4);
        ring(cap, top, 0.0, radius + 0.4);
        ring(cap, top.above(), 0.0, radius - 0.2);
        ring(cap, top.above(2), 0.0, radius - 1.0);
        if (radius >= 3) ring(cap, top.above(3), 0.0, radius - 2.0);
    }
}