package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.tag.ProspectCommonTags;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ProspectBlockTags extends BlockTagsProvider {
    public ProspectBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Prospect.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.registerModTags();
        this.registerMinecraftTags();
        this.registerCommonTags();
        this.registerCompatTags();
        this.registerBlockMineables();
    }

    protected void registerModTags() {
        tag(ProspectModTags.SPAWNS_SPARKLE_NODES)
                .addTags(
                        BlockTags.SAND,
                        BlockTags.DIRT)
                .add(
                        Blocks.GRAVEL,
                        Blocks.NETHERRACK,
                        Blocks.BASALT,
                        Blocks.BLACKSTONE,
                        Blocks.SOUL_SAND,
                        Blocks.SOUL_SOIL);

        tag(ProspectModTags.RUBY_ORE_BLOCKS).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get()
        );

        tag(ProspectModTags.SAPPHIRE_ORE_BLOCKS).add(
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get()
        );

        tag(ProspectModTags.TOPAZ_ORE_BLOCKS).add(
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );
    }

    protected void registerMinecraftTags() {
        tag(BlockTags.IMPERMEABLE).add(ProspectModBlocks.SAPPHIRE_GLASS.get());

        tag(BlockTags.INCORRECT_FOR_GOLD_TOOL).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(BlockTags.INCORRECT_FOR_STONE_TOOL).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(BlockTags.INCORRECT_FOR_WOODEN_TOOL).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(BlockTags.NEEDS_IRON_TOOL).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );
    }

    protected void registerCommonTags() {
        tag(Tags.Blocks.GLASS_BLOCKS).add(ProspectModBlocks.SAPPHIRE_GLASS.get());
        tag(Tags.Blocks.GLASS_PANES).add(ProspectModBlocks.TINTED_GLASS_PANE.get(), ProspectModBlocks.SAPPHIRE_GLASS_PANE.get());

        tag(Tags.Blocks.ORES).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get()
        );

        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(Tags.Blocks.ORE_RATES_SINGULAR).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(ProspectCommonTags.RUBY_ORES).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get()
        );

        tag(ProspectCommonTags.SAPPHIRE_ORES).add(
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get()
        );

        tag(ProspectCommonTags.TOPAZ_ORES).add(
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );

        tag(ProspectCommonTags.RUBY_STORAGE_BLOCKS).add(ProspectModBlocks.RUBY_BLOCK.get());
        tag(ProspectCommonTags.SAPPHIRE_STORAGE_BLOCKS).add(ProspectModBlocks.SAPPHIRE_BLOCK.get());
        tag(ProspectCommonTags.TOPAZ_STORAGE_BLOCKS).add(ProspectModBlocks.TOPAZ_BLOCK.get());

        tag(Tags.Blocks.STORAGE_BLOCKS).addTags(
                ProspectCommonTags.RUBY_STORAGE_BLOCKS,
                ProspectCommonTags.SAPPHIRE_STORAGE_BLOCKS,
                ProspectCommonTags.TOPAZ_STORAGE_BLOCKS
        );

        tag(BlockTags.BEACON_BASE_BLOCKS).add(
                ProspectModBlocks.RUBY_BLOCK.get(),
                ProspectModBlocks.SAPPHIRE_BLOCK.get(),
                ProspectModBlocks.TOPAZ_BLOCK.get()
        );
    }

    protected void registerCompatTags() {

    }

    protected void registerBlockMineables() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ProspectModBlocks.EXTINGUISHED_LANTERN.get(),
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get(),
                ProspectModBlocks.RUBY_BLOCK.get(),
                ProspectModBlocks.SAPPHIRE_BLOCK.get(),
                ProspectModBlocks.TOPAZ_BLOCK.get(),
                ProspectModBlocks.CRYSTALLINE_QUARTZ.get(),
                ProspectModBlocks.BUDDING_QUARTZ.get(),
                ProspectModBlocks.SMALL_QUARTZ_BUD.get(),
                ProspectModBlocks.MEDIUM_QUARTZ_BUD.get(),
                ProspectModBlocks.LARGE_QUARTZ_BUD.get(),
                ProspectModBlocks.QUARTZ_CLUSTER.get(),
                ProspectModBlocks.LAUNCH_RAIL.get(),
                ProspectModBlocks.PAYLOAD_RAIL.get(),
                ProspectModBlocks.SAPPHIRE_GLASS.get(),
                ProspectModBlocks.SAPPHIRE_GLASS_PANE.get()
        );

        tag(BlockTags.MINEABLE_WITH_AXE).add(
                ProspectModBlocks.WOODEN_RAIL.get(),
                ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_BLUE.get(),
                ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_GREEN.get(),
                ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_PURPLE.get()
        );

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS).add(ProspectModBlocks.CRYSTALLINE_QUARTZ.get(), ProspectModBlocks.BUDDING_QUARTZ.get());

        tag(BlockTags.CLIMBABLE).add(ProspectModBlocks.GLOWTHREAD.get(), ProspectModBlocks.GLOWTHREAD_PLANT.get());

        tag(BlockTags.FLOWER_POTS).add(
                ProspectModBlocks.POTTED_GLOWING_MUSHROOM_BLUE.get(),
                ProspectModBlocks.POTTED_GLOWING_MUSHROOM_GREEN.get(),
                ProspectModBlocks.POTTED_GLOWING_MUSHROOM_PURPLE.get()
        );

        tag(BlockTags.RAILS).add(
                ProspectModBlocks.WOODEN_RAIL.get(),
                ProspectModBlocks.LAUNCH_RAIL.get(),
                ProspectModBlocks.PAYLOAD_RAIL.get()
        );
    }
}