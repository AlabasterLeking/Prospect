package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ExtinguishedWallTorchBlock extends WallTorchBlock {

    public static final MapCodec<ExtinguishedWallTorchBlock> CODEC = simpleCodec(ExtinguishedWallTorchBlock::new);
    private static final int SMOKE_CHANCE = 12;

    public ExtinguishedWallTorchBlock(Properties properties) {
        super(ParticleTypes.SMOKE, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SMOKE_CHANCE) != 0) return;
        Direction back = state.getValue(FACING).getOpposite();
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + 0.27 * back.getStepX(), pos.getY() + 0.85, pos.getZ() + 0.5 + 0.27 * back.getStepZ(), 0.0, 0.0, 0.0);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ExtinguishedLight.relight(stack, level, pos, player, hand, Blocks.WALL_TORCH.defaultBlockState().setValue(FACING, state.getValue(FACING)));
    }
}