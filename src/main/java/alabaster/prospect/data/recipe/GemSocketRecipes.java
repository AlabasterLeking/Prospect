package alabaster.prospect.data.recipe;

import alabaster.prospect.common.prospecting.GemSocketRecipeBuilder;
import alabaster.prospect.common.prospecting.ProspectingGems;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class GemSocketRecipes {

    public static void register(RecipeOutput output) {
        Ingredient basePickaxe = Ingredient.of(ProspectModItems.PROSPECTING_PICKAXE.get());

        socket(output, basePickaxe, Items.QUARTZ, ProspectingGems.QUARTZ);
        socket(output, basePickaxe, Items.DIAMOND, ProspectingGems.DIAMOND);
        socket(output, basePickaxe, Items.EMERALD, ProspectingGems.EMERALD);
        socket(output, basePickaxe, Items.LAPIS_LAZULI, ProspectingGems.LAPIS);
        socket(output, basePickaxe, Items.AMETHYST_SHARD, ProspectingGems.AMETHYST);
        socket(output, basePickaxe, ProspectModItems.TOPAZ.get(), ProspectingGems.TOPAZ);
        socket(output, basePickaxe, ProspectModItems.RUBY.get(), ProspectingGems.RUBY);
        socket(output, basePickaxe, ProspectModItems.SAPPHIRE.get(), ProspectingGems.SAPPHIRE);
    }

    private static void socket(RecipeOutput output, Ingredient base, ItemLike gemItem, ResourceLocation gemId) {
        GemSocketRecipeBuilder.socket(base, Ingredient.of(gemItem), gemId)
                .unlockedBy("has_gem", InventoryChangeTrigger.TriggerInstance.hasItems(gemItem))
                .save(output, gemId.withPrefix("gem_socket/"));
    }
}