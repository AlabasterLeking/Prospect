package alabaster.prospect.common.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GunpowderFuseBlock extends Block {

    // 0 = unlit, 1-4 = burning (consumed at 4)
    public static final IntegerProperty BURNING = IntegerProperty.create("burning", 0, 4);

    public static final EnumProperty<RedstoneSide> NORTH = EnumProperty.create("north", RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> SOUTH = EnumProperty.create("south", RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> EAST = EnumProperty.create("east",  RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> WEST = EnumProperty.create("west",  RedstoneSide.class);

    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH, NORTH,
                    Direction.SOUTH, SOUTH,
                    Direction.EAST, EAST,
                    Direction.WEST, WEST
            )
    );

    private static final int BURN_TIME = 2; // ticks between burning stages

    private static final VoxelShape SHAPE_DOT = Block.box(3, 0, 3, 13, 1, 13);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH, Block.box(3, 0, 0, 13, 1, 13),
                    Direction.SOUTH, Block.box(3, 0, 3, 13, 1, 16),
                    Direction.EAST, Block.box(3, 0, 3, 16, 1, 13),
                    Direction.WEST, Block.box(0, 0, 3, 13, 1, 13)
            )
    );
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3, 0, 0, 13, 16, 1)),
                    Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3, 0, 15, 13, 16, 16)),
                    Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST),  Block.box(15, 0, 3, 16, 16, 13)),
                    Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST),  Block.box(0, 0, 3, 1, 16, 13))
            )
    );

    private final Map<BlockState, VoxelShape> SHAPES_CACHE;
    private final BlockState crossState;

    public GunpowderFuseBlock(Properties properties) {
        super(properties.noOcclusion());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BURNING, 0)
                .setValue(NORTH, RedstoneSide.NONE)
                .setValue(SOUTH, RedstoneSide.NONE)
                .setValue(EAST, RedstoneSide.NONE)
                .setValue(WEST, RedstoneSide.NONE)
        );

        this.crossState = this.defaultBlockState()
                .setValue(NORTH, RedstoneSide.SIDE)
                .setValue(SOUTH, RedstoneSide.SIDE)
                .setValue(EAST, RedstoneSide.SIDE)
                .setValue(WEST, RedstoneSide.SIDE);

        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();
        for (BlockState state : this.getStateDefinition().getPossibleStates()) {
            if (state.getValue(BURNING) == 0) {
                builder.put(state, calculateShape(state));
            }
        }
        this.SHAPES_CACHE = builder.build();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BURNING, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_CACHE.get(state.setValue(BURNING, 0));
    }

    private VoxelShape calculateShape(BlockState state) {
        VoxelShape shape = SHAPE_DOT;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = state.getValue(PROPERTY_BY_DIRECTION.get(dir));
            if (side == RedstoneSide.SIDE) {
                shape = Shapes.or(shape, SHAPES_FLOOR.get(dir));
            } else if (side == RedstoneSide.UP) {
                shape = Shapes.or(shape, SHAPES_UP.get(dir));
            }
        }
        return shape;
    }

    private static boolean isCross(BlockState state) {
        return state.getValue(NORTH).isConnected() && state.getValue(SOUTH).isConnected()
                && state.getValue(EAST).isConnected() && state.getValue(WEST).isConnected();
    }

    private static boolean isDot(BlockState state) {
        return !state.getValue(NORTH).isConnected() && !state.getValue(SOUTH).isConnected()
                && !state.getValue(EAST).isConnected() && !state.getValue(WEST).isConnected();
    }

    private BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) {
        boolean wasDot = isDot(state);
        state = getMissingConnections(level,
                this.defaultBlockState().setValue(BURNING, state.getValue(BURNING)), pos);

        if (!wasDot || !isDot(state)) {
            boolean n = state.getValue(NORTH).isConnected();
            boolean s = state.getValue(SOUTH).isConnected();
            boolean e = state.getValue(EAST).isConnected();
            boolean w = state.getValue(WEST).isConnected();

            if (!w && !n && !s) state = state.setValue(WEST,  RedstoneSide.SIDE);
            if (!e && !n && !s) state = state.setValue(EAST,  RedstoneSide.SIDE);
            if (!n && !e && !w) state = state.setValue(NORTH, RedstoneSide.SIDE);
            if (!s && !e && !w) state = state.setValue(SOUTH, RedstoneSide.SIDE);
        }
        return state;
    }

    private BlockState getMissingConnections(BlockGetter level, BlockState state, BlockPos pos) {
        boolean canClimbUp = !level.getBlockState(pos.above()).isRedstoneConductor(level, pos);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!state.getValue(PROPERTY_BY_DIRECTION.get(dir)).isConnected()) {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), getConnectingSide(level, pos, dir, canClimbUp));
            }
        }
        return state;
    }

    private RedstoneSide getConnectingSide(BlockGetter level, BlockPos pos, Direction dir, boolean canClimbUp) {
        BlockPos sidePos    = pos.relative(dir);
        BlockState sideState = level.getBlockState(sidePos);

        if (canClimbUp && sideState.isFaceSturdy(level, sidePos, Direction.UP)) {
            if (level.getBlockState(sidePos.above()).is(this)) {
                return RedstoneSide.UP;
            }
        }

        if (sideState.is(this)) {
            return RedstoneSide.SIDE;
        }

        if (!sideState.isRedstoneConductor(level, sidePos)) {
            if (level.getBlockState(sidePos.below()).is(this)) {
                return RedstoneSide.SIDE;
            }
        }

        return RedstoneSide.NONE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectionState(context.getLevel(), this.crossState, context.getClickedPos());
    }

    public BlockState getConnectedStateForPlacement(BlockGetter level, BlockPos pos) {
        return getConnectionState(level, this.crossState, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP || direction.getAxis().isHorizontal()) {
            return getConnectionState(level, state, pos);
        }
        return state;
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags, int recursion) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = state.getValue(PROPERTY_BY_DIRECTION.get(dir));
            if (side == RedstoneSide.NONE || level.getBlockState(mutable.setWithOffset(pos, dir)).is(this)) continue;

            // One step below the horizontal neighbour
            mutable.move(Direction.DOWN);
            BlockPos downPos = mutable.immutable();
            BlockState downState = level.getBlockState(downPos);
            BlockState newDown =
                    downState.updateShape(dir.getOpposite(),
                    level.getBlockState(downPos.relative(dir.getOpposite())),
                    level, downPos, downPos.relative(dir.getOpposite()));
            updateOrDestroy(downState, newDown, level, downPos, flags, recursion);

            // One step above the horizontal neighbour
            mutable.setWithOffset(pos, dir).move(Direction.UP);
            BlockPos upPos = mutable.immutable();
            BlockState upState = level.getBlockState(upPos);
            BlockState newUp =
                    upState.updateShape(dir.getOpposite(),
                    level.getBlockState(upPos.relative(dir.getOpposite())),
                    level, upPos, upPos.relative(dir.getOpposite()));
            updateOrDestroy(upState, newUp, level, upPos, flags, recursion);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            updateNeighborsOfNeighboringWires(level, pos);
            for (Direction dir : Direction.Plane.VERTICAL) {
                level.updateNeighborsAt(pos.relative(dir), this);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            if (!level.isClientSide) {
                for (Direction dir : Direction.values()) {
                    level.updateNeighborsAt(pos.relative(dir), this);
                }
                updateNeighborsOfNeighboringWires(level, pos);
            }
        }
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            checkCornerChangeAt(level, pos.relative(dir));
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos offset = pos.relative(dir);
            if (level.getBlockState(offset).isRedstoneConductor(level, offset)) {
                checkCornerChangeAt(level, offset.above());
            } else {
                checkCornerChangeAt(level, offset.below());
            }
        }
    }

    private void checkCornerChangeAt(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(this)) {
            level.updateNeighborsAt(pos, this);
            for (Direction dir : Direction.values()) {
                level.updateNeighborsAt(pos.relative(dir), this);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 ->
                    state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST))
                            .setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH))
                            .setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH))
                            .setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!isLit(state) && stack.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                ignite(level, pos, state);
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean isLit(BlockState state) {
        return state.getValue(BURNING) != 0;
    }

    private void ignite(Level level, BlockPos pos, BlockState state) {
        if (isLit(state)) return;
        level.setBlock(pos, state.setValue(BURNING, 1), 3);
        float pitch = 0.8F + level.random.nextFloat() * 0.4F;
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, pitch);
        level.scheduleTick(pos, this, BURN_TIME);
    }

    private void lightUpByWire(BlockState targetState, BlockPos targetPos, LevelAccessor level) {
        if (isLit(targetState)) return;
        level.setBlock(targetPos, targetState.setValue(BURNING, 1), 3);
        level.scheduleTick(targetPos, this, BURN_TIME);
        float pitch = 1.9F + level.getRandom().nextFloat() * 0.1F;
        level.playSound(null, targetPos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 2.0F, pitch);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && !isLit(state)) {
            if (canBlockIgniteFuse(level.getBlockState(fromPos))) {
                ignite(level, pos, state);
            }
        }
    }

    private static boolean canBlockIgniteFuse(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.TORCH);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int burning = state.getValue(BURNING);

        if (burning == 0) {
            for (Direction dir : Direction.values()) {
                if (canBlockIgniteFuse(level.getBlockState(pos.relative(dir)))) {
                    ignite(level, pos, state);
                    break;
                }
            }
            return;
        }

        if (burning >= 2) {
            spreadFire(pos, state, level);
        }
        if (burning >= 4) {
            level.removeBlock(pos, false);
            return;
        }

        level.setBlockAndUpdate(pos, state.setValue(BURNING, burning + 1));
        level.scheduleTick(pos, this, BURN_TIME);
    }

    private void spreadFire(BlockPos pos, BlockState state, Level level) {
        boolean canClimbUp = !level.getBlockState(pos.above()).isRedstoneConductor(level, pos);

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(dir);
            BlockState sideState = level.getBlockState(sidePos);

            if (sideState.is(this)) {
                lightUpByWire(sideState, sidePos, level);
                continue;
            }

            if (canClimbUp && sideState.isFaceSturdy(level, sidePos, Direction.UP)) {
                BlockPos abovePos = sidePos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (aboveState.is(this)) {
                    lightUpByWire(aboveState, abovePos, level);
                    continue;
                }
            }

            if (!sideState.isRedstoneConductor(level, sidePos)) {
                BlockPos belowPos = sidePos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if (belowState.is(this)) {
                    lightUpByWire(belowState, belowPos, level);
                }
            }
        }

        for (Direction dir : Direction.values()) {
            BlockPos offset = pos.relative(dir);
            if (level.getBlockState(offset).is(Blocks.TNT)) {
                TntBlock.explode(level, offset);
                level.removeBlock(offset, false);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int burning = state.getValue(BURNING);
        if (burning == 0) return;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = state.getValue(PROPERTY_BY_DIRECTION.get(dir));
            switch (side) {
                case UP -> spawnParticlesAlongLine(level, random, pos, burning, dir, Direction.UP, -0.5F, 0.5F);
                case SIDE -> spawnParticlesAlongLine(level, random, pos, burning, Direction.DOWN, dir, 0.0F, 0.5F);
                default -> spawnParticlesAlongLine(level, random, pos, burning, Direction.DOWN, dir, 0.0F, 0.3F);
            }
        }
    }

    private void spawnParticlesAlongLine(Level level, RandomSource random, BlockPos pos, int burning, Direction axis1, Direction axis2, float from, float to) {
        float range   = to - from;
        float density = (float) burning / 4.0F;
        if (random.nextFloat() >= range * density) return;

        float  t = from + range * random.nextFloat();
        double x = pos.getX() + 0.5 + 0.4375 * axis1.getStepX() + t * axis2.getStepX();
        double y = pos.getY() + 0.5 + 0.4375 * axis1.getStepY() + t * axis2.getStepY();
        double z = pos.getZ() + 0.5 + 0.4375 * axis1.getStepZ() + t * axis2.getStepZ();
        float  vy = (burning / 4.0F) * 0.03F;
        float  vx = random.nextFloat() * 0.02F - 0.01F;
        float  vz = random.nextFloat() * 0.02F - 0.01F;

        level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
        level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, vx, vy, vz);
    }
}