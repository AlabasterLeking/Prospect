package alabaster.prospect.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class GlowingMushroomBlock extends MushroomBlock {

    public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, 2);
    public static final int BASE_LIGHT = 5;

    public static final MapCodec<GlowingMushroomBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GlowColor.CODEC.fieldOf("color").forGetter(GlowingMushroomBlock::getColor),
            ResourceKey.codec(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(block -> block.hugeFeature),
            propertiesCodec()
    ).apply(instance, GlowingMushroomBlock::new));

    private final GlowColor color;
    private final ResourceKey<ConfiguredFeature<?, ?>> hugeFeature;

    public GlowingMushroomBlock(GlowColor color, ResourceKey<ConfiguredFeature<?, ?>> hugeFeature, Properties properties) {
        super(hugeFeature, properties);
        this.color = color;
        this.hugeFeature = hugeFeature;
        registerDefaultState(stateDefinition.any().setValue(GLOW, 1));
    }

    public GlowColor getColor() {
        return color;
    }

    @Override
    public MapCodec<MushroomBlock> codec() {
        return (MapCodec<MushroomBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLOW);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.getBlockState(pos).is(this)) {
            level.setBlock(pos, state.setValue(GLOW, random.nextInt(3)), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) != 0) return;
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
        double y = pos.getY() + 0.4 + random.nextDouble() * 0.3;
        double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        level.addParticle(new DustParticleOptions(color.particleColor(), 0.6F), x, y, z, 0.0, 0.02, 0.0);
    }
}