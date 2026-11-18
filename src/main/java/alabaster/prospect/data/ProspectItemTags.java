package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.registry.ProspectTrimMaterials;
import alabaster.prospect.common.tag.ProspectCommonTags;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
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

        // Rails
        tag(ItemTags.RAILS).add(ProspectModItems.WOODEN_RAIL.get(), ProspectModItems.LAUNCH_RAIL.get(), ProspectModItems.PAYLOAD_RAIL.get());

        // Mushrooms
        tag(Tags.Items.MUSHROOMS).add(
                ProspectModItems.GLOWING_MUSHROOM_BLUE.get(),
                ProspectModItems.GLOWING_MUSHROOM_GREEN.get(),
                ProspectModItems.GLOWING_MUSHROOM_PURPLE.get());

        // Rock Candy
        tag(Tags.Items.FOODS_CANDY).add(ProspectModItems.ROCK_CANDY.get());

        // Gems
        tag(ProspectCommonTags.RUBY_GEMS).add(ProspectModItems.RUBY.get());
        tag(ProspectCommonTags.SAPPHIRE_GEMS).add(ProspectModItems.SAPPHIRE.get());
        tag(ProspectCommonTags.TOPAZ_GEMS).add(ProspectModItems.TOPAZ.get());
        tag(Tags.Items.GEMS).addTags(ProspectCommonTags.RUBY_GEMS, ProspectCommonTags.SAPPHIRE_GEMS, ProspectCommonTags.TOPAZ_GEMS);

        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ProspectModItems.RUBY.get())
                .add(ProspectModItems.SAPPHIRE.get())
                .add(ProspectModItems.TOPAZ.get());

        // Ores and Storage Blocks
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.Blocks.ORE_RATES_SINGULAR, Tags.Items.ORE_RATES_SINGULAR);
        copy(ProspectCommonTags.RUBY_ORES, ProspectCommonTags.RUBY_ORE_ITEMS);
        copy(ProspectCommonTags.SAPPHIRE_ORES, ProspectCommonTags.SAPPHIRE_ORE_ITEMS);
        copy(ProspectCommonTags.TOPAZ_ORES, ProspectCommonTags.TOPAZ_ORE_ITEMS);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        copy(ProspectCommonTags.RUBY_STORAGE_BLOCKS, ProspectCommonTags.RUBY_STORAGE_BLOCK_ITEMS);
        copy(ProspectCommonTags.SAPPHIRE_STORAGE_BLOCKS, ProspectCommonTags.SAPPHIRE_STORAGE_BLOCK_ITEMS);
        copy(ProspectCommonTags.TOPAZ_STORAGE_BLOCKS, ProspectCommonTags.TOPAZ_STORAGE_BLOCK_ITEMS);

        // Glass
        copy(Tags.Blocks.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS);
        copy(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);

        // Trim Materials
        tag(ItemTags.TRIM_MATERIALS)
                .add(ProspectModItems.RUBY.get())
                .add(ProspectModItems.SAPPHIRE.get())
                .add(ProspectModItems.TOPAZ.get());

        tag(ProspectModTags.MINING_HELMET_LIGHT)
                .add(ProspectModItems.MINING_HELMET.get());

        tag(ProspectModTags.ROPES)
                .add(Items.LEAD)
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "rope"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("supplementaries", "rope"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "ropes"));

        tag(ProspectModTags.CHAINS)
                .add(Items.CHAIN)
                .addTag(Tags.Items.CHAINS);

        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ProspectModItems.PROSPECTING_PICKAXE.get())
                .add(ProspectModItems.COPPER_PAN.get())
                .add(ProspectModItems.IRON_PAN.get())
                .add(ProspectModItems.GOLDEN_PAN.get())
                .add(ProspectModItems.NETHERITE_PAN.get());

        tag(ProspectModTags.ENCHANTABLE_PANS)
                .add(ProspectModItems.COPPER_PAN.get())
                .add(ProspectModItems.IRON_PAN.get())
                .add(ProspectModItems.GOLDEN_PAN.get())
                .add(ProspectModItems.NETHERITE_PAN.get());

        tag(ItemTags.DYEABLE)
                .add(ProspectModItems.MINING_HELMET.get());

        tag(ItemTags.PIGLIN_LOVED)
                .add(ProspectModItems.GOLDEN_PAN.get());

        tag(ProspectModTags.RUBY_ORES)
                .add(ProspectModItems.RUBY_ORE.get())
                .add(ProspectModItems.DEEPSLATE_RUBY_ORE.get());

        tag(ProspectModTags.SAPPHIRE_ORES)
                .add(ProspectModItems.SAPPHIRE_ORE.get())
                .add(ProspectModItems.DEEPSLATE_SAPPHIRE_ORE.get());

        tag(ProspectModTags.TOPAZ_ORES)
                .add(ProspectModItems.TOPAZ_ORE.get())
                .add(ProspectModItems.DEEPSLATE_TOPAZ_ORE.get());


        this.registerModTags();
    }

    private void registerModTags() {

    }
}