package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TintedGlassPaneBlock extends IronBarsBlock {

    public static final MapCodec<TintedGlassPaneBlock> CODEC = simpleCodec(TintedGlassPaneBlock::new);

    private static final VoxelShape[] FACES = {
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0),
            Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0)
    };
    private static final VoxelShape[] LIGHT_SHAPES = new VoxelShape[16];

    static {
        for (int mask = 0; mask < LIGHT_SHAPES.length; mask++) {
            VoxelShape shape = Shapes.empty();
            if (Integer.bitCount(mask) >= 2) {
                for (int i = 0; i < FACES.length; i++) {
                    if ((mask & (1 << i)) == 0) shape = Shapes.or(shape, FACES[i]);
                }
            }
            LIGHT_SHAPES[mask] = shape;
        }
    }

    public TintedGlassPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TintedGlassPaneBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        int mask = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0) | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0);
        return LIGHT_SHAPES[mask];
    }
}