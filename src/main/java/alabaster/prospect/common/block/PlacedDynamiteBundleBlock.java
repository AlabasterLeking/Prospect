package alabaster.prospect.common.block;

import alabaster.prospect.common.item.DynamiteType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class PlacedDynamiteBundleBlock extends AbstractPlacedDynamiteBlock {

    public static final MapCodec<PlacedDynamiteBundleBlock> CODEC = simpleCodec(PlacedDynamiteBundleBlock::new);
    public static final int MAX_BUNDLES = 3;
    public static final IntegerProperty BUNDLES = IntegerProperty.create("bundles", 1, MAX_BUNDLES);
    private static final double[][] ROWS = {{6.5}, {4.5, 8.5}, {2.5, 6.5, 10.5}};

    public PlacedDynamiteBundleBlock(Properties properties) {
        super(DynamiteType.BUNDLE, properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BUNDLES, 1));
    }

    @Override
    protected IntegerProperty countProperty() {
        return BUNDLES;
    }

    @Override
    protected int maxCount() {
        return MAX_BUNDLES;
    }

    @Override
    protected List<double[]> layout(int count) {
        List<double[]> boxes = new ArrayList<>();
        for (double z : ROWS[count - 1]) {
            boxes.add(new double[]{3.5, 0, z, 12.5, 12, z + 3});
        }
        return boxes;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BUNDLES);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}