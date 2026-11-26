package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.network.MinecartLinkPayload;
import alabaster.prospect.common.tag.ProspectModTags;
import alabaster.prospect.common.utilities.JumpingMinecart;
import alabaster.prospect.common.utilities.MinecartLinks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Prospect.MODID)
public class MinecartLinkEvents {

    private static final double SLACK = 0.1;
    private static final double CORRECTION = 0.25;
    private static final double MAX_IMPULSE = 0.8;
    private static final int ITERATIONS = 4;
    private static final int MAX_TRAIN = 256;

    private static final Map<UUID, UUID> PENDING = new HashMap<>();
    private static final Map<ServerLevel, Set<AbstractMinecart>> ACTIVE = new HashMap<>();

    public static final Map<Integer, Set<Integer>> CLIENT_LINKS = new HashMap<>();
    public static int clientPending = -1;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractMinecart cart)) return;
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        boolean chain = stack.is(ProspectModTags.CHAINS);
        boolean shears = stack.is(Tags.Items.TOOLS_SHEAR);
        if (!chain && !shears) return;
        if (chain && !Config.MINECART_COUPLING.get()) return;

        if (player.level().isClientSide()) {
            if (shears && !CLIENT_LINKS.containsKey(cart.getId())) return;
            if (chain) {
                clientPending = clientPending == -1 ? cart.getId() : -1;
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ServerLevel level = serverPlayer.serverLevel();

        if (shears) {
            if (!MinecartLinks.has(cart)) return;
            unlinkAll(level, cart, true);
            level.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.8F, 1.2F);
            level.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            message(serverPlayer, "unlinked");
        } else {
            UUID pending = PENDING.remove(player.getUUID());
            Entity first = pending == null ? null : level.getEntity(pending);
            if (!(first instanceof AbstractMinecart other) || !other.isAlive()) {
                PENDING.put(player.getUUID(), cart.getUUID());
                sound(level, cart.position(), SoundEvents.CHAIN_HIT, 1.2F);
                message(serverPlayer, "started");
            } else if (other == cart) {
                message(serverPlayer, "cancelled");
            } else {
                tryLink(serverPlayer, level, other, cart, stack);
            }
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void tryLink(ServerPlayer player, ServerLevel level, AbstractMinecart first, AbstractMinecart second, ItemStack stack) {
        prune(level, first);
        prune(level, second);
        if (MinecartLinks.isLinked(first, second)) {
            message(player, "already");
        } else if (first.distanceTo(second) > Config.COUPLING_REACH.get()) {
            message(player, "too_far");
        } else if (MinecartLinks.get(first).size() >= MinecartLinks.MAX_LINKS || MinecartLinks.get(second).size() >= MinecartLinks.MAX_LINKS) {
            message(player, "full");
        } else if (connected(level, second, first)) {
            message(player, "loop");
        } else if (Config.MAX_TRAIN_LENGTH.get() > 0 && trainSize(level, first) + trainSize(level, second) > Config.MAX_TRAIN_LENGTH.get()) {
            player.displayClientMessage(Component.translatable("message.prospect.minecart_link.too_long", Config.MAX_TRAIN_LENGTH.get()), true);
        } else {
            MinecartLinks.add(first, second.getUUID());
            MinecartLinks.add(second, first.getUUID());
            track(level, first);
            track(level, second);
            sync(first, second, true);
            stack.consume(1, player);
            sound(level, first.position().add(second.position()).scale(0.5), SoundEvents.CHAIN_PLACE, 1.0F);
            message(player, "linked");
        }
    }

    private static boolean connected(ServerLevel level, Entity start, Entity target) {
        Entity previous = null;
        Entity current = start;
        for (int i = 0; i < MAX_TRAIN && current != null; i++) {
            if (current == target) return true;
            Entity next = null;
            for (UUID id : MinecartLinks.get(current)) {
                Entity linked = level.getEntity(id);
                if (linked != null && linked != previous) {
                    next = linked;
                    break;
                }
            }
            previous = current;
            current = next;
        }
        return false;
    }

    private static int trainSize(ServerLevel level, Entity start) {
        Set<Entity> seen = new HashSet<>();
        List<Entity> open = new ArrayList<>(List.of(start));
        while (!open.isEmpty() && seen.size() < MAX_TRAIN) {
            Entity current = open.remove(open.size() - 1);
            if (!seen.add(current)) continue;
            for (UUID id : MinecartLinks.get(current)) {
                Entity linked = level.getEntity(id);
                if (linked != null && !seen.contains(linked)) open.add(linked);
            }
        }
        return seen.size();
    }

    private static void prune(ServerLevel level, Entity cart) {
        for (UUID id : MinecartLinks.get(cart)) {
            if (!(level.getEntity(id) instanceof AbstractMinecart other) || !other.isAlive()) {
                MinecartLinks.remove(cart, id);
            }
        }
    }

    private static void unlink(ServerLevel level, AbstractMinecart first, AbstractMinecart second, boolean drop) {
        MinecartLinks.remove(first, second.getUUID());
        MinecartLinks.remove(second, first.getUUID());
        sync(first, second, false);
        if (drop) {
            dropChain(level, first.position().add(second.position()).scale(0.5));
        }
    }

    private static void unlinkAll(ServerLevel level, AbstractMinecart cart, boolean drop) {
        for (UUID id : MinecartLinks.get(cart)) {
            if (level.getEntity(id) instanceof AbstractMinecart other) {
                unlink(level, cart, other, drop);
            } else {
                MinecartLinks.remove(cart, id);
                if (drop) dropChain(level, cart.position());
            }
        }
    }

    private static void dropChain(ServerLevel level, Vec3 pos) {
        ItemEntity item = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, new ItemStack(Items.CHAIN));
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    private static void track(ServerLevel level, AbstractMinecart cart) {
        ACTIVE.computeIfAbsent(level, key -> new HashSet<>()).add(cart);
    }

    private static void sync(AbstractMinecart first, AbstractMinecart second, boolean linked) {
        MinecartLinkPayload payload = new MinecartLinkPayload(first.getId(), second.getId(), linked);
        PacketDistributor.sendToPlayersTrackingEntity(first, payload);
        PacketDistributor.sendToPlayersTrackingEntity(second, payload);
    }

    private static void sound(ServerLevel level, Vec3 pos, SoundEvent sound, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.prospect.minecart_link." + key), true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Set<AbstractMinecart> carts = ACTIVE.get(level);
        if (carts == null || carts.isEmpty() || !Config.MINECART_COUPLING.get()) return;
        carts.removeIf(cart -> cart.isRemoved() || !MinecartLinks.has(cart));

        List<AbstractMinecart[]> pairs = new ArrayList<>();
        for (AbstractMinecart cart : carts) {
            for (UUID id : MinecartLinks.get(cart)) {
                if (level.getEntity(id) instanceof AbstractMinecart other && !other.isRemoved() && cart.getId() < other.getId()) {
                    pairs.add(new AbstractMinecart[]{cart, other});
                }
            }
        }

        double spacing = Config.COUPLING_SPACING.get();
        double snap = Math.max(Config.COUPLING_SNAP_DISTANCE.get(), Math.max(Config.COUPLING_REACH.get(), spacing) + 1.0);
        List<AbstractMinecart[]> active = new ArrayList<>();
        for (AbstractMinecart[] pair : pairs) {
            AbstractMinecart first = pair[0];
            AbstractMinecart second = pair[1];
            if (!level.isPositionEntityTicking(first.blockPosition()) || !level.isPositionEntityTicking(second.blockPosition())) continue;
            if (airborne(first) || airborne(second)) continue;
            if (first.distanceTo(second) > snap) {
                unlink(level, first, second, true);
                sound(level, first.position().add(second.position()).scale(0.5), SoundEvents.CHAIN_BREAK, 0.8F);
                continue;
            }
            active.add(pair);
        }

        for (int i = 0; i < ITERATIONS; i++) {
            for (AbstractMinecart[] pair : active) {
                constrain(pair[0], pair[1], spacing);
            }
        }
    }

    private static boolean airborne(AbstractMinecart cart) {
        return cart instanceof JumpingMinecart jumping && jumping.prospect$isAirborne();
    }

    private static void constrain(AbstractMinecart first, AbstractMinecart second, double spacing) {
        Vec3 delta = second.position().subtract(first.position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal < 1.0E-4) return;
        double nx = delta.x / horizontal;
        double nz = delta.z / horizontal;
        double error = delta.length() - spacing;
        error = error > SLACK ? error - SLACK : error < -SLACK ? error + SLACK : 0.0;
        Vec3 firstMotion = first.getDeltaMovement();
        Vec3 secondMotion = second.getDeltaMovement();
        double relative = (secondMotion.x - firstMotion.x) * nx + (secondMotion.z - firstMotion.z) * nz;
        double impulse = Mth.clamp(relative + error * CORRECTION, -MAX_IMPULSE, MAX_IMPULSE) * 0.5;
        if (Math.abs(impulse) < 1.0E-5) return;
        first.setDeltaMovement(firstMotion.x + nx * impulse, firstMotion.y, firstMotion.z + nz * impulse);
        second.setDeltaMovement(secondMotion.x - nx * impulse, secondMotion.y, secondMotion.z - nz * impulse);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (PENDING.isEmpty() || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().is(ProspectModTags.CHAINS) && !player.getOffhandItem().is(ProspectModTags.CHAINS)) {
            PENDING.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof AbstractMinecart cart) || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!MinecartLinks.has(cart) || !Config.MINECART_COUPLING.get()) return;
        for (UUID id : MinecartLinks.get(cart)) {
            if (player.serverLevel().getEntity(id) instanceof AbstractMinecart other) {
                PacketDistributor.sendToPlayer(player, new MinecartLinkPayload(cart.getId(), other.getId(), true));
            }
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof AbstractMinecart cart && MinecartLinks.has(cart)) {
            track(level, cart);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof AbstractMinecart cart)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) {
            forgetClient(cart.getId());
            return;
        }
        Set<AbstractMinecart> carts = ACTIVE.get(level);
        if (carts != null) carts.remove(cart);
        Entity.RemovalReason reason = cart.getRemovalReason();
        if (reason == null || !MinecartLinks.has(cart)) return;
        if (reason.shouldDestroy() || reason == Entity.RemovalReason.CHANGED_DIMENSION) {
            unlinkAll(level, cart, level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS));
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ACTIVE.remove(level);
        }
    }

    public static void applyClient(MinecartLinkPayload payload) {
        if (payload.linked()) {
            CLIENT_LINKS.computeIfAbsent(payload.first(), key -> new HashSet<>()).add(payload.second());
            CLIENT_LINKS.computeIfAbsent(payload.second(), key -> new HashSet<>()).add(payload.first());
        } else {
            unlinkClient(payload.first(), payload.second());
            unlinkClient(payload.second(), payload.first());
        }
    }

    private static void unlinkClient(int id, int other) {
        Set<Integer> links = CLIENT_LINKS.get(id);
        if (links == null) return;
        links.remove(other);
        if (links.isEmpty()) CLIENT_LINKS.remove(id);
    }

    private static void forgetClient(int id) {
        Set<Integer> links = CLIENT_LINKS.remove(id);
        if (links != null) {
            for (int other : links) {
                unlinkClient(other, id);
            }
        }
        if (clientPending == id) clientPending = -1;
    }
}
