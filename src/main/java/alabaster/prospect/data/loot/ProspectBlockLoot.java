package alabaster.prospect.data.loot;

import alabaster.prospect.common.block.PlacedDynamiteBlock;
import alabaster.prospect.common.block.PlacedDynamiteBundleBlock;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.neoforged.neoforge.common.Tags;

import java.util.HashSet;
import java.util.Set;

public class ProspectBlockLoot extends BlockLootSubProvider {
    private static final float GLOWTHREAD_CHANCE = 0.33F;
    private static final float CLUSTER_DROPS = 4.0F;

    private final Set<Block> generatedLootTables = new HashSet<>();

    public ProspectBlockLoot(HolderLookup.Provider holder) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), holder);
    }

    @Override
    protected void generate() {
        dropOther(ProspectModBlocks.GUNPOWDER_FUSE.get(), Items.GUNPOWDER);
        dropSelf(ProspectModBlocks.RUBY_BLOCK.get());
        dropSelf(ProspectModBlocks.SAPPHIRE_BLOCK.get());
        dropSelf(ProspectModBlocks.TOPAZ_BLOCK.get());
        dropSelf(ProspectModBlocks.TINTED_GLASS_PANE.get());
        dropSelf(ProspectModBlocks.SAPPHIRE_GLASS.get());
        dropSelf(ProspectModBlocks.SAPPHIRE_GLASS_PANE.get());
        dropSelf(ProspectModBlocks.LAUNCH_RAIL.get());
        dropSelf(ProspectModBlocks.PAYLOAD_RAIL.get());
        dropSelf(ProspectModBlocks.WOODEN_RAIL.get());
        dropSelf(ProspectModBlocks.EXTINGUISHED_TORCH.get());
        dropOther(ProspectModBlocks.EXTINGUISHED_WALL_TORCH.get(), ProspectModItems.EXTINGUISHED_TORCH.get());
        dropSelf(ProspectModBlocks.EXTINGUISHED_LANTERN.get());
        dropSelf(ProspectModBlocks.CRYSTALLINE_QUARTZ.get());
        add(ProspectModBlocks.BUDDING_QUARTZ.get(), noDrop());
        dropWhenSilkTouch(ProspectModBlocks.SMALL_QUARTZ_BUD.get());
        dropWhenSilkTouch(ProspectModBlocks.MEDIUM_QUARTZ_BUD.get());
        dropWhenSilkTouch(ProspectModBlocks.LARGE_QUARTZ_BUD.get());
        add(ProspectModBlocks.QUARTZ_CLUSTER.get(), this::createClusterDrop);

        add(ProspectModBlocks.RUBY_ORE.get(), block -> createOreDrop(block, ProspectModItems.RUBY.get()));
        add(ProspectModBlocks.SAPPHIRE_ORE.get(), block -> createOreDrop(block, ProspectModItems.SAPPHIRE.get()));
        add(ProspectModBlocks.TOPAZ_ORE.get(), block -> createOreDrop(block, ProspectModItems.TOPAZ.get()));
        add(ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(), block -> createOreDrop(block, ProspectModItems.RUBY.get()));
        add(ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), block -> createOreDrop(block, ProspectModItems.SAPPHIRE.get()));
        add(ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get(), block -> createOreDrop(block, ProspectModItems.TOPAZ.get()));

        dropSelf(ProspectModBlocks.GLOWING_MUSHROOM_BLUE.get());
        dropSelf(ProspectModBlocks.GLOWING_MUSHROOM_GREEN.get());
        dropSelf(ProspectModBlocks.GLOWING_MUSHROOM_PURPLE.get());
        dropOther(ProspectModBlocks.GLOWING_SHELF_MUSHROOM_BLUE.get(), ProspectModItems.GLOWING_MUSHROOM_BLUE.get());
        dropOther(ProspectModBlocks.GLOWING_SHELF_MUSHROOM_GREEN.get(), ProspectModItems.GLOWING_MUSHROOM_GREEN.get());
        dropOther(ProspectModBlocks.GLOWING_SHELF_MUSHROOM_PURPLE.get(), ProspectModItems.GLOWING_MUSHROOM_PURPLE.get());
        dropPottedContents(ProspectModBlocks.POTTED_GLOWING_MUSHROOM_BLUE.get());
        dropPottedContents(ProspectModBlocks.POTTED_GLOWING_MUSHROOM_GREEN.get());
        dropPottedContents(ProspectModBlocks.POTTED_GLOWING_MUSHROOM_PURPLE.get());
        add(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_BLUE.get(), block -> createMushroomBlockDrop(block, ProspectModItems.GLOWING_MUSHROOM_BLUE.get()));
        add(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_GREEN.get(), block -> createMushroomBlockDrop(block, ProspectModItems.GLOWING_MUSHROOM_GREEN.get()));
        add(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_PURPLE.get(), block -> createMushroomBlockDrop(block, ProspectModItems.GLOWING_MUSHROOM_PURPLE.get()));

        add(ProspectModBlocks.PLACED_DYNAMITE.get(), block -> createCountDrop(block, ProspectModItems.DYNAMITE.get(), PlacedDynamiteBlock.STICKS));
        add(ProspectModBlocks.PLACED_DYNAMITE_BUNDLE.get(), block -> createCountDrop(block, ProspectModItems.DYNAMITE_BUNDLE.get(), PlacedDynamiteBundleBlock.BUNDLES));

        add(ProspectModBlocks.GLOWTHREAD.get(), block -> createGlowthreadDrop());
        add(ProspectModBlocks.GLOWTHREAD_PLANT.get(), block -> createGlowthreadDrop());
    }

    private LootTable.Builder createCountDrop(Block block, Item item, IntegerProperty count) {
        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(item);
        for (int amount : count.getPossibleValues()) {
            if (amount > 1) {
                entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(count, amount))));
            }
        }
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry));
    }

    private LootTable.Builder createClusterDrop(Block block) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(block, LootItem.lootTableItem(Items.QUARTZ)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(CLUSTER_DROPS)))
                .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
                .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                .otherwise((LootPoolEntryContainer.Builder<?>) applyExplosionDecay(block, LootItem.lootTableItem(Items.QUARTZ).apply(SetItemCountFunction.setCount(ConstantValue.exactly(CLUSTER_DROPS / 2))))));
    }

    private LootItemCondition.Builder shearsOrSilkTouch() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_SHEAR)).or(hasSilkTouch());
    }

    private LootTable.Builder createGlowthreadDrop() {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ProspectModItems.GLOWTHREAD.get()).when(shearsOrSilkTouch())
                        .otherwise(LootItem.lootTableItem(ProspectModItems.GLOWTHREAD.get()).when(LootItemRandomChanceCondition.randomChance(GLOWTHREAD_CHANCE)))));
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