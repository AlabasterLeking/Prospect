package alabaster.prospect.common.mixin;

import alabaster.prospect.common.network.MinecartJumpingPayload;
import alabaster.prospect.common.utilities.JumpingMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static alabaster.prospect.common.utilities.MinecartJumpConstants.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity implements JumpingMinecart {

    protected AbstractMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Unique private boolean prospect$airborne = false;
    @Unique private int prospect$airTicks = 0;
    @Unique private double prospect$lastX = Double.NaN;
    @Unique private double prospect$lastZ = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleCartTick(CallbackInfo ci) {
        if (level().isClientSide()) return;

        prospect$lastX = getX();
        prospect$lastZ = getZ();

        if (!prospect$airborne) return;

        prospect$applyAirborneTick();

        if (prospect$airborne) {
            ci.cancel();
        }
    }

    @Override
    public void prospect$startJump(int charge) {
        if (prospect$airborne || charge <= 0) return;
        if (!(getFirstPassenger() instanceof LivingEntity)) return;

        double strength = MIN_JUMP + (Math.min(charge, 100) / 100.0) * (MAX_JUMP - MIN_JUMP);
        double velX = Double.isNaN(prospect$lastX) ? 0 : getX() - prospect$lastX;
        double velZ = Double.isNaN(prospect$lastZ) ? 0 : getZ() - prospect$lastZ;

        setOnGround(false);
        setDeltaMovement(new Vec3(velX, strength, velZ));
        prospect$airborne = true;
        prospect$airTicks = 0;
        prospect$sync();

        level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_JUMP, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 8, 0.3, 0.1, 0.3, 0.02);
        }
    }

    @Unique
    private void prospect$applyAirborneTick() {
        ((AbstractMinecartLerpAccessor) (Object) this).setOnRails(false);

        prospect$airTicks++;

        Vec3 motion = getDeltaMovement();
        if (!onGround()) {
            motion = motion.add(0.0, -GRAVITY, 0.0);
        }

        setDeltaMovement(motion);
        move(MoverType.SELF, motion);

        Vec3 afterMove = getDeltaMovement();
        setDeltaMovement(new Vec3(afterMove.x * AIR_DRAG, afterMove.y, afterMove.z * AIR_DRAG));

        if (onGround() && prospect$airTicks > MIN_AIR_TICKS) {
            prospect$tryReattach();
        }
    }

    @Unique
    private void prospect$tryReattach() {
        BlockPos pos = blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos railPos = pos.below(dy);
            BlockState state = level().getBlockState(railPos);
            if (state.getBlock() instanceof BaseRailBlock) {
                setPos(getX(), railPos.getY() + RAIL_REST_OFFSET, getZ());
                for (Entity passenger : getPassengers()) {
                    positionRider(passenger, Entity::setPos);
                }
                Vec3 landingVel = getDeltaMovement().multiply(1.0, 0.0, 1.0);
                setDeltaMovement(prospect$applyLandingSpeedFloor(landingVel));
                prospect$airborne = false;
                prospect$sync();

                level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 6, 0.3, 0.05, 0.3, 0.01);
                }
                return;
            }
        }
        setDeltaMovement(getDeltaMovement().multiply(0.5, 0.0, 0.5));
        prospect$airborne = false;
        prospect$sync();
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

    @Unique
    private void prospect$sync() {
        Vec3 vel = getDeltaMovement();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                this, new MinecartJumpingPayload(getId(), prospect$airborne, getX(), getY(), getZ(), vel.x, vel.y, vel.z)
        );
    }
}