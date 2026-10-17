package alabaster.prospect.common.entity.ai;

import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class AvoidDynamiteGoal extends Goal {

    private static final double DETECT_RANGE = 8.0;

    private final PathfinderMob mob;
    private final double speed;
    @Nullable
    private final SoundEvent alarmSound;
    private Entity threat;
    private Path path;

    public AvoidDynamiteGoal(PathfinderMob mob, double speed, @Nullable SoundEvent alarmSound) {
        this.mob = mob;
        this.speed = speed;
        this.alarmSound = alarmSound;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        threat = mob.level().getEntities(mob, mob.getBoundingBox().inflate(DETECT_RANGE), AvoidDynamiteGoal::isLitDynamite)
                .stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr))
                .orElse(null);
        if (threat == null) return false;

        Vec3 away = DefaultRandomPos.getPosAway(mob, 16, 7, threat.position());
        if (away == null || threat.distanceToSqr(away) < threat.distanceToSqr(mob)) return false;

        path = mob.getNavigation().createPath(away.x, away.y, away.z, 0);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && threat != null && threat.isAlive();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(path, speed);
        if (alarmSound != null) {
            mob.playSound(alarmSound, 1.0F, mob.getVoicePitch());
        }
    }

    @Override
    public void stop() {
        threat = null;
        path = null;
    }

    private static boolean isLitDynamite(Entity entity) {
        return entity instanceof DynamiteEntity
                || entity instanceof ItemEntity item && item.getItem().is(ProspectModItems.LIT_DYNAMITE.get());
    }
}