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
import net.minecraft.world.phys.shapes.VoxelShape;

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

    private static final int BURN_TIME = 4;

    private static final VoxelShape SHAPE =
            Block.box(0, 0, 0, 16, 2, 16);

    public GunpowderFuseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false)
                .setValue(NORTH, RedstoneSide.NONE)
                .setValue(SOUTH, RedstoneSide.NONE)
                .setValue(EAST, RedstoneSide.NONE)
                .setValue(WEST, RedstoneSide.NONE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return updateConnections(level, pos, this.defaultBlockState());
    }

    @Override
    public void onPlace(BlockState state, Level level,
                        BlockPos pos, BlockState oldState, boolean isMoving) {

        if (!oldState.is(state.getBlock())) {
            updateAllNeighbors(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level,
                         BlockPos pos, BlockState newState, boolean isMoving) {

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

        BlockPos offset = pos.relative(dir);
        BlockState neighbor = level.getBlockState(offset);

        // Connect directly
        if (neighbor.is(this)) {
            return RedstoneSide.SIDE;
        }

        // Check upward climb
        if (neighbor.isFaceSturdy(level, offset, dir.getOpposite())) {
            BlockPos above = offset.above();
            BlockState aboveState = level.getBlockState(above);

            if (aboveState.is(this)) {
                return RedstoneSide.UP;
            }
        }

        return RedstoneSide.NONE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!state.getValue(LIT)
                && stack.is(Items.FLINT_AND_STEEL)) {

            if (!level.isClientSide) {
                ignite(level, pos, state);

                EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND)
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;

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

        level.playSound(
                null,
                pos,
                SoundEvents.TNT_PRIMED,
                SoundSource.BLOCKS,
                1.0F,
                pitch
        );

        level.scheduleTick(pos, this, BURN_TIME);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (!state.getValue(LIT)) return;

        for (Direction dir : Direction.Plane.HORIZONTAL) {

            BlockPos offset = pos.relative(dir);
            BlockState neighbor = level.getBlockState(offset);

            // Spread to other fuse blocks
            if (neighbor.is(this) && !neighbor.getValue(LIT)) {
                ignite(level, offset, neighbor);
            }

            // Ignite TNT
            if (neighbor.is(Blocks.TNT)) {
                TntBlock.explode(level, offset);
                level.removeBlock(offset, false);
            }
        }

        // Remove after burn
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if (state.getValue(LIT)) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5,
                    pos.getY() + 0.05,
                    pos.getZ() + 0.5,
                    0, 0.02, 0
            );
        }
    }
}