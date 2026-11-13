package alabaster.prospect.common.block;

import alabaster.prospect.common.registry.ProspectModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowthreadPlantBlock extends GrowingPlantBodyBlock {

    public static final MapCodec<GlowthreadPlantBlock> CODEC = simpleCodec(GlowthreadPlantBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public GlowthreadPlantBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected MapCodec<? extends GrowingPlantBodyBlock> codec() {
        return CODEC;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock) ProspectModBlocks.GLOWTHREAD.get();
    }
}