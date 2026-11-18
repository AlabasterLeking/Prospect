package alabaster.prospect.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record WallShelfPatchConfiguration(BlockStateProvider toPlace, int tries, int xzSpread, int ySpread) implements FeatureConfiguration {

    public static final Codec<WallShelfPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("to_place").forGetter(WallShelfPatchConfiguration::toPlace),
            Codec.intRange(1, 128).fieldOf("tries").forGetter(WallShelfPatchConfiguration::tries),
            Codec.intRange(0, 8).fieldOf("xz_spread").forGetter(WallShelfPatchConfiguration::xzSpread),
            Codec.intRange(0, 8).fieldOf("y_spread").forGetter(WallShelfPatchConfiguration::ySpread)
    ).apply(instance, WallShelfPatchConfiguration::new));
}
