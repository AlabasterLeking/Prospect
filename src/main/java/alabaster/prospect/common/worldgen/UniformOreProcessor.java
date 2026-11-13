package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

public class UniformOreProcessor extends StructureProcessor {

    private static final String LOOT_TABLE_TAG = "LootTable";

    public static final MapCodec<UniformOreProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("targets").forGetter(processor -> processor.targets),
            SimpleWeightedRandomList.wrappedCodec(BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("options").forGetter(processor -> processor.options),
            ResourceLocation.CODEC.optionalFieldOf("loot_base").forGetter(processor -> processor.lootBase),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), ResourceLocation.CODEC).optionalFieldOf("loot_by_ore", Map.of()).forGetter(processor -> processor.lootByOre)
    ).apply(instance, (targets, options, lootBase, lootByOre) -> new UniformOreProcessor(targets, options, lootBase, lootByOre, OptionalLong.empty())));

    private final List<Block> targets;
    private final SimpleWeightedRandomList<Block> options;
    private final Optional<ResourceLocation> lootBase;
    private final Map<Block, ResourceLocation> lootByOre;
    private final OptionalLong seed;

    public UniformOreProcessor(List<Block> targets, SimpleWeightedRandomList<Block> options, Optional<ResourceLocation> lootBase, Map<Block, ResourceLocation> lootByOre, OptionalLong seed) {
        this.targets = targets;
        this.options = options;
        this.lootBase = lootBase;
        this.lootByOre = lootByOre;
        this.seed = seed;
    }

    public UniformOreProcessor seeded(long theme) {
        return new UniformOreProcessor(targets, options, lootBase, lootByOre, OptionalLong.of(theme));
    }

    private Optional<Block> pick(BlockPos offset) {
        return options.getRandomValue(RandomSource.create(seed.isPresent() ? seed.getAsLong() : Mth.getSeed(offset)));
    }

    @Nullable
    private CompoundTag themedLoot(@Nullable CompoundTag nbt, BlockPos offset) {
        if (nbt == null || !Config.MINE_THEMED_LOOT.get() || lootBase.isEmpty() || !lootBase.get().toString().equals(nbt.getString(LOOT_TABLE_TAG))) return null;
        ResourceLocation table = pick(offset).map(lootByOre::get).orElse(null);
        if (table == null) return null;
        CompoundTag copy = nbt.copy();
        copy.putString(LOOT_TABLE_TAG, table.toString());
        return copy;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
                                                             StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (!targets.contains(relativeBlockInfo.state().getBlock())) {
            CompoundTag loot = themedLoot(relativeBlockInfo.nbt(), offset);
            return loot == null ? relativeBlockInfo : new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), loot);
        }
        return pick(offset)
                .map(block -> new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), block.defaultBlockState(), null))
                .orElse(relativeBlockInfo);
    }

    @Nullable
    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos seedPos, StructureTemplate.StructureEntityInfo rawEntityInfo,
                                                               StructureTemplate.StructureEntityInfo entityInfo, StructurePlaceSettings settings, StructureTemplate template) {
        CompoundTag loot = themedLoot(entityInfo.nbt, seedPos);
        return loot == null ? entityInfo : new StructureTemplate.StructureEntityInfo(entityInfo.pos, entityInfo.blockPos, loot);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProspectStructureTypes.UNIFORM_ORE.get();
    }
}
