package alabaster.prospect.common.block;

import alabaster.prospect.common.item.DynamiteType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlacedDynamiteBlock extends AbstractPlacedDynamiteBlock {

    public static final MapCodec<PlacedDynamiteBlock> CODEC = simpleCodec(PlacedDynamiteBlock::new);
    public static final int MAX_STICKS = 9;
    public static final IntegerProperty STICKS = IntegerProperty.create("sticks", 1, MAX_STICKS);
    private static final int[][] FILL_ORDER = {{1, 1}, {0, 1}, {2, 1}, {1, 0}, {1, 2}, {0, 0}, {2, 2}, {2, 0}, {0, 2}};
    public static final BooleanProperty LYING = BooleanProperty.create("lying");
    private static final double[][] PILE = {{0, 5}, {0, 8}, {0, 2}, {0, 11}, {1, 6.5}, {1, 3.5}, {1, 9.5}, {2, 5}, {2, 8}};
    private static final double STICK_START = 2;
    private static final double STICK_END = 14;
    private static final double STICK_WIDTH = 3;

    public PlacedDynamiteBlock(Properties properties) {
        super(DynamiteType.STANDARD, properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STICKS, 1).setValue(LYING, false));
    }

    public static double[] stick(int index) {
        double x = 1.5 + FILL_ORDER[index][0] * 5;
        double z = 1.5 + FILL_ORDER[index][1] * 5;
        double height = index % 3 == 1 ? 11 : 12;
        return new double[]{x, 0, z, x + 3, height, z + 3};
    }

    public static double[] lyingStick(int index) {
        double y = PILE[index][0] * STICK_WIDTH;
        double z = PILE[index][1];
        return new double[]{STICK_START, y, z, STICK_END, y + STICK_WIDTH, z + STICK_WIDTH};
    }

    @Override
    protected List<double[]> layout(BlockState state) {
        if (!state.getValue(LYING)) return super.layout(state);
        List<double[]> boxes = new ArrayList<>();
        for (int i = 0; i < state.getValue(STICKS); i++) {
            boxes.add(lyingStick(i));
        }
        return boxes;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || context.getLevel().getBlockState(context.getClickedPos()).is(this)) return state;
        return state.setValue(LYING, context.isSecondaryUseActive());
    }

    @Override
    protected IntegerProperty countProperty() {
        return STICKS;
    }

    @Override
    protected int maxCount() {
        return MAX_STICKS;
    }

    @Override
    protected List<double[]> layout(int count) {
        List<double[]> boxes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boxes.add(stick(i));
        }
        return boxes;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STICKS, LYING);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}