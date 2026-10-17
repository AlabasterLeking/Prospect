package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.registry.ProspectDataComponents;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LitDynamiteItem extends Item {

    public static final int FUSE_TICKS = 80;
    public static final float EXPLOSION_POWER = 2.0F;

    private static final int FUSE_BAR_COLOR = 0xFF8A1E;
    private static final int FULL_CHARGE_TICKS = 20;
    private static final float MIN_THROW_VELOCITY = 0.5F;
    private static final float MAX_THROW_VELOCITY = 1.5F;

    public LitDynamiteItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create() {
        ItemStack stack = new ItemStack(ProspectModItems.LIT_DYNAMITE.get());
        stack.set(ProspectDataComponents.FUSE.get(), FUSE_TICKS);
        return stack;
    }

    public static int getFuse(ItemStack stack) {
        return stack.getOrDefault(ProspectDataComponents.FUSE.get(), FUSE_TICKS);
    }

    public static void explode(Level level, @Nullable Entity source, double x, double y, double z) {
        level.explode(source, x, y, z, EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
    }

    public static void playDouse(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.6F, 1.6F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.2, z, 6, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        float charge = Mth.clamp((float) (getUseDuration(stack, entity) - timeLeft) / FULL_CHARGE_TICKS, 0.0F, 1.0F);
        float velocity = Mth.lerp(charge, MIN_THROW_VELOCITY, MAX_THROW_VELOCITY);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide()) {
            DynamiteEntity dynamite = new DynamiteEntity(level, player, getFuse(stack));
            dynamite.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
            level.addFreshEntity(dynamite);
            if (player instanceof ServerPlayer serverPlayer) {
                ProspectAdvancements.award(serverPlayer, ProspectAdvancements.FIRE_IN_THE_HOLE);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.shrink(1);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            if (entity instanceof LivingEntity living && level.getGameTime() % 2 == 0) {
                spawnHandSmoke(level, living, stack);
            }
            return;
        }

        if (entity.isUnderWater()) {
            douseInInventory(stack, level, entity);
            return;
        }

        int fuse = getFuse(stack) - 1;
        if (fuse <= 0) {
            stack.shrink(1);
            explode(level, null, entity.getX(), entity.getY(0.5), entity.getZ());
            if (entity instanceof ServerPlayer serverPlayer) {
                ProspectAdvancements.award(serverPlayer, ProspectAdvancements.HOT_POTATO);
            }
            return;
        }
        stack.set(ProspectDataComponents.FUSE.get(), fuse);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (level.isClientSide()) {
            if (level.getGameTime() % 2 == 0) {
                level.addParticle(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 0.3, entity.getZ(), 0.0, 0.02, 0.0);
            }
            return false;
        }

        if (entity.isInWater()) {
            entity.setItem(new ItemStack(ProspectModItems.DYNAMITE.get(), stack.getCount()));
            playDouse(level, entity.getX(), entity.getY(), entity.getZ());
            return false;
        }

        int fuse = getFuse(stack) - 1;
        if (fuse <= 0) {
            entity.discard();
            explode(level, null, entity.getX(), entity.getY(), entity.getZ());
            return true;
        }
        stack.set(ProspectDataComponents.FUSE.get(), fuse);
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getFuse(stack) / FUSE_TICKS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return FUSE_BAR_COLOR;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    private static void douseInInventory(ItemStack stack, Level level, Entity entity) {
        ItemStack unlit = new ItemStack(ProspectModItems.DYNAMITE.get());
        playDouse(level, entity.getX(), entity.getEyeY() - 0.4, entity.getZ());

        if (entity instanceof LivingEntity living) {
            for (InteractionHand hand : InteractionHand.values()) {
                if (living.getItemInHand(hand) == stack) {
                    living.setItemInHand(hand, unlit);
                    return;
                }
            }
        }

        stack.shrink(1);
        if (entity instanceof Player player && !player.getInventory().add(unlit)) {
            player.drop(unlit, false);
        }
    }

    private static void spawnHandSmoke(Level level, LivingEntity living, ItemStack stack) {
        boolean mainHand = living.getMainHandItem() == stack;
        if (!mainHand && living.getOffhandItem() != stack) return;

        boolean rightSide = mainHand == (living.getMainArm() == HumanoidArm.RIGHT);
        Vec3 look = living.getViewVector(1.0F);
        Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
        Vec3 pos = living.getEyePosition()
                .add(look.scale(0.5))
                .add(right.scale(rightSide ? 0.35 : -0.35))
                .add(0.0, -0.35, 0.0);
        level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0, 0.02, 0.0);
    }
}