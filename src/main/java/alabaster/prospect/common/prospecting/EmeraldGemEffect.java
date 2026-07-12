package alabaster.prospect.common.prospecting;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class EmeraldGemEffect implements ProspectingGemEffect {

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(hit.oreTagId()));
        int veinSize = VeinFinder.findVein(ctx.level(), hit.pos(), tag).size();

        MutableComponent oreName = Component.translatable(hit.oreTranslationKey())
                .withStyle(Style.EMPTY.withColor(hit.color()));

        ctx.player().displayClientMessage(
                Component.translatable("tooltip.prospect.prospecting_pickaxe.vein_size", oreName, veinSize),
                true
        );
    }
}