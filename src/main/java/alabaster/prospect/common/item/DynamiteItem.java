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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DynamiteItem extends Item {

    private final DynamiteType type;

    public DynamiteItem(Properties properties, DynamiteType type) {
        super(properties);
        this.type = type;
    }

    public DynamiteType getType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block placed = type.placedBlock();
        if (placed != null && (player.isSecondaryUseActive() || level.getBlockState(pos).is(placed))) {
            return place(context, placed);
        }
        if (!isOpenFlame(level.getBlockState(pos))) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            ignite(level, player, context.getHand(), context.getItemInHand(), type);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult place(UseOnContext context, Block block) {
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!placeContext.canPlace()) return InteractionResult.FAIL;
        Level level = placeContext.getLevel();
        BlockPos pos = placeContext.getClickedPos();
        BlockState state = block.getStateForPlacement(placeContext);
        if (state == null || !state.canSurvive(level, pos)) return InteractionResult.FAIL;
        if (!level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) return InteractionResult.FAIL;
        Player player = placeContext.getPlayer();
        SoundType sound = state.getSoundType(level, pos, player);
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
        placeContext.getItemInHand().consume(1, player);
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
            ignite(level, player, hand, stack, type);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.prospect.dynamite.light").withStyle(ChatFormatting.GRAY));
        if (type.placedBlock() != null) {
            tooltip.add(Component.translatable("tooltip.prospect.dynamite.place").withStyle(ChatFormatting.GRAY));
        }
    }

    private static boolean isOpenFlame(BlockState state) {
        if (CampfireBlock.isLitCampfire(state)) return true;
        if (state.getBlock() instanceof AbstractCandleBlock) return state.getValue(AbstractCandleBlock.LIT);
        return state.getBlock() instanceof BaseFireBlock || state.getBlock() instanceof TorchBlock;
    }

    private static void ignite(Level level, Player player, InteractionHand hand, ItemStack stack, DynamiteType type) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.6F, 1.4F);

        ItemStack lit = LitDynamiteItem.create(type);
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