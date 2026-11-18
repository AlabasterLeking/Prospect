package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.grapple.GrapplingHookEntity;
import alabaster.prospect.common.entity.grapple.ZiplineEntity;
import alabaster.prospect.common.network.RopePendingPayload;
import alabaster.prospect.common.network.ZiplineRidingPayload;
import alabaster.prospect.common.tag.ProspectModTags;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Prospect.MODID)
public class GrappleEvents {

    private static final double LEASH_RANGE = 7.0;
    private static final double BREAK_TOLERANCE = 1.0;

    private static final Map<UUID, Integer> RIDERS = new HashMap<>();
    private static final Map<UUID, GlobalPos> PENDING = new HashMap<>();

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof CrossbowItem) || CrossbowItem.isCharged(stack)) return;
        GrapplingHookEntity hook = GrapplingHookEntity.active(event.getEntity());
        if (hook == null) return;
        hook.retract();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(BlockTags.FENCES)) return;
        Player player = event.getEntity();

        GrapplingHookEntity hook = GrapplingHookEntity.active(player);
        if (hook != null && hook.isAnchored()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            tie(level, player, pos, hook);
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (stringsLine(player, level, pos)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            rope(level, player, pos.immutable(), held);
            return;
        }

        if (player.isSecondaryUseActive() && held.isEmpty()) {
            boolean untied = false;
            for (ZiplineEntity line : level.getEntitiesOfClass(ZiplineEntity.class, new AABB(pos).inflate(Config.ZIPLINE_MAX_LENGTH.get()),
                    line -> line.getFence().equals(pos) || line.getAnchor().equals(pos))) {
                line.breakLine();
                untied = true;
            }
            if (untied) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    public static boolean isLine(ItemStack stack) {
        return stack.is(ProspectModTags.ROPES) || stack.is(ProspectModTags.CHAINS);
    }

    public static boolean stringsLine(Player player, Level level, BlockPos fence) {
        if (!isLine(player.getMainHandItem()) || player.isSecondaryUseActive()) return false;
        return level.getEntitiesOfClass(Mob.class, new AABB(fence).inflate(LEASH_RANGE), mob -> mob.getLeashHolder() == player).isEmpty();
    }

    private static boolean lengthOk(Player player, double length) {
        if (length > Config.ZIPLINE_MAX_LENGTH.get()) {
            player.displayClientMessage(Component.translatable("message.prospect.zipline.too_long"), true);
            return false;
        }
        return true;
    }

    private static void tie(ServerLevel level, Player player, BlockPos fence, GrapplingHookEntity hook) {
        Vec3 start = ZiplineEntity.fencePoint(fence);
        Vec3 end = hook.position();
        BlockPos anchor = hook.getAnchorBlock();
        if (anchor == null || !lengthOk(player, start.distanceTo(end))) return;
        ItemStack stack = hook.takeForZipline(player);
        if (PENDING.containsKey(player.getUUID())) {
            setPending(player, null);
        }
        level.addFreshEntity(new ZiplineEntity(level, fence.immutable(), start, end, anchor, stack, true, hook.getHitFace()));
        level.playSound(null, fence, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void rope(ServerLevel level, Player player, BlockPos fence, ItemStack held) {
        boolean chain = held.is(ProspectModTags.CHAINS);
        String kind = chain ? "chain" : "rope";
        GlobalPos first = PENDING.get(player.getUUID());
        if (first == null || !first.dimension().equals(level.dimension()) || !level.getBlockState(first.pos()).is(BlockTags.FENCES)) {
            setPending(player, GlobalPos.of(level.dimension(), fence));
            player.displayClientMessage(Component.translatable("message.prospect." + kind + ".started"), true);
            level.playSound(null, fence, placeSound(chain), SoundSource.BLOCKS, 0.6F, 1.3F);
            return;
        }
        if (first.pos().equals(fence)) {
            setPending(player, null);
            player.displayClientMessage(Component.translatable("message.prospect." + kind + ".cancelled"), true);
            return;
        }
        Vec3 start = ZiplineEntity.fencePoint(first.pos());
        Vec3 end = ZiplineEntity.fencePoint(fence);
        if (!lengthOk(player, start.distanceTo(end))) return;
        setPending(player, null);
        ItemStack stack = held.copyWithCount(1);
        held.consume(1, player);
        level.addFreshEntity(new ZiplineEntity(level, first.pos(), start, end, fence, stack, false, Direction.UP));
        level.playSound(null, fence, placeSound(chain), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static SoundEvent placeSound(boolean chain) {
        return chain ? SoundEvents.CHAIN_PLACE : SoundEvents.LEASH_KNOT_PLACE;
    }

    private static void setPending(Player player, @Nullable GlobalPos fence) {
        if (fence == null) {
            PENDING.remove(player.getUUID());
        } else {
            PENDING.put(player.getUUID(), fence);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new RopePendingPayload(Optional.ofNullable(fence).map(GlobalPos::pos), Config.ZIPLINE_MAX_LENGTH.get().floatValue()));
        }
    }

    private static void tickPending(ServerPlayer player) {
        GlobalPos fence = PENDING.get(player.getUUID());
        if (fence == null) return;
        if (!isLine(player.getMainHandItem())) {
            setPending(player, null);
            player.displayClientMessage(Component.translatable("message.prospect.rope.cancelled"), true);
            return;
        }
        Level level = player.level();
        if (!fence.dimension().equals(level.dimension()) || level.isLoaded(fence.pos()) && !level.getBlockState(fence.pos()).is(BlockTags.FENCES)) {
            setPending(player, null);
        }
    }

    private static void setRider(ServerPlayer player, @Nullable Integer ziplineId) {
        boolean wasRiding = RIDERS.containsKey(player.getUUID());
        if (ziplineId == null) {
            RIDERS.remove(player.getUUID());
        } else {
            RIDERS.put(player.getUUID(), ziplineId);
        }
        if (wasRiding != (ziplineId != null)) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new ZiplineRidingPayload(player.getId(), ziplineId != null));
        }
    }

    public static void setRiding(ServerPlayer player, int ziplineId) {
        if (ziplineId >= 0 && player.level().getEntity(ziplineId) instanceof ZiplineEntity line
                && line.distanceToLine(hangPoint(player)) <= ZiplineEntity.RIDE_REACH) {
            setRider(player, ziplineId);
            player.resetFallDistance();
            ProspectAdvancements.award(player, ProspectAdvancements.ZIPPEE);
        } else {
            setRider(player, null);
        }
    }

    public static void breakLine(ServerPlayer player, int ziplineId) {
        if (player.isSpectator() || !(player.level().getEntity(ziplineId) instanceof ZiplineEntity line)) return;
        if (line.distanceToLine(player.getEyePosition()) > player.entityInteractionRange() + BREAK_TOLERANCE) return;
        line.breakLine();
    }

    private static Vec3 hangPoint(Player player) {
        return player.position().add(0.0, ZiplineEntity.HANG * player.getScale(), 0.0);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        tickPending(player);
        Integer id = RIDERS.get(player.getUUID());
        if (id == null) return;
        if (!(player.level().getEntity(id) instanceof ZiplineEntity line) || line.distanceToLine(hangPoint(player)) > ZiplineEntity.RIDE_REACH) {
            setRider(player, null);
            return;
        }
        player.resetFallDistance();
        GrapplingHookEntity.keepAloft(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        RIDERS.remove(event.getEntity().getUUID());
        PENDING.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        setRider(player, null);
        if (PENDING.containsKey(player.getUUID())) {
            setPending(player, null);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer rider && RIDERS.containsKey(rider.getUUID()) && event.getEntity() instanceof ServerPlayer tracker) {
            PacketDistributor.sendToPlayer(tracker, new ZiplineRidingPayload(rider.getId(), true));
        }
    }
}