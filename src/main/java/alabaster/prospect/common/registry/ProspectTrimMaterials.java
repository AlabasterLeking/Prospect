package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;

import java.util.Map;

public class ProspectTrimMaterials {
    public static final ResourceKey<TrimMaterial> RUBY =
            ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "ruby"));
    public static final ResourceKey<TrimMaterial> SAPPHIRE =
            ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "sapphire"));
    public static final ResourceKey<TrimMaterial> TOPAZ =
            ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "topaz"));

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        register(context, RUBY, ProspectModItems.RUBY.get(), Style.EMPTY.withColor(TextColor.parseColor("#f9483c").getOrThrow()), -1.0F);
        register(context, SAPPHIRE, ProspectModItems.SAPPHIRE.get(), Style.EMPTY.withColor(TextColor.parseColor("#137aff").getOrThrow()), -1.0F);
        register(context, TOPAZ, ProspectModItems.TOPAZ.get(), Style.EMPTY.withColor(TextColor.parseColor("#ec7505").getOrThrow()), -1.0F);
    }

    private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Item item, Style style, float itemModelIndex) {
        TrimMaterial trimmaterial = TrimMaterial.create(trimKey.location().getPath(), item, itemModelIndex,
                Component.translatable(Util.makeDescriptionId("trim_material", trimKey.location())).withStyle(style), Map.of());
        context.register(trimKey, trimmaterial);
    }
}
