package alabaster.prospect.common.mixin;

import alabaster.prospect.common.network.ProspectModNetworking;
import net.minecraft.client.Minecraft;
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

import static alabaster.prospect.common.utilities.MinecartJumpConstants.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartClientMixin extends Entity {

    protected AbstractMinecartClientMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Unique private boolean prospect$clientAirborne = false;
    @Unique private int prospect$clientJumpCharge = 0;
    @Unique private boolean prospect$clientWasJumping = false;
    @Unique private int prospect$clientAirTicks = 0;
    @Unique private double prospect$clientVelX = 0;
    @Unique private double prospect$clientVelY = 0;
    @Unique private double prospect$clientVelZ = 0;
    @Unique private double prospect$lastX = Double.NaN;
    @Unique private double prospect$lastZ = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleClientPredictedJump(CallbackInfo ci) {
        double observedVelX = Double.isNaN(prospect$lastX) ? 0 : getX() - prospect$lastX;
        double observedVelZ = Double.isNaN(prospect$lastZ) ? 0 : getZ() - prospect$lastZ;
        prospect$lastX = getX();
        prospect$lastZ = getZ();

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.player.getVehicle() != (Object) this) {
            return;
        }

        if (!prospect$clientAirborne) {
            boolean jumping = mc.options.keyJump.isDown();

            if (jumping) {
                prospect$clientJumpCharge = Math.min(prospect$clientJumpCharge + CHARGE_RATE, 100);
                prospect$clientWasJumping = true;
            } else if (prospect$clientWasJumping) {
                if (prospect$clientJumpCharge > 0) {
                    double strength = MIN_JUMP + (prospect$clientJumpCharge / 100.0) * (MAX_JUMP - MIN_JUMP);
                    prospect$clientVelX = observedVelX;
                    prospect$clientVelY = strength;
                    prospect$clientVelZ = observedVelZ;
                    prospect$clientAirborne = true;
                    prospect$clientAirTicks = 0;
                }
                prospect$clientJumpCharge = 0;
                prospect$clientWasJumping = false;
            }
        }

        if (!prospect$clientAirborne) return;

        AbstractMinecartLerpAccessor accessor = (AbstractMinecartLerpAccessor) (Object) this;
        accessor.setOnRails(false);

        if (accessor.getLerpSteps() > 0) {
            double correctedX = Mth.lerp(0.1, getX(), accessor.getLerpX());
            double correctedZ = Mth.lerp(0.1, getZ(), accessor.getLerpZ());
            setPos(correctedX, getY(), correctedZ);
            accessor.setLerpSteps(0);
        }

        prospect$clientAirTicks++;

        if (!onGround()) {
            prospect$clientVelY -= GRAVITY;
        }

        Vec3 motion = new Vec3(prospect$clientVelX, prospect$clientVelY, prospect$clientVelZ);
        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        prospect$clientVelX *= 0.997;
        prospect$clientVelZ *= 0.997;

        if (onGround() && prospect$clientAirTicks > MIN_AIR_TICKS) {
            prospect$tryReattachClient();
        }

        if (prospect$clientAirborne) {
            ci.cancel();
        }
    }

    @Inject(method = "getPos(DDD)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void suppressRailSnapWhileAirborne(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        if (prospect$clientAirborne || ProspectModNetworking.isRemoteJumping(getId())) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    private void prospect$tryReattachClient() {
        BlockPos pos = blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos railPos = pos.below(dy);
            BlockState state = level().getBlockState(railPos);
            if (state.getBlock() instanceof BaseRailBlock) {
                double landY = railPos.getY() + RAIL_REST_OFFSET;
                setPos(getX(), landY, getZ());
                for (Entity passenger : getPassengers()) {
                    positionRider(passenger, Entity::setPos);
                }

                Vec3 floored = prospect$applyLandingSpeedFloor(new Vec3(prospect$clientVelX, 0, prospect$clientVelZ));
                prospect$clientVelX = floored.x;
                prospect$clientVelZ = floored.z;

                AbstractMinecartLerpAccessor acc = (AbstractMinecartLerpAccessor)(Object)this;
                acc.setLerpX(getX() + prospect$clientVelX * 0.75);
                acc.setLerpY(landY);
                acc.setLerpZ(getZ() + prospect$clientVelZ * 0.75);
                acc.setLerpSteps(1);

                prospect$clientAirborne = false;
                prospect$clientVelY = 0;
                return;
            }
        }
        prospect$clientAirborne = false;
        prospect$clientVelX *= 0.5;
        prospect$clientVelZ *= 0.5;
        prospect$clientVelY = 0;
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