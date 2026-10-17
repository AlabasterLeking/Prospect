package alabaster.prospect.common.entity.dynamite;

import alabaster.prospect.common.item.LitDynamiteItem;
import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DynamiteEntity extends Projectile implements ItemSupplier {

    private static final float MIN_SPIN_SPEED = 8.0F;
    private static final float MAX_SPIN_SPEED = 30.0F;
    private static final float SETTLE_SPEED = 20.0F;

    private int fuse = LitDynamiteItem.FUSE_TICKS;
    private float spin;
    private float spinO;
    private boolean settling;

    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level level) {
        super(type, level);
    }

    public DynamiteEntity(Level level, LivingEntity owner, int fuse) {
        this(ProspectModEntities.DYNAMITE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.fuse = fuse;
    }

    public DynamiteEntity(Level level, double x, double y, double z, int fuse) {
        this(ProspectModEntities.DYNAMITE.get(), level);
        setPos(x, y, z);
        this.fuse = fuse;
    }

    public void shortenFuse(int ticks) {
        fuse = Math.min(fuse, ticks);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        super.tick();

        if (!onGround()) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() == HitResult.Type.ENTITY) {
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(-motion.x * 0.3, Math.max(motion.y, 0.0) * 0.3, -motion.z * 0.3);
            }
        }

        applyGravity();
        Vec3 motion = getDeltaMovement();
        move(MoverType.SELF, motion);

        Vec3 after = getDeltaMovement();
        if (horizontalCollision) {
            after = new Vec3(after.x == 0.0 ? -motion.x * 0.4 : after.x, after.y, after.z == 0.0 ? -motion.z * 0.4 : after.z);
        }
        after = after.scale(0.98);
        if (onGround()) {
            after = after.multiply(0.7, -0.5, 0.7);
        }
        setDeltaMovement(after);

        if (level().isClientSide()) {
            spinO = spin;
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
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.25, getZ(), 0.0, 0.02, 0.0);
            if (random.nextFloat() < 0.3F) {
                level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.25, getZ(), 0.0, 0.01, 0.0);
            }
        } else if (isInWater()) {
            extinguish();
        } else if (--fuse <= 0) {
            discard();
            LitDynamiteItem.explode(level(), this, getX(), getY(0.0625), getZ());
        }
    }

    private void extinguish() {
        LitDynamiteItem.playDouse(level(), getX(), getY(), getZ());
        ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), new ItemStack(ProspectModItems.DYNAMITE.get()));
        drop.setDeltaMovement(getDeltaMovement().scale(0.5));
        level().addFreshEntity(drop);
        discard();
    }

    public float getSpin(float partialTick) {
        return Mth.lerp(partialTick, spinO, spin);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ProspectModItems.LIT_DYNAMITE.get());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short) fuse);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getShort("Fuse");
    }
}