package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PayloadRailBlock extends StraightRailBlock {

    public static final MapCodec<PayloadRailBlock> CODEC = simpleCodec(PayloadRailBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final int CHECK_INTERVAL = 20;

    public PayloadRailBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }

    public static boolean isCarrying(AbstractMinecart cart) {
        return PayloadSignal.read(cart) > 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && direction == Direction.UP ? 15 : 0;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide()) {
            checkPressed(level, pos, state);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            checkPressed(level, pos, state);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            checkPressed(level, pos, updateState(state, level, pos, movedByPiston));
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(POWERED)) return 0;

        int signal = 0;
        for (AbstractMinecart cart : carriers(level, pos)) {
            signal = Math.max(signal, PayloadSignal.read(cart));
        }
        return signal;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.prospect.payload_rail").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.prospect.payload_rail.comparator").withStyle(ChatFormatting.GRAY));
    }

    private void checkPressed(Level level, BlockPos pos, BlockState state) {
        if (!canSurvive(state, level, pos)) return;

        boolean carrying = !carriers(level, pos).isEmpty();

        if (carrying != state.getValue(POWERED)) {
            BlockState updated = state.setValue(POWERED, carrying);
            level.setBlock(pos, updated, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.setBlocksDirty(pos, state, updated);
        }

        if (carrying) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
        level.updateNeighbourForOutputSignal(pos, this);
    }

    private static List<AbstractMinecart> carriers(Level level, BlockPos pos) {
        AABB box = new AABB(pos.getX() + 0.2, pos.getY(), pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 0.8, pos.getZ() + 0.8);
        return level.getEntitiesOfClass(AbstractMinecart.class, box, PayloadRailBlock::isCarrying);
    }
}