package alabaster.prospect.data.loot;

import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

public class ProspectEntityLoot extends EntityLootSubProvider {

    public ProspectEntityLoot(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(ProspectModEntities.ZOMBIE_PROSPECTOR.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0, 1)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.12F, 0.01F))
                        .add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(4))
                        .add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(3))
                        .add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(2))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.025F, 0.01F))
                        .add(LootItem.lootTableItem(ProspectModItems.RUBY.get()))
                        .add(LootItem.lootTableItem(ProspectModItems.SAPPHIRE.get()))
                        .add(LootItem.lootTableItem(ProspectModItems.TOPAZ.get()))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(ProspectModEntities.ZOMBIE_PROSPECTOR.get());
    }
}