package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ConfigPlacement extends PlacementFilter {
    public static final MapCodec<ConfigPlacement> CODEC = Codec.STRING.fieldOf("key").xmap(ConfigPlacement::new, placement -> placement.key);

    private final String key;

    public ConfigPlacement(String key) {
        this.key = key;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        Config.Feature feature = Config.FEATURES.get(key);
        if (feature == null) return true;
        if (!feature.enabled().get()) return false;
        int rarity = feature.rarity().get();
        return rarity <= 1 || random.nextFloat() < 1.0F / rarity;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ProspectStructureTypes.CONFIG_PLACEMENT.get();
    }
}
