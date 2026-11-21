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
    private static final int LOBES = 6;
    private static final double LOBE_MIN = 0.86;
    private static final double LOBE_RANGE = 0.22;
    private static final double CROWN = 0.55;
    private static final double BELLY = 0.42;
    private static final int CROWN_MIN_WIDTH = 3;
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
            double[] lobes = new double[LOBES + 1];
            for (int i = 0; i <= LOBES; i++) {
                lobes[i] = LOBE_MIN + random.nextDouble() * LOBE_RANGE;
            }
            double reachAlong = width + 0.5;
            double reachOut = Math.max(1.0, width * (0.75 + random.nextDouble() * 0.2)) + 0.5;
            int span = width + 1;
            double[][] layer = new double[span * 2 + 1][span + 1];
            for (int a = -span; a <= span; a++) {
                for (int b = 0; b <= span; b++) {
                    layer[a + span][b] = Math.sqrt((a * a) / (reachAlong * reachAlong) + (b * b) / (reachOut * reachOut)) / lobe(lobes, a, b);
                }
            }
            for (int a = -span; a <= span; a++) {
                for (int b = 0; b <= span; b++) {
                    double dist = layer[a + span][b];
                    if (dist > 1.0 || b > 0 && neighbours(layer, a + span, b) < 2) continue;
                    BlockPos pos = center.relative(along, a).relative(out, b);
                    cap.add(pos);
                    if (dist < BELLY) cap.add(pos.below());
                    if (width >= CROWN_MIN_WIDTH && dist < CROWN) cap.add(pos.above());
                }
            }
            width -= random.nextInt(4) == 0 ? 2 : 1;
            int shift = random.nextBoolean() ? random.nextInt(3) - 1 : (random.nextBoolean() ? 2 : -2);
            center = center.above(4 + random.nextInt(2)).relative(along, shift);
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

    private static double lobe(double[] lobes, int a, int b) {
        double angle = a == 0 && b == 0 ? Math.PI / 2 : Math.atan2(b, a);
        double t = angle / Math.PI * LOBES;
        int i = Math.min((int) t, LOBES - 1);
        double f = t - i;
        f = f * f * (3.0 - 2.0 * f);
        return lobes[i] * (1.0 - f) + lobes[i + 1] * f;
    }

    private static int neighbours(double[][] layer, int x, int y) {
        int count = 0;
        if (x > 0 && layer[x - 1][y] <= 1.0) count++;
        if (x < layer.length - 1 && layer[x + 1][y] <= 1.0) count++;
        if (y > 0 && layer[x][y - 1] <= 1.0) count++;
        if (y < layer[x].length - 1 && layer[x][y + 1] <= 1.0) count++;
        return count;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}