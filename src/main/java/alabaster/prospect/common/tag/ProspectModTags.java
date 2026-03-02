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

    private static TagKey<Block> modBlockTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path));
    }

    private static TagKey<Item> modItemTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path));
    }
}
