package alabaster.prospect.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrovePoolFeature extends Feature<GrovePoolConfiguration> {

    private static final Direction[] SEALED = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private static final int MIN_COLUMNS = 4;

    public GrovePoolFeature() {
        super(GrovePoolConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<GrovePoolConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        GrovePoolConfiguration config = context.config();
        BlockPos origin = context.origin();
        if (!isFloor(level, origin, config)) return false;

        int radius = config.radius().sample(random);
        int maxDepth = config.depth().sample(random);
        double rx = radius + random.nextDouble() - 0.5;
        double rz = radius + random.nextDouble() - 0.5;

        Map<BlockPos, Integer> columns = new HashMap<>();
        Set<BlockPos> water = new HashSet<>();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double dist = (dx * dx) / (rx * rx) + (dz * dz) / (rz * rz) + (random.nextDouble() - 0.5) * 0.3;
                if (dist >= 1.0) continue;
                BlockPos top = origin.offset(dx, 0, dz);
                BlockState above = level.getBlockState(top.above());
                if (above.isSolid()) continue;
                if (!above.getFluidState().isEmpty() || !level.getBlockState(top).is(config.replaceable())) return false;
                int depth = Math.max(1, (int) Math.round(maxDepth * (1.0 - dist)));
                int filled = 0;
                while (filled < depth && level.getBlockState(top.below(filled)).is(config.replaceable())) {
                    water.add(top.below(filled));
                    filled++;
                }
                columns.put(top, filled);
            }
        }
        if (columns.size() < MIN_COLUMNS) return false;

        for (BlockPos pos : water) {
            for (Direction dir : SEALED) {
                BlockPos side = pos.relative(dir);
                if (water.contains(side)) continue;
                BlockState state = level.getBlockState(side);
                if (!state.isSolid() || !state.getFluidState().isEmpty()) return false;
            }
        }

        Set<BlockPos> shore = new HashSet<>();
        for (BlockPos pos : water) {
            for (Direction dir : SEALED) {
                BlockPos side = pos.relative(dir);
                if (water.contains(side) || !level.getBlockState(side).is(config.replaceable())) continue;
                level.setBlock(side, config.bed().getState(random, side), 2);
                if (side.getY() == origin.getY()) shore.add(side);
            }
        }
        for (BlockPos pos : shore) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(dir);
                if (water.contains(side) || shore.contains(side) || random.nextFloat() >= config.shoreChance()) continue;
                if (isFloor(level, side, config)) level.setBlock(side, config.bed().getState(random, side), 2);
            }
        }

        BlockState fluid = Blocks.WATER.defaultBlockState();
        for (BlockPos pos : water) {
            level.setBlock(pos, fluid, 2);
        }

        for (Map.Entry<BlockPos, Integer> column : columns.entrySet()) {
            BlockPos top = column.getKey();
            BlockState above = level.getBlockState(top.above());
            if (!above.isAir() && above.canBeReplaced()) level.setBlock(top.above(), Blocks.AIR.defaultBlockState(), 2);
            if (random.nextFloat() >= config.vegetationChance()) continue;
            BlockPos bottom = top.below(column.getValue() - 1);
            BlockState plant = config.vegetation().getState(random, bottom);
            if (plant.hasProperty(BlockStateProperties.WATERLOGGED)) plant = plant.setValue(BlockStateProperties.WATERLOGGED, true);
            if (plant.canSurvive(level, bottom)) level.setBlock(bottom, plant, 2);
        }
        return true;
    }

    private static boolean isFloor(WorldGenLevel level, BlockPos pos, GrovePoolConfiguration config) {
        if (!level.getBlockState(pos).is(config.replaceable())) return false;
        BlockState above = level.getBlockState(pos.above());
        return !above.isSolid() && above.getFluidState().isEmpty();
    }
}
