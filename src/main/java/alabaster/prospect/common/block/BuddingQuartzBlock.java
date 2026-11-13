package alabaster.prospect.common.block;

import alabaster.prospect.common.registry.ProspectModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.Supplier;

public class BuddingQuartzBlock extends Block {
    public static final MapCodec<BuddingQuartzBlock> CODEC = simpleCodec(BuddingQuartzBlock::new);
    private static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final List<Supplier<Block>> STAGES = List.of(ProspectModBlocks.SMALL_QUARTZ_BUD, ProspectModBlocks.MEDIUM_QUARTZ_BUD, ProspectModBlocks.LARGE_QUARTZ_BUD, ProspectModBlocks.QUARTZ_CLUSTER);

    public BuddingQuartzBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) return;
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos target = pos.relative(direction);
        BlockState current = level.getBlockState(target);
        Block next = null;
        if (BuddingAmethystBlock.canClusterGrowAtState(current)) {
            next = STAGES.get(0).get();
        } else {
            for (int i = 0; i < STAGES.size() - 1; i++) {
                if (current.is(STAGES.get(i).get()) && current.getValue(AmethystClusterBlock.FACING) == direction) {
                    next = STAGES.get(i + 1).get();
                    break;
                }
            }
        }
        if (next == null) return;
        level.setBlockAndUpdate(target, next.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, current.getFluidState().getType() == Fluids.WATER));
    }
}
