package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.crafting.GemSocketRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ProspectRecipeSerializers {
    private ProspectRecipeSerializers() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Prospect.MODID);

    public static final Supplier<RecipeSerializer<GemSocketRecipe>> GEM_SOCKET =
            RECIPE_SERIALIZERS.register("gem_socket", () -> GemSocketRecipe.Serializer.INSTANCE);
}