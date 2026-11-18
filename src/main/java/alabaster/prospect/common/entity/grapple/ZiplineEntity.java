package alabaster.prospect.common.entity.grapple;

import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ZiplineEntity extends Entity {

    public static final double HANG = 1.95;
    public static final double RIDE_REACH = 4.0;
    public static final double SEARCH_RANGE = 256.0;
    private static final int CHECK_INTERVAL = 20;

    private static final EntityDataAccessor<Vector3f> END = SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> HOOKED = SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Direction> FACE = SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> CHAIN = SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.BOOLEAN);

    private BlockPos fence = BlockPos.ZERO;
    private BlockPos anchor = BlockPos.ZERO;
    private ItemStack hook = ItemStack.EMPTY;

    public ZiplineEntity(EntityType<? extends ZiplineEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public ZiplineEntity(Level level, BlockPos fence, Vec3 start, Vec3 end, BlockPos anchor, ItemStack hook, boolean hooked, Direction face) {
        this(ProspectModEntities.ZIPLINE.get(), level);
        this.fence = fence;
        this.anchor = anchor;
        this.hook = hook;
        setPos(start);
        entityData.set(END, end.toVector3f());
        entityData.set(HOOKED, hooked);
        entityData.set(FACE, face);
        entityData.set(CHAIN, hook.is(ProspectModTags.CHAINS));
    }

    public static Vec3 fencePoint(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.575, pos.getZ() + 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(END, new Vector3f());
        builder.define(HOOKED, true);
        builder.define(FACE, Direction.UP);
        builder.define(CHAIN, false);
    }

    public Vec3 getStart() {
        return position();
    }

    public Vec3 getEnd() {
        return new Vec3(entityData.get(END));
    }

    public boolean isHooked() {
        return entityData.get(HOOKED);
    }

    public Direction getHookFace() {
        return entityData.get(FACE);
    }

    public boolean isChain() {
        return entityData.get(CHAIN);
    }

    public BlockPos getFence() {
        return fence;
    }

    public BlockPos getAnchor() {
        return anchor;
    }

    public Vec3 pointAt(double t) {
        return getStart().lerp(getEnd(), t);
    }

    public double closestT(Vec3 point) {
        Vec3 span = getEnd().subtract(getStart());
        double lengthSqr = span.lengthSqr();
        if (lengthSqr < 1.0E-6) return 0.0;
        return Mth.clamp(point.subtract(getStart()).dot(span) / lengthSqr, 0.0, 1.0);
    }

    public double distanceToLine(Vec3 point) {
        return pointAt(closestT(point)).distanceTo(point);
    }

    @Override
    public void tick() {
        if (level().isClientSide() || level().getGameTime() % CHECK_INTERVAL != 0) return;
        boolean fenceIntact = level().getBlockState(fence).is(BlockTags.FENCES);
        boolean anchorIntact = !level().getBlockState(anchor).getCollisionShape(level(), anchor).isEmpty();
        if (!fenceIntact || !anchorIntact) {
            breakLine();
        }
    }

    public void breakLine() {
        if (isRemoved()) return;
        if (!hook.isEmpty()) {
            spawnAtLocation(hook.copy());
        }
        level().playSound(null, getX(), getY(), getZ(), isChain() ? SoundEvents.CHAIN_BREAK : SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getStart(), getEnd()).inflate(0.5);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = (64.0 + getStart().distanceTo(getEnd())) * getViewScale();
        return distance < range * range;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        fence = BlockPos.of(tag.getLong("Fence"));
        anchor = BlockPos.of(tag.getLong("Anchor"));
        entityData.set(END, new Vector3f(tag.getFloat("EndX"), tag.getFloat("EndY"), tag.getFloat("EndZ")));
        entityData.set(HOOKED, !tag.contains("Hooked") || tag.getBoolean("Hooked"));
        entityData.set(FACE, tag.contains("Face") ? Direction.from3DDataValue(tag.getByte("Face")) : Direction.UP);
        hook = tag.contains("Hook") ? ItemStack.parseOptional(registryAccess(), tag.getCompound("Hook")) : ItemStack.EMPTY;
        entityData.set(CHAIN, hook.is(ProspectModTags.CHAINS));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("Fence", fence.asLong());
        tag.putLong("Anchor", anchor.asLong());
        Vector3f end = entityData.get(END);
        tag.putFloat("EndX", end.x());
        tag.putFloat("EndY", end.y());
        tag.putFloat("EndZ", end.z());
        tag.putBoolean("Hooked", isHooked());
        tag.putByte("Face", (byte) getHookFace().get3DDataValue());
        if (!hook.isEmpty()) {
            tag.put("Hook", hook.save(registryAccess()));
        }
    }
}
