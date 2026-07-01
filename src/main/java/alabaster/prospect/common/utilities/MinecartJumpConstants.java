package alabaster.prospect.common.utilities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class MinecartJumpConstants {
    private MinecartJumpConstants() {}

    public static final double MIN_JUMP = 0.35;
    public static final double MAX_JUMP = 0.85;
    public static final double GRAVITY = 0.15;
    public static final int CHARGE_RATE = 10;
    public static final double RAIL_REST_OFFSET = 0.0625;
    public static final double LANDING_SPEED_FLOOR = 0.1;
    public static final int MIN_AIR_TICKS = 3;

    public static final EntityDataAccessor<Boolean> DATA_JUMPING =
            SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
}