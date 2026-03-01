package alabaster.prospect.common.block;

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
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class GunpowderFuseBlock extends Block {

    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public static final EnumProperty<RedstoneSide> NORTH =
            EnumProperty.create("north", RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> SOUTH =
            EnumProperty.create("south", RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> EAST =
            EnumProperty.create("east", RedstoneSide.class);
    public static final EnumProperty<RedstoneSide> WEST =
            EnumProperty.create("west", RedstoneSide.class);

    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Map.of(
            Direction.NORTH, NORTH,
            Direction.SOUTH, SOUTH,
            Direction.EAST, EAST,
            Direction.WEST, WEST
    );

    private static final int BURN_TIME = 8;

    private static final VoxelShape SHAPE_DOT = Block.box(3, 0, 3, 13, 1, 13);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Map.of(
            Direction.NORTH, Block.box(3, 0, 0, 13, 1, 13),
            Direction.SOUTH, Block.box(3, 0, 3, 13, 1, 16),
            Direction.EAST,  Block.box(3, 0, 3, 16, 1, 13),
            Direction.WEST,  Block.box(0, 0, 3, 13, 1, 13)
    );
    private static final Map<Direction, VoxelShape> SHAPES_UP = Map.of(
            Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3, 0, 0, 13, 16, 1)),
            Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3, 0, 15, 13, 16, 16)),
            Direction.EAST,  Shapes.or(SHAPES_FLOOR.get(Direction.EAST),  Block.box(15, 0, 3, 16, 16, 13)),
            Direction.WEST,  Shapes.or(SHAPES_FLOOR.get(Direction.WEST),  Block.box(0, 0, 3, 1, 16, 13))
    );
    private static final Map<BlockState, VoxelShape> SHAPES_CACHE = new HashMap<>();


    public GunpowderFuseBlock(Properties properties) {
        super(properties.noOcclusion());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false)
                .setValue(NORTH, RedstoneSide.NONE)
                .setValue(SOUTH, RedstoneSide.NONE)
                .setValue(EAST, RedstoneSide.NONE)
                .setValue(WEST, RedstoneSide.NONE)
        );

        for (BlockState state : this.getStateDefinition().getPossibleStates()) {
            SHAPES_CACHE.put(state, calculateShape(state));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_CACHE.get(state);
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return updateConnections(level, pos, this.defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return state.canSurvive(level, pos)
                    ? updateConnections(level, pos, state)
                    : Blocks.AIR.defaultBlockState();
        }

        if (direction.getAxis().isHorizontal() || direction == Direction.UP) {
            return updateConnections(level, pos, state);
        }

        return state;
    }

    private BlockState updateConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, getConnection(level, pos, Direction.NORTH))
                .setValue(SOUTH, getConnection(level, pos, Direction.SOUTH))
                .setValue(EAST,  getConnection(level, pos, Direction.EAST))
                .setValue(WEST,  getConnection(level, pos, Direction.WEST));
    }

    private RedstoneSide getConnection(LevelAccessor level, BlockPos pos, Direction dir) {
        BlockPos sidePos = pos.relative(dir);
        BlockState sideState = level.getBlockState(sidePos);

        if (sideState.is(this)) {
            return RedstoneSide.SIDE;
        }

        BlockPos aboveCurrent = pos.above();
        BlockState aboveCurrentState = level.getBlockState(aboveCurrent);

        if (sideState.isFaceSturdy(level, sidePos, Direction.UP)) {
            BlockPos aboveSide = sidePos.above();
            BlockState aboveSideState = level.getBlockState(aboveSide);

            if (!aboveCurrentState.isRedstoneConductor(level, aboveCurrent)
                    && aboveSideState.is(this)) {
                return RedstoneSide.UP;
            }
        }

        if (!sideState.isRedstoneConductor(level, sidePos)) {
            BlockPos belowSide = sidePos.below();
            BlockState belowSideState = level.getBlockState(belowSide);

            if (belowSideState.is(this)) {
                return RedstoneSide.SIDE;
            }
        }

        return RedstoneSide.NONE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            updateAllNeighbors(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            updateAllNeighbors(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void updateAllNeighbors(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos offset = pos.relative(dir);
            BlockState neighbor = level.getBlockState(offset);

            if (neighbor.is(this)) {
                level.setBlock(offset,
                        updateConnections(level, offset, neighbor),
                        3);
            }
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!state.getValue(LIT) && stack.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                ignite(level, pos, state);
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && !state.getValue(LIT)) {
            BlockState neighbor = level.getBlockState(fromPos);
            if (neighbor.is(Blocks.FIRE)
                    || neighbor.is(Blocks.LAVA)
                    || neighbor.is(Blocks.TORCH)) {
                ignite(level, pos, state);
            }
        }
    }

    private void ignite(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(LIT, true), 3);
        float pitch = 0.8F + level.random.nextFloat() * 0.4F;
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, pitch);
        level.scheduleTick(pos, this, BURN_TIME);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        for (Direction dir : Direction.values()) {
            BlockPos offset = pos.relative(dir);
            BlockState neighbor = level.getBlockState(offset);

            if (neighbor.is(this) && !neighbor.getValue(LIT)) {
                ignite(level, offset, neighbor);
            }

            if (neighbor.is(Blocks.TNT)) {
                TntBlock.explode(level, offset);
                level.removeBlock(offset, false);
            }
        }

        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.05;
        double centerZ = pos.getZ() + 0.5;

        for (int i = 0; i < 5; i++) {
            level.addParticle(
                    ParticleTypes.FLAME,
                    centerX + (random.nextDouble() - 0.5) * 0.4,
                    centerY,
                    centerZ + (random.nextDouble() - 0.5) * 0.4,
                    (random.nextDouble() - 0.5) * 0.05,
                    0.02 + random.nextDouble() * 0.04,
                    (random.nextDouble() - 0.5) * 0.05
            );
        }

        for (int i = 0; i < 6; i++) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    centerX + (random.nextDouble() - 0.5) * 0.5,
                    centerY,
                    centerZ + (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.03,
                    0.04 + random.nextDouble() * 0.03,
                    (random.nextDouble() - 0.5) * 0.03
            );
        }

        for (int i = 0; i < 8; i++) {
            level.addParticle(
                    ParticleTypes.CRIT,
                    centerX,
                    centerY,
                    centerZ,
                    (random.nextDouble() - 0.5) * 0.2,
                    random.nextDouble() * 0.1,
                    (random.nextDouble() - 0.5) * 0.2
            );
        }

        if (random.nextFloat() < 0.4F) {
            level.addParticle(
                    ParticleTypes.LAVA,
                    centerX + (random.nextDouble() - 0.5) * 0.3,
                    centerY,
                    centerZ + (random.nextDouble() - 0.5) * 0.3,
                    0,
                    0,
                    0
            );
        }
    }
}