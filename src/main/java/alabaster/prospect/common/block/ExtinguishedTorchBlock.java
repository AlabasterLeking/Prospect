package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ExtinguishedTorchBlock extends TorchBlock {

    public static final MapCodec<ExtinguishedTorchBlock> CODEC = simpleCodec(ExtinguishedTorchBlock::new);
    private static final int SMOKE_CHANCE = 12;

    public ExtinguishedTorchBlock(Properties properties) {
        super(ParticleTypes.SMOKE, properties);
    }

    @Override
    public MapCodec<? extends TorchBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SMOKE_CHANCE) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ExtinguishedLight.relight(stack, level, pos, player, hand, Blocks.TORCH.defaultBlockState());
    }
}