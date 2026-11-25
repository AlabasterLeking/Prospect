package alabaster.prospect.common.mixin;

import alabaster.prospect.Config;
import alabaster.prospect.common.block.ExtinguishedLight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin extends BaseTorchBlock {

    protected TorchBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || super.isRandomlyTicking(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(Blocks.TORCH) && !state.is(Blocks.WALL_TORCH)) {
            super.randomTick(state, level, pos, random);
            return;
        }
        if (Config.TORCHES_EXTINGUISH_IN_RAIN.get() && level.isRainingAt(pos) && random.nextFloat() < Config.TORCH_RAIN_CHANCE.get()) {
            ExtinguishedLight.douse(level, pos, state);
        }
    }
}