package alabaster.prospect.common.block;

import alabaster.prospect.common.particle.GlowSporeOptions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GlowingShelfMushroomBlock extends HorizontalDirectionalBlock implements BonemealableBlock {

    public static final MapCodec<GlowingShelfMushroomBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GlowColor.CODEC.fieldOf("color").forGetter(GlowingShelfMushroomBlock::getColor),
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(block -> block.hugeFeature),
            propertiesCodec()
    ).apply(instance, GlowingShelfMushroomBlock::new));

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.SOUTH, Block.box(4.0, 0.0, 0.0, 12.0, 12.0, 9.0),
            Direction.NORTH, Block.box(4.0, 0.0, 7.0, 12.0, 12.0, 16.0),
            Direction.EAST, Block.box(0.0, 0.0, 4.0, 9.0, 12.0, 12.0),
            Direction.WEST, Block.box(7.0, 0.0, 4.0, 16.0, 12.0, 12.0)
    );

    private static final float GROWTH_CHANCE = 0.4F;
    private static final double CAP_OFFSET = 5.0 / 16.0;

    private final GlowColor color;
    private final ResourceKey<ConfiguredFeature<?, ?>> hugeFeature;

    public GlowingShelfMushroomBlock(GlowColor color, ResourceKey<ConfiguredFeature<?, ?>> hugeFeature, Properties properties) {
        super(properties);
        this.color = color;
        this.hugeFeature = hugeFeature;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public GlowColor getColor() {
        return color;
    }

    @Override
    protected MapCodec<GlowingShelfMushroomBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            state = state.setValue(FACING, direction.getOpposite());
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
        }
        return null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) != 0) return;
        Direction facing = state.getValue(FACING);
        double out = CAP_OFFSET - 0.5;
        double x = pos.getX() + 0.5 + facing.getStepX() * out + (random.nextDouble() - 0.5) * 0.3;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * out + (random.nextDouble() - 0.5) * 0.3;
        level.addParticle(GlowSporeOptions.of(color), x, pos.getY() + 0.35, z, 0.0, -0.01, 0.0);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < GROWTH_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(hugeFeature).ifPresent(feature -> {
            level.removeBlock(pos, false);
            if (!feature.value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        });
    }
}
