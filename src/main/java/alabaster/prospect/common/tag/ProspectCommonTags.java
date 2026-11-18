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

    public static final TagKey<Block> RUBY_STORAGE_BLOCKS = commonBlockTag("storage_blocks/ruby");
    public static final TagKey<Block> SAPPHIRE_STORAGE_BLOCKS = commonBlockTag("storage_blocks/sapphire");
    public static final TagKey<Block> TOPAZ_STORAGE_BLOCKS = commonBlockTag("storage_blocks/topaz");

    public static final TagKey<Item> RUBY_ORE_ITEMS = commonItemTag("ores/ruby");
    public static final TagKey<Item> SAPPHIRE_ORE_ITEMS = commonItemTag("ores/sapphire");
    public static final TagKey<Item> TOPAZ_ORE_ITEMS = commonItemTag("ores/topaz");

    public static final TagKey<Item> RUBY_STORAGE_BLOCK_ITEMS = commonItemTag("storage_blocks/ruby");
    public static final TagKey<Item> SAPPHIRE_STORAGE_BLOCK_ITEMS = commonItemTag("storage_blocks/sapphire");
    public static final TagKey<Item> TOPAZ_STORAGE_BLOCK_ITEMS = commonItemTag("storage_blocks/topaz");

    public static final TagKey<Item> RUBY_GEMS = commonItemTag("gems/ruby");
    public static final TagKey<Item> SAPPHIRE_GEMS = commonItemTag("gems/sapphire");
    public static final TagKey<Item> TOPAZ_GEMS = commonItemTag("gems/topaz");

    private static TagKey<Block> commonBlockTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> commonItemTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
