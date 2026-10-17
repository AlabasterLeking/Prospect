package alabaster.prospect.common.mixin;

import alabaster.prospect.common.utilities.ClientJumpingMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static alabaster.prospect.common.utilities.MinecartJumpConstants.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartClientMixin extends Entity implements ClientJumpingMinecart {

    protected AbstractMinecartClientMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Unique private boolean prospect$clientAirborne = false;
    @Unique private boolean prospect$awaitingLanding = false;
    @Unique private int prospect$clientAirTicks = 0;
    @Unique private int prospect$landingWaitTicks = 0;
    @Unique private Vec3 prospect$simPos = Vec3.ZERO;
    @Unique private Vec3 prospect$simVel = Vec3.ZERO;
    @Unique private Vec3 prospect$renderOffset = Vec3.ZERO;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleClientJumpTick(CallbackInfo ci) {
        if (!level().isClientSide()) return;

        if (prospect$clientAirborne) {
            prospect$simulateAirborneTick();
        } else if (prospect$awaitingLanding) {
            prospect$extrapolateLandingTick();
        } else {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "getPos(DDD)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void suppressRailSnapWhileAirborne(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        if (level().isClientSide() && prospect$clientAirborne) {
            cir.setReturnValue(null);
        }
    }

    @Override
    public void prospect$applyJumpState(boolean airborne, Vec3 pos, Vec3 vel) {
        if (airborne) {
            prospect$renderOffset = position().subtract(pos);
            if (prospect$renderOffset.lengthSqr() > MAX_RENDER_OFFSET * MAX_RENDER_OFFSET) {
                prospect$renderOffset = Vec3.ZERO;
            }
            prospect$simPos = pos;
            prospect$simVel = vel;
            prospect$clientAirTicks = 0;
            prospect$clientAirborne = true;
            prospect$awaitingLanding = false;
            setOnGround(false);
            ((AbstractMinecartLerpAccessor) (Object) this).setLerpSteps(0);
            return;
        }

        if (!prospect$clientAirborne && !prospect$awaitingLanding) return;

        Vec3 from = prospect$awaitingLanding ? prospect$simPos : pos;
        prospect$clientAirborne = false;
        prospect$awaitingLanding = false;
        prospect$handOffToServer(from, vel);
    }

    @Override
    public boolean prospect$isClientAirborne() {
        return prospect$clientAirborne || prospect$awaitingLanding;
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

        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        Vec3 afterMove = getDeltaMovement();
        prospect$simVel = new Vec3(afterMove.x * AIR_DRAG, afterMove.y, afterMove.z * AIR_DRAG);
        prospect$simPos = position();

        if (onGround() && prospect$clientAirTicks > MIN_AIR_TICKS) {
            prospect$landClient();
        }

        prospect$applyRenderOffset();
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
            prospect$awaitingLanding = false;
            prospect$handOffToServer(prospect$simPos, prospect$simVel);
            return;
        }

        setPos(prospect$simPos.x, prospect$simPos.y, prospect$simPos.z);
        setDeltaMovement(prospect$simVel);
        move(MoverType.SELF, prospect$simVel);
        prospect$simPos = new Vec3(getX(), prospect$simPos.y, getZ());

        prospect$applyRenderOffset();
    }

    @Unique
    private void prospect$applyRenderOffset() {
        prospect$renderOffset = prospect$renderOffset.scale(RENDER_OFFSET_DECAY);
        if (prospect$renderOffset.lengthSqr() < 1.0E-6) {
            prospect$renderOffset = Vec3.ZERO;
        }
        Vec3 display = prospect$simPos.add(prospect$renderOffset);
        setPos(display.x, display.y, display.z);
    }

    @Unique
    private void prospect$handOffToServer(Vec3 pos, Vec3 vel) {
        AbstractMinecartLerpAccessor accessor = (AbstractMinecartLerpAccessor) (Object) this;
        accessor.setLerpX(pos.x + vel.x * LANDING_HANDOFF_TICKS);
        accessor.setLerpY(pos.y);
        accessor.setLerpZ(pos.z + vel.z * LANDING_HANDOFF_TICKS);
        accessor.setLerpSteps(LANDING_HANDOFF_TICKS);
        setDeltaMovement(vel);
        prospect$renderOffset = Vec3.ZERO;
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