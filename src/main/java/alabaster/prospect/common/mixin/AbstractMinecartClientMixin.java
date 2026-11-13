package alabaster.prospect.common.mixin;

import alabaster.prospect.common.utilities.ClientJumpingMinecart;
import alabaster.prospect.common.utilities.RailCatch;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;

import static alabaster.prospect.common.utilities.MinecartJumpConstants.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartClientMixin extends Entity implements ClientJumpingMinecart {

    protected AbstractMinecartClientMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Unique private static final int MAX_DISPLAY_DELAY = 8;
    @Unique private static final double MIN_DELAY_SPEED = 0.02;
    @Unique private static final double RAIL_SNAP_TOLERANCE = 0.1;

    @Unique private boolean prospect$clientAirborne = false;
    @Unique private boolean prospect$awaitingLanding = false;
    @Unique private boolean prospect$draining = false;
    @Unique private Vec3 prospect$handoffPos = Vec3.ZERO;
    @Unique private Vec3 prospect$handoffVel = Vec3.ZERO;
    @Unique private int prospect$clientAirTicks = 0;
    @Unique private int prospect$landingWaitTicks = 0;
    @Unique private Vec3 prospect$simPos = Vec3.ZERO;
    @Unique private Vec3 prospect$simVel = Vec3.ZERO;
    @Unique private final ArrayDeque<Vec3> prospect$displayQueue = new ArrayDeque<>();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleClientJumpTick(CallbackInfo ci) {
        if (!level().isClientSide()) return;

        if (prospect$clientAirborne) {
            prospect$simulateAirborneTick();
        } else if (prospect$awaitingLanding) {
            prospect$extrapolateLandingTick();
        } else if (prospect$draining) {
            if (!prospect$drainTick()) return;
        } else {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "getPos(DDD)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void suppressRailSnapWhileAirborne(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        if (level().isClientSide() && (prospect$isClientAirborne() || prospect$isAboveFlatRail(x, y, z))) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    private boolean prospect$isAboveFlatRail(double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level().getBlockState(pos);
        if (!(state.getBlock() instanceof BaseRailBlock)) {
            pos = pos.below();
            state = level().getBlockState(pos);
        }
        if (!(state.getBlock() instanceof BaseRailBlock rail)) return false;
        if (rail.getRailDirection(state, level(), pos, (AbstractMinecart) (Object) this).isAscending()) return false;
        return y > pos.getY() + RAIL_REST_OFFSET + RAIL_SNAP_TOLERANCE;
    }

    @Override
    public void prospect$applyJumpState(boolean airborne, Vec3 pos, Vec3 vel) {
        if (airborne) {
            prospect$queueLaunchPath(position(), pos, vel);
            prospect$simPos = pos;
            prospect$simVel = vel;
            prospect$clientAirTicks = 0;
            prospect$clientAirborne = true;
            prospect$awaitingLanding = false;
            prospect$draining = false;
            setOnGround(false);
            ((AbstractMinecartLerpAccessor) (Object) this).setLerpSteps(0);
            return;
        }

        if (!prospect$clientAirborne && !prospect$awaitingLanding) return;

        if (prospect$clientAirborne) {
            prospect$displayQueue.add(prospect$simPos.lerp(pos, 0.5));
            prospect$displayQueue.add(pos);
            prospect$startDrain(pos, vel);
        } else {
            prospect$startDrain(prospect$simPos, vel);
        }
    }

    @Unique
    private void prospect$startDrain(Vec3 landed, Vec3 vel) {
        prospect$clientAirborne = false;
        prospect$awaitingLanding = false;
        prospect$draining = true;
        prospect$handoffPos = landed;
        prospect$handoffVel = vel;
    }

    @Unique
    private boolean prospect$drainTick() {
        Vec3 frame = prospect$displayQueue.poll();
        if (frame == null) {
            prospect$draining = false;
            prospect$handOffToServer(prospect$handoffPos, prospect$handoffVel);
            return false;
        }
        setPos(frame.x, frame.y, frame.z);
        return true;
    }

    @Override
    public boolean prospect$isClientAirborne() {
        return prospect$clientAirborne || prospect$awaitingLanding || prospect$draining;
    }

    @Unique
    private void prospect$queueLaunchPath(Vec3 display, Vec3 launchPos, Vec3 vel) {
        prospect$displayQueue.clear();

        double speed = vel.horizontalDistance();
        if (speed < MIN_DELAY_SPEED) return;

        Vec3 direction = new Vec3(vel.x / speed, 0.0, vel.z / speed);
        double behind = launchPos.subtract(display).dot(direction);
        int delay = Mth.clamp((int) Math.round(behind / speed), 0, MAX_DISPLAY_DELAY);
        for (int i = 1; i <= delay; i++) {
            prospect$displayQueue.add(display.lerp(launchPos, (double) i / delay));
        }
    }

    @Unique
    private void prospect$showNextFrame() {
        prospect$displayQueue.add(prospect$simPos);
        Vec3 display = prospect$displayQueue.poll();
        setPos(display.x, display.y, display.z);
    }

    @Unique
    private void prospect$simulateAirborneTick() {
        ((AbstractMinecartLerpAccessor) (Object) this).setOnRails(false);
        setPos(prospect$simPos.x, prospect$simPos.y, prospect$simPos.z);

        prospect$clientAirTicks++;

        Vec3 motion = prospect$simVel;
        if (!onGround()) {
            motion = motion.add(0.0, -GRAVITY, 0.0);
        }

        RailCatch.Landing landing = prospect$clientAirTicks > 1 ? RailCatch.tryCatch(level(), prospect$simPos, motion) : null;
        if (landing != null) {
            prospect$simPos = landing.position();
            prospect$simVel = landing.velocity();
            prospect$clientAirborne = false;
            prospect$awaitingLanding = true;
            prospect$landingWaitTicks = 0;
            prospect$showNextFrame();
            return;
        }

        motion = RailCatch.assist(level(), prospect$simPos, motion);
        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        Vec3 afterMove = getDeltaMovement();
        prospect$simVel = new Vec3(afterMove.x * AIR_DRAG, afterMove.y, afterMove.z * AIR_DRAG);
        prospect$simPos = position();

        if (onGround() && prospect$clientAirTicks > MIN_AIR_TICKS) {
            prospect$landClient();
        }

        prospect$showNextFrame();
    }

    @Unique
    private void prospect$landClient() {
        BlockPos pos = blockPosition();
        prospect$clientAirborne = false;
        prospect$awaitingLanding = true;
        prospect$landingWaitTicks = 0;

        for (int dy = 0; dy <= 1; dy++) {
            BlockPos railPos = pos.below(dy);
            if (level().getBlockState(railPos).getBlock() instanceof BaseRailBlock) {
                prospect$simPos = new Vec3(prospect$simPos.x, railPos.getY() + RAIL_REST_OFFSET, prospect$simPos.z);
                prospect$simVel = prospect$applyLandingSpeedFloor(prospect$simVel.multiply(1.0, 0.0, 1.0));
                return;
            }
        }
        prospect$simVel = prospect$simVel.multiply(0.5, 0.0, 0.5);
    }

    @Unique
    private void prospect$extrapolateLandingTick() {
        prospect$landingWaitTicks++;

        if (prospect$landingWaitTicks > LANDING_WAIT_TIMEOUT) {
            prospect$startDrain(prospect$simPos, prospect$simVel);
            prospect$drainTick();
            return;
        }

        setPos(prospect$simPos.x, prospect$simPos.y, prospect$simPos.z);
        setDeltaMovement(prospect$simVel);
        move(MoverType.SELF, prospect$simVel);
        prospect$simPos = new Vec3(getX(), prospect$simPos.y, getZ());

        prospect$showNextFrame();
    }

    @Unique
    private void prospect$handOffToServer(Vec3 landed, Vec3 vel) {
        prospect$displayQueue.clear();
        Vec3 target = landed.add(vel.x * LANDING_HANDOFF_TICKS, 0.0, vel.z * LANDING_HANDOFF_TICKS);

        AbstractMinecartLerpAccessor accessor = (AbstractMinecartLerpAccessor) (Object) this;
        accessor.setLerpX(target.x);
        accessor.setLerpY(target.y);
        accessor.setLerpZ(target.z);
        accessor.setLerpSteps(LANDING_HANDOFF_TICKS);
        setDeltaMovement(vel);
    }

    @Unique
    private Vec3 prospect$applyLandingSpeedFloor(Vec3 vel) {
        double speed = vel.horizontalDistance();
        if (speed > 1.0E-4 && speed < LANDING_SPEED_FLOOR) {
            double scale = LANDING_SPEED_FLOOR / speed;
            return new Vec3(vel.x * scale, vel.y, vel.z * scale);
        }
        return vel;
    }
}