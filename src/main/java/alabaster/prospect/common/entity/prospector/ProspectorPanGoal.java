package alabaster.prospect.common.entity.prospector;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ProspectorPanGoal extends Goal {

    private static final int SEARCH_RADIUS = 8;
    private static final int PAN_DURATION = 120;
    private static final int GIVE_UP_TICKS = 400;

    private final WanderingProspector prospector;
    private BlockPos waterPos;
    private BlockPos standPos;
    private int panTicks;
    private int totalTicks;

    public ProspectorPanGoal(WanderingProspector prospector) {
        this.prospector = prospector;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (prospector.isTrading() || prospector.level().isNight()) return false;
        if (prospector.getRandom().nextInt(reducedTickDelay(400)) != 0) return false;
        return findWater();
    }

    @Override
    public boolean canContinueToUse() {
        return waterPos != null
                && panTicks < PAN_DURATION
                && totalTicks < GIVE_UP_TICKS
                && !prospector.isTrading()
                && prospector.level().getFluidState(waterPos).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        panTicks = 0;
        totalTicks = 0;
        moveToStand();
    }

    @Override
    public void stop() {
        prospector.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        prospector.getNavigation().stop();
        waterPos = null;
        standPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        totalTicks++;
        prospector.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);

        if (prospector.distanceToSqr(Vec3.atBottomCenterOf(standPos)) > 2.25) {
            if (prospector.getNavigation().isDone()) {
                moveToStand();
            }
            return;
        }

        if (panTicks == 0) {
            prospector.getNavigation().stop();
            prospector.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ProspectModItems.COPPER_PAN.get()));
        }

        panTicks++;

        if (panTicks % 10 == 0 && prospector.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, waterPos.getX() + 0.5, waterPos.getY() + 0.9, waterPos.getZ() + 0.5, 6, 0.25, 0.05, 0.25, 0.0);
            if (panTicks % 40 == 0) {
                prospector.playSound(SoundEvents.GENERIC_SPLASH, 0.3F, 1.2F);
            }
        }
    }

    private void moveToStand() {
        prospector.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, 0.35);
    }

    private boolean findWater() {
        Level level = prospector.level();
        BlockPos origin = prospector.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double best = Double.MAX_VALUE;
        BlockPos bestWater = null;
        BlockPos bestStand = null;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int dy = -3; dy <= 2; dy++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (!level.getFluidState(cursor).is(FluidTags.WATER)) continue;
                    if (!level.getBlockState(cursor.above()).isAir()) continue;

                    BlockPos stand = findStandPos(level, cursor);
                    if (stand == null) continue;

                    double dist = stand.distSqr(origin);
                    if (dist < best) {
                        best = dist;
                        bestWater = cursor.immutable();
                        bestStand = stand;
                    }
                }
            }
        }

        waterPos = bestWater;
        standPos = bestStand;
        return waterPos != null;
    }

    private static BlockPos findStandPos(Level level, BlockPos water) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = water.relative(dir);
            for (BlockPos stand : new BlockPos[]{side, side.above()}) {
                BlockPos below = stand.below();
                if (level.getBlockState(stand).isAir()
                        && level.getBlockState(stand.above()).isAir()
                        && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    return stand;
                }
            }
        }
        return null;
    }
}