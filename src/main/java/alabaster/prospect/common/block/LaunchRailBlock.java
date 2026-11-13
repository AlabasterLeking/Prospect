package alabaster.prospect.common.block;

import alabaster.prospect.common.utilities.JumpingMinecart;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LaunchRailBlock extends StraightRailBlock {

    public static final MapCodec<LaunchRailBlock> CODEC = simpleCodec(LaunchRailBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final double LAUNCH_STRENGTH = 0.85;
    private static final double MIN_LAUNCH_SPEED = 0.4;
    private static final double MOVING_THRESHOLD = 0.05;

    public LaunchRailBlock(Properties properties) {
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

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block neighborBlock) {
        boolean powered = state.getValue(POWERED);
        boolean shouldBePowered = level.hasNeighborSignal(pos);
        if (powered == shouldBePowered) return;

        level.setBlock(pos, state.setValue(POWERED, shouldBePowered), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos.below(), this);
        if (state.getValue(SHAPE).isAscending()) {
            level.updateNeighborsAt(pos.above(), this);
        }

        if (shouldBePowered) {
            for (AbstractMinecart cart : level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos).inflate(0.2))) {
                launch(level, pos, cart, Vec3.ZERO);
            }
        }
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (level.isClientSide() || !state.getValue(POWERED)) return;

        Vec3 horizontal = cart.getDeltaMovement().multiply(1.0, 0.0, 1.0);
        double speed = horizontal.length();
        if (speed < MOVING_THRESHOLD) return;

        launch(level, pos, cart, speed < MIN_LAUNCH_SPEED ? horizontal.scale(MIN_LAUNCH_SPEED / speed) : horizontal);
    }

    private static void launch(Level level, BlockPos pos, AbstractMinecart cart, Vec3 horizontal) {
        if (!(cart instanceof JumpingMinecart jumping) || jumping.prospect$isAirborne()) return;

        jumping.prospect$queueLaunch(horizontal, LAUNCH_STRENGTH);
        level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.8F, 1.3F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 8, 0.3, 0.05, 0.3, 0.02);
        }

        for (Entity passenger : cart.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                ProspectAdvancements.award(player, ProspectAdvancements.LIFTOFF);
            }
        }
    }
}