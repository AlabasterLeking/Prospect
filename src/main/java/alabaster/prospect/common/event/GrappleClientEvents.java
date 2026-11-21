package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.client.renderer.ChainRenderer;
import alabaster.prospect.client.renderer.ChainKnotRenderer;
import alabaster.prospect.client.renderer.LeadKnotRenderer;
import alabaster.prospect.client.renderer.RopeHand;
import alabaster.prospect.client.renderer.RopeRenderer;
import alabaster.prospect.client.sound.ZiplineRideSound;
import alabaster.prospect.common.entity.grapple.GrapplingHookEntity;
import alabaster.prospect.common.entity.grapple.RopeWrap;
import alabaster.prospect.common.entity.grapple.ZiplineEntity;
import alabaster.prospect.common.network.GrappleReelPayload;
import alabaster.prospect.common.network.ZiplineBreakPayload;
import alabaster.prospect.common.network.ZiplineRidePayload;
import alabaster.prospect.common.registry.ProspectKeyBindings;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.tag.ProspectModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import alabaster.prospect.common.registry.ProspectSounds;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = Prospect.MODID, value = Dist.CLIENT)
public class GrappleClientEvents {

    private static final double REEL_SPEED = 0.2;
    private static final double AIR_FRICTION = 0.91;
    private static final double SWING_DAMPING = 0.99;
    private static final double SWING_PUSH = 0.03;
    private static final double SLACK_TOLERANCE = 0.05;
    private static final double MIN_FREE = 0.5;
    private static final double TAUT_SPEED = 0.3;
    private static final double BUMP_SPEED = 0.15;

    private static final double RIDE_GRAVITY = 0.08;
    private static final double RIDE_DRAG = 0.995;
    private static final double RIDE_PUSH = 0.01;
    private static final double RIDE_MAX_SPEED = 1.5;
    private static final double RIDE_END_MARGIN = 0.8;
    private static final double RIDE_BLOCKED = 0.25;
    private static final double MOUNT_REACH = 4.5;
    private static final double MOUNT_RADIUS = 0.4;
    private static final double HOP = 0.42;
    private static final double PREVIEW_SAG = 0.04;

    private static int hookId = -1;
    private static int releasedId = -1;
    private static int reelSent;
    private static double length;
    @Nullable
    private static Vec3 lastAnchor;
    private static boolean taut;

    private static int ziplineId = -1;
    private static double ziplineT;
    private static double ziplineSpeed;

    private static final Set<Integer> RIDING = new HashSet<>();
    private static final Map<Integer, RopeWrap> WRAPS = new HashMap<>();
    private static int clientTick;
    private static BlockPos pendingFence;
    private static float pendingMax;

    public static void setRiding(int playerId, boolean riding) {
        if (riding) {
            if (RIDING.add(playerId)) startRideSound(playerId);
        } else {
            RIDING.remove(playerId);
        }
    }

    private static void startRideSound(int playerId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(playerId) instanceof Player rider) {
            mc.getSoundManager().play(new ZiplineRideSound(rider));
        }
    }

    public static boolean isRiding(int playerId) {
        return RIDING.contains(playerId);
    }

    public static void setPending(Optional<BlockPos> fence, float maxLength) {
        pendingFence = fence.orElse(null);
        pendingMax = maxLength;
    }

    public static double ropeLength(GrapplingHookEntity hook) {
        return hook.getId() == hookId ? length : -1.0;
    }

    @Nullable
    public static RopeWrap wraps(GrapplingHookEntity hook) {
        return WRAPS.get(hook.getId());
    }

    public static Vec3 anchorStart(GrapplingHookEntity hook) {
        Direction face = hook.getHitFace();
        return hook.position().add(face.getStepX() * 0.1, face.getStepY() * 0.1, face.getStepZ() * 0.1);
    }

    private static RopeWrap updateWrap(GrapplingHookEntity hook, Player owner) {
        RopeWrap wrap = WRAPS.computeIfAbsent(hook.getId(), id -> new RopeWrap());
        wrap.update(hook.level(), owner, hook.position(), anchorStart(hook), GrapplingHookEntity.ropePoint(owner), clientTick);
        return wrap;
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        clientTick++;
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            WRAPS.clear();
            return;
        }
        WRAPS.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof GrapplingHookEntity hook) || hook.isRemoved() || !hook.isAnchored());
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof GrapplingHookEntity hook && hook.isAnchored() && hook.getOwner() instanceof Player owner) {
                updateWrap(hook, owner);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player != Minecraft.getInstance().player) return;
        pendingReadout(player);
        if (ziplineId != -1) {
            rideTick(player);
            return;
        }
        GrapplingHookEntity hook = currentHook(player);
        if (hook == null) {
            sendReel(0);
        } else if (hook.isAnchored()) {
            sendReel(reelInput());
            swingTick(player, hook);
        } else {
            pullTick(player, hook);
        }
    }

    private static boolean attached(GrapplingHookEntity hook) {
        return hook.isAnchored() || hook.getHookedEntityId() >= 0;
    }

    private static int reelInput() {
        return ProspectKeyBindings.GRAPPLE_REEL_IN.isDown() ? 1 : ProspectKeyBindings.GRAPPLE_LET_OUT.isDown() ? -1 : 0;
    }

    private static void pullTick(LocalPlayer player, GrapplingHookEntity hook) {
        int direction = reelInput();
        double distance = GrapplingHookEntity.ropePoint(player).distanceTo(hook.position());
        if (direction > 0) {
            length = Math.max(GrapplingHookEntity.MIN_LENGTH, Math.min(length, distance) - REEL_SPEED);
        } else if (direction < 0) {
            length = Math.min(hook.getRopeCap(), length + REEL_SPEED);
        }
        sendReel(direction);
    }

    private static void sendReel(int direction) {
        if (direction == reelSent) return;
        reelSent = direction;
        PacketDistributor.sendToServer(new GrappleReelPayload(direction));
    }

    private static GrapplingHookEntity currentHook(LocalPlayer player) {
        if (hookId != -1) {
            if (player.level().getEntity(hookId) instanceof GrapplingHookEntity hook && !hook.isRemoved() && attached(hook) && hook.getOwner() == player) {
                return hook;
            }
            hookId = -1;
        }
        if (!GrapplingHookEntity.holdsLauncher(player)) return null;
        for (Entity entity : player.clientLevel.entitiesForRendering()) {
            if (entity instanceof GrapplingHookEntity hook && attached(hook) && hook.getOwner() == player && hook.getId() != releasedId) {
                hookId = hook.getId();
                length = Math.max(hook.getRopeLength(), GrapplingHookEntity.ropePoint(player).distanceTo(hook.position()));
                lastAnchor = null;
                taut = false;
                reelSent = 0;
                return hook;
            }
        }
        return null;
    }

    private static void swingTick(LocalPlayer player, GrapplingHookEntity hook) {
        RopeWrap wrap = updateWrap(hook, player);
        length += wrap.takeDelta();
        Vec3 anchor = hook.position();
        if (lastAnchor != null && !anchor.equals(lastAnchor)) {
            Vec3 point = GrapplingHookEntity.ropePoint(player);
            double before = wrap.fixedLength(lastAnchor) + point.distanceTo(wrap.pivot(lastAnchor));
            double after = wrap.fixedLength(anchor) + point.distanceTo(wrap.pivot(anchor));
            length += Math.max(0.0, after - before);
        }
        lastAnchor = anchor;
        double fixed = wrap.fixedLength(anchor);
        Vec3 offset = GrapplingHookEntity.ropePoint(player).subtract(wrap.pivot(anchor));
        double distance = offset.length();
        boolean paying = false;
        if (ProspectKeyBindings.GRAPPLE_REEL_IN.isDown()) {
            length = Math.max(Math.min(length, fixed + GrapplingHookEntity.MIN_LENGTH), length - REEL_SPEED);
        } else if (ProspectKeyBindings.GRAPPLE_LET_OUT.isDown()) {
            paying = length < hook.getRopeCap();
            length = Math.max(length, Math.min(hook.getRopeCap(), Math.min(length + REEL_SPEED, Math.max(length, fixed + distance))));
        }
        length = Math.max(length, fixed + MIN_FREE);
        double free = length - fixed;
        player.resetFallDistance();
        if (distance < free - SLACK_TOLERANCE || distance < 1.0E-4) {
            taut = false;
            return;
        }

        Vec3 normal = offset.scale(1.0 / distance);
        boolean grounded = player.onGround();
        if (distance > free) {
            Vec3 correction = normal.scale(free - distance);
            if (grounded) {
                correction = new Vec3(correction.x, Math.max(0.0, correction.y), correction.z);
            }
            player.move(MoverType.SELF, correction);
        }
        Vec3 velocity = player.getDeltaMovement();
        double outward = velocity.dot(normal);
        if (!taut && !grounded && outward > TAUT_SPEED) {
            float strength = (float) Mth.clamp(outward, TAUT_SPEED, 1.0);
            player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), ProspectSounds.GRAPPLING_HOOK_TAUT.get(), SoundSource.PLAYERS, strength, 0.9F + player.getRandom().nextFloat() * 0.2F, false);
        }
        taut = true;
        double allowed = paying ? REEL_SPEED : 0.0;
        if (outward > allowed) {
            Vec3 removed = normal.scale(outward - allowed);
            if (grounded) {
                removed = new Vec3(removed.x, Math.min(0.0, removed.y), removed.z);
            }
            velocity = velocity.subtract(removed);
        }
        if (!grounded && !player.horizontalCollision && !player.isInWater()) {
            double restore = SWING_DAMPING / AIR_FRICTION;
            velocity = new Vec3(velocity.x * restore, velocity.y, velocity.z * restore);
            Vec3 push = inputDirection(player).scale(SWING_PUSH);
            velocity = velocity.add(push.subtract(normal.scale(push.dot(normal))));
        }
        player.setDeltaMovement(velocity);
    }

    private static Vec3 inputDirection(LocalPlayer player) {
        double forward = player.input.forwardImpulse;
        double strafe = player.input.leftImpulse;
        double yaw = Math.toRadians(player.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        Vec3 wish = new Vec3(strafe * cos - forward * sin, 0.0, forward * cos + strafe * sin);
        return wish.lengthSqr() > 1.0 ? wish.normalize() : wish;
    }

    private static void rideTick(LocalPlayer player) {
        if (!(player.level().getEntity(ziplineId) instanceof ZiplineEntity line) || line.isRemoved()) {
            dismount(player, Vec3.ZERO);
            return;
        }
        Vec3 span = line.getEnd().subtract(line.getStart());
        double spanLength = span.length();
        if (spanLength < 1.0E-3) {
            dismount(player, Vec3.ZERO);
            return;
        }
        Vec3 direction = span.scale(1.0 / spanLength);
        if (player.input.shiftKeyDown) {
            dismount(player, direction.scale(ziplineSpeed));
            return;
        }
        if (player.input.jumping) {
            dismount(player, direction.scale(ziplineSpeed).add(0.0, HOP, 0.0));
            return;
        }

        Vec3 flat = new Vec3(direction.x, 0.0, direction.z);
        ziplineSpeed -= RIDE_GRAVITY * direction.y;
        ziplineSpeed += inputDirection(player).dot(flat) * RIDE_PUSH;
        ziplineSpeed = Mth.clamp(ziplineSpeed * RIDE_DRAG, -RIDE_MAX_SPEED, RIDE_MAX_SPEED);
        double previousT = ziplineT;
        double margin = Math.min(0.5, RIDE_END_MARGIN / spanLength);
        ziplineT = Mth.clamp(ziplineT + ziplineSpeed / spanLength, margin, 1.0 - margin);
        if (ziplineT == margin || ziplineT == 1.0 - margin) {
            bump(player);
            ziplineSpeed = 0.0;
        }

        double hang = ZiplineEntity.HANG * player.getScale();
        Vec3 target = line.pointAt(ziplineT).subtract(0.0, hang, 0.0);
        player.move(MoverType.SELF, target.subtract(player.position()));
        if (player.position().distanceToSqr(target) > RIDE_BLOCKED) {
            ziplineT = previousT;
            bump(player);
            ziplineSpeed = 0.0;
        }
        player.setDeltaMovement(direction.scale(ziplineSpeed));
        player.resetFallDistance();
    }

    private static void bump(LocalPlayer player) {
        if (Math.abs(ziplineSpeed) < BUMP_SPEED) return;
        float strength = (float) Mth.clamp(Math.abs(ziplineSpeed), BUMP_SPEED, 1.0);
        player.level().playLocalSound(player.getX(), player.getY() + player.getBbHeight(), player.getZ(), ProspectSounds.ZIPLINE_BUMP.get(), SoundSource.PLAYERS, strength, 0.9F + player.getRandom().nextFloat() * 0.2F, false);
    }

    private static void mount(LocalPlayer player, ZiplineEntity line, double t) {
        Vec3 direction = line.getEnd().subtract(line.getStart()).normalize();
        ziplineId = line.getId();
        ziplineT = t;
        ziplineSpeed = player.getDeltaMovement().dot(direction);
        if (RIDING.add(player.getId())) startRideSound(player.getId());
        PacketDistributor.sendToServer(new ZiplineRidePayload(line.getId()));
    }

    private static void dismount(LocalPlayer player, Vec3 velocity) {
        ziplineId = -1;
        ziplineSpeed = 0.0;
        RIDING.remove(player.getId());
        player.setDeltaMovement(velocity);
        PacketDistributor.sendToServer(new ZiplineRidePayload(-1));
    }

    private static boolean canGrabLine(ItemStack held) {
        return held.isEmpty() || held.is(ProspectModItems.GRAPPLING_HOOK.get()) || held.getItem() instanceof CrossbowItem && !CrossbowItem.isCharged(held);
    }

    private record LineHit(ZiplineEntity line, double t) {
    }

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) return;

        if (event.isAttack()) {
            LineHit hit = lineInSight(mc, player, player.entityInteractionRange());
            if (hit == null) return;
            PacketDistributor.sendToServer(new ZiplineBreakPayload(hit.line().getId()));
            event.setCanceled(true);
            event.setSwingHand(true);
            return;
        }

        if (!event.isUseItem() || ziplineId != -1 || hookId != -1 || !canGrabLine(player.getMainHandItem())) return;
        LineHit hit = lineInSight(mc, player, MOUNT_REACH);
        if (hit == null) return;
        mount(player, hit.line(), hit.t());
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    private static LineHit lineInSight(Minecraft mc, LocalPlayer player, double maxReach) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double reach = maxReach;
        if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) {
            reach = Math.min(reach, mc.hitResult.getLocation().distanceTo(eye));
        }
        LineHit best = null;
        double bestDistance = MOUNT_RADIUS;
        for (ZiplineEntity line : player.level().getEntitiesOfClass(ZiplineEntity.class, player.getBoundingBox().inflate(ZiplineEntity.SEARCH_RANGE))) {
            double[] hit = rayToSegment(eye, look, reach, line.getStart(), line.getEnd());
            if (hit != null && hit[0] < bestDistance) {
                best = new LineHit(line, hit[1]);
                bestDistance = hit[0];
            }
        }
        return best;
    }

    private static double[] rayToSegment(Vec3 origin, Vec3 direction, double reach, Vec3 a, Vec3 b) {
        Vec3 span = b.subtract(a);
        double spanSqr = span.lengthSqr();
        if (spanSqr < 1.0E-6) return null;
        Vec3 toOrigin = origin.subtract(a);
        double along = direction.dot(span);
        double denominator = spanSqr - along * along;
        double t = denominator < 1.0E-8 ? 0.0 : (span.dot(toOrigin) - along * direction.dot(toOrigin)) / denominator;
        t = Mth.clamp(t, 0.0, 1.0);
        double s = Mth.clamp(direction.dot(a.add(span.scale(t)).subtract(origin)), 0.0, reach);
        Vec3 onRay = origin.add(direction.scale(s));
        t = Mth.clamp(onRay.subtract(a).dot(span) / spanSqr, 0.0, 1.0);
        double distance = onRay.distanceTo(a.add(span.scale(t)));
        return new double[]{distance, t};
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || hookId == -1) return;
        ItemStack stack = event.getItemStack();
        if (stack.is(ProspectModItems.GRAPPLING_HOOK.get())) {
            release();
            return;
        }
        if (!(stack.getItem() instanceof CrossbowItem) || CrossbowItem.isCharged(stack)) return;
        release();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(BlockTags.FENCES)) return;
        boolean tie = hookId != -1 && event.getLevel().getEntity(hookId) instanceof GrapplingHookEntity hook && hook.isAnchored();
        if (!tie && !GrappleEvents.stringsLine(event.getEntity(), event.getLevel(), event.getPos())) return;
        if (tie) release();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        int index = beforeAdvancedInfo(tooltip, stack);
        if (stack.is(ProspectModItems.GRAPPLING_HOOK.get())) {
            tooltip.add(index++, Component.translatable("tooltip.prospect.grappling_hook.controls",
                    ProspectKeyBindings.GRAPPLE_REEL_IN.getTranslatedKeyMessage(), ProspectKeyBindings.GRAPPLE_LET_OUT.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ProspectModTags.ROPES)) {
            tooltip.add(index, Component.translatable("tooltip.prospect.rope").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ProspectModTags.CHAINS)) {
            tooltip.add(index, Component.translatable("tooltip.prospect.chain").withStyle(ChatFormatting.GRAY));
        }
    }

    private static int beforeAdvancedInfo(List<Component> tooltip, ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (int i = 1; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (line.getContents() instanceof TranslatableContents contents && contents.getKey().equals("item.durability")) return i;
            if (line.getString().equals(id)) return i;
        }
        return tooltip.size();
    }

    private static void release() {
        releasedId = hookId;
        hookId = -1;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        hookId = -1;
        releasedId = -1;
        reelSent = 0;
        ziplineId = -1;
        ziplineSpeed = 0.0;
        RIDING.clear();
        pendingFence = null;
    }

    private static void pendingReadout(LocalPlayer player) {
        if (pendingFence == null) return;
        double distance = ZiplineEntity.fencePoint(pendingFence).distanceTo(RopeHand.position(player, true, 1.0F));
        String kind = player.getMainHandItem().is(ProspectModTags.CHAINS) ? "chain" : "rope";
        Component readout = Component.translatable("message.prospect." + kind + ".length", Math.round(distance), Math.round(pendingMax))
                .withStyle(distance > pendingMax ? ChatFormatting.RED : ChatFormatting.WHITE);
        player.displayClientMessage(readout, true);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || pendingFence == null) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        Vec3 start = ZiplineEntity.fencePoint(pendingFence);
        Vec3 hand = RopeHand.position(player, true, partialTick);
        double distance = start.distanceTo(hand);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
        float sag = (float) Math.min(1.0, distance * PREVIEW_SAG);
        if (player.getMainHandItem().is(ProspectModTags.CHAINS)) {
            ChainRenderer.render(poseStack, buffer, mc.level, start, hand, sag, distance > pendingMax);
            ChainKnotRenderer.render(poseStack, buffer, mc.level, start, pendingFence);
            poseStack.popPose();
            buffer.endBatch(ChainRenderer.renderType());
            buffer.endBatch(ChainKnotRenderer.renderType());
            return;
        }
        RopeRenderer.render(poseStack, buffer, mc.level, start, hand, sag, distance > pendingMax);
        LeadKnotRenderer.render(poseStack, buffer, mc.level, start, pendingFence);
        poseStack.popPose();
        buffer.endBatch(RenderType.leash());
        buffer.endBatch(LeadKnotRenderer.renderType());
    }
}