package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public final class ProspectingGemItems {
    private ProspectingGemItems() {}

    private static final Map<ResourceLocation, java.util.function.Supplier<ItemStack>> ITEMS = Map.of(
            ProspectingGems.QUARTZ, () -> new ItemStack(Items.QUARTZ),
            ProspectingGems.DIAMOND, () -> new ItemStack(Items.DIAMOND),
            ProspectingGems.EMERALD, () -> new ItemStack(Items.EMERALD),
            ProspectingGems.LAPIS, () -> new ItemStack(Items.LAPIS_LAZULI),
            ProspectingGems.AMETHYST, () -> new ItemStack(Items.AMETHYST_SHARD),
            ProspectingGems.TOPAZ, () -> new ItemStack(ProspectModItems.TOPAZ.get()),
            ProspectingGems.RUBY, () -> new ItemStack(ProspectModItems.RUBY.get()),
            ProspectingGems.SAPPHIRE, () -> new ItemStack(ProspectModItems.SAPPHIRE.get())
    );

    public static ItemStack getItem(ResourceLocation gemId) {
        var supplier = ITEMS.get(gemId);
        return supplier != null ? supplier.get() : ItemStack.EMPTY;
    }
}