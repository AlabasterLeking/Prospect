package alabaster.prospect.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static alabaster.prospect.common.utilities.MinecartJumpConstants.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity {

    protected AbstractMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Unique private boolean airborne = false;
    @Unique private int jumpCharge = 0;
    @Unique private boolean wasJumping = false;
    @Unique private int airTicks = 0;
    @Unique private double lastX = Double.NaN;
    @Unique private double lastZ = Double.NaN;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void addJumpingSyncedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_JUMPING, false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleCartTick(CallbackInfo ci) {
        double observedVelX = Double.isNaN(lastX) ? 0 : getX() - lastX;
        double observedVelZ = Double.isNaN(lastZ) ? 0 : getZ() - lastZ;
        lastX = getX();
        lastZ = getZ();

        if (!level().isClientSide()) {
            handleJumpInput(observedVelX, observedVelZ);
        }

        if (!airborne) return;

        applyAirborneTick();

        if (airborne) {
            ci.cancel();
        }
    }

    @Unique
    private void handleJumpInput(double observedVelX, double observedVelZ) {
        List<Entity> passengers = getPassengers();

        if (passengers.isEmpty() || !(passengers.get(0) instanceof LivingEntity rider)) {
            jumpCharge = 0;
            wasJumping = false;
            return;
        }

        if (airborne) return;

        boolean jumping = rider.jumping;

        if (jumping) {
            jumpCharge = Math.min(jumpCharge + CHARGE_RATE, 100);
            wasJumping = true;
        } else if (wasJumping) {
            if (jumpCharge > 0) {
                double strength = MIN_JUMP + (jumpCharge / 100.0) * (MAX_JUMP - MIN_JUMP);
                setDeltaMovement(new Vec3(observedVelX, strength, observedVelZ));
                setAirborne(true);
                airTicks = 0;

                level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_JUMP, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 8, 0.3, 0.1, 0.3, 0.02);
                }
            }
            jumpCharge = 0;
            wasJumping = false;
        }
    }

    @Unique
    private void applyAirborneTick() {
        ((AbstractMinecartLerpAccessor) (Object) this).setOnRails(false);

        airTicks++;

        Vec3 motion = getDeltaMovement();
        if (!onGround()) {
            motion = motion.add(0.0, -GRAVITY, 0.0);
        }

        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        Vec3 afterMove = getDeltaMovement();
        setDeltaMovement(new Vec3(afterMove.x * 0.997, afterMove.y, afterMove.z * 0.997));

        if (onGround() && airTicks > MIN_AIR_TICKS) {
            tryReattach();
        }
    }

    @Unique
    private void tryReattach() {
        BlockPos pos = blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos railPos = pos.below(dy);
            BlockState state = level().getBlockState(railPos);
            if (state.getBlock() instanceof BaseRailBlock) {
                setPos(getX(), railPos.getY() + RAIL_REST_OFFSET, getZ());
                for (Entity passenger : getPassengers()) {
                    positionRider(passenger, Entity::setPos);
                }
                setAirborne(false);
                Vec3 landingVel = getDeltaMovement().multiply(1.0, 0.0, 1.0);
                setDeltaMovement(applyLandingSpeedFloor(landingVel));

                level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 6, 0.3, 0.05, 0.3, 0.01);
                }
                return;
            }
        }
        setAirborne(false);
        setDeltaMovement(getDeltaMovement().multiply(0.5, 0.0, 0.5));
    }

    @Unique
    private Vec3 applyLandingSpeedFloor(Vec3 vel) {
        double speed = vel.horizontalDistance();
        if (speed > 1.0E-4 && speed < LANDING_SPEED_FLOOR) {
            double scale = LANDING_SPEED_FLOOR / speed;
            return new Vec3(vel.x * scale, vel.y, vel.z * scale);
        }
        return vel;
    }

    @Unique
    private void setAirborne(boolean value) {
        airborne = value;
        getEntityData().set(DATA_JUMPING, value);
    }
}