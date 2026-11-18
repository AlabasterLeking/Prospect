package alabaster.prospect.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record GrovePoolConfiguration(IntProvider radius, IntProvider depth, BlockStateProvider bed, TagKey<Block> replaceable,
                                     BlockStateProvider vegetation, float vegetationChance, float shoreChance) implements FeatureConfiguration {

    public static final Codec<GrovePoolConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.codec(1, 8).fieldOf("radius").forGetter(GrovePoolConfiguration::radius),
            IntProvider.codec(1, 6).fieldOf("depth").forGetter(GrovePoolConfiguration::depth),
            BlockStateProvider.CODEC.fieldOf("bed").forGetter(GrovePoolConfiguration::bed),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(GrovePoolConfiguration::replaceable),
            BlockStateProvider.CODEC.fieldOf("vegetation").forGetter(GrovePoolConfiguration::vegetation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter(GrovePoolConfiguration::vegetationChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("shore_chance").forGetter(GrovePoolConfiguration::shoreChance)
    ).apply(instance, GrovePoolConfiguration::new));
}
