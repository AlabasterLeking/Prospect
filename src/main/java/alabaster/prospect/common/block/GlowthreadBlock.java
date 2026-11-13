package alabaster.prospect.common.block;

import alabaster.prospect.common.registry.ProspectModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowthreadBlock extends GrowingPlantHeadBlock {

    public static final MapCodec<GlowthreadBlock> CODEC = simpleCodec(GlowthreadBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

    public GlowthreadBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.1);
    }

    @Override
    protected MapCodec<? extends GrowingPlantHeadBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return NetherVines.isValidGrowthState(state);
    }

    @Override
    protected Block getBodyBlock() {
        return ProspectModBlocks.GLOWTHREAD_PLANT.get();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(25) != 0) return;
        level.addParticle(new DustParticleOptions(GlowColor.BLUE.particleColor(), 0.5F),
                pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.1, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, -0.02, 0.0);
    }
}