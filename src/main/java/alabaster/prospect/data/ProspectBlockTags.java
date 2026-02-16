package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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

    }

    protected void registerMinecraftTags() {

    }

    protected void registerCommonTags() {

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
                ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get()
        );
    }
}
