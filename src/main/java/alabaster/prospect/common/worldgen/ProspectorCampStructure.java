package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

public class ProspectorCampStructure extends Structure {

    public static final MapCodec<ProspectorCampStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            JigsawStructure.CODEC.codec().fieldOf("jigsaw").forGetter(structure -> structure.jigsaw),
            Codec.intRange(0, 16).optionalFieldOf("max_height_difference", 4).forGetter(structure -> structure.maxHeightDifference),
            Codec.intRange(1, 64).optionalFieldOf("footprint_radius", 19).forGetter(structure -> structure.footprintRadius),
            Codec.STRING.optionalFieldOf("variant", "").forGetter(structure -> structure.variant)
    ).apply(instance, ProspectorCampStructure::new));

    private static final int SAMPLES_PER_SIDE = 5;

    private final JigsawStructure jigsaw;
    private final int maxHeightDifference;
    private final int footprintRadius;
    private final String variant;

    public ProspectorCampStructure(JigsawStructure jigsaw, int maxHeightDifference, int footprintRadius, String variant) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(), jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
        this.maxHeightDifference = maxHeightDifference;
        this.footprintRadius = footprintRadius;
        this.variant = variant;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!Config.ENABLE_PROSPECTOR_CAMPS.get() || !Config.campVariant(variant) || context.random().nextDouble() >= Config.PROSPECTOR_CAMP_CHANCE.get()) return Optional.empty();
        if (!HillsideStructure.biomeNearby(context, biomes()) || !isDryAndLevel(context)) return Optional.empty();
        return jigsaw.findValidGenerationPoint(context);
    }

    private boolean isDryAndLevel(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        int originX = chunk.getMinBlockX();
        int originZ = chunk.getMinBlockZ();
        int step = footprintRadius * 2 / (SAMPLES_PER_SIDE - 1);
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;

        for (int i = 0; i < SAMPLES_PER_SIDE; i++) {
            for (int j = 0; j < SAMPLES_PER_SIDE; j++) {
                int x = originX - footprintRadius + i * step;
                int z = originZ - footprintRadius + j * step;
                int floor = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                int surface = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (surface > floor) return false;
                lowest = Math.min(lowest, floor);
                highest = Math.max(highest, floor);
                if (highest - lowest > maxHeightDifference) return false;
            }
        }
        return true;
    }

    @Override
    public StructureType<?> type() {
        return ProspectStructureTypes.PROSPECTOR_CAMP.get();
    }
}