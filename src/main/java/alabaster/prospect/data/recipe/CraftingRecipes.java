package alabaster.prospect.data.recipe;

import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.data.loot.ProspectBlockLoot;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
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
    }

    private static void recipesMaterials(RecipeOutput output) {
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
}