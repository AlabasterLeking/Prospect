package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ExtinguishedLanternBlock extends LanternBlock {

    public static final MapCodec<ExtinguishedLanternBlock> CODEC = simpleCodec(ExtinguishedLanternBlock::new);

    public ExtinguishedLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LanternBlock> codec() {
        return (MapCodec<LanternBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockState lit = Blocks.LANTERN.defaultBlockState().setValue(HANGING, state.getValue(HANGING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
        return ExtinguishedLight.relight(stack, level, pos, player, hand, lit);
    }
}