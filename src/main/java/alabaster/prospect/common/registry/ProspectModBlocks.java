package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.BuddingQuartzBlock;
import alabaster.prospect.common.block.ExtinguishedLanternBlock;
import alabaster.prospect.common.block.ExtinguishedTorchBlock;
import alabaster.prospect.common.block.ExtinguishedWallTorchBlock;
import alabaster.prospect.common.block.GlowColor;
import alabaster.prospect.common.block.GlowingMushroomBlock;
import alabaster.prospect.common.block.GlowingMushroomCapBlock;
import alabaster.prospect.common.block.GlowingShelfMushroomBlock;
import alabaster.prospect.common.block.GlowthreadBlock;
import alabaster.prospect.common.block.GlowthreadPlantBlock;
import alabaster.prospect.common.block.GunpowderFuseBlock;
import alabaster.prospect.common.block.HelmetLightBlock;
import alabaster.prospect.common.block.LaunchRailBlock;
import alabaster.prospect.common.block.WoodenRailBlock;
import alabaster.prospect.common.block.PayloadRailBlock;
import alabaster.prospect.common.block.PlacedDynamiteBlock;
import alabaster.prospect.common.block.PlacedDynamiteBundleBlock;
import alabaster.prospect.common.block.TintedGlassPaneBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProspectModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Prospect.MODID);

    // Ores and Minerals
    public static final Supplier<Block> RUBY_ORE = BLOCKS.register("ruby_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
    public static final Supplier<Block> SAPPHIRE_ORE = BLOCKS.register("sapphire_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
    public static final Supplier<Block> TOPAZ_ORE = BLOCKS.register("topaz_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));

    public static final Supplier<Block> DEEPSLATE_RUBY_ORE = BLOCKS.register("deepslate_ruby_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));
    public static final Supplier<Block> DEEPSLATE_SAPPHIRE_ORE = BLOCKS.register("deepslate_sapphire_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));
    public static final Supplier<Block> DEEPSLATE_TOPAZ_ORE = BLOCKS.register("deepslate_topaz_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));

    public static final Supplier<Block> RUBY_BLOCK = BLOCKS.register("ruby_block",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK)));
    public static final Supplier<Block> SAPPHIRE_BLOCK = BLOCKS.register("sapphire_block",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK)));
    public static final Supplier<Block> TOPAZ_BLOCK = BLOCKS.register("topaz_block",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK)));

    // Glass
    public static final Supplier<Block> TINTED_GLASS_PANE = BLOCKS.register("tinted_glass_pane",
            () -> new TintedGlassPaneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_GRAY)));
    public static final Supplier<Block> SAPPHIRE_GLASS = BLOCKS.register("sapphire_glass",
            () -> new TransparentBlock(sapphireGlassProperties(Blocks.GLASS)));
    public static final Supplier<Block> SAPPHIRE_GLASS_PANE = BLOCKS.register("sapphire_glass_pane",
            () -> new IronBarsBlock(sapphireGlassProperties(Blocks.GLASS_PANE)));

    // Quartz Geodes
    public static final Supplier<Block> CRYSTALLINE_QUARTZ = BLOCKS.register("crystalline_quartz",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).mapColor(MapColor.QUARTZ).sound(SoundType.NETHER_ORE)));
    public static final Supplier<Block> BUDDING_QUARTZ = BLOCKS.register("budding_quartz",
            () -> new BuddingQuartzBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST).mapColor(MapColor.QUARTZ).sound(SoundType.NETHER_ORE)));
    public static final Supplier<Block> SMALL_QUARTZ_BUD = quartzBud("small_quartz_bud", 3, 4, Blocks.SMALL_AMETHYST_BUD);
    public static final Supplier<Block> MEDIUM_QUARTZ_BUD = quartzBud("medium_quartz_bud", 4, 3, Blocks.MEDIUM_AMETHYST_BUD);
    public static final Supplier<Block> LARGE_QUARTZ_BUD = quartzBud("large_quartz_bud", 5, 3, Blocks.LARGE_AMETHYST_BUD);
    public static final Supplier<Block> QUARTZ_CLUSTER = quartzBud("quartz_cluster", 7, 3, Blocks.AMETHYST_CLUSTER);

    // Tools
    public static final Supplier<Block> GUNPOWDER_FUSE = BLOCKS.register("gunpowder_fuse",
            () -> new GunpowderFuseBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(0.1F)
                    .lightLevel(state -> GunpowderFuseBlock.isLit(state) ? 6 : 0)));

    // Rails
    public static final Supplier<Block> EXTINGUISHED_TORCH = BLOCKS.register("extinguished_torch",
            () -> new ExtinguishedTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH).lightLevel(state -> 0)));
    public static final Supplier<Block> EXTINGUISHED_WALL_TORCH = BLOCKS.register("extinguished_wall_torch",
            () -> new ExtinguishedWallTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH).lightLevel(state -> 0)));
    public static final Supplier<Block> EXTINGUISHED_LANTERN = BLOCKS.register("extinguished_lantern",
            () -> new ExtinguishedLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(state -> 0)));

    public static final Supplier<Block> PLACED_DYNAMITE = BLOCKS.register("placed_dynamite",
            () -> new PlacedDynamiteBlock(placedDynamiteProperties()));
    public static final Supplier<Block> PLACED_DYNAMITE_BUNDLE = BLOCKS.register("placed_dynamite_bundle",
            () -> new PlacedDynamiteBundleBlock(placedDynamiteProperties()));
    public static final Supplier<Block> WOODEN_RAIL = BLOCKS.register("wooden_rail",
            () -> new WoodenRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).mapColor(MapColor.WOOD).sound(SoundType.WOOD).ignitedByLava()));
    public static final Supplier<Block> LAUNCH_RAIL = BLOCKS.register("launch_rail",
            () -> new LaunchRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)));
    public static final Supplier<Block> PAYLOAD_RAIL = BLOCKS.register("payload_rail",
            () -> new PayloadRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL)));

    // Glowing Mushrooms
    public static final Supplier<Block> GLOWING_MUSHROOM_BLUE = glowingMushroom(GlowColor.BLUE);
    public static final Supplier<Block> GLOWING_MUSHROOM_GREEN = glowingMushroom(GlowColor.GREEN);
    public static final Supplier<Block> GLOWING_MUSHROOM_PURPLE = glowingMushroom(GlowColor.PURPLE);
    public static final Supplier<Block> GLOWING_MUSHROOM_BLOCK_BLUE = glowingMushroomBlock(GlowColor.BLUE);
    public static final Supplier<Block> GLOWING_MUSHROOM_BLOCK_GREEN = glowingMushroomBlock(GlowColor.GREEN);
    public static final Supplier<Block> GLOWING_MUSHROOM_BLOCK_PURPLE = glowingMushroomBlock(GlowColor.PURPLE);
    public static final Supplier<Block> GLOWING_SHELF_MUSHROOM_BLUE = glowingShelfMushroom(GlowColor.BLUE);
    public static final Supplier<Block> GLOWING_SHELF_MUSHROOM_GREEN = glowingShelfMushroom(GlowColor.GREEN);
    public static final Supplier<Block> GLOWING_SHELF_MUSHROOM_PURPLE = glowingShelfMushroom(GlowColor.PURPLE);
    public static final Supplier<Block> POTTED_GLOWING_MUSHROOM_BLUE = pottedGlowingMushroom(GlowColor.BLUE, GLOWING_MUSHROOM_BLUE);
    public static final Supplier<Block> POTTED_GLOWING_MUSHROOM_GREEN = pottedGlowingMushroom(GlowColor.GREEN, GLOWING_MUSHROOM_GREEN);
    public static final Supplier<Block> POTTED_GLOWING_MUSHROOM_PURPLE = pottedGlowingMushroom(GlowColor.PURPLE, GLOWING_MUSHROOM_PURPLE);

    public static final Supplier<Block> GLOWTHREAD = BLOCKS.register("glowthread",
            () -> new GlowthreadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WEEPING_VINES).mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.CAVE_VINES).lightLevel(state -> 10)));
    public static final Supplier<Block> GLOWTHREAD_PLANT = BLOCKS.register("glowthread_plant",
            () -> new GlowthreadPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WEEPING_VINES_PLANT).mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.CAVE_VINES).lightLevel(state -> 1)));

    // Technical
    public static final Supplier<Block> HELMET_LIGHT = BLOCKS.register("helmet_light",
            () -> new HelmetLightBlock(BlockBehaviour.Properties.of()
                    .replaceable()
                    .noCollission()
                    .noLootTable()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> HelmetLightBlock.LIGHT_LEVEL)));

    private static BlockBehaviour.Properties sapphireGlassProperties(Block template) {
        return BlockBehaviour.Properties.ofFullCopy(template).mapColor(MapColor.COLOR_BLUE).strength(1.5F, 1200.0F).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties placedDynamiteProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instabreak().sound(SoundType.GRASS).noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private static Supplier<Block> quartzBud(String name, int height, int offset, Block template) {
        return BLOCKS.register(name, () -> new AmethystClusterBlock(height, offset, BlockBehaviour.Properties.ofFullCopy(template).mapColor(MapColor.QUARTZ)));
    }

    private static Supplier<Block> glowingMushroom(GlowColor color) {
        return BLOCKS.register("glowing_mushroom_" + color.getSerializedName(),
                () -> new GlowingMushroomBlock(color, ProspectFeatures.hugeGlowingMushroom(color), BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
                        .lightLevel(state -> GlowingMushroomBlock.BASE_LIGHT + state.getValue(GlowingMushroomBlock.GLOW))));
    }

    private static Supplier<Block> glowingShelfMushroom(GlowColor color) {
        MapColor mapColor = switch (color) {
            case BLUE -> MapColor.COLOR_LIGHT_BLUE;
            case GREEN -> MapColor.COLOR_LIGHT_GREEN;
            case PURPLE -> MapColor.COLOR_PURPLE;
        };
        return BLOCKS.register("glowing_shelf_mushroom_" + color.getSerializedName(),
                () -> new GlowingShelfMushroomBlock(color, ProspectFeatures.hugeGlowingShelf(color), BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .noOcclusion()
                        .sound(SoundType.FUNGUS)
                        .pushReaction(PushReaction.DESTROY)
                        .lightLevel(state -> GlowingMushroomBlock.BASE_LIGHT + 1)));
    }

    private static Supplier<Block> pottedGlowingMushroom(GlowColor color, Supplier<Block> mushroom) {
        return BLOCKS.register("potted_glowing_mushroom_" + color.getSerializedName(),
                () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, mushroom, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_BROWN_MUSHROOM)
                        .lightLevel(state -> GlowingMushroomBlock.BASE_LIGHT + 1)));
    }

    public static void registerPottedPlants() {
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        pot.addPlant(BuiltInRegistries.BLOCK.getKey(GLOWING_MUSHROOM_BLUE.get()), POTTED_GLOWING_MUSHROOM_BLUE);
        pot.addPlant(BuiltInRegistries.BLOCK.getKey(GLOWING_MUSHROOM_GREEN.get()), POTTED_GLOWING_MUSHROOM_GREEN);
        pot.addPlant(BuiltInRegistries.BLOCK.getKey(GLOWING_MUSHROOM_PURPLE.get()), POTTED_GLOWING_MUSHROOM_PURPLE);
    }

    private static Supplier<Block> glowingMushroomBlock(GlowColor color) {
        return BLOCKS.register("glowing_mushroom_block_" + color.getSerializedName(),
                () -> new GlowingMushroomCapBlock(color, BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK).lightLevel(state -> 10)));
    }
}