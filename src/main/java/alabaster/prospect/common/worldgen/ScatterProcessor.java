package alabaster.prospect.common.worldgen;

import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class ScatterProcessor extends StructureProcessor {

    public static final MapCodec<ScatterProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("marker").forGetter(processor -> processor.marker),
            SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC).fieldOf("options").forGetter(processor -> processor.options)
    ).apply(instance, ScatterProcessor::new));

    private static final long SALT = 0x6B3D19A5L;

    private final Block marker;
    private final SimpleWeightedRandomList<BlockState> options;

    public ScatterProcessor(Block marker, SimpleWeightedRandomList<BlockState> options) {
        this.marker = marker;
        this.options = options;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
                                                             StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (!relativeBlockInfo.state().is(marker)) return relativeBlockInfo;
        RandomSource random = RandomSource.create(Mth.getSeed(relativeBlockInfo.pos()) ^ SALT);
        BlockState state = options.getRandomValue(random).orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), state, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProspectStructureTypes.SCATTER.get();
    }
}