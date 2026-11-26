package alabaster.prospect.common.entity.dynamite;

import alabaster.prospect.common.item.DynamiteType;
import alabaster.prospect.common.item.LitDynamiteItem;
import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DynamiteEntity extends Projectile implements ItemSupplier {

    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STUCK_ENTITY = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RESTING = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_REST_SPIN = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.FLOAT);

    private static final float MIN_SPIN_SPEED = 8.0F;
    private static final float MAX_SPIN_SPEED = 30.0F;
    private static final float SETTLE_SPEED = 20.0F;
    private static final int STUCK_CHECK_INTERVAL = 10;
    private static final double STICK_HALF_WIDTH = 1.5 / 16.0;
    private static final double BUNDLE_HALF_WIDTH = 4.5 / 16.0;
    private static final double BODY_HALF_LENGTH = 6.0 / 16.0;
    public static final double FUSE_TIP = 10.0 / 16.0;
    private static final double WAKE_SPEED_SQR = 1.0E-4;
    private static final double TRAIL_SPACING = 0.15;
    private static final int MAX_TRAIL_STEPS = 6;
    private static final int SLIME_DRIP_CHANCE = 12;

    private int fuse = DynamiteType.STANDARD.fuseTicks;
    private float spin;
    private float spinO;
    private boolean settling;
    private Vec3 attachOffset;
    private Vec3 lastFuseTip;
    private BlockPos restSupport;

    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level level) {
        super(type, level);
    }

    public DynamiteEntity(Level level, LivingEntity owner, int fuse, DynamiteType type) {
        this(ProspectModEntities.DYNAMITE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        setFuse(fuse);
        entityData.set(DATA_TYPE, type.ordinal());
    }

    public DynamiteEntity(Level level, double x, double y, double z, int fuse, DynamiteType type) {
        this(ProspectModEntities.DYNAMITE.get(), level);
        setPos(x, y, z);
        setFuse(fuse);
        entityData.set(DATA_TYPE, type.ordinal());
    }

    public DynamiteType getDynamiteType() {
        return DynamiteType.byId(entityData.get(DATA_TYPE));
    }

    public boolean isStuck() {
        return entityData.get(DATA_STUCK);
    }

    public boolean isResting() {
        return entityData.get(DATA_RESTING);
    }

    public void rest(float yaw, float restSpin, BlockPos support) {
        setYRot(yaw);
        yRotO = yaw;
        spin = restSpin;
        spinO = restSpin;
        restSupport = support;
        entityData.set(DATA_REST_SPIN, restSpin);
        entityData.set(DATA_RESTING, true);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        setDeltaMovement(x, y, z);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void wake() {
        restSupport = null;
        entityData.set(DATA_RESTING, false);
    }

    public double restHeight(float angle) {
        double halfWidth = getDynamiteType() == DynamiteType.BUNDLE ? BUNDLE_HALF_WIDTH : STICK_HALF_WIDTH;
        double radians = Math.toRadians(angle);
        return halfWidth * Math.abs(Math.sin(radians)) + BODY_HALF_LENGTH * Math.abs(Math.cos(radians));
    }

    public void shortenFuse(int ticks) {
        setFuse(Math.min(fuse, ticks));
    }

    private void setFuse(int ticks) {
        fuse = ticks;
        entityData.set(DATA_FUSE, ticks);
    }

    public int getFuse() {
        return fuse;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_FUSE.equals(key)) {
            fuse = entityData.get(DATA_FUSE);
        } else if (DATA_REST_SPIN.equals(key)) {
            spin = entityData.get(DATA_REST_SPIN);
            spinO = spin;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TYPE, 0);
        builder.define(DATA_STUCK, false);
        builder.define(DATA_STUCK_ENTITY, -1);
        builder.define(DATA_FUSE, DynamiteType.STANDARD.fuseTicks);
        builder.define(DATA_RESTING, false);
        builder.define(DATA_REST_SPIN, 0.0F);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        super.tick();

        if (isStuck()) {
            tickStuck();
        } else if (isResting()) {
            tickResting();
        } else {
            tickPhysics();
        }

        if (level().isClientSide()) {
            tickSpin();
            fuse--;
            tickFuseParticles();
            tickSlimeDrip();
        } else if (isInWater()) {
            extinguish();
        } else if (--fuse <= 0) {
            discard();
            LitDynamiteItem.explode(level(), this, getX(), getY(0.0625), getZ(), getDynamiteType());
            if (getDynamiteType() == DynamiteType.BUNDLE && getOwner() instanceof ServerPlayer player) {
                ProspectAdvancements.award(player, ProspectAdvancements.OVERKILL);
            }
        }
    }

    private void tickPhysics() {
        boolean sticky = getDynamiteType().sticky;

        if (!onGround()) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit instanceof EntityHitResult entityHit) {
                if (sticky) {
                    stickToEntity(entityHit.getEntity());
                    return;
                }
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(-motion.x * 0.3, Math.max(motion.y, 0.0) * 0.3, -motion.z * 0.3);
            }
        }

        applyGravity();
        Vec3 motion = getDeltaMovement();
        move(MoverType.SELF, motion);

        if (sticky && (horizontalCollision || verticalCollision)) {
            stickToBlock();
            return;
        }

        Vec3 after = getDeltaMovement();
        if (horizontalCollision) {
            after = new Vec3(after.x == 0.0 ? -motion.x * 0.4 : after.x, after.y, after.z == 0.0 ? -motion.z * 0.4 : after.z);
        }
        after = after.scale(0.98);
        if (onGround()) {
            after = after.multiply(0.7, -0.5, 0.7);
        }
        setDeltaMovement(after);
    }

    private void tickResting() {
        if (level().isClientSide()) return;
        boolean supported = restSupport == null || !level().getBlockState(restSupport).getCollisionShape(level(), restSupport).isEmpty();
        if (!supported || getDeltaMovement().lengthSqr() > WAKE_SPEED_SQR) {
            wake();
        }
    }

    private void tickStuck() {
        setDeltaMovement(Vec3.ZERO);

        int targetId = entityData.get(DATA_STUCK_ENTITY);
        if (targetId >= 0) {
            Entity target = level().getEntity(targetId);
            if (target == null || !target.isAlive()) {
                if (!level().isClientSide()) unstick();
                return;
            }
            if (attachOffset == null) {
                attachOffset = clampToBox(position().subtract(target.position()), target);
            }
            Vec3 attached = target.position().add(attachOffset);
            setPos(attached.x, attached.y, attached.z);
            return;
        }

        if (!level().isClientSide() && tickCount % STUCK_CHECK_INTERVAL == 0 && level().noCollision(this, getBoundingBox().inflate(0.06))) {
            unstick();
        }
    }

    private void stickToEntity(Entity target) {
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide()) return;

        attachOffset = clampToBox(position().subtract(target.position()), target);
        entityData.set(DATA_STUCK_ENTITY, target.getId());
        entityData.set(DATA_STUCK, true);
        playSound(SoundEvents.SLIME_BLOCK_HIT, 0.8F, 1.2F);
    }

    private void stickToBlock() {
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide()) return;

        entityData.set(DATA_STUCK_ENTITY, -1);
        entityData.set(DATA_STUCK, true);
        playSound(SoundEvents.SLIME_BLOCK_PLACE, 0.8F, 1.2F);
    }

    private void unstick() {
        attachOffset = null;
        entityData.set(DATA_STUCK, false);
        entityData.set(DATA_STUCK_ENTITY, -1);
    }

    private static Vec3 clampToBox(Vec3 offset, Entity target) {
        AABB box = target.getBoundingBox();
        double halfWidth = box.getXsize() / 2.0 + 0.1;
        return new Vec3(
                Mth.clamp(offset.x, -halfWidth, halfWidth),
                Mth.clamp(offset.y, 0.0, box.getYsize()),
                Mth.clamp(offset.z, -halfWidth, halfWidth));
    }

    private void tickSpin() {
        spinO = spin;
        if (isStuck() || isResting()) return;

        Vec3 velocity = getDeltaMovement();
        if (onGround() && velocity.horizontalDistance() < 0.1 && Math.abs(velocity.y) < 0.2) {
            settling = true;
        }
        if (settling || onGround()) {
            float target = 90.0F + 180.0F * Math.round((spin - 90.0F) / 180.0F);
            spin += Mth.clamp(target - spin, -SETTLE_SPEED, SETTLE_SPEED);
        } else {
            spin += Mth.clamp((float) velocity.length() * 40.0F, MIN_SPIN_SPEED, MAX_SPIN_SPEED);
        }
    }

    private void tickFuseParticles() {
        Vec3 tip = fuseTip();
        Vec3 from = lastFuseTip == null ? tip : lastFuseTip;
        int steps = Mth.clamp(Mth.ceil(from.distanceTo(tip) / TRAIL_SPACING), 1, MAX_TRAIL_STEPS);
        for (int i = 1; i <= steps; i++) {
            Vec3 at = from.lerp(tip, (double) i / steps);
            level().addParticle(ParticleTypes.SMOKE, at.x, at.y, at.z, 0.0, 0.02, 0.0);
        }
        if (random.nextFloat() < 0.3F) {
            level().addParticle(ParticleTypes.SMALL_FLAME, tip.x, tip.y, tip.z, 0.0, 0.01, 0.0);
        }
        lastFuseTip = tip;
    }

    private void tickSlimeDrip() {
        if (!getDynamiteType().sticky || !isStuck() || random.nextInt(SLIME_DRIP_CHANCE) != 0) return;
        double x = getX() + (random.nextDouble() - 0.5) * 0.2;
        double y = getY() + random.nextDouble() * 0.1;
        double z = getZ() + (random.nextDouble() - 0.5) * 0.2;
        level().addParticle(ParticleTypes.ITEM_SLIME, x, y, z, 0.0, -0.02, 0.0);
    }

    private Vec3 fuseTip() {
        double spinRad = Math.toRadians(spin);
        double yawRad = Math.toRadians(getYRot() - 90.0F);
        double side = -Math.sin(spinRad) * FUSE_TIP;
        double up = Math.cos(spinRad) * FUSE_TIP + restHeight(spin);
        return position().add(side * Math.cos(yawRad), up, -side * Math.sin(yawRad));
    }

    private void extinguish() {
        LitDynamiteItem.playDouse(level(), getX(), getY(), getZ());
        ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), new ItemStack(getDynamiteType().unlitItem()));
        drop.setDeltaMovement(getDeltaMovement().scale(0.5));
        level().addFreshEntity(drop);
        discard();
    }

    public float getSpin(float partialTick) {
        return Mth.lerp(partialTick, spinO, spin);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(getDynamiteType().litItem());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short) fuse);
        tag.putInt("Type", getDynamiteType().ordinal());
        tag.putBoolean("Stuck", isStuck() && entityData.get(DATA_STUCK_ENTITY) < 0);
        if (isResting()) {
            tag.putFloat("RestSpin", spin);
            if (restSupport != null) tag.putLong("RestSupport", restSupport.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFuse(tag.getShort("Fuse"));
        entityData.set(DATA_TYPE, tag.getInt("Type"));
        entityData.set(DATA_STUCK, tag.getBoolean("Stuck"));
        if (tag.contains("RestSpin")) {
            rest(getYRot(), tag.getFloat("RestSpin"), tag.contains("RestSupport") ? BlockPos.of(tag.getLong("RestSupport")) : null);
        }
    }
}