package alabaster.prospect.common.prospecting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class EmeraldGemEffect implements ProspectingGemEffect {

    private static final int MAX_VEIN_SCAN = 64;
    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(hit.oreTagId()));
        int veinSize = countVein(ctx.level(), hit.pos(), tag);

        ctx.player().displayClientMessage(
                Component.translatable("tooltip.prospect.prospecting_pickaxe.vein_size", veinSize),
                true
        );
    }

    private static int countVein(Level level, BlockPos start, TagKey<Block> tag) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() < MAX_VEIN_SCAN) {
            BlockPos pos = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (visited.contains(next)) continue;
                BlockState state = level.getBlockState(next);
                if (state.is(tag)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        return visited.size();
    }
}