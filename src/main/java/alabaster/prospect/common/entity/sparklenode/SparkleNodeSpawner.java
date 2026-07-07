package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SparkleNodeSpawner {

    private static final int CHECK_RADIUS = 2;
    private static final float MIN_FLUID_FRACTION = 0.25f;
    private static final int AREA_CELLS = (CHECK_RADIUS * 2 + 1) * (CHECK_RADIUS * 2 + 1); // 25
    private static final int MAX_FLOOR_SEARCH_DEPTH = 32;

    public static boolean trySpawn(ServerLevel level, BlockPos pos) {
        boolean isLava = isLava(level, pos);
        if (isLava && level.dimension() != Level.NETHER) return false;
        if (!isLava && !isWater(level, pos)) return false;

        BlockPos above = pos.above();
        if (isSameFluid(level, above, isLava)) return false;

        BlockPos below = findFloor(level, pos, isLava);
        if (below == null) return false;
        BlockState groundState = level.getBlockState(below);
        if (!groundState.is(ProspectModTags.SPAWNS_SPARKLE_NODES)) return false;

        AABB exclusionBox = new AABB(
                pos.getX() - CHECK_RADIUS, pos.getY() - 1, pos.getZ() - CHECK_RADIUS,
                pos.getX() + CHECK_RADIUS + 1, pos.getY() + 2, pos.getZ() + CHECK_RADIUS + 1
        );
        List<SparkleNodeEntity> nearby = level.getEntitiesOfClass(SparkleNodeEntity.class, exclusionBox);
        if (!nearby.isEmpty()) return false;

        int fluidCount = 0;
        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                if (isSameFluid(level, pos.offset(dx, 0, dz), isLava)) {
                    fluidCount++;
                }
            }
        }
        if ((float) fluidCount / AREA_CELLS < MIN_FLUID_FRACTION) return false;

        SparkleNodeEntity node = ProspectModEntities.SPARKLE_NODE.get().create(level);
        if (node == null) return false;

        node.moveTo(pos.getX() + 0.5, pos.above().getY(), pos.getZ() + 0.5, 0f, 0f);
        node.setInLava(isLava);
        level.addFreshEntity(node);
        return true;
    }

    private static BlockPos findFloor(ServerLevel level, BlockPos surface, boolean lava) {
        BlockPos.MutableBlockPos cursor = surface.mutable();
        for (int i = 0; i < MAX_FLOOR_SEARCH_DEPTH; i++) {
            cursor.move(0, -1, 0);
            if (!isSameFluid(level, cursor, lava)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isSameFluid(ServerLevel level, BlockPos pos, boolean lava) {
        return lava ? isLava(level, pos) : isWater(level, pos);
    }

    private static boolean isWater(ServerLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER);
    }

    private static boolean isLava(ServerLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(Fluids.LAVA) || fluid.is(Fluids.FLOWING_LAVA);
    }
}