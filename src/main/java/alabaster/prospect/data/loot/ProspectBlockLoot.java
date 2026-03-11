package alabaster.prospect.data.loot;

import alabaster.prospect.common.registry.ProspectModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class ProspectBlockLoot extends BlockLootSubProvider {
    private final Set<Block> generatedLootTables = new HashSet<>();

    public ProspectBlockLoot(HolderLookup.Provider holder) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), holder);
    }

    @Override
    protected void generate() {
        dropOther(ProspectModBlocks.GUNPOWDER_FUSE.get(), Items.GUNPOWDER);
        dropSelf(ProspectModBlocks.TOPAZ_BLOCK.get());
    }

    @Override
    protected void add(Block block, LootTable.Builder builder) {
        this.generatedLootTables.add(block);
        this.map.put(block.getLootTable(), builder);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return generatedLootTables;
    }
}
