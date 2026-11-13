package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaveInProcessor extends StructureProcessor {

    private static final long DEFAULT_SALT = 0x2F7A3C91L;

    public static final MapCodec<CaveInProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BoundingBox.CODEC.fieldOf("area").forGetter(processor -> processor.area),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(processor -> processor.chance),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("rubble").forGetter(processor -> processor.rubble),
            Codec.LONG.optionalFieldOf("salt", DEFAULT_SALT).forGetter(processor -> processor.salt)
    ).apply(instance, CaveInProcessor::new));

    private static final int LAYER_FALLOFF = 40;

    private final BoundingBox area;
    private final float chance;
    private final List<Block> rubble;
    private final long salt;

    public CaveInProcessor(BoundingBox area, float chance, List<Block> rubble, long salt) {
        this.area = area;
        this.chance = chance;
        this.rubble = rubble;
        this.salt = salt;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
                                                             StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (!Config.MINE_CAVE_INS.get() || rubble.isEmpty() || !area.isInside(blockInfo.pos())) return relativeBlockInfo;
        if (RandomSource.create(Mth.getSeed(offset) ^ salt).nextFloat() >= chance) return relativeBlockInfo;
        BlockPos local = blockInfo.pos();
        if (!piled(local)) return relativeBlockInfo;
        int cell = (Math.floorDiv(local.getX(), 2) * 73856093) ^ (Math.floorDiv(local.getY(), 2) * 19349663) ^ (Math.floorDiv(local.getZ(), 2) * 83492791) ^ (int) salt;
        cell ^= cell >>> 13;
        cell *= 0x5bd1e995;
        cell ^= cell >>> 15;
        Block block = rubble.get(Math.floorMod(cell, rubble.size()));
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), block.defaultBlockState(), null);
    }

    private boolean piled(BlockPos local) {
        for (int y = local.getY(); y > area.minY(); y--) {
            int layer = y - area.minY();
            int roll = Math.floorMod((local.getX() * 73856093) ^ (y * 19349663) ^ (local.getZ() * 83492791) ^ (int) salt, 100);
            if (roll >= 100 - layer * LAYER_FALLOFF) return false;
        }
        return true;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProspectStructureTypes.CAVE_IN.get();
    }
}