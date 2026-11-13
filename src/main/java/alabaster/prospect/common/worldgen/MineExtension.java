package alabaster.prospect.common.worldgen;

import alabaster.prospect.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MineExtension(List<Option> options, float chance, Optional<Holder<StructureProcessorList>> processors, String group) {

    public record Option(ResourceLocation template, BlockPos offset) {
        public static final Codec<Option> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("template").forGetter(Option::template),
                BlockPos.CODEC.fieldOf("offset").forGetter(Option::offset)
        ).apply(instance, Option::new));
    }

    public static final Codec<MineExtension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Option.CODEC.listOf().fieldOf("options").forGetter(MineExtension::options),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(MineExtension::chance),
            StructureProcessorType.LIST_CODEC.optionalFieldOf("processors").forGetter(MineExtension::processors),
            Codec.STRING.optionalFieldOf("group", "").forGetter(MineExtension::group)
    ).apply(instance, MineExtension::new));

    private static final String ROOM_PREFIX = "abandoned_mine_ext_";

    public record Chosen(ResourceLocation template, BlockPos origin, Holder<StructureProcessorList> processors) {
    }

    @FunctionalInterface
    public interface Ground {
        int at(int x, int z);
    }

    public static List<Chosen> choose(List<MineExtension> extensions, RandomSource random, BlockPos origin, Rotation rotation, Mirror mirror, Holder<StructureProcessorList> fallback, StructureTemplateManager manager, Ground ground) {
        List<Chosen> chosen = new ArrayList<>();
        for (MineExtension extension : extensions) {
            if (extension.options.isEmpty() || random.nextFloat() >= Config.extensionChance(extension.group, extension.chance)) continue;
            int start = random.nextInt(extension.options.size());
            for (int i = 0; i < extension.options.size(); i++) {
                Option option = extension.options.get((start + i) % extension.options.size());
                if (!Config.mineRoom(option.template().getPath().replace(ROOM_PREFIX, ""))) continue;
                if (!buried(option, origin, rotation, mirror, manager, ground)) continue;
                BlockPos at = origin.offset(StructureTemplate.transform(option.offset(), mirror, rotation, BlockPos.ZERO));
                chosen.add(new Chosen(option.template(), at, extension.processors.orElse(fallback)));
                break;
            }
        }
        return chosen;
    }

    private static boolean buried(Option option, BlockPos origin, Rotation rotation, Mirror mirror, StructureTemplateManager manager, Ground ground) {
        Optional<StructureTemplate> template = manager.get(option.template());
        if (template.isEmpty()) return false;
        Vec3i size = template.get().getSize();
        BlockPos near = StructureTemplate.transform(option.offset(), mirror, rotation, BlockPos.ZERO);
        BlockPos far = StructureTemplate.transform(option.offset().offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1), mirror, rotation, BlockPos.ZERO);
        int minX = origin.getX() + Math.min(near.getX(), far.getX());
        int maxX = origin.getX() + Math.max(near.getX(), far.getX());
        int minZ = origin.getZ() + Math.min(near.getZ(), far.getZ());
        int maxZ = origin.getZ() + Math.max(near.getZ(), far.getZ());
        int top = origin.getY() + Math.max(near.getY(), far.getY());
        int[][] samples = {{minX, minZ}, {minX, maxZ}, {maxX, minZ}, {maxX, maxZ}, {(minX + maxX) / 2, (minZ + maxZ) / 2}};
        for (int[] sample : samples) {
            int height = ground.at(sample[0], sample[1]);
            if (height < 0 || height <= top) return false;
        }
        return true;
    }

    public static void addPieces(StructurePiecesBuilder builder, StructureTemplateManager manager, List<Chosen> chosen, Rotation rotation, Mirror mirror, long theme) {
        for (Chosen piece : chosen) {
            builder.addPiece(new HillsidePiece(manager, piece.template(), piece.origin(), rotation, mirror, piece.processors(), HillsidePiece.NO_TERRAIN, theme));
        }
    }
}
