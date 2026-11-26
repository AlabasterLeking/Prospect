package alabaster.prospect.data.recipe;

import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.tag.ProspectModTags;
import alabaster.prospect.data.loot.ProspectBlockLoot;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public class CraftingRecipes {
    public static void register(RecipeOutput output) {
        recipesBlocks(output);
        recipesTools(output);
        recipesMaterials(output);
    }

    private static void recipesBlocks(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.RUBY_BLOCK.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ProspectModItems.RUBY.get())
                .unlockedBy("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.RUBY.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.SAPPHIRE_BLOCK.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ProspectModItems.SAPPHIRE.get())
                .unlockedBy("has_sapphire", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.SAPPHIRE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.TOPAZ_BLOCK.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ProspectModItems.TOPAZ.get())
                .unlockedBy("has_topaz", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.TOPAZ.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ProspectModItems.TINTED_GLASS_PANE.get(), 16)
                .pattern("###")
                .pattern("###")
                .define('#', Items.TINTED_GLASS)
                .unlockedBy("has_tinted_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Items.TINTED_GLASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.SAPPHIRE_GLASS.get(), 8)
                .pattern("GGG")
                .pattern("GSG")
                .pattern("GGG")
                .define('G', Items.GLASS)
                .define('S', ProspectModItems.SAPPHIRE.get())
                .unlockedBy("has_sapphire", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.SAPPHIRE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ProspectModItems.SAPPHIRE_GLASS_PANE.get(), 16)
                .pattern("###")
                .pattern("###")
                .define('#', ProspectModItems.SAPPHIRE_GLASS.get())
                .unlockedBy("has_sapphire_glass", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.SAPPHIRE_GLASS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.CALCITE, 2)
                .pattern("BS")
                .pattern("SB")
                .define('B', Items.BONE_MEAL)
                .define('S', Items.STONE)
                .unlockedBy("has_bone_meal", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE_MEAL))
                .unlockedBy("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.TUFF, 2)
                .pattern("CA")
                .define('C', Items.COBBLESTONE)
                .define('A', Items.CALCITE)
                .unlockedBy("has_cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE))
                .unlockedBy("has_calcite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CALCITE))
                .save(output);
    }

    private static void recipesTools(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.MINING_HELMET.get(), 1)
                .pattern("III")
                .pattern("ILI")
                .pattern("S S")
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LANTERN)
                .define('S', Items.LEATHER)
                .unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.COPPER_PAN.get(), 1)
                .pattern("MBM")
                .pattern(" M ")
                .define('M', Items.COPPER_INGOT)
                .define('B', Items.BOWL)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .unlockedBy("has_copper_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.IRON_PAN.get(), 1)
                .pattern("MBM")
                .pattern(" M ")
                .define('M', Items.IRON_INGOT)
                .define('B', Items.BOWL)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.GOLDEN_PAN.get(), 1)
                .pattern("MBM")
                .pattern(" M ")
                .define('M', Items.GOLD_INGOT)
                .define('B', Items.BOWL)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .unlockedBy("has_gold_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.NETHERITE_PAN.get(), 1)
                .pattern("MBM")
                .pattern(" M ")
                .define('M', Items.NETHERITE_SCRAP)
                .define('B', ProspectModItems.GOLDEN_PAN.get())
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .unlockedBy("has_netherite_scrap", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_SCRAP))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.PROSPECTING_PICKAXE.get(), 1)
                .pattern("mm ")
                .pattern(" sm")
                .pattern(" s ")
                .define('m', Items.IRON_INGOT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
                .unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ProspectModItems.LAUNCH_RAIL.get(), 6)
                .pattern("I I")
                .pattern("IPI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('P', Items.PISTON)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_rail", InventoryChangeTrigger.TriggerInstance.hasItems(Items.RAIL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ProspectModItems.PAYLOAD_RAIL.get(), 6)
                .pattern("I I")
                .pattern("IPI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_rail", InventoryChangeTrigger.TriggerInstance.hasItems(Items.RAIL))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ProspectModItems.DYNAMITE.get(), 4)
                .requires(Items.TNT)
                .unlockedBy("has_tnt", InventoryChangeTrigger.TriggerInstance.hasItems(Items.TNT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ProspectModItems.DYNAMITE.get(), 1)
                .requires(Items.GUNPOWDER)
                .requires(Items.PAPER)
                .requires(Items.STRING)
                .unlockedBy("has_gunpowder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GUNPOWDER))
                .save(output, "prospect:dynamite_from_gunpowder");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ProspectModItems.STICKY_DYNAMITE.get(), 4)
                .requires(ProspectModItems.DYNAMITE.get(), 4)
                .requires(Items.SLIME_BALL)
                .unlockedBy("has_dynamite", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.DYNAMITE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.GRAPPLING_HOOK.get(), 1)
                .pattern(" II")
                .pattern(" RI")
                .pattern("R  ")
                .define('I', Items.IRON_INGOT)
                .define('R', ProspectModTags.ROPES)
                .unlockedBy("has_rope", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ProspectModTags.ROPES)))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.CRYSTALLINE_QUARTZ.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Items.QUARTZ)
                .unlockedBy("has_quartz", InventoryChangeTrigger.TriggerInstance.hasItems(Items.QUARTZ))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.QUARTZ, 9)
                .requires(ProspectModItems.CRYSTALLINE_QUARTZ.get())
                .unlockedBy("has_crystalline_quartz", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.CRYSTALLINE_QUARTZ.get()))
                .save(output, "prospect:quartz_from_crystalline_quartz");

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.ALTIMETER.get(), 1)
                .pattern(" C ")
                .pattern("CRC")
                .pattern(" C ")
                .define('C', Items.COPPER_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_copper_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.PORTABLE_SMELTER.get(), 1)
                .pattern(" R ")
                .pattern("IFI")
                .pattern("III")
                .define('R', ProspectModItems.RUBY.get())
                .define('I', Items.IRON_INGOT)
                .define('F', Items.FURNACE)
                .unlockedBy("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.RUBY.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ProspectModItems.DYNAMITE_BUNDLE.get(), 1)
                .requires(ProspectModItems.DYNAMITE.get(), 4)
                .requires(Items.STRING)
                .unlockedBy("has_dynamite", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.DYNAMITE.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ProspectModItems.DYNAMITE.get(), 4)
                .requires(ProspectModItems.DYNAMITE_BUNDLE.get())
                .unlockedBy("has_dynamite_bundle", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.DYNAMITE_BUNDLE.get()))
                .save(output, "prospect:dynamite_from_dynamite_bundle");
    }

    private static void recipesMaterials(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ProspectModItems.ROCK_CANDY.get(), 1)
                .pattern("  G")
                .pattern(" G ")
                .pattern("S  ")
                .define('G', Tags.Items.GEMS)
                .define('S', Items.STICK)
                .unlockedBy("has_gem", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Tags.Items.GEMS)))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ProspectModItems.WOODEN_RAIL.get(), 12)
                .pattern("S S")
                .pattern("SPS")
                .pattern("S S")
                .define('S', Items.STICK)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CYAN_DYE, 1)
                .requires(ProspectModItems.GLOWTHREAD.get())
                .group("cyan_dye")
                .unlockedBy("has_glowthread", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.GLOWTHREAD.get()))
                .save(output, "prospect:cyan_dye_from_glowthread");
        mushroomDye(output, ProspectModItems.GLOWING_MUSHROOM_BLUE.get(), Items.BLUE_DYE, "blue");
        mushroomDye(output, ProspectModItems.GLOWING_MUSHROOM_GREEN.get(), Items.GREEN_DYE, "green");
        mushroomDye(output, ProspectModItems.GLOWING_MUSHROOM_PURPLE.get(), Items.PURPLE_DYE, "purple");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STRING, 1)
                .requires(ProspectModItems.GLOWTHREAD.get(), 2)
                .unlockedBy("has_glowthread", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.GLOWTHREAD.get()))
                .save(output, "prospect:string_from_glowthread");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ProspectModItems.RUBY.get(), 9)
                .requires(ProspectModItems.RUBY_BLOCK.get())
                .unlockedBy("has_ruby_block", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.RUBY_BLOCK.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ProspectModItems.SAPPHIRE.get(), 9)
                .requires(ProspectModItems.SAPPHIRE_BLOCK.get())
                .unlockedBy("has_sapphire_block", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.SAPPHIRE_BLOCK.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ProspectModItems.TOPAZ.get(), 9)
                .requires(ProspectModItems.TOPAZ_BLOCK.get())
                .unlockedBy("has_topaz_block", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.TOPAZ_BLOCK.get()))
                .save(output);
    }

    private static void mushroomDye(RecipeOutput output, ItemLike mushroom, ItemLike dye, String color) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, dye, 1)
                .requires(mushroom)
                .group(color + "_dye")
                .unlockedBy("has_glowing_mushroom_" + color, InventoryChangeTrigger.TriggerInstance.hasItems(mushroom))
                .save(output, "prospect:" + color + "_dye_from_glowing_mushroom");
    }
}