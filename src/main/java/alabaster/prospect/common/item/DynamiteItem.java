package alabaster.prospect.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DynamiteItem extends Item {

    public DynamiteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!isOpenFlame(level.getBlockState(pos))) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            ignite(level, player, context.getHand(), context.getItemInHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack igniter = player.getItemInHand(otherHand);
        if (!igniter.is(Items.FLINT_AND_STEEL)) return InteractionResultHolder.pass(stack);

        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!level.isClientSide()) {
            igniter.hurtAndBreak(1, player, LivingEntity.getSlotForHand(otherHand));
            ignite(level, player, hand, stack);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.prospect.dynamite.light").withStyle(ChatFormatting.GRAY));
    }

    private static boolean isOpenFlame(BlockState state) {
        if (CampfireBlock.isLitCampfire(state)) return true;
        if (state.getBlock() instanceof AbstractCandleBlock) return state.getValue(AbstractCandleBlock.LIT);
        return state.getBlock() instanceof BaseFireBlock || state.getBlock() instanceof TorchBlock;
    }

    private static void ignite(Level level, Player player, InteractionHand hand, ItemStack stack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.6F, 1.4F);

        ItemStack lit = LitDynamiteItem.create();
        if (stack.getCount() > 1) {
            ItemStack remainder = stack.copyWithCount(stack.getCount() - 1);
            player.setItemInHand(hand, lit);
            if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        } else {
            player.setItemInHand(hand, lit);
        }
    }
}