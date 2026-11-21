package alabaster.prospect.common.worldgen;

import alabaster.prospect.common.registry.ProspectStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class HillsidePiece extends TemplateStructurePiece {

    private static final String ROTATION_TAG = "Rot";
    private static final String MIRROR_TAG = "Mirror";
    private static final String PROCESSORS_TAG = "Processors";

    private static final String TERRAIN_TAG = "Terrain";
    private static final String THEME_TAG = "Theme";

    private final Optional<ResourceKey<StructureProcessorList>> processorKey;
    public static final HillsideTerrain NO_TERRAIN = new HillsideTerrain(Optional.empty(), Optional.empty(), Optional.empty(), java.util.List.of(), 0.0F, java.util.List.of(), java.util.List.of(), java.util.List.of(),
            Optional.empty(), Optional.empty(), 0.0F, java.util.List.of(), java.util.List.of(), Optional.empty(), Optional.empty());

    private final HillsideTerrain terrain;
    private final long theme;

    public HillsidePiece(StructureTemplateManager manager, ResourceLocation template, BlockPos position, Rotation rotation, Mirror mirror, Holder<StructureProcessorList> processors,
                         HillsideTerrain terrain, long theme) {
        super(ProspectStructureTypes.HILLSIDE_PIECE.get(), 0, manager, template, template.toString(), settings(rotation, mirror, Optional.of(processors), theme), position);
        this.processorKey = processors.unwrapKey();
        this.terrain = terrain;
        this.theme = theme;
        if (terrain != NO_TERRAIN) {
            BoundingBox box = this.boundingBox;
            this.boundingBox = new BoundingBox(box.minX() - HillsideTerrain.MARGIN, box.minY() - HillsideTerrain.MARGIN, box.minZ() - HillsideTerrain.MARGIN,
                    box.maxX() + HillsideTerrain.MARGIN, box.maxY(), box.maxZ() + HillsideTerrain.MARGIN);
        }
    }

    public HillsidePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ProspectStructureTypes.HILLSIDE_PIECE.get(), tag, context.structureTemplateManager(), id -> settings(
                Rotation.valueOf(tag.getString(ROTATION_TAG)),
                tag.contains(MIRROR_TAG) ? Mirror.valueOf(tag.getString(MIRROR_TAG)) : Mirror.NONE,
                processorsFrom(context, tag), tag.getLong(THEME_TAG)));
        this.processorKey = tag.contains(PROCESSORS_TAG)
                ? Optional.of(ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.parse(tag.getString(PROCESSORS_TAG))))
                : Optional.empty();
        this.terrain = tag.contains(TERRAIN_TAG)
                ? HillsideTerrain.MAP_CODEC.codec().parse(NbtOps.INSTANCE, tag.get(TERRAIN_TAG)).result().orElse(NO_TERRAIN)
                : NO_TERRAIN;
        this.theme = tag.getLong(THEME_TAG);
    }

    private static Optional<Holder<StructureProcessorList>> processorsFrom(StructurePieceSerializationContext context, CompoundTag tag) {
        if (!tag.contains(PROCESSORS_TAG)) return Optional.empty();
        ResourceKey<StructureProcessorList> key = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.parse(tag.getString(PROCESSORS_TAG)));
        return context.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST).getHolder(key).map(holder -> (Holder<StructureProcessorList>) holder);
    }

    private static StructurePlaceSettings settings(Rotation rotation, Mirror mirror, Optional<Holder<StructureProcessorList>> processors, long theme) {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).addProcessor(ZiplineProcessor.INSTANCE);
        processors.ifPresent(list -> list.value().list().forEach(processor -> settings.addProcessor(processor instanceof UniformOreProcessor ore ? ore.seeded(theme) : processor)));
        return settings;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(ROTATION_TAG, placeSettings.getRotation().name());
        tag.putString(MIRROR_TAG, placeSettings.getMirror().name());
        processorKey.ifPresent(key -> tag.putString(PROCESSORS_TAG, key.location().toString()));
        HillsideTerrain.MAP_CODEC.codec().encodeStart(NbtOps.INSTANCE, terrain).result().ifPresent(encoded -> tag.put(TERRAIN_TAG, encoded));
        tag.putLong(THEME_TAG, theme);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        terrain.prepare(level, box, template.getSize(), this::toWorld);
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pivot);
        terrain.apply(level, box, boundingBox, template.getSize(), random, this::toWorld);
        settle(level, box);
    }

    private void settle(WorldGenLevel level, BoundingBox box) {
        BoundingBox area = BoundingBox.fromCorners(new Vec3i(Math.max(box.minX(), boundingBox.minX()), Math.max(box.minY(), boundingBox.minY()), Math.max(box.minZ(), boundingBox.minZ())),
                new Vec3i(Math.min(box.maxX(), boundingBox.maxX()), Math.min(box.maxY(), boundingBox.maxY()), Math.min(box.maxZ(), boundingBox.maxZ())));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = area.minY(); y <= area.maxY(); y++) {
            for (int x = area.minX(); x <= area.maxX(); x++) {
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof FallingBlock) || !FallingBlock.isFree(level.getBlockState(pos.below()))) continue;
                    level.setBlock(pos, settled(state), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static BlockState settled(BlockState state) {
        if (state.is(Blocks.SAND) || state.is(Blocks.SUSPICIOUS_SAND)) return Blocks.SANDSTONE.defaultBlockState();
        if (state.is(Blocks.RED_SAND)) return Blocks.RED_SANDSTONE.defaultBlockState();
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SUSPICIOUS_GRAVEL)) return Blocks.COBBLESTONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private BlockPos toWorld(BlockPos local) {
        return StructureTemplate.calculateRelativePosition(placeSettings, local).offset(templatePosition);
    }


    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
    }
}