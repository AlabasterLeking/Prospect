package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.registry.ProspectTrimMaterials;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ProspectItemTags extends ItemTagsProvider {

    public ProspectItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, blockTagProvider, Prospect.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        // Trim Materials
        tag(Tags.Items.GEMS)
                .add(ProspectModItems.RUBY.get())
                .add(ProspectModItems.SAPPHIRE.get())
                .add(ProspectModItems.TOPAZ.get());

        // Trim Materials
        tag(ItemTags.TRIM_MATERIALS)
                .add(ProspectModItems.RUBY.get())
                .add(ProspectModItems.SAPPHIRE.get())
                .add(ProspectModItems.TOPAZ.get());

        tag(ProspectModTags.MINING_HELMET_LIGHT)
                .add(ProspectModItems.MINING_HELMET.get());

        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ProspectModItems.STONE_PROSPECTING_PICKAXE.get())
                .add(ProspectModItems.IRON_PROSPECTING_PICKAXE.get())
                .add(ProspectModItems.GOLDEN_PROSPECTING_PICKAXE.get())
                .add(ProspectModItems.DIAMOND_PROSPECTING_PICKAXE.get())
                .add(ProspectModItems.NETHERITE_PROSPECTING_PICKAXE.get());

        this.registerModTags();
    }

    private void registerModTags() {

    }
}