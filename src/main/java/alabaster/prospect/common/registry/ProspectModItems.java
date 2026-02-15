package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
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

    // Pans
    public static final Supplier<Item> COPPER_PAN = registerWithTab("copper_pan",
            () -> new Item(basicItem()));
    public static final Supplier<Item> IRON_PAN = registerWithTab("iron_pan",
            () -> new Item(basicItem()));
    public static final Supplier<Item> GOLD_PAN = registerWithTab("gold_pan",
            () -> new Item(basicItem()));
    public static final Supplier<Item> NETHERITE_PAN = registerWithTab("netherite_pan",
            () -> new Item(basicItem()));

    // Minerals and Ores
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
}