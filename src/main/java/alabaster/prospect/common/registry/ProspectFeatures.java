package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.GlowColor;
import alabaster.prospect.common.worldgen.BellMushroomFeature;
import alabaster.prospect.common.worldgen.DomeMushroomFeature;
import alabaster.prospect.common.worldgen.GrovePoolConfiguration;
import alabaster.prospect.common.worldgen.GrovePoolFeature;
import alabaster.prospect.common.worldgen.HugeShelfMushroomConfiguration;
import alabaster.prospect.common.worldgen.HugeShelfMushroomFeature;
import alabaster.prospect.common.worldgen.WallShelfPatchConfiguration;
import alabaster.prospect.common.worldgen.WallShelfPatchFeature;
import alabaster.prospect.common.worldgen.ParasolMushroomFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProspectFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Prospect.MODID);

    public static final Supplier<Feature<HugeMushroomFeatureConfiguration>> HUGE_DOME_MUSHROOM = FEATURES.register("huge_dome_mushroom", DomeMushroomFeature::new);
    public static final Supplier<Feature<HugeMushroomFeatureConfiguration>> HUGE_BELL_MUSHROOM = FEATURES.register("huge_bell_mushroom", BellMushroomFeature::new);
    public static final Supplier<Feature<HugeMushroomFeatureConfiguration>> HUGE_PARASOL_MUSHROOM = FEATURES.register("huge_parasol_mushroom", ParasolMushroomFeature::new);
    public static final Supplier<Feature<GrovePoolConfiguration>> GROVE_POOL = FEATURES.register("grove_pool", GrovePoolFeature::new);
    public static final Supplier<Feature<HugeShelfMushroomConfiguration>> HUGE_SHELF_MUSHROOM = FEATURES.register("huge_shelf_mushroom", HugeShelfMushroomFeature::new);
    public static final Supplier<Feature<WallShelfPatchConfiguration>> WALL_SHELF_PATCH = FEATURES.register("wall_shelf_patch", WallShelfPatchFeature::new);

    public static ResourceKey<ConfiguredFeature<?, ?>> hugeGlowingMushroom(GlowColor color) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "huge_glowing_mushroom_" + color.getSerializedName()));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> hugeGlowingShelf(GlowColor color) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "huge_glowing_shelf_" + color.getSerializedName()));
    }
}