package alabaster.prospect.common.mixin;

import alabaster.prospect.common.network.MinecartJumpingPayload;
import alabaster.prospect.common.utilities.JumpingMinecart;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import alabaster.prospect.common.registry.ProspectParticleTypes;
import alabaster.prospect.common.utilities.RailCatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
    @Unique private Vec3 prospect$pendingLaunchVelocity = null;
    @Unique private double prospect$pendingLaunchStrength = 0;
    @Unique private double prospect$fallSpeed = 0;

    @Unique private static final double CURVE_SPARK_SPEED = 0.3;
    @Unique private static final double HARD_LANDING_SPEED = 1.2;
    @Unique private static final double BRAKE_MIN_SPEED = 0.15;
    @Unique private static final double BRAKE_DECELERATION = 0.02;
    @Unique private static final double WHEEL_OFFSET = 0.4;
    @Unique private double prospect$lastSpeed = 0;
    @Unique private long prospect$lastTickTime = -1;
    @Unique private static final double MERGED_DISTANCE = 0.5;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void handleCartTick(CallbackInfo ci) {
        if (level().isClientSide()) return;

        prospect$lastTickTime = level().getGameTime();

        if (!prospect$airborne && !Double.isNaN(prospect$lastX)) {
            double moveX = getX() - prospect$lastX;
            double moveZ = getZ() - prospect$lastZ;
            double speed = Math.hypot(moveX, moveZ);
            prospect$curveSparks(speed, moveX, moveZ);
            prospect$brakeSparks(speed, moveX, moveZ);
            prospect$lastSpeed = speed;
        } else {
            prospect$lastSpeed = 0;
        }

        prospect$lastX = getX();
        prospect$lastZ = getZ();

        if (prospect$pendingLaunchVelocity != null) {
            if (!prospect$airborne) {
                prospect$launch(prospect$pendingLaunchVelocity.x, prospect$pendingLaunchVelocity.z, prospect$pendingLaunchStrength);
            }
            prospect$pendingLaunchVelocity = null;
        }

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

        prospect$launch(velX, velZ, strength);
        if (getFirstPassenger() instanceof ServerPlayer player) {
            ProspectAdvancements.award(player, ProspectAdvancements.CATCHING_AIR);
        }

        level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_JUMP, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 8, 0.3, 0.1, 0.3, 0.02);
        }
    }

    @Override
    public void prospect$queueLaunch(Vec3 horizontalVelocity, double strength) {
        if (prospect$airborne || prospect$pendingLaunchVelocity != null) return;
        prospect$pendingLaunchVelocity = horizontalVelocity;
        prospect$pendingLaunchStrength = strength;
    }

    @Override
    public boolean prospect$isAirborne() {
        return prospect$airborne;
    }

    @Unique
    private void prospect$launch(double velX, double velZ, double strength) {
        List<AbstractMinecart> merged = level().getEntitiesOfClass(AbstractMinecart.class, getBoundingBox(),
                cart -> cart != (Object) this && cart.distanceToSqr(this) < MERGED_DISTANCE * MERGED_DISTANCE);

        Vec3 velocity = new Vec3(velX, strength, velZ);
        prospect$startAirborne(position(), velocity);

        for (AbstractMinecart cart : merged) {
            if (cart instanceof JumpingMinecart partner && !partner.prospect$isAirborne()) {
                partner.prospect$followLaunch(position(), velocity);
            }
        }
    }

    @Override
    public void prospect$followLaunch(Vec3 position, Vec3 velocity) {
        if (prospect$airborne) return;
        prospect$startAirborne(position, velocity);
        if (prospect$lastTickTime == level().getGameTime()) {
            prospect$applyAirborneTick();
        }
    }

    @Unique
    private void prospect$startAirborne(Vec3 position, Vec3 velocity) {
        prospect$pendingLaunchVelocity = null;
        setPos(position.x, position.y, position.z);
        setOnGround(false);
        setDeltaMovement(velocity);
        prospect$airborne = true;
        prospect$airTicks = 0;
        prospect$sync();
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void skipAirbornePush(Entity entity, CallbackInfo ci) {
        if (level().isClientSide()) return;
        if (prospect$airborne || entity instanceof JumpingMinecart other && other.prospect$isAirborne()) {
            ci.cancel();
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

        prospect$fallSpeed = Math.max(0.0, -motion.y);

        RailCatch.Landing landing = prospect$airTicks > 1 ? RailCatch.tryCatch(level(), position(), motion) : null;
        if (landing != null) {
            prospect$finishRailLanding(landing.position(), landing.velocity());
            return;
        }

        motion = RailCatch.assist(level(), position(), motion);
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
                Vec3 landingVel = prospect$applyLandingSpeedFloor(getDeltaMovement().multiply(1.0, 0.0, 1.0));
                prospect$finishRailLanding(new Vec3(getX(), railPos.getY() + RAIL_REST_OFFSET, getZ()), landingVel);
                return;
            }
        }
        setDeltaMovement(getDeltaMovement().multiply(0.5, 0.0, 0.5));
        prospect$airborne = false;
        prospect$sync();
        prospect$landingEffects(false);
    }

    @Unique
    private void prospect$finishRailLanding(Vec3 position, Vec3 velocity) {
        setPos(position.x, position.y, position.z);
        for (Entity passenger : getPassengers()) {
            positionRider(passenger, Entity::setPos);
        }
        setDeltaMovement(velocity);
        prospect$airborne = false;
        prospect$sync();
        prospect$landingEffects(true);
    }

    @Unique
    private void prospect$landingEffects(boolean onRail) {
        float impact = (float) Mth.clamp(prospect$fallSpeed / HARD_LANDING_SPEED, 0.0, 1.0);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 0.25F + 0.6F * impact, 1.5F - 0.5F * impact);
        if (impact > 0.6F) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.NEUTRAL, 0.5F * impact, 1.2F);
        }

        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 4 + (int) (8 * impact), 0.3, 0.05, 0.3, 0.01);
        if (onRail) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 0.8F, 1.4F);
            Vec3 velocity = getDeltaMovement();
            prospect$wheelSparks(serverLevel, velocity.x, velocity.z, 4 + (int) (10 * impact), 0.12 + 0.12 * impact);
        }
    }

    @Unique
    private void prospect$brakeSparks(double speed, double moveX, double moveZ) {
        if (prospect$lastSpeed < BRAKE_MIN_SPEED || prospect$lastSpeed - speed < BRAKE_DECELERATION) return;
        if (!(level() instanceof ServerLevel serverLevel) || !prospect$isOnRail()) return;

        float strength = (float) Mth.clamp((prospect$lastSpeed - speed) / 0.15, 0.0, 1.0);
        prospect$wheelSparks(serverLevel, moveX, moveZ, 1 + (int) (4 * strength), 0.08 + 0.08 * strength);
        if (tickCount % 6 == 0) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.NEUTRAL, 0.2F + 0.2F * strength, 1.8F);
        }
    }

    @Unique
    private void prospect$wheelSparks(ServerLevel level, double dirX, double dirZ, int countPerSide, double spread) {
        double length = Math.hypot(dirX, dirZ);
        double sideX = length > 1.0E-4 ? -dirZ / length : 1.0;
        double sideZ = length > 1.0E-4 ? dirX / length : 0.0;
        for (int side = -1; side <= 1; side += 2) {
            double x = getX() + sideX * WHEEL_OFFSET * side;
            double z = getZ() + sideZ * WHEEL_OFFSET * side;
            level.sendParticles(ProspectParticleTypes.SPARK.get(), x, getY() + 0.05, z, countPerSide, 0.05, 0.0, 0.05, spread);
        }
    }

    @Unique
    private boolean prospect$isOnRail() {
        BlockPos pos = blockPosition();
        return level().getBlockState(pos).getBlock() instanceof BaseRailBlock
                || level().getBlockState(pos.below()).getBlock() instanceof BaseRailBlock;
    }

    @Unique
    private void prospect$curveSparks(double speed, double moveX, double moveZ) {
        if (speed < CURVE_SPARK_SPEED || !(level() instanceof ServerLevel serverLevel)) return;
        if (!prospect$isOnCurve()) return;

        if (random.nextInt(2) == 0) {
            prospect$wheelSparks(serverLevel, moveX, moveZ, 1 + random.nextInt(2), 0.1);
        }
        if (tickCount % 8 == 0) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.NEUTRAL, 0.15F, 1.9F);
        }
    }

    @Unique
    private boolean prospect$isOnCurve() {
        BlockPos pos = blockPosition();
        BlockState state = level().getBlockState(pos);
        if (!(state.getBlock() instanceof BaseRailBlock)) {
            pos = pos.below();
            state = level().getBlockState(pos);
        }
        if (!(state.getBlock() instanceof BaseRailBlock rail)) return false;

        RailShape shape = rail.getRailDirection(state, level(), pos, (AbstractMinecart) (Object) this);
        return shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST;
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