package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.DynamiteItem;
import alabaster.prospect.common.item.DynamiteType;
import alabaster.prospect.common.item.GrapplingHookItem;
import alabaster.prospect.common.item.LitDynamiteItem;
import alabaster.prospect.common.item.trinket.AltimeterItem;
import alabaster.prospect.common.item.trinket.PortableSmelterItem;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.item.SlimeBucketItem;
import alabaster.prospect.common.item.ProspectingPickaxeItem;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
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
    public static final Supplier<Item> GRAPPLING_HOOK = registerWithTab("grappling_hook",
            () -> new GrapplingHookItem(basicItem().stacksTo(16)));

    // Explosives
    public static final Supplier<Item> DYNAMITE = registerWithTab("dynamite",
            () -> new DynamiteItem(basicItem().stacksTo(16), DynamiteType.STANDARD));
    public static final Supplier<Item> LIT_DYNAMITE = ITEMS.register("lit_dynamite",
            () -> new LitDynamiteItem(basicItem().stacksTo(1), DynamiteType.STANDARD));
    public static final Supplier<Item> STICKY_DYNAMITE = registerWithTab("sticky_dynamite",
            () -> new DynamiteItem(basicItem().stacksTo(16), DynamiteType.STICKY));
    public static final Supplier<Item> LIT_STICKY_DYNAMITE = ITEMS.register("lit_sticky_dynamite",
            () -> new LitDynamiteItem(basicItem().stacksTo(1), DynamiteType.STICKY));
    public static final Supplier<Item> DYNAMITE_BUNDLE = registerWithTab("dynamite_bundle",
            () -> new DynamiteItem(basicItem().stacksTo(8), DynamiteType.BUNDLE));
    public static final Supplier<Item> LIT_DYNAMITE_BUNDLE = ITEMS.register("lit_dynamite_bundle",
            () -> new LitDynamiteItem(basicItem().stacksTo(1), DynamiteType.BUNDLE));

    // Pans
    public static final Supplier<Item> COPPER_PAN = registerWithTab("copper_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability(32), false, Items.COPPER_INGOT, 0.0F, 10));
    public static final Supplier<Item> IRON_PAN = registerWithTab("iron_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability(64), false, Items.IRON_INGOT, 1.0F, 14));
    public static final Supplier<Item> GOLDEN_PAN = registerWithTab("golden_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability(16), false, Items.GOLD_INGOT, 3.0F, 22));
    public static final Supplier<Item> NETHERITE_PAN = registerWithTab("netherite_pan",
            () -> new PanItem(basicItem().stacksTo(1).durability(128).fireResistant(), true, Items.NETHERITE_INGOT, 2.0F, 15));

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

    public static final Supplier<Item> ROCK_CANDY = registerWithTab("rock_candy",
            () -> new Item(basicItem().food(rockCandyFood())));

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

    // Glass
    public static final Supplier<Item> TINTED_GLASS_PANE = registerWithTab("tinted_glass_pane",
            () -> new BlockItem(ProspectModBlocks.TINTED_GLASS_PANE.get(), basicItem()));
    public static final Supplier<Item> SAPPHIRE_GLASS = registerWithTab("sapphire_glass",
            () -> new BlockItem(ProspectModBlocks.SAPPHIRE_GLASS.get(), basicItem()));
    public static final Supplier<Item> SAPPHIRE_GLASS_PANE = registerWithTab("sapphire_glass_pane",
            () -> new BlockItem(ProspectModBlocks.SAPPHIRE_GLASS_PANE.get(), basicItem()));

    // Quartz Geodes
    public static final Supplier<Item> CRYSTALLINE_QUARTZ = registerWithTab("crystalline_quartz",
            () -> new BlockItem(ProspectModBlocks.CRYSTALLINE_QUARTZ.get(), basicItem()));
    public static final Supplier<Item> BUDDING_QUARTZ = registerWithTab("budding_quartz",
            () -> new BlockItem(ProspectModBlocks.BUDDING_QUARTZ.get(), basicItem()));
    public static final Supplier<Item> SMALL_QUARTZ_BUD = registerWithTab("small_quartz_bud",
            () -> new BlockItem(ProspectModBlocks.SMALL_QUARTZ_BUD.get(), basicItem()));
    public static final Supplier<Item> MEDIUM_QUARTZ_BUD = registerWithTab("medium_quartz_bud",
            () -> new BlockItem(ProspectModBlocks.MEDIUM_QUARTZ_BUD.get(), basicItem()));
    public static final Supplier<Item> LARGE_QUARTZ_BUD = registerWithTab("large_quartz_bud",
            () -> new BlockItem(ProspectModBlocks.LARGE_QUARTZ_BUD.get(), basicItem()));
    public static final Supplier<Item> QUARTZ_CLUSTER = registerWithTab("quartz_cluster",
            () -> new BlockItem(ProspectModBlocks.QUARTZ_CLUSTER.get(), basicItem()));

    // Glowing Mushrooms
    public static final Supplier<Item> GLOWING_MUSHROOM_BLUE = registerWithTab("glowing_mushroom_blue",
            () -> new StandingAndWallBlockItem(ProspectModBlocks.GLOWING_MUSHROOM_BLUE.get(), ProspectModBlocks.GLOWING_SHELF_MUSHROOM_BLUE.get(), basicItem().food(glowingMushroomFood()), Direction.DOWN));
    public static final Supplier<Item> GLOWING_MUSHROOM_GREEN = registerWithTab("glowing_mushroom_green",
            () -> new StandingAndWallBlockItem(ProspectModBlocks.GLOWING_MUSHROOM_GREEN.get(), ProspectModBlocks.GLOWING_SHELF_MUSHROOM_GREEN.get(), basicItem().food(glowingMushroomFood()), Direction.DOWN));
    public static final Supplier<Item> GLOWING_MUSHROOM_PURPLE = registerWithTab("glowing_mushroom_purple",
            () -> new StandingAndWallBlockItem(ProspectModBlocks.GLOWING_MUSHROOM_PURPLE.get(), ProspectModBlocks.GLOWING_SHELF_MUSHROOM_PURPLE.get(), basicItem().food(glowingMushroomFood()), Direction.DOWN));
    public static final Supplier<Item> GLOWING_MUSHROOM_BLOCK_BLUE = registerWithTab("glowing_mushroom_block_blue",
            () -> new BlockItem(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_BLUE.get(), basicItem()));
    public static final Supplier<Item> GLOWING_MUSHROOM_BLOCK_GREEN = registerWithTab("glowing_mushroom_block_green",
            () -> new BlockItem(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_GREEN.get(), basicItem()));
    public static final Supplier<Item> GLOWING_MUSHROOM_BLOCK_PURPLE = registerWithTab("glowing_mushroom_block_purple",
            () -> new BlockItem(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_PURPLE.get(), basicItem()));
    public static final Supplier<Item> GLOWTHREAD = registerWithTab("glowthread",
            () -> new BlockItem(ProspectModBlocks.GLOWTHREAD.get(), basicItem()));

    // Extinguished lights
    public static final Supplier<Item> EXTINGUISHED_TORCH = registerWithTab("extinguished_torch",
            () -> new StandingAndWallBlockItem(ProspectModBlocks.EXTINGUISHED_TORCH.get(), ProspectModBlocks.EXTINGUISHED_WALL_TORCH.get(), basicItem(), Direction.DOWN));
    public static final Supplier<Item> EXTINGUISHED_LANTERN = registerWithTab("extinguished_lantern",
            () -> new BlockItem(ProspectModBlocks.EXTINGUISHED_LANTERN.get(), basicItem()));

    // Rails
    public static final Supplier<Item> WOODEN_RAIL = registerWithTab("wooden_rail",
            () -> new BlockItem(ProspectModBlocks.WOODEN_RAIL.get(), basicItem()));
    public static final Supplier<Item> LAUNCH_RAIL = registerWithTab("launch_rail",
            () -> new BlockItem(ProspectModBlocks.LAUNCH_RAIL.get(), basicItem()));
    public static final Supplier<Item> PAYLOAD_RAIL = registerWithTab("payload_rail",
            () -> new BlockItem(ProspectModBlocks.PAYLOAD_RAIL.get(), basicItem()));

    // Spawn Eggs
    // Trinkets
    public static final Supplier<Item> SLIME_BUCKET = registerWithTab("slime_bucket",
            () -> new SlimeBucketItem(basicItem().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));
    public static final Supplier<Item> ALTIMETER = registerWithTab("altimeter",
            () -> new AltimeterItem(basicItem().stacksTo(1)));
    public static final Supplier<Item> PORTABLE_SMELTER = registerWithTab("portable_smelter",
            () -> new PortableSmelterItem(basicItem().stacksTo(1)));

    public static final Supplier<Item> WANDERING_PROSPECTOR_SPAWN_EGG = registerWithTab("wandering_prospector_spawn_egg",
            () -> new DeferredSpawnEggItem(ProspectModEntities.WANDERING_PROSPECTOR, 0x5A3E2B, 0xE0A040, basicItem()));
    public static final Supplier<Item> ZOMBIE_PROSPECTOR_SPAWN_EGG = registerWithTab("zombie_prospector_spawn_egg",
            () -> new DeferredSpawnEggItem(ProspectModEntities.ZOMBIE_PROSPECTOR, 0x4E7A37, 0x6F5233, basicItem()));

    private static FoodProperties rockCandyFood() {
        return new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.2F)
                .alwaysEdible()
                .fast()
                .usingConvertsTo(Items.STICK)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 2400), 1.0F)
                .effect(() -> new MobEffectInstance(ProspectMobEffects.GLIMMER, 1200), 1.0F)
                .build();
    }

    private static FoodProperties glowingMushroomFood() {
        return new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200), 1.0F)
                .build();
    }

}