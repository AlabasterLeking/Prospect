package alabaster.prospect.common.mixin;

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

    @Unique private boolean clientAirborne = false;
    @Unique private int clientJumpCharge = 0;
    @Unique private boolean clientWasJumping = false;
    @Unique private int clientAirTicks = 0;
    @Unique private double clientVelX = 0;
    @Unique private double clientVelY = 0;
    @Unique private double clientVelZ = 0;
    @Unique private double lastX = Double.NaN;
    @Unique private double lastZ = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleClientPredictedJump(CallbackInfo ci) {
        double observedVelX = Double.isNaN(lastX) ? 0 : getX() - lastX;
        double observedVelZ = Double.isNaN(lastZ) ? 0 : getZ() - lastZ;
        lastX = getX();
        lastZ = getZ();

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.player.getVehicle() != (Object) this) {
            return;
        }

        if (!clientAirborne) {
            boolean jumping = mc.options.keyJump.isDown();

            if (jumping) {
                clientJumpCharge = Math.min(clientJumpCharge + CHARGE_RATE, 100);
                clientWasJumping = true;
            } else if (clientWasJumping) {
                if (clientJumpCharge > 0) {
                    double strength = MIN_JUMP + (clientJumpCharge / 100.0) * (MAX_JUMP - MIN_JUMP);
                    clientVelX = observedVelX;
                    clientVelY = strength;
                    clientVelZ = observedVelZ;
                    clientAirborne = true;
                    clientAirTicks = 0;
                }
                clientJumpCharge = 0;
                clientWasJumping = false;
            }
        }

        if (!clientAirborne) return;

        AbstractMinecartLerpAccessor accessor = (AbstractMinecartLerpAccessor) (Object) this;
        accessor.setOnRails(false);

        if (accessor.getLerpSteps() > 0) {
            double correctedX = Mth.lerp(0.1, getX(), accessor.getLerpX());
            double correctedZ = Mth.lerp(0.1, getZ(), accessor.getLerpZ());
            setPos(correctedX, getY(), correctedZ);
            accessor.setLerpSteps(0);
        }

        clientAirTicks++;

        if (!onGround()) {
            clientVelY -= GRAVITY;
        }

        Vec3 motion = new Vec3(clientVelX, clientVelY, clientVelZ);
        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        clientVelX *= 0.997;
        clientVelZ *= 0.997;

        if (onGround() && clientAirTicks > MIN_AIR_TICKS) {
            tryReattachClient();
        }

        if (clientAirborne) {
            ci.cancel();
        }
    }

    @Inject(method = "getPos(DDD)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void suppressRailSnapWhileAirborne(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        if (clientAirborne || getEntityData().get(DATA_JUMPING)) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    private void tryReattachClient() {
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

                Vec3 floored = applyLandingSpeedFloor(new Vec3(clientVelX, 0, clientVelZ));
                clientVelX = floored.x;
                clientVelZ = floored.z;

                AbstractMinecartLerpAccessor acc = (AbstractMinecartLerpAccessor)(Object)this;
                acc.setLerpX(getX() + clientVelX * 0.75);
                acc.setLerpY(landY);
                acc.setLerpZ(getZ() + clientVelZ * 0.75);
                acc.setLerpSteps(1);

                clientAirborne = false;
                clientVelY = 0;
                return;
            }
        }
        clientAirborne = false;
        clientVelX *= 0.5;
        clientVelZ *= 0.5;
        clientVelY = 0;
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
}