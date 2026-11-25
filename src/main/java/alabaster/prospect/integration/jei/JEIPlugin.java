package alabaster.prospect.integration.jei;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.crafting.GemSocketRecipe;
import alabaster.prospect.common.prospecting.PanningTables;
import alabaster.prospect.common.registry.ProspectKeyBindings;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.tag.ProspectModTags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "jei_plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        int mostEntries = PanningTables.clientTables().stream().mapToInt(table -> table.entries().size()).max().orElse(1);
        registration.addRecipeCategories(new PanningCategory(registration.getJeiHelpers().getGuiHelper(), Mth.positiveCeilDiv(mostEntries, PanningCategory.COLUMNS)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ProspectModItems.COPPER_PAN.get()), PanningCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ProspectModItems.IRON_PAN.get()), PanningCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ProspectModItems.GOLDEN_PAN.get()), PanningCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ProspectModItems.NETHERITE_PAN.get()), PanningCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PanningCategory.TYPE, PanningTables.clientTables());
        registration.addIngredientInfo(new ItemStack(ProspectModItems.PROSPECTING_PICKAXE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.prospecting_pickaxe"));
        registration.addIngredientInfo(new ItemStack(Items.QUARTZ), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_quartz"));
        registration.addIngredientInfo(new ItemStack(Items.DIAMOND), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_diamond"));
        registration.addIngredientInfo(new ItemStack(Items.EMERALD), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_emerald"));
        registration.addIngredientInfo(new ItemStack(Items.LAPIS_LAZULI), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_lapis"));
        registration.addIngredientInfo(new ItemStack(Items.AMETHYST_SHARD), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_amethyst"));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.TOPAZ.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_topaz"));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.RUBY.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_ruby"));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.SAPPHIRE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.gem_sapphire"));

        List<ItemStack> lines = Stream.concat(BuiltInRegistries.ITEM.getTag(ProspectModTags.ROPES).stream().flatMap(set -> set.stream()), BuiltInRegistries.ITEM.getTag(ProspectModTags.CHAINS).stream().flatMap(set -> set.stream()))
                .distinct()
                .map(ItemStack::new)
                .toList();
        if (!lines.isEmpty()) {
            registration.addIngredientInfo(lines, VanillaTypes.ITEM_STACK, Component.translatable("jei.info.zipline"));
        }
        registration.addIngredientInfo(new ItemStack(ProspectModItems.GRAPPLING_HOOK.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.grappling_hook", ProspectKeyBindings.GRAPPLE_REEL_IN.getTranslatedKeyMessage(), ProspectKeyBindings.GRAPPLE_LET_OUT.getTranslatedKeyMessage()));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.SLIME_BUCKET.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.slime_bucket"));
        registration.addIngredientInfo(List.of(new ItemStack(ProspectModItems.DYNAMITE.get()), new ItemStack(ProspectModItems.STICKY_DYNAMITE.get()), new ItemStack(ProspectModItems.DYNAMITE_BUNDLE.get())), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.dynamite"));
        registration.addIngredientInfo(List.of(new ItemStack(ProspectModItems.COPPER_PAN.get()), new ItemStack(ProspectModItems.IRON_PAN.get()), new ItemStack(ProspectModItems.GOLDEN_PAN.get()), new ItemStack(ProspectModItems.NETHERITE_PAN.get())), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.pan"));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.ROCK_CANDY.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.rock_candy"));
        registration.addIngredientInfo(new ItemStack(ProspectModItems.ALTIMETER.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.info.altimeter"));

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            RecipeManager recipeManager = level.getRecipeManager();
            List<RecipeHolder<SmithingRecipe>> gemSocketHolders = recipeManager.getAllRecipesFor(RecipeType.SMITHING).stream()
                    .filter(holder -> holder.value() instanceof GemSocketRecipe)
                    .toList();
            registration.addRecipes(RecipeTypes.SMITHING, gemSocketHolders);
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}