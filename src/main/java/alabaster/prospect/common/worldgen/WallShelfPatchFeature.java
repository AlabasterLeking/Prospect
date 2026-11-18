package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class WallShelfPatchFeature extends Feature<WallShelfPatchConfiguration> {

    public WallShelfPatchFeature() {
        super(WallShelfPatchConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<WallShelfPatchConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        WallShelfPatchConfiguration config = context.config();
        BlockPos origin = context.origin();
        BlockState base = config.toPlace().getState(random, origin);
        if (!base.hasProperty(HorizontalDirectionalBlock.FACING)) return false;

        int xz = config.xzSpread();
        int y = config.ySpread();
        int placed = 0;
        for (int i = 0; i < config.tries(); i++) {
            BlockPos pos = origin.offset(random.nextInt(xz * 2 + 1) - xz, random.nextInt(y * 2 + 1) - y, random.nextInt(xz * 2 + 1) - xz);
            if (!level.isEmptyBlock(pos)) continue;
            for (Direction dir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                BlockState state = base.setValue(HorizontalDirectionalBlock.FACING, dir);
                if (state.canSurvive(level, pos)) {
                    level.setBlock(pos, state, 2);
                    placed++;
                    break;
                }
            }
        }
        return placed > 0;
    }
}
