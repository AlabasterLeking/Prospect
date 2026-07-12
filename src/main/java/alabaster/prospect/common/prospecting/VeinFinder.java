package alabaster.prospect.common.prospecting;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class VeinFinder {
    private VeinFinder() {}

    private static final int MAX_VEIN_SCAN = 64;

    public static Set<BlockPos> findVein(Level level, BlockPos start, TagKey<Block> tag) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() < MAX_VEIN_SCAN) {
            BlockPos pos = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = pos.offset(dx, dy, dz);
                        if (visited.contains(next)) continue;
                        BlockState state = level.getBlockState(next);
                        if (state.is(tag)) {
                            visited.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }

        return visited;
    }
}