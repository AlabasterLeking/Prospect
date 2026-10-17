package alabaster.prospect.common.utilities;

import net.minecraft.world.phys.Vec3;

public interface ClientJumpingMinecart {
    void prospect$applyJumpState(boolean airborne, Vec3 pos, Vec3 vel);

    boolean prospect$isClientAirborne();
}