package alabaster.prospect.common.crafting;

import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.registry.ProspectDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class GemSocketRecipe implements SmithingRecipe {

    private final Ingredient base;
    private final Ingredient gem;
    private final ResourceLocation gemId;

    public GemSocketRecipe(Ingredient base, Ingredient gem, ResourceLocation gemId) {
        this.base = base;
        this.gem = gem;
        this.gemId = gemId;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!input.template().isEmpty()) return false;
        if (!base.test(input.base()) || !gem.test(input.addition())) return false;
        ProspectingSockets sockets = input.base().getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        return !sockets.has(gemId);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack result = input.base().copy();
        ProspectingSockets sockets = result.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        result.set(ProspectDataComponents.PROSPECTING_SOCKETS.get(), sockets.with(gemId));
        return result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return gem.test(stack);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    public static class Serializer implements RecipeSerializer<GemSocketRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<GemSocketRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.gem),
                ResourceLocation.CODEC.fieldOf("gem_id").forGetter(r -> r.gemId)
        ).apply(inst, GemSocketRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GemSocketRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.gem,
                ResourceLocation.STREAM_CODEC, r -> r.gemId,
                GemSocketRecipe::new
        );

        @Override
        public MapCodec<GemSocketRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GemSocketRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}