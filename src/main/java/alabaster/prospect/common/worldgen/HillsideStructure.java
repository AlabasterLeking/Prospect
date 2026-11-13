package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import java.util.Optional;

public class HillsideStructure extends Structure {

    private static final int[] SIDE_OFFSETS = {-4, 4};
    private static final int MAX_LOWER = 3;
    private static final int CAVE_STEP = 4;
    private static final int CAVE_COVER = 2;
    private static final int[][] APPROACH_SAMPLES = {{2, 0}, {4, 0}, {6, 0}, {3, -3}, {3, 3}, {5, -3}, {5, 3}};
    private static final int[] BIOME_HEIGHTS = {72, 112, 160};
    private static final int[][] BIOME_PROBES = {{0, 0}, {8, 8}, {-8, -8}, {8, -8}, {-8, 8}};
    private static final int[][] CANDIDATES = {{0, 0}, {6, 0}, {-6, 0}, {0, 6}, {0, -6}, {6, 6}, {-6, -6}, {6, -6}, {-6, 6}};
    private static final Holder<StructureProcessorList> NO_PROCESSORS = Holder.direct(new StructureProcessorList(java.util.List.of()));

    public static final MapCodec<HillsideStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.fieldOf("template").forGetter(structure -> structure.template),
            BlockPos.CODEC.fieldOf("entrance").forGetter(structure -> structure.entrance),
            StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(structure -> structure.processors),
            Codec.intRange(1, 32).optionalFieldOf("min_rise", 6).forGetter(structure -> structure.minRise),
            Codec.intRange(2, 32).optionalFieldOf("depth", 10).forGetter(structure -> structure.depth),
            Codec.intRange(0, 32).optionalFieldOf("tunnel_length", 0).forGetter(structure -> structure.tunnelLength),
            Codec.intRange(0, 32).optionalFieldOf("tunnel_cover", 5).forGetter(structure -> structure.tunnelCover),
            HillsideTerrain.MAP_CODEC.forGetter(structure -> structure.terrain),
            ResourceLocation.CODEC.optionalFieldOf("overlay").forGetter(structure -> structure.overlay),
            Codec.BOOL.optionalFieldOf("allow_water", false).forGetter(structure -> structure.allowWater),
            MineExtension.CODEC.listOf().optionalFieldOf("extensions", java.util.List.of()).forGetter(structure -> structure.extensions),
            Codec.STRING.optionalFieldOf("variant", "").forGetter(structure -> structure.variant)
    ).apply(instance, HillsideStructure::new));

    private final ResourceLocation template;
    private final BlockPos entrance;
    private final Holder<StructureProcessorList> processors;
    private final int minRise;
    private final int depth;
    private final int tunnelLength;
    private final int tunnelCover;
    private final HillsideTerrain terrain;
    private final Optional<ResourceLocation> overlay;
    private final boolean allowWater;
    private final java.util.List<MineExtension> extensions;
    private final String variant;

    public HillsideStructure(StructureSettings settings, ResourceLocation template, BlockPos entrance, Holder<StructureProcessorList> processors, int minRise, int depth, int tunnelLength, int tunnelCover, HillsideTerrain terrain, Optional<ResourceLocation> overlay, boolean allowWater, java.util.List<MineExtension> extensions, String variant) {
        super(settings);
        this.template = template;
        this.entrance = entrance;
        this.processors = processors;
        this.minRise = minRise;
        this.depth = depth;
        this.tunnelLength = tunnelLength;
        this.tunnelCover = tunnelCover;
        this.terrain = terrain;
        this.overlay = overlay;
        this.allowWater = allowWater;
        this.extensions = extensions;
        this.variant = variant;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!Config.ENABLE_ABANDONED_MINES.get() || !Config.mineVariant(variant) || context.random().nextDouble() >= Config.ABANDONED_MINE_CHANCE.get()) return Optional.empty();
        if (!biomeNearby(context, biomes())) return Optional.empty();
        ChunkPos chunk = context.chunkPos();
        for (int[] offset : CANDIDATES) {
            Optional<GenerationStub> stub = tryAt(context, chunk.getMiddleBlockX() + offset[0], chunk.getMiddleBlockZ() + offset[1]);
            if (stub.isPresent()) return stub;
        }
        return Optional.empty();
    }

    private Optional<GenerationStub> tryAt(GenerationContext context, int x, int z) {
        int ground = ground(context, x, z);
        if (ground < 0) return Optional.empty();

        Direction best = null;
        int bestRise = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int far = ground(context, x + direction.getStepX() * depth, z + direction.getStepZ() * depth);
            int rise = far - ground;
            if (far < 0 || rise < minRise || rise <= bestRise) continue;
            int near = ground(context, x + direction.getStepX() * (depth / 2), z + direction.getStepZ() * (depth / 2));
            if (near < ground + 3) continue;
            int front = ground(context, x - direction.getStepX() * 5, z - direction.getStepZ() * 5);
            if (front < 0 || Math.abs(front - ground) > 2) continue;
            if ((allowWater || dryApproach(context, x, z, direction)) && buried(context, x, z, direction, ground) && caveBuried(context, x, z, direction, ground)) {
                best = direction;
                bestRise = rise;
            }
        }
        if (best == null) return Optional.empty();

        Rotation rotation = switch (best) {
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        Mirror mirror = context.random().nextBoolean() ? Mirror.FRONT_BACK : Mirror.NONE;
        int approach = approachHeight(context, x, z, best);
        int floorY = approach < 0 ? ground : Math.max(ground - MAX_LOWER, Math.min(ground, approach));
        BlockPos door = new BlockPos(x, floorY, z);
        BlockPos origin = door.subtract(StructureTemplate.transform(entrance, mirror, rotation, BlockPos.ZERO));
        long theme = context.random().nextLong();
        java.util.List<MineExtension.Chosen> chosen = MineExtension.choose(extensions, context.random(), origin, rotation, mirror, processors, context.structureTemplateManager(), (gx, gz) -> ground(context, gx, gz));
        return Optional.of(new GenerationStub(door, builder -> {
            builder.addPiece(new HillsidePiece(context.structureTemplateManager(), template, origin, rotation, mirror, processors, terrain, theme));
            overlay.ifPresent(extras -> builder.addPiece(new HillsidePiece(context.structureTemplateManager(), extras, origin, rotation, mirror, NO_PROCESSORS, HillsidePiece.NO_TERRAIN, theme)));
            MineExtension.addPieces(builder, context.structureTemplateManager(), chosen, rotation, mirror, theme);
        }));
    }

    private static int approachHeight(GenerationContext context, int x, int z, Direction direction) {
        Direction side = direction.getClockWise();
        int[] heights = new int[APPROACH_SAMPLES.length];
        for (int i = 0; i < APPROACH_SAMPLES.length; i++) {
            int back = APPROACH_SAMPLES[i][0];
            int offset = APPROACH_SAMPLES[i][1];
            heights[i] = ground(context, x - direction.getStepX() * back + side.getStepX() * offset, z - direction.getStepZ() * back + side.getStepZ() * offset);
        }
        java.util.Arrays.sort(heights);
        return heights[heights.length / 2];
    }

    private static boolean dryApproach(GenerationContext context, int x, int z, Direction direction) {
        Direction side = direction.getClockWise();
        for (int back = 1; back <= 6; back += 5) {
            for (int offset : SIDE_OFFSETS) {
                int px = x - direction.getStepX() * back + side.getStepX() * offset;
                int pz = z - direction.getStepZ() * back + side.getStepZ() * offset;
                if (ground(context, px, pz) < 0) return false;
            }
        }
        return true;
    }

    private boolean caveBuried(GenerationContext context, int x, int z, Direction direction, int ground) {
        int length = context.structureTemplateManager().get(template).map(found -> found.getSize().getZ() - entrance.getZ()).orElse(0);
        Direction side = direction.getClockWise();
        for (int step = tunnelLength + 2; step < length; step += CAVE_STEP) {
            int cx = x + direction.getStepX() * step;
            int cz = z + direction.getStepZ() * step;
            if (!covered(context, cx, cz, ground)) return false;
            for (int offset : SIDE_OFFSETS) {
                if (!covered(context, cx + side.getStepX() * offset, cz + side.getStepZ() * offset, ground)) return false;
            }
        }
        return true;
    }

    private boolean covered(GenerationContext context, int x, int z, int ground) {
        int height = ground(context, x, z);
        return height < 0 ? allowWater : height >= ground + CAVE_COVER;
    }

    private boolean buried(GenerationContext context, int x, int z, Direction direction, int ground) {
        Direction side = direction.getClockWise();
        for (int step = 3; step <= tunnelLength; step += 2) {
            int cx = x + direction.getStepX() * step;
            int cz = z + direction.getStepZ() * step;
            if (ground(context, cx, cz) < ground + tunnelCover) return false;
            for (int offset : SIDE_OFFSETS) {
                int height = ground(context, cx + side.getStepX() * offset, cz + side.getStepZ() * offset);
                if (height < ground + tunnelCover - 1) return false;
            }
        }
        return true;
    }

    static boolean biomeNearby(GenerationContext context, HolderSet<Biome> biomes) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        for (int height : BIOME_HEIGHTS) {
            int y = QuartPos.fromBlock(height);
            for (int[] offset : BIOME_PROBES) {
                Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x + offset[0]), y, QuartPos.fromBlock(z + offset[1]), context.randomState().sampler());
                if (biomes.contains(biome)) return true;
            }
        }
        return false;
    }

    static int ground(GenerationContext context, int x, int z) {
        int floor = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        return floor < context.chunkGenerator().getSeaLevel() ? -1 : floor;
    }

    @Override
    public StructureType<?> type() {
        return ProspectStructureTypes.HILLSIDE.get();
    }
}