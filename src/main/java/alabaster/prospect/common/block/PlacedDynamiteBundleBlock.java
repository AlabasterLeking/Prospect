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
    public static final int MAX_BUNDLES = 4;
    public static final IntegerProperty BUNDLES = IntegerProperty.create("bundles", 1, MAX_BUNDLES);
    private static final double[][][] SLOTS = {{{5, 5}}, {{5, 2}, {5, 8}}, {{2, 2}, {2, 8}, {8, 5}}, {{2, 2}, {8, 2}, {2, 8}, {8, 8}}};
    private static final double FOOTPRINT = 6.0;
    private static final double HEIGHT = 12.0;

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
        for (double[] slot : SLOTS[count - 1]) {
            boxes.add(new double[]{slot[0], 0, slot[1], slot[0] + FOOTPRINT, HEIGHT, slot[1] + FOOTPRINT});
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