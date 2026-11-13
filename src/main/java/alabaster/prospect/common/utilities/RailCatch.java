package alabaster.prospect.common.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static alabaster.prospect.common.utilities.MinecartJumpConstants.LANDING_SPEED_FLOOR;
import static alabaster.prospect.common.utilities.MinecartJumpConstants.RAIL_REST_OFFSET;

public final class RailCatch {
    private RailCatch() {}

    private static final double ASSIST_HEIGHT = 1.5;
    private static final double ASSIST_STRENGTH = 0.35;

    public record Landing(Vec3 position, Vec3 velocity) {}

    private record RailHit(BlockPos pos, boolean northSouth) {
        double surface() {
            return pos.getY() + RAIL_REST_OFFSET;
        }

        double center() {
            return northSouth ? pos.getX() + 0.5 : pos.getZ() + 0.5;
        }
    }

    @Nullable
    public static Landing tryCatch(Level level, Vec3 pos, Vec3 motion) {
        if (motion.y >= 0.0) return null;

        RailHit rail = findStraightRail(level, pos, -motion.y + 1.0);
        if (rail == null || pos.y + motion.y > rail.surface()) return null;

        double t = Mth.clamp((pos.y - rail.surface()) / -motion.y, 0.0, 1.0);
        double along = rail.northSouth ? pos.z + motion.z * t : pos.x + motion.x * t;
        Vec3 landed = rail.northSouth
                ? new Vec3(rail.center(), rail.surface(), along)
                : new Vec3(along, rail.surface(), rail.center());

        double axisSpeed = rail.northSouth ? motion.z : motion.x;
        double speed = Math.abs(axisSpeed) < 1.0E-4 ? 0.0 : Math.copySign(Math.max(motion.horizontalDistance(), LANDING_SPEED_FLOOR), axisSpeed);
        Vec3 velocity = rail.northSouth ? new Vec3(0.0, 0.0, speed) : new Vec3(speed, 0.0, 0.0);

        return new Landing(landed, velocity);
    }

    public static Vec3 assist(Level level, Vec3 pos, Vec3 motion) {
        if (motion.y >= 0.0) return motion;

        RailHit rail = findStraightRail(level, pos, ASSIST_HEIGHT + 1.0);
        if (rail == null) return motion;

        double height = pos.y - rail.surface();
        if (height < 0.0 || height > ASSIST_HEIGHT) return motion;

        if (rail.northSouth) {
            return new Vec3(Mth.lerp(ASSIST_STRENGTH, motion.x, rail.center() - pos.x), motion.y, motion.z);
        }
        return new Vec3(motion.x, motion.y, Mth.lerp(ASSIST_STRENGTH, motion.z, rail.center() - pos.z));
    }

    @Nullable
    private static RailHit findStraightRail(Level level, Vec3 pos, double depth) {
        BlockPos start = BlockPos.containing(pos);
        int bottom = Mth.floor(pos.y - depth);
        for (int y = start.getY(); y >= bottom; y--) {
            BlockPos check = new BlockPos(start.getX(), y, start.getZ());
            BlockState state = level.getBlockState(check);
            if (state.getBlock() instanceof BaseRailBlock rail) {
                RailShape shape = rail.getRailDirection(state, level, check, null);
                if (shape == RailShape.NORTH_SOUTH) return new RailHit(check, true);
                if (shape == RailShape.EAST_WEST) return new RailHit(check, false);
                return null;
            }
            if (!state.getCollisionShape(level, check).isEmpty()) return null;
        }
        return null;
    }
}