package alabaster.prospect.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record HugeShelfMushroomConfiguration(BlockStateProvider cap, IntProvider radius, IntProvider tiers) implements FeatureConfiguration {

    public static final Codec<HugeShelfMushroomConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("cap").forGetter(HugeShelfMushroomConfiguration::cap),
            IntProvider.codec(1, 6).fieldOf("radius").forGetter(HugeShelfMushroomConfiguration::radius),
            IntProvider.codec(1, 4).fieldOf("tiers").forGetter(HugeShelfMushroomConfiguration::tiers)
    ).apply(instance, HugeShelfMushroomConfiguration::new));
}
