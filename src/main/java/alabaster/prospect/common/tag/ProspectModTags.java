package alabaster.prospect.common.tag;

import alabaster.prospect.Prospect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ProspectModTags {

    public static final TagKey<Block> SPAWNS_SPARKLE_NODES = modBlockTag("spawns_sparkle_nodes");

    public static final TagKey<Item> RUBY_ORES = modItemTag("ruby_ores");
    public static final TagKey<Item> SAPPHIRE_ORES = modItemTag("sapphire_ores");
    public static final TagKey<Item> TOPAZ_ORES = modItemTag("topaz_ores");

    public static final TagKey<Block> RUBY_ORE_BLOCKS = modBlockTag("ruby_ores");
    public static final TagKey<Block> SAPPHIRE_ORE_BLOCKS = modBlockTag("sapphire_ores");
    public static final TagKey<Block> TOPAZ_ORE_BLOCKS = modBlockTag("topaz_ores");

    public static final TagKey<Item> MINING_HELMET_LIGHT = modItemTag("mining_helmet_light");

    public static final TagKey<Item> ENCHANTABLE_PANS = modItemTag("enchantable/pans");

    private static TagKey<Block> modBlockTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path));
    }

    private static TagKey<Item> modItemTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path));
    }
}