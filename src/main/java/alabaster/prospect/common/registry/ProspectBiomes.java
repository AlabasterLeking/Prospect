package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class ProspectBiomes {
    private ProspectBiomes() {}

    public static final ResourceKey<Biome> GLOWSHROOM_GROVE = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "glowshroom_grove"));
}