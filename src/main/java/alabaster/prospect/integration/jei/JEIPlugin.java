package alabaster.prospect.integration.jei;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.crafting.GemSocketRecipe;
import alabaster.prospect.common.registry.ProspectModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "jei_plugin");

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
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