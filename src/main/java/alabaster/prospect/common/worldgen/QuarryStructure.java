package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class QuarryStructure extends Structure {

    public static final MapCodec<QuarryStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.fieldOf("template").forGetter(structure -> structure.template),
            BlockPos.CODEC.fieldOf("rim").forGetter(structure -> structure.rim),
            StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(structure -> structure.processors),
            Codec.intRange(3, 48).optionalFieldOf("footprint", 13).forGetter(structure -> structure.footprint),
            Codec.intRange(0, 16).optionalFieldOf("max_spread", 4).forGetter(structure -> structure.maxSpread),
            HillsideTerrain.MAP_CODEC.forGetter(structure -> structure.terrain),
            MineExtension.CODEC.listOf().optionalFieldOf("extensions", java.util.List.of()).forGetter(structure -> structure.extensions),
            Codec.STRING.optionalFieldOf("variant", "").forGetter(structure -> structure.variant)
    ).apply(instance, QuarryStructure::new));

    private static final int[][] CANDIDATES = {{0, 0}, {6, 0}, {-6, 0}, {0, 6}, {0, -6}};
    private static final int SAMPLES = 5;

    private final ResourceLocation template;
    private final BlockPos rim;
    private final Holder<StructureProcessorList> processors;
    private final int footprint;
    private final int maxSpread;
    private final HillsideTerrain terrain;
    private final java.util.List<MineExtension> extensions;
    private final String variant;

    public QuarryStructure(StructureSettings settings, ResourceLocation template, BlockPos rim, Holder<StructureProcessorList> processors, int footprint, int maxSpread, HillsideTerrain terrain, java.util.List<MineExtension> extensions, String variant) {
        super(settings);
        this.template = template;
        this.rim = rim;
        this.processors = processors;
        this.footprint = footprint;
        this.maxSpread = maxSpread;
        this.terrain = terrain;
        this.extensions = extensions;
        this.variant = variant;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!Config.ENABLE_ABANDONED_MINES.get() || !Config.mineVariant(variant) || context.random().nextDouble() >= Config.ABANDONED_MINE_CHANCE.get()) return Optional.empty();
        if (!HillsideStructure.biomeNearby(context, biomes())) return Optional.empty();
        ChunkPos chunk = context.chunkPos();
        Rotation rotation = Rotation.getRandom(context.random());
        for (int[] offset : CANDIDATES) {
            int x = chunk.getMiddleBlockX() + offset[0];
            int z = chunk.getMiddleBlockZ() + offset[1];
            int rimY = level(context, x, z);
            if (rimY < 0) continue;
            BlockPos center = new BlockPos(x, rimY, z);
            BlockPos origin = center.subtract(StructureTemplate.transform(rim, Mirror.NONE, rotation, BlockPos.ZERO));
            long theme = context.random().nextLong();
            java.util.List<MineExtension.Chosen> chosen = MineExtension.choose(extensions, context.random(), origin, rotation, Mirror.NONE, processors, context.structureTemplateManager(), (gx, gz) -> HillsideStructure.ground(context, gx, gz));
            return Optional.of(new GenerationStub(center, builder -> {
                builder.addPiece(new HillsidePiece(context.structureTemplateManager(), template, origin, rotation, Mirror.NONE, processors, terrain, theme));
                MineExtension.addPieces(builder, context.structureTemplateManager(), chosen, rotation, Mirror.NONE, theme);
            }));
        }
        return Optional.empty();
    }

    private int level(GenerationContext context, int x, int z) {
        int half = footprint / 2;
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int a = 0; a < SAMPLES; a++) {
            for (int b = 0; b < SAMPLES; b++) {
                int height = HillsideStructure.ground(context, x - half + a * footprint / (SAMPLES - 1), z - half + b * footprint / (SAMPLES - 1));
                if (height < 0) return -1;
                lowest = Math.min(lowest, height);
                highest = Math.max(highest, height);
                if (highest - lowest > maxSpread) return -1;
            }
        }
        return lowest;
    }

    @Override
    public StructureType<?> type() {
        return ProspectStructureTypes.QUARRY.get();
    }
}