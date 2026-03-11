package alabaster.prospect.data.recipe;

import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.data.loot.ProspectBlockLoot;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

public class CraftingRecipes {
    public static void register(RecipeOutput output) {
        recipesBlocks(output);
        recipesTools(output);
        recipesMaterials(output);
    }

    private static void recipesBlocks(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ProspectModItems.TOPAZ_BLOCK.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ProspectModItems.TOPAZ.get())
                .unlockedBy("has_topaz", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.TOPAZ.get()))
                .save(output);
    }

    private static void recipesTools(RecipeOutput output) {
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
    }

    private static void recipesMaterials(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ProspectModItems.TOPAZ.get(), 9)
                .requires(ProspectModItems.TOPAZ_BLOCK.get())
                .unlockedBy("has_topaz_block", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.TOPAZ_BLOCK.get()))
                .save(output);
    }
}