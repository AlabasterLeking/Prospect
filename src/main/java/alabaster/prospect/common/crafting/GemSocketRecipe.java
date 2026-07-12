package alabaster.prospect.common.crafting;

import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.registry.ProspectDataComponents;
import alabaster.prospect.common.registry.ProspectModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

public class GemSocketRecipe extends SmithingTransformRecipe {

    private final Ingredient gemSocketBase;
    private final Ingredient gemSocketAddition;
    private final ResourceLocation gemId;

    public GemSocketRecipe(Ingredient base, Ingredient gem, ResourceLocation gemId) {
        super(Ingredient.of(), base, gem, new ItemStack(ProspectModItems.PROSPECTING_PICKAXE.get()));
        this.gemSocketBase = base;
        this.gemSocketAddition = gem;
        this.gemId = gemId;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!input.template().isEmpty()) return false;
        if (!gemSocketBase.test(input.base()) || !gemSocketAddition.test(input.addition())) return false;
        ProspectingSockets sockets = input.base().getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        if (sockets.gemIds().size() >= ProspectingSockets.MAX_GEMS) return false;
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ProspectModItems.PROSPECTING_PICKAXE.get());
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return gemSocketBase.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return gemSocketAddition.test(stack);
    }

    @Override
    public boolean isIncomplete() {
        return gemSocketBase.hasNoItems() || gemSocketAddition.hasNoItems();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public Ingredient getBase() {
        return gemSocketBase;
    }

    public Ingredient getGem() {
        return gemSocketAddition;
    }

    public ResourceLocation getGemId() {
        return gemId;
    }

    public static class Serializer implements RecipeSerializer<GemSocketRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<GemSocketRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("base").forGetter(GemSocketRecipe::getBase),
                Ingredient.CODEC.fieldOf("addition").forGetter(GemSocketRecipe::getGem),
                ResourceLocation.CODEC.fieldOf("gem_id").forGetter(GemSocketRecipe::getGemId)
        ).apply(inst, GemSocketRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GemSocketRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, GemSocketRecipe::getBase,
                Ingredient.CONTENTS_STREAM_CODEC, GemSocketRecipe::getGem,
                ResourceLocation.STREAM_CODEC, GemSocketRecipe::getGemId,
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