package alabaster.prospect;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    public static ModConfigSpec COMMON_CONFIG;
    private static final Map<String, ModConfigSpec.BooleanValue> ITEMS = new HashMap<>();

    public static ModConfigSpec.BooleanValue ENABLE_MINING_HELMET_OVERLAY;
    public static ModConfigSpec.BooleanValue ENABLE_GUNPOWDER_FUSE_PLACEMENT;
    public static ModConfigSpec.BooleanValue ENABLE_WANDERING_PROSPECTOR;
    public static ModConfigSpec.EnumValue<HelmetLightMode> MINING_HELMET_LIGHT_MODE;
    public static ModConfigSpec.BooleanValue ENABLE_PROSPECTOR_CAMPS;
    public static ModConfigSpec.DoubleValue PROSPECTOR_CAMP_CHANCE;
    public static ModConfigSpec.BooleanValue ENABLE_ABANDONED_MINES;
    public static ModConfigSpec.DoubleValue ABANDONED_MINE_CHANCE;
    public static ModConfigSpec.BooleanValue TORCHES_EXTINGUISH_IN_RAIN;
    public static final Map<String, ModConfigSpec.BooleanValue> CAMP_VARIANTS = new HashMap<>();
    public static final Map<String, ModConfigSpec.BooleanValue> MINE_VARIANTS = new HashMap<>();
    public static final Map<String, ModConfigSpec.DoubleValue> MINE_EXTENSION_CHANCES = new HashMap<>();
    public static final Map<String, ModConfigSpec.BooleanValue> MINE_ROOMS = new HashMap<>();
    public static ModConfigSpec.DoubleValue MINE_ZOMBIE_PROSPECTOR_CHANCE;
    public static ModConfigSpec.BooleanValue MINE_CAVE_INS;
    public static ModConfigSpec.BooleanValue MINE_THEMED_LOOT;
    public static final Map<String, Feature> FEATURES = new HashMap<>();
    public static ModConfigSpec.BooleanValue ZOMBIE_PROSPECTOR_SPAWNS;

    public record Feature(ModConfigSpec.BooleanValue enabled, ModConfigSpec.IntValue rarity) {
    }

    public static boolean campVariant(String variant) {
        ModConfigSpec.BooleanValue value = CAMP_VARIANTS.get(variant);
        return value == null || value.get();
    }

    public static boolean mineVariant(String variant) {
        ModConfigSpec.BooleanValue value = MINE_VARIANTS.get(variant);
        return value == null || value.get();
    }

    public static boolean mineRoom(String room) {
        ModConfigSpec.BooleanValue value = MINE_ROOMS.get(room);
        return value == null || value.get();
    }

    public static float extensionChance(String group, float fallback) {
        ModConfigSpec.DoubleValue value = MINE_EXTENSION_CHANCES.get(group);
        return value == null ? fallback : value.get().floatValue();
    }

    public enum HelmetLightMode {
        AUTO,
        BLOCKS,
        DYNAMIC_LIGHTS
    }

    public Config() {
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        ENABLE_MINING_HELMET_OVERLAY = COMMON_BUILDER
                .comment("Whether the vignette glow overlay is shown while wearing a lit mining helmet underground.")
                .define("enableMiningHelmetOverlay", true);

        ENABLE_GUNPOWDER_FUSE_PLACEMENT = COMMON_BUILDER
                .comment("Whether right-clicking gunpowder against a block places a gunpowder fuse.")
                .define("enableGunpowderFusePlacement", true);

        ENABLE_WANDERING_PROSPECTOR = COMMON_BUILDER
                .comment("Whether the Wandering Prospector periodically spawns near players in the Overworld.")
                .define("enableWanderingProspector", true);

        MINING_HELMET_LIGHT_MODE = COMMON_BUILDER
                .comment("How the mining helmet lights up its surroundings.",
                        "AUTO: use LambDynamicLights when it is installed, otherwise place invisible light blocks.",
                        "BLOCKS: always place invisible light blocks, which every player sees.",
                        "DYNAMIC_LIGHTS: never place light blocks; only players running LambDynamicLights see the light.")
                .defineEnum("miningHelmetLightMode", HelmetLightMode.AUTO);

        COMMON_BUILDER.push("lights");
        TORCHES_EXTINGUISH_IN_RAIN = COMMON_BUILDER
                .comment("Whether torches left out in the rain slowly go out, leaving Extinguished Torches that can be relit with flint and steel.")
                .define("torchesExtinguishInRain", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("structures");
        ENABLE_PROSPECTOR_CAMPS = COMMON_BUILDER
                .comment("Whether Prospector Camps generate in new chunks.")
                .define("enableProspectorCamps", true);
        PROSPECTOR_CAMP_CHANCE = COMMON_BUILDER
                .comment("Chance that each valid Prospector Camp spot actually gets a camp. Lower it to make camps rarer.",
                        "To make camps more common, lower the spacing in the prospector_camps structure set with a datapack.")
                .defineInRange("prospectorCampChance", 1.0, 0.0, 1.0);
        ENABLE_ABANDONED_MINES = COMMON_BUILDER
                .comment("Whether Abandoned Mines generate in new chunks.")
                .define("enableAbandonedMines", true);
        ABANDONED_MINE_CHANCE = COMMON_BUILDER
                .comment("Chance that each valid Abandoned Mine spot actually gets a mine. Lower it to make mines rarer.",
                        "To make mines more common, lower the spacing in the abandoned_mines structure set with a datapack.")
                .defineInRange("abandonedMineChance", 1.0, 0.0, 1.0);

        COMMON_BUILDER.comment("Which Prospector Camp variants can generate.").push("campVariants");
        bool(COMMON_BUILDER, CAMP_VARIANTS, "regular", "Camps in plains, meadows and forests.");
        bool(COMMON_BUILDER, CAMP_VARIANTS, "badlands", "Camps in badlands.");
        bool(COMMON_BUILDER, CAMP_VARIANTS, "savanna", "Camps in savannas.");
        bool(COMMON_BUILDER, CAMP_VARIANTS, "snowy", "Camps in snowy biomes.");
        bool(COMMON_BUILDER, CAMP_VARIANTS, "taiga", "Camps in taigas.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Which Abandoned Mine variants can generate. A disabled variant leaves its biomes without mines.").push("mineVariants");
        bool(COMMON_BUILDER, MINE_VARIANTS, "regular", "Timber-fronted mines in hills, meadows, forests, taigas and cherry groves.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "badlands", "Headframe mines in badlands.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "desert", "Sandstone-arch mines in deserts.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "fungal", "Giant-mushroom mines in dark forests.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "jungle", "Overgrown mines in jungles.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "mangrove", "Boardwalk mines in mangroves and swamps.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "quarry", "Open quarries on stony peaks and gravelly hills.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "savanna", "Hide-canopy mines in savannas.");
        bool(COMMON_BUILDER, MINE_VARIANTS, "snowy", "Windbreak mines in snowy slopes and peaks.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Extra rooms that can branch off an Abandoned Mine.").push("mineExtensions");
        chance(COMMON_BUILDER, "drift", 0.75, "Chance of a side drift off the upper tunnel.");
        chance(COMMON_BUILDER, "storeroom", 0.6, "Chance of a storeroom off the right of the cave.");
        chance(COMMON_BUILDER, "side_room", 0.65, "Chance of a side room off the left of the cave.");
        chance(COMMON_BUILDER, "lower_gallery", 0.5, "Chance of a ladder shaft down to a deep lower gallery.");
        bool(COMMON_BUILDER, MINE_ROOMS, "drift_ore", "Ore drift.");
        bool(COMMON_BUILDER, MINE_ROOMS, "drift_rest", "Miners' rest drift.");
        bool(COMMON_BUILDER, MINE_ROOMS, "drift_unstable", "Collapsing drift.");
        bool(COMMON_BUILDER, MINE_ROOMS, "magazine", "Powder magazine.");
        bool(COMMON_BUILDER, MINE_ROOMS, "loading_bay", "Loading bay.");
        bool(COMMON_BUILDER, MINE_ROOMS, "office", "Foreman's office.");
        bool(COMMON_BUILDER, MINE_ROOMS, "sump", "Flooded sump.");
        bool(COMMON_BUILDER, MINE_ROOMS, "spider_nest", "Spider nest.");
        bool(COMMON_BUILDER, MINE_ROOMS, "nether_portal", "Broken nether portal room overrun with netherrack.");
        bool(COMMON_BUILDER, MINE_ROOMS, "collapse", "Collapsed room at the back of the cave with a Zombie Prospector trapped in the rubble.");
        bool(COMMON_BUILDER, MINE_ROOMS, "lower_gallery", "Lower gallery.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Details inside Abandoned Mines.").push("mineDetails");
        MINE_ZOMBIE_PROSPECTOR_CHANCE = COMMON_BUILDER
                .comment("Chance of a collapsed room at the back of the cave with a Zombie Prospector trapped in the rubble.")
                .defineInRange("zombieProspectorChance", 0.5, 0.0, 1.0);
        MINE_EXTENSION_CHANCES.put("collapse", MINE_ZOMBIE_PROSPECTOR_CHANCE);
        MINE_CAVE_INS = COMMON_BUILDER
                .comment("Whether parts of mines are randomly blocked by cave-ins.")
                .define("caveIns", true);
        MINE_THEMED_LOOT = COMMON_BUILDER
                .comment("Whether mine chests add extra loot matching the ore the mine was dug for.")
                .define("themedLoot", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("features");
        feature(COMMON_BUILDER, "quartz_geodes", "quartzGeodes", "Quartz Geodes in the Nether.", 18,
                "One geode is attempted in this many chunks on average. Raise it to make geodes rarer.");
        feature(COMMON_BUILDER, "glowing_mushrooms", "glowingMushrooms", "glowing mushroom patches in Overworld caves.", 1,
                "Only one in this many patch attempts goes ahead. Raise it to make patches rarer.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("mobs");
        ZOMBIE_PROSPECTOR_SPAWNS = COMMON_BUILDER
                .comment("Whether Zombie Prospectors spawn naturally underground. The ones trapped in mines are set under structures.mineDetails.")
                .define("zombieProspectorSpawns", true);
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    private static void bool(ModConfigSpec.Builder builder, Map<String, ModConfigSpec.BooleanValue> map, String key, String comment) {
        map.put(key, builder.comment(comment).define(camel(key), true));
    }

    private static void chance(ModConfigSpec.Builder builder, String key, double value, String comment) {
        MINE_EXTENSION_CHANCES.put(key, builder.comment(comment).defineInRange(camel(key) + "Chance", value, 0.0, 1.0));
    }

    private static void feature(ModConfigSpec.Builder builder, String key, String name, String comment, int rarity, String rarityComment) {
        FEATURES.put(key, new Feature(builder.comment("Whether to generate " + comment).define(name, true),
                builder.comment(rarityComment).defineInRange(name + "Rarity", rarity, 1, 256)));
    }

    private static String camel(String key) {
        StringBuilder out = new StringBuilder();
        boolean upper = false;
        for (char c : key.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                out.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return out.toString();
    }
}