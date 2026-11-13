package alabaster.prospect.common.utilities;

import net.minecraft.world.phys.Vec3;

public interface JumpingMinecart {
    void prospect$startJump(int charge);

    void prospect$queueLaunch(Vec3 horizontalVelocity, double strength);

    boolean prospect$isAirborne();

    void prospect$followLaunch(Vec3 position, Vec3 velocity);
}