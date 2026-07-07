package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
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

        tag(ProspectModTags.RUBY_ORES).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get()
        );

        tag(ProspectModTags.SAPPHIRE_ORES).add(
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get()
        );

        tag(ProspectModTags.TOPAZ_ORES).add(
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );
    }

    protected void registerMinecraftTags() {
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
    }

    protected void registerCompatTags() {

    }

    protected void registerBlockMineables() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ProspectModBlocks.RUBY_ORE.get(),
                ProspectModBlocks.SAPPHIRE_ORE.get(),
                ProspectModBlocks.TOPAZ_ORE.get(),
                ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(),
                ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get(),
                ProspectModBlocks.RUBY_BLOCK.get(),
                ProspectModBlocks.TOPAZ_BLOCK.get()
        );
    }
}
