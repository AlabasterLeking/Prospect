package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class EntityChanceProcessor extends StructureProcessor {

    public static final MapCodec<EntityChanceProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(processor -> processor.entity),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(processor -> processor.chance),
            BoundingBox.CODEC.optionalFieldOf("fill_area").forGetter(processor -> processor.fillArea),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().optionalFieldOf("fill", java.util.List.of()).forGetter(processor -> processor.fill)
    ).apply(instance, EntityChanceProcessor::new));

    private static final long SALT = 0x51A7E2B9L;
    private static final String ZOMBIE_PROSPECTOR = "zombie_prospector";

    private final ResourceLocation entity;
    private final float chance;
    private final java.util.Optional<BoundingBox> fillArea;
    private final java.util.List<Block> fill;

    public EntityChanceProcessor(ResourceLocation entity, float chance, java.util.Optional<BoundingBox> fillArea, java.util.List<Block> fill) {
        this.entity = entity;
        this.chance = chance;
        this.fillArea = fillArea;
        this.fill = fill;
    }

    private boolean present(BlockPos seedPos) {
        float odds = entity.getPath().equals(ZOMBIE_PROSPECTOR) ? Config.MINE_ZOMBIE_PROSPECTOR_CHANCE.get().floatValue() : chance;
        return RandomSource.create(Mth.getSeed(seedPos) ^ SALT).nextFloat() < odds;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
                                                             StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (fill.isEmpty() || fillArea.isEmpty() || !fillArea.get().isInside(blockInfo.pos()) || !relativeBlockInfo.state().isAir() || present(offset)) {
            return relativeBlockInfo;
        }
        BlockPos local = blockInfo.pos();
        Block block = fill.get(Math.floorMod(local.getX() * 7 + local.getY() * 13 + local.getZ() * 5, fill.size()));
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), block.defaultBlockState(), null);
    }

    @Nullable
    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos seedPos, StructureTemplate.StructureEntityInfo rawEntityInfo,
                                                               StructureTemplate.StructureEntityInfo entityInfo, StructurePlaceSettings settings, StructureTemplate template) {
        if (!entity.toString().equals(entityInfo.nbt.getString("id"))) return entityInfo;
        return present(seedPos) ? entityInfo : null;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProspectStructureTypes.ENTITY_CHANCE.get();
    }
}