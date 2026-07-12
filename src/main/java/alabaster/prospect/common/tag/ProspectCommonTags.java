package alabaster.prospect.common.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ProspectCommonTags {

    public static final TagKey<Block> RUBY_ORES = commonBlockTag("ores/ruby");
    public static final TagKey<Block> SAPPHIRE_ORES = commonBlockTag("ores/sapphire");
    public static final TagKey<Block> TOPAZ_ORES = commonBlockTag("ores/topaz");

    private static TagKey<Block> commonBlockTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> commonItemTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
