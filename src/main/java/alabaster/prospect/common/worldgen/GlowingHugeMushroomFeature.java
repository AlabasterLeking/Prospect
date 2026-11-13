package alabaster.prospect.common.worldgen;

import alabaster.prospect.common.block.GlowingMushroomBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class GlowingHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {

    private static final Map<Direction, BooleanProperty> FACES = Map.of(
            Direction.UP, HugeMushroomBlock.UP,
            Direction.NORTH, HugeMushroomBlock.NORTH,
            Direction.SOUTH, HugeMushroomBlock.SOUTH,
            Direction.EAST, HugeMushroomBlock.EAST,
            Direction.WEST, HugeMushroomBlock.WEST
    );

    protected GlowingHugeMushroomFeature() {
        super(HugeMushroomFeatureConfiguration.CODEC);
    }

    protected abstract int stemHeight(RandomSource random, int radius);

    protected abstract void buildCap(Set<BlockPos> cap, BlockPos top, int radius, RandomSource random);

    protected static void ring(Set<BlockPos> cap, BlockPos center, double minDist, double maxDist) {
        int r = (int) Math.ceil(maxDist);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d >= minDist && d <= maxDist) {
                    cap.add(center.offset(dx, 0, dz));
                }
            }
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        HugeMushroomFeatureConfiguration config = context.config();

        BlockState ground = level.getBlockState(origin.below());
        if (!isDirt(ground) && !ground.is(BlockTags.MUSHROOM_GROW_BLOCK) && !ground.is(BlockTags.BASE_STONE_OVERWORLD)) return false;

        int height = stemHeight(random, config.foliageRadius);
        BlockPos top = origin.above(height);
        if (top.getY() + 6 >= level.getMaxBuildHeight()) return false;

        Set<BlockPos> cap = new HashSet<>();
        buildCap(cap, top, config.foliageRadius, random);

        for (int i = 0; i < height; i++) {
            if (!canReplace(level, origin.above(i))) return false;
        }
        for (BlockPos pos : cap) {
            if (!canReplace(level, pos)) return false;
        }

        for (int i = 0; i < height; i++) {
            BlockPos pos = origin.above(i);
            setBlock(level, pos, config.stemProvider.getState(random, pos));
        }
        for (BlockPos pos : cap) {
            BlockState state = config.capProvider.getState(random, pos);
            if (state.hasProperty(HugeMushroomBlock.DOWN)) {
                for (Map.Entry<Direction, BooleanProperty> face : FACES.entrySet()) {
                    state = state.setValue(face.getValue(), !cap.contains(pos.relative(face.getKey())));
                }
                state = state.setValue(HugeMushroomBlock.DOWN, false);
            }
            setBlock(level, pos, state);
        }
        return true;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof GlowingMushroomBlock;
    }
}