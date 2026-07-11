package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.crafting.GemSocketRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;


public class GemSocketRecipeBuilder implements RecipeBuilder {

    private final Ingredient base;
    private final Ingredient gem;
    private final ResourceLocation gemId;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    public GemSocketRecipeBuilder(Ingredient base, Ingredient gem, ResourceLocation gemId) {
        this.base = base;
        this.gem = gem;
        this.gemId = gemId;
    }

    public static GemSocketRecipeBuilder socket(Ingredient base, Ingredient gem, ResourceLocation gemId) {
        return new GemSocketRecipeBuilder(base, gem, gemId);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return null;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        GemSocketRecipe recipe = new GemSocketRecipe(base, gem, gemId);
        AdvancementHolder advancementHolder = this.advancement
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(id.withPrefix("recipes/"));
        output.accept(id, recipe, advancementHolder);
    }
}