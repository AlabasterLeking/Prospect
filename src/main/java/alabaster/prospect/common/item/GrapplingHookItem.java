package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.grapple.GrapplingHookEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrapplingHookItem extends ArrowItem {

    private static final float THROW_SPEED = 1.7F;
    private static final int THROW_COOLDOWN = 10;

    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new GrapplingHookEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        GrapplingHookEntity hook = new GrapplingHookEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        hook.pickup = AbstractArrow.Pickup.ALLOWED;
        return hook;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GrapplingHookEntity existing = GrapplingHookEntity.owned(player);
        if (existing != null) {
            if (!level.isClientSide()) existing.retract();
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide()) {
            GrapplingHookEntity hook = GrapplingHookEntity.thrown(level, player, stack);
            hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, THROW_SPEED, 1.0F);
            level.addFreshEntity(hook);
        }
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.prospect.grappling_hook").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.prospect.grappling_hook.zipline").withStyle(ChatFormatting.GRAY));
    }
}