package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.Set;

public class ParasolMushroomFeature extends GlowingHugeMushroomFeature {

    @Override
    protected int stemHeight(RandomSource random, int radius) {
        return radius * 2 + random.nextInt(3);
    }

    @Override
    protected void buildCap(Set<BlockPos> cap, BlockPos top, int radius, RandomSource random) {
        ring(cap, top.below(), radius, radius + 0.5);
        ring(cap, top, 0.0, radius + 0.5);
        if (radius >= 2) ring(cap, top.above(), 0.0, radius - 0.7);
        if (radius >= 4) ring(cap, top.above(2), 0.0, radius - 2.2);
    }
}