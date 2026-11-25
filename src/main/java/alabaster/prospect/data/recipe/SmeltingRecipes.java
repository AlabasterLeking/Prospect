package alabaster.prospect.data.recipe;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SmeltingRecipes
{
    private static final float GEM_EXPERIENCE = 1.0F;
    private static final int SMELTING_TIME = 200;
    private static final int BLASTING_TIME = 100;
    private static final int BULK_BLASTING_TIME = 800;
    private static final float IRON_BLOCK_EXPERIENCE = 6.3F;
    private static final float COPPER_BLOCK_EXPERIENCE = 6.3F;
    private static final float GOLD_BLOCK_EXPERIENCE = 9.0F;

    public static void register(RecipeOutput output) {
        gemOre(output, ProspectModTags.RUBY_ORES, ProspectModItems.RUBY.get(), "ruby");
        gemOre(output, ProspectModTags.SAPPHIRE_ORES, ProspectModItems.SAPPHIRE.get(), "sapphire");
        gemOre(output, ProspectModTags.TOPAZ_ORES, ProspectModItems.TOPAZ.get(), "topaz");
        rawBlock(output, Items.RAW_IRON_BLOCK, Items.IRON_BLOCK, IRON_BLOCK_EXPERIENCE, "iron");
        rawBlock(output, Items.RAW_COPPER_BLOCK, Items.COPPER_BLOCK, COPPER_BLOCK_EXPERIENCE, "copper");
        rawBlock(output, Items.RAW_GOLD_BLOCK, Items.GOLD_BLOCK, GOLD_BLOCK_EXPERIENCE, "gold");
    }

    private static void rawBlock(RecipeOutput output, ItemLike rawBlock, ItemLike block, float experience, String name) {
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(rawBlock), RecipeCategory.MISC, block, experience, BULK_BLASTING_TIME)
                .group(name + "_block")
                .unlockedBy("has_raw_" + name + "_block", InventoryChangeTrigger.TriggerInstance.hasItems(rawBlock))
                .save(output, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name + "_block_from_blasting_raw_" + name + "_block"));
    }

    private static void gemOre(RecipeOutput output, TagKey<Item> ores, ItemLike gem, String name) {
        Ingredient ingredient = Ingredient.of(ores);
        String criterion = "has_" + name + "_ore";
        SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, gem, GEM_EXPERIENCE, SMELTING_TIME)
                .group(name)
                .unlockedBy(criterion, InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ores)))
                .save(output, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name + "_from_smelting_" + name + "_ore"));
        SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, gem, GEM_EXPERIENCE, BLASTING_TIME)
                .group(name)
                .unlockedBy(criterion, InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ores)))
                .save(output, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name + "_from_blasting_" + name + "_ore"));
    }
}