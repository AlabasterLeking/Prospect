package alabaster.prospect.common.block;

import alabaster.prospect.common.registry.ProspectModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class ExtinguishedLight {
    private ExtinguishedLight() {}

    public static ItemInteractionResult relight(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockState lit) {
        boolean flint = stack.is(Items.FLINT_AND_STEEL);
        boolean torch = stack.is(Items.TORCH);
        if (!flint && !torch && !stack.is(Items.FIRE_CHARGE)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide()) {
            level.setBlock(pos, lit, Block.UPDATE_ALL_IMMEDIATE);
            SoundEvent sound = flint ? SoundEvents.FLINTANDSTEEL_USE : torch ? SoundEvents.FIRE_AMBIENT : SoundEvents.FIRECHARGE_USE;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (flint) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            } else if (!torch) {
                stack.consume(1, player);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    public static BlockState extinguished(BlockState state) {
        if (state.is(Blocks.TORCH)) {
            return ProspectModBlocks.EXTINGUISHED_TORCH.get().defaultBlockState();
        }
        if (state.is(Blocks.WALL_TORCH)) {
            return ProspectModBlocks.EXTINGUISHED_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING));
        }
        if (state.is(Blocks.LANTERN)) {
            return ProspectModBlocks.EXTINGUISHED_LANTERN.get().defaultBlockState()
                    .setValue(LanternBlock.HANGING, state.getValue(LanternBlock.HANGING))
                    .setValue(LanternBlock.WATERLOGGED, state.getValue(LanternBlock.WATERLOGGED));
        }
        return null;
    }

    public static boolean douse(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState out = extinguished(state);
        if (out == null) return false;
        level.setBlock(pos, out, Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.1, 0.1, 0.1, 0.01);
        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }
}