package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SparkleNodeSpawner {

    private static final int CHECK_RADIUS = 2;
    private static final float MIN_WATER_FRACTION = 0.25f;
    private static final int AREA_CELLS = (CHECK_RADIUS * 2 + 1) * (CHECK_RADIUS * 2 + 1); // 25

    public static boolean trySpawn(ServerLevel level, BlockPos pos) {
        if (!isWater(level, pos)) return false;

        BlockPos above = pos.above();
        if (isWater(level, above)) return false;

        BlockPos below = pos.below();
        BlockState groundState = level.getBlockState(below);
        if (!groundState.is(ProspectModTags.SPAWNS_SPARKLE_NODES)) return false;

        AABB exclusionBox = new AABB(
                pos.getX() - CHECK_RADIUS, pos.getY() - 1, pos.getZ() - CHECK_RADIUS,
                pos.getX() + CHECK_RADIUS + 1, pos.getY() + 2, pos.getZ() + CHECK_RADIUS + 1
        );
        List<SparkleNodeEntity> nearby = level.getEntitiesOfClass(SparkleNodeEntity.class, exclusionBox);
        if (!nearby.isEmpty()) return false;

        int waterCount = 0;
        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                if (isWater(level, pos.offset(dx, 0, dz))) {
                    waterCount++;
                }
            }
        }
        if ((float) waterCount / AREA_CELLS < MIN_WATER_FRACTION) return false;

        SparkleNodeEntity node = ProspectModEntities.SPARKLE_NODE.get().create(level);
        if (node == null) return false;

        node.moveTo(pos.getX() + 0.5, pos.above().getY(), pos.getZ() + 0.5, 0f, 0f);
        level.addFreshEntity(node);
        return true;
    }

    private static boolean isWater(ServerLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER);
    }
}