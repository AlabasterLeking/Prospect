package alabaster.prospect.common.block;

import alabaster.prospect.common.particle.GlowSporeOptions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GlowingMushroomCapBlock extends HugeMushroomBlock {

    public static final MapCodec<GlowingMushroomCapBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GlowColor.CODEC.fieldOf("color").forGetter(GlowingMushroomCapBlock::getColor),
            propertiesCodec()
    ).apply(instance, GlowingMushroomCapBlock::new));

    private final GlowColor color;

    public GlowingMushroomCapBlock(GlowColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    public GlowColor getColor() {
        return color;
    }

    @Override
    public MapCodec<HugeMushroomBlock> codec() {
        return (MapCodec<HugeMushroomBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        if (!level.getBlockState(pos.below()).isAir()) return;
        double x = pos.getX() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(GlowSporeOptions.of(color), x, pos.getY() - 0.05, z, 0.0, -0.015, 0.0);
    }
}
