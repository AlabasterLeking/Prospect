package alabaster.prospect.data.recipe;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

public class CraftingRecipes {
    public static void register(RecipeOutput output) {
        recipesTools(output);
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
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ProspectModItems.GOLD_PAN.get(), 1)
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
                .define('M', Items.NETHERITE_INGOT)
                .define('B', Items.BOWL)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .unlockedBy("has_netherite_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
                .save(output);
    }
}