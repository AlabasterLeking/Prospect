package alabaster.prospect.common.entity.grapple;

import alabaster.prospect.Config;
import alabaster.prospect.common.mixin.ServerGamePacketListenerImplAccessor;
import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.registry.ProspectSounds;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GrapplingHookEntity extends AbstractArrow {

    public static final double MIN_LENGTH = 1.5;
    public static final double ROPE_HEIGHT = 1.5;
    private static final double SNAP_MARGIN = 8.0;
    private static final double TRACKING_LIMIT = 128.0;
    private static final double REEL_SPEED = 0.2;
    private static final double PULL_STRENGTH = 0.25;
    private static final double MAX_PULL = 0.6;
    private static final double GROUND_LIFT = 0.1;
    private static final double ATTACH_HEIGHT = 0.6;
    private static final int REEL_SOUND_INTERVAL = 4;

    private static final EntityDataAccessor<Boolean> ANCHORED = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Direction> FACE = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> HOOKED_ENTITY = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MAX = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CURRENT = SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.FLOAT);

    private static final Map<UUID, GrapplingHookEntity> ACTIVE = new HashMap<>();

    private boolean claimed;
    private boolean tethered;
    private long claimTime;
    private double reelLength;
    private int reel;
    private boolean swingAwarded;
    private boolean pullAwarded;
    @Nullable
    private BlockPos anchorBlock;

    public GrapplingHookEntity(EntityType<? extends GrapplingHookEntity> type, Level level) {
        super(type, level);
        setBaseDamage(0.0);
    }

    public GrapplingHookEntity(Level level, LivingEntity owner, ItemStack ammo, @Nullable ItemStack weapon) {
        super(ProspectModEntities.GRAPPLING_HOOK.get(), owner, level, ammo, weapon);
        setBaseDamage(0.0);
    }

    public GrapplingHookEntity(Level level, double x, double y, double z, ItemStack ammo) {
        super(ProspectModEntities.GRAPPLING_HOOK.get(), x, y, z, level, ammo, null);
        setBaseDamage(0.0);
    }

    public static GrapplingHookEntity thrown(Level level, Player owner, ItemStack stack) {
        GrapplingHookEntity hook = new GrapplingHookEntity(level, owner, stack.copyWithCount(1), null);
        hook.pickup = Pickup.DISALLOWED;
        hook.entityData.set(THROWN, true);
        return hook;
    }

    @Nullable
    public static GrapplingHookEntity active(Player player) {
        GrapplingHookEntity hook = ACTIVE.get(player.getUUID());
        if (hook == null) return null;
        if (hook.isRemoved() || !hook.tethered) {
            ACTIVE.remove(player.getUUID(), hook);
            return null;
        }
        return hook;
    }

    @Nullable
    public static GrapplingHookEntity owned(Player player) {
        if (!player.level().isClientSide()) return active(player);
        List<GrapplingHookEntity> hooks = player.level().getEntitiesOfClass(GrapplingHookEntity.class, player.getBoundingBox().inflate(TRACKING_LIMIT), hook -> hook.getOwner() == player);
        return hooks.isEmpty() ? null : hooks.get(0);
    }

    public static boolean holding(Player player, boolean thrown) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (thrown ? held.is(ProspectModItems.GRAPPLING_HOOK.get()) : held.getItem() instanceof CrossbowItem) return true;
        }
        return false;
    }

    public static boolean holdsLauncher(Player player) {
        return holding(player, false) || holding(player, true);
    }

    public static Vec3 ropePoint(Entity entity) {
        return entity.position().add(0.0, ROPE_HEIGHT, 0.0);
    }

    public static void keepAloft(ServerPlayer player) {
        ((ServerGamePacketListenerImplAccessor) player.connection).prospect$setAboveGroundTickCount(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANCHORED, false);
        builder.define(LENGTH, 0.0F);
        builder.define(THROWN, false);
        builder.define(FACE, Direction.UP);
        builder.define(HOOKED_ENTITY, -1);
        builder.define(MAX, 48.0F);
        builder.define(CURRENT, 0.0F);
    }

    public boolean isAnchored() {
        return entityData.get(ANCHORED);
    }

    public boolean isThrown() {
        return entityData.get(THROWN);
    }

    public int getHookedEntityId() {
        return entityData.get(HOOKED_ENTITY);
    }

    @Nullable
    public Entity getHookedEntity() {
        int id = getHookedEntityId();
        return id < 0 ? null : level().getEntity(id);
    }

    public void setReel(int direction) {
        reel = Integer.signum(direction);
    }

    public Direction getHitFace() {
        return entityData.get(FACE);
    }

    public double getMaxLength() {
        return entityData.get(MAX);
    }

    public float getCurrentLength() {
        return entityData.get(CURRENT);
    }

    private void setReelLength(double length) {
        reelLength = length;
        if (Math.abs(entityData.get(CURRENT) - length) > 0.01) {
            entityData.set(CURRENT, (float) length);
        }
    }

    public double getRopeCap() {
        return Math.max(getMaxLength(), getRopeLength());
    }

    public float getRopeLength() {
        return entityData.get(LENGTH);
    }

    @Nullable
    public BlockPos getAnchorBlock() {
        return anchorBlock;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ProspectModItems.GRAPPLING_HOOK.get());
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !claimed) {
            claim(getOwner() instanceof Player player ? player : null);
            if (isRemoved()) return;
        }
        Entity target = getHookedEntity();
        if (getHookedEntityId() >= 0) {
            if (target == null || !target.isAlive()) {
                if (!level().isClientSide()) retract();
                return;
            }
            setPos(target.getX(), target.getY(ATTACH_HEIGHT), target.getZ());
            setDeltaMovement(Vec3.ZERO);
            if (level().isClientSide()) return;
        } else {
            super.tick();
            if (level().isClientSide() || isRemoved()) return;
        }
        if (!tethered) return;

        Player owner = getOwner() instanceof Player player ? player : null;
        if (owner == null || !owner.isAlive() || owner.level() != level()) {
            loosen();
            return;
        }
        if (!holding(owner, isThrown())) {
            retract();
            return;
        }

        if (target != null) {
            pull(owner, target);
            return;
        }
        double distance = ropePoint(owner).distanceTo(position());
        if (!isAnchored()) {
            if (distance > TRACKING_LIMIT) retract();
            return;
        }
        if (distance > getRopeCap() + SNAP_MARGIN || anchorBlock == null || level().getBlockState(anchorBlock).getCollisionShape(level(), anchorBlock).isEmpty()) {
            retract();
            return;
        }
        double before = reelLength;
        if (reel > 0) {
            setReelLength(Math.max(MIN_LENGTH, reelLength - REEL_SPEED));
        } else if (reel < 0) {
            setReelLength(Math.min(getRopeCap(), Math.min(reelLength + REEL_SPEED, Math.max(reelLength, distance))));
        }
        reelSound(owner, before);
        owner.resetFallDistance();
        if (owner instanceof ServerPlayer serverPlayer) {
            keepAloft(serverPlayer);
            if (!swingAwarded && !serverPlayer.onGround()) {
                swingAwarded = true;
                ProspectAdvancements.award(serverPlayer, ProspectAdvancements.HOOKED_ON_A_FEELING);
            }
        }
    }

    private void reelSound(Player owner, double before) {
        if (reel == 0 || Math.abs(reelLength - before) < 1.0E-3 || tickCount % REEL_SOUND_INTERVAL != 0) return;
        level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), ProspectSounds.GRAPPLING_HOOK_REEL.get(), SoundSource.PLAYERS, 0.4F, reel > 0 ? 1.15F : 0.9F);
    }

    private void pull(Player owner, Entity target) {
        Vec3 offset = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(ropePoint(owner));
        double distance = offset.length();
        if (distance > getRopeCap() + SNAP_MARGIN) {
            retract();
            return;
        }
        double before = reelLength;
        if (reel > 0) {
            setReelLength(Math.max(MIN_LENGTH, Math.min(reelLength, distance) - REEL_SPEED));
        } else if (reel < 0) {
            setReelLength(Math.min(getRopeCap(), reelLength + REEL_SPEED));
        }
        reelSound(owner, before);
        if (distance <= reelLength || distance < 1.0E-4) return;
        Vec3 normal = offset.scale(1.0 / distance);
        Vec3 velocity = target.getDeltaMovement();
        double outward = Math.max(0.0, velocity.dot(normal));
        double tug = Math.min(MAX_PULL, (distance - reelLength) * PULL_STRENGTH);
        velocity = velocity.subtract(normal.scale(outward + tug));
        if (target.onGround()) {
            velocity = velocity.add(0.0, GROUND_LIFT, 0.0);
        }
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        if (!pullAwarded && reel > 0 && target instanceof Mob && owner instanceof ServerPlayer serverPlayer) {
            pullAwarded = true;
            ProspectAdvancements.award(serverPlayer, ProspectAdvancements.GET_OVER_HERE);
        }
    }

    private void claim(@Nullable Player owner) {
        claimed = true;
        if (owner == null) return;
        GrapplingHookEntity current = active(owner);
        if (current != null && current != this) {
            if (current.claimTime == level().getGameTime()) {
                discard();
                return;
            }
            current.retract();
        }
        tethered = true;
        claimTime = level().getGameTime();
        entityData.set(MAX, (isThrown() ? Config.GRAPPLE_THROWN_MAX_LENGTH : Config.GRAPPLE_MAX_LENGTH).get().floatValue());
        ACTIVE.put(owner.getUUID(), this);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide() || !tethered || !(getOwner() instanceof Player owner)) return;
        double distance = ropePoint(owner).distanceTo(position());
        anchorBlock = result.getBlockPos();
        entityData.set(FACE, result.getDirection());
        entityData.set(LENGTH, (float) Math.max(MIN_LENGTH, distance));
        setReelLength(Math.max(MIN_LENGTH, distance));
        entityData.set(ANCHORED, true);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) return;
        Entity target = result.getEntity();
        if (!tethered || !(getOwner() instanceof Player owner) || target == owner || target.getType().is(Tags.EntityTypes.BOSSES)
                || target instanceof Player && !Config.GRAPPLE_PULL_PLAYERS.get()) {
            if (tethered) {
                retract();
            } else {
                setDeltaMovement(getDeltaMovement().scale(-0.05));
            }
            return;
        }
        double distance = ropePoint(owner).distanceTo(target.position());
        setReelLength(Math.max(MIN_LENGTH, distance));
        entityData.set(LENGTH, (float) reelLength);
        entityData.set(HOOKED_ENTITY, target.getId());
        setPos(target.getX(), target.getY(ATTACH_HEIGHT), target.getZ());
        setDeltaMovement(Vec3.ZERO);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return !tethered && super.tryPickup(player);
    }

    @Override
    protected void tickDespawn() {
        if (!tethered) super.tickDespawn();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = 96.0 * getViewScale();
        return distance < range * range;
    }

    public void retract() {
        if (isRemoved()) return;
        Entity owner = getOwner();
        if (owner instanceof Player player && pickup == Pickup.ALLOWED) {
            ItemStack stack = getPickupItem();
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        if (owner != null) {
            level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.6F, 1.4F);
        }
        discard();
    }

    public ItemStack takeForZipline(Player player) {
        ItemStack stack = ItemStack.EMPTY;
        if (isThrown()) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = player.getItemInHand(hand);
                if (held.is(ProspectModItems.GRAPPLING_HOOK.get())) {
                    stack = held.copyWithCount(1);
                    held.consume(1, player);
                    break;
                }
            }
        } else if (pickup == Pickup.ALLOWED) {
            stack = getPickupItem();
        }
        discard();
        return stack;
    }

    private void loosen() {
        tethered = false;
        entityData.set(ANCHORED, false);
        entityData.set(HOOKED_ENTITY, -1);
        ACTIVE.values().removeIf(hook -> hook == this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Thrown", isThrown());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(THROWN, tag.getBoolean("Thrown"));
    }
}