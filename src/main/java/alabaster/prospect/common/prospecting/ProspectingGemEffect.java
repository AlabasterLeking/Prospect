package alabaster.prospect.common.prospecting;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface ProspectingGemEffect {


    default int modifyRadius(int radius, ProspectingContext ctx) {
        return radius;
    }


    default TagKey<Block> lockedOreTag(ItemStack stack) {
        return null;
    }

    default OreCandidate selectCandidate(List<OreCandidate> candidates, OreCandidate currentBest, ProspectingContext ctx) {
        return null;
    }

    default void onHitFound(OreCandidate hit, ProspectingContext ctx) {}

    default void onNoOreFound(ProspectingContext ctx) {}

    default List<Component> getTooltipLines(ItemStack stack) {
        return List.of();
    }
}