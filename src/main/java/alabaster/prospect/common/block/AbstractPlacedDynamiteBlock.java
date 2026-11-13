package alabaster.prospect.common.block;

import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.item.DynamiteType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPlacedDynamiteBlock extends HorizontalDirectionalBlock {

    private static final int CHAIN_FUSE_MIN = 10;
    private static final int CHAIN_FUSE_SPREAD = 20;

    private final DynamiteType type;
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    protected AbstractPlacedDynamiteBlock(DynamiteType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public DynamiteType getDynamiteType() {
        return type;
    }

    protected abstract IntegerProperty countProperty();

    protected abstract int maxCount();

    protected abstract List<double[]> layout(int count);

    protected List<double[]> layout(BlockState state) {
        return layout(state.getValue(countProperty()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.computeIfAbsent(state, this::buildShape);
    }

    private VoxelShape buildShape(BlockState state) {
        VoxelShape shape = Shapes.empty();
        for (double[] box : layout(state)) {
            double[] r = rotate(box, state.getValue(FACING));
            shape = Shapes.or(shape, Block.box(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        return shape;
    }

    private static double[] rotate(double[] box, Direction facing) {
        double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
        return switch (facing) {
            case EAST -> new double[]{16 - z1, box[1], x0, 16 - z0, box[4], x1};
            case SOUTH -> new double[]{16 - x1, box[1], 16 - z1, 16 - x0, box[4], 16 - z0};
            case WEST -> new double[]{z0, box[1], 16 - x1, z1, box[4], 16 - x0};
            default -> box;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            return existing.setValue(countProperty(), Math.min(maxCount(), existing.getValue(countProperty()) + 1));
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(type.unlitItem()) && state.getValue(countProperty()) < maxCount() || super.canBeReplaced(state, context);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        prime(level, pos, state, player);
        if (stack.is(Items.FLINT_AND_STEEL)) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        } else {
            stack.consume(1, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            prime(level, pos, state, null);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos)) {
            prime(level, pos, state, null);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos pos = hit.getBlockPos();
        if (!level.isClientSide() && projectile.isOnFire() && projectile.mayInteract(level, pos)) {
            prime(level, pos, state, projectile.getOwner() instanceof LivingEntity owner ? owner : null);
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
        prime(level, pos, state, igniter);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (level.isClientSide()) return;
        Entity source = explosion.getIndirectSourceEntity();
        spawn(level, pos, state, source instanceof LivingEntity living ? living : null, CHAIN_FUSE_MIN + level.random.nextInt(CHAIN_FUSE_SPREAD));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(type.unlitItem());
    }

    public void prime(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity igniter) {
        if (level.isClientSide() || !state.is(this)) return;
        level.removeBlock(pos, false);
        spawn(level, pos, state, igniter, type.fuseTicks);
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
    }

    private void spawn(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity igniter, int fuse) {
        if (!(level instanceof ServerLevel server)) return;
        List<double[]> boxes = layout(state);
        for (double[] box : boxes) {
            double[] r = rotate(box, state.getValue(FACING));
            Vec3 at = new Vec3(pos.getX() + (r[0] + r[3]) / 32.0, pos.getY(), pos.getZ() + (r[2] + r[5]) / 32.0);
            DynamiteEntity dynamite = new DynamiteEntity(server, at.x, at.y, at.z, fuse, type);
            if (igniter != null) {
                dynamite.setOwner(igniter);
            }
            server.addFreshEntity(dynamite);
        }
    }
}