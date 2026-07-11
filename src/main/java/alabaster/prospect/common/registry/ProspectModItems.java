package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.item.ProspectingPickaxeItem;
import com.google.common.collect.Sets;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class ProspectModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Prospect.MODID);
    public static LinkedHashSet<Supplier<Item>> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    public static Supplier<Item> registerWithTab(String name, Supplier<Item> supplier) {
        Supplier<Item> item = ITEMS.register(name, supplier);
        CREATIVE_TAB_ITEMS.add(item);
        return item;
    }

    // Helper methods
    public static Item.Properties basicItem() {
        return (new Item.Properties());
    }

    // Misc Tools
    public static final Supplier<Item> MINING_HELMET = registerWithTab("mining_helmet",
            () -> new MiningHelmetItem(basicItem().stacksTo(1)));

    // Pans
    public static final Supplier<Item> COPPER_PAN = registerWithTab("copper_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability( 32), false));
    public static final Supplier<Item> IRON_PAN = registerWithTab("iron_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability( 64), false));
    public static final Supplier<Item> GOLDEN_PAN = registerWithTab("golden_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability( 16), false));
    public static final Supplier<Item> NETHERITE_PAN = registerWithTab("netherite_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability( 128), true));

    // Prospecting Pickaxe
    public static final Supplier<Item> PROSPECTING_PICKAXE = registerWithTab("prospecting_pickaxe",
            () -> new ProspectingPickaxeItem(ProspectingPickaxeItem.defaultProperties()));

    // Ores and Minerals
    public static final Supplier<Item> RUBY = registerWithTab("ruby",
            () -> new Item(basicItem()));
    public static final Supplier<Item> SAPPHIRE = registerWithTab("sapphire",
            () -> new Item(basicItem()));
    public static final Supplier<Item> TOPAZ = registerWithTab("topaz",
            () -> new Item(basicItem()));

    public static final Supplier<Item> RUBY_ORE = registerWithTab("ruby_ore",
            () -> new BlockItem(ProspectModBlocks.RUBY_ORE.get(), basicItem()));
    public static final Supplier<Item> SAPPHIRE_ORE = registerWithTab("sapphire_ore",
            () -> new BlockItem(ProspectModBlocks.SAPPHIRE_ORE.get(), basicItem()));
    public static final Supplier<Item> TOPAZ_ORE = registerWithTab("topaz_ore",
            () -> new BlockItem(ProspectModBlocks.TOPAZ_ORE.get(), basicItem()));

    public static final Supplier<Item> DEEPSLATE_RUBY_ORE = registerWithTab("deepslate_ruby_ore",
            () -> new BlockItem(ProspectModBlocks.DEEPSLATE_RUBY_ORE.get(), basicItem()));
    public static final Supplier<Item> DEEPSLATE_SAPPHIRE_ORE = registerWithTab("deepslate_sapphire_ore",
            () -> new BlockItem(ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), basicItem()));
    public static final Supplier<Item> DEEPSLATE_TOPAZ_ORE = registerWithTab("deepslate_topaz_ore",
            () -> new BlockItem(ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get(), basicItem()));

    public static final Supplier<Item> RUBY_BLOCK = registerWithTab("ruby_block",
            () -> new BlockItem(ProspectModBlocks.RUBY_BLOCK.get(), basicItem()));
    public static final Supplier<Item> SAPPHIRE_BLOCK = registerWithTab("sapphire_block",
            () -> new BlockItem(ProspectModBlocks.SAPPHIRE_BLOCK.get(), basicItem()));
    public static final Supplier<Item> TOPAZ_BLOCK = registerWithTab("topaz_block",
            () -> new BlockItem(ProspectModBlocks.TOPAZ_BLOCK.get(), basicItem()));
}