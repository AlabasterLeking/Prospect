package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.HashSet;
import java.util.Set;

public class HugeShelfMushroomFeature extends Feature<HugeShelfMushroomConfiguration> {

    private static final int WALL_SEARCH = 4;
    private static final int MIN_BLOCKS = 3;
    private static final double STEP = 0.3;
    private static final double RIM = 0.72;
    private static final double THICK_CORE = 0.35;
    private static final double EDGE_NOISE = 0.12;
    private static final Direction[] VISIBLE = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public HugeShelfMushroomFeature() {
        super(HugeShelfMushroomConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeShelfMushroomConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        HugeShelfMushroomConfiguration config = context.config();
        BlockPos origin = context.origin();
        if (!canReplace(level, origin)) return false;

        Direction wallDir = null;
        int distance = Integer.MAX_VALUE;
        for (Direction dir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            for (int i = 1; i <= WALL_SEARCH && i < distance; i++) {
                BlockPos pos = origin.relative(dir, i);
                if (level.getBlockState(pos).isFaceSturdy(level, pos, dir.getOpposite())) {
                    wallDir = dir;
                    distance = i;
                    break;
                }
                if (!canReplace(level, pos)) break;
            }
        }
        if (wallDir == null) return false;

        Direction out = wallDir.getOpposite();
        Direction along = wallDir.getClockWise();
        int radius = config.radius().sample(random);
        int tiers = config.tiers().sample(random);
        BlockPos center = origin.relative(wallDir, distance - 1);

        Set<BlockPos> cap = new HashSet<>();
        int width = radius;
        for (int tier = 0; tier < tiers && width >= 1; tier++) {
            BlockPos behind = center.relative(wallDir);
            if (!level.getBlockState(behind).isFaceSturdy(level, behind, out)) break;
            double reachAlong = width + 0.5;
            double reachOut = Math.max(1.0, width * (0.7 + random.nextDouble() * 0.25)) + 0.5;
            for (int a = -width; a <= width; a++) {
                for (int b = 0; b <= width; b++) {
                    double dist = (a * a) / (reachAlong * reachAlong) + (b * b) / (reachOut * reachOut) + (random.nextDouble() - 0.5) * EDGE_NOISE;
                    if (dist > 1.0) continue;
                    BlockPos pos = center.relative(along, a).relative(out, b);
                    if (dist < STEP) cap.add(pos.above());
                    if (dist < RIM) {
                        cap.add(pos);
                        if (dist < THICK_CORE) cap.add(pos.below());
                    } else {
                        cap.add(pos.below());
                    }
                }
            }
            width -= random.nextInt(3) == 0 ? 2 : 1;
            center = center.above(4 + random.nextInt(2)).relative(along, random.nextInt(5) - 2);
        }

        cap.removeIf(pos -> !canReplace(level, pos));
        if (cap.size() < MIN_BLOCKS) return false;

        BlockState base = config.cap().getState(random, origin);
        for (BlockPos pos : cap) {
            BlockState state = base;
            if (state.hasProperty(HugeMushroomBlock.DOWN)) {
                for (Direction dir : VISIBLE) {
                    state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), !cap.contains(pos.relative(dir)));
                }
                state = state.setValue(HugeMushroomBlock.DOWN, false);
            }
            setBlock(level, pos, state);
        }
        return true;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
