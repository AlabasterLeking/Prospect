package alabaster.prospect.common.worldgen;

import alabaster.prospect.common.entity.grapple.ZiplineEntity;
import alabaster.prospect.common.registry.ProspectStructureTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ZiplineProcessor extends StructureProcessor {

    public static final ZiplineProcessor INSTANCE = new ZiplineProcessor();
    public static final MapCodec<ZiplineProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final String ZIPLINE = "prospect:zipline";

    @Nullable
    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos seedPos, StructureTemplate.StructureEntityInfo rawEntityInfo,
                                                               StructureTemplate.StructureEntityInfo entityInfo, StructurePlaceSettings settings, StructureTemplate template) {
        CompoundTag tag = entityInfo.nbt;
        if (!ZIPLINE.equals(tag.getString("id"))) return entityInfo;
        Vec3 shift = savedPos(tag, rawEntityInfo.pos).subtract(rawEntityInfo.pos);
        BlockPos blockShift = new BlockPos((int) Math.round(shift.x), (int) Math.round(shift.y), (int) Math.round(shift.z));
        BlockPos fence = place(BlockPos.of(tag.getLong("Fence")).subtract(blockShift), settings, seedPos);
        BlockPos anchor = place(BlockPos.of(tag.getLong("Anchor")).subtract(blockShift), settings, seedPos);
        boolean hooked = !tag.contains("Hooked") || tag.getBoolean("Hooked");
        Vec3 start = ZiplineEntity.fencePoint(fence);
        Vec3 end = hooked
                ? StructureTemplate.transformedVec3d(settings, new Vec3(tag.getFloat("EndX"), tag.getFloat("EndY"), tag.getFloat("EndZ")).subtract(shift)).add(Vec3.atLowerCornerOf(seedPos))
                : ZiplineEntity.fencePoint(anchor);
        CompoundTag placed = tag.copy();
        placed.putLong("Fence", fence.asLong());
        placed.putLong("Anchor", anchor.asLong());
        placed.putFloat("EndX", (float) end.x);
        placed.putFloat("EndY", (float) end.y);
        placed.putFloat("EndZ", (float) end.z);
        if (hooked && tag.contains("Face")) {
            Direction face = Direction.from3DDataValue(tag.getByte("Face"));
            placed.putByte("Face", (byte) settings.getRotation().rotate(settings.getMirror().mirror(face)).get3DDataValue());
        }
        return new StructureTemplate.StructureEntityInfo(start, fence, placed);
    }

    private static Vec3 savedPos(CompoundTag tag, Vec3 fallback) {
        ListTag pos = tag.getList("Pos", Tag.TAG_DOUBLE);
        return pos.size() == 3 ? new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2)) : fallback;
    }

    private static BlockPos place(BlockPos local, StructurePlaceSettings settings, BlockPos seedPos) {
        return StructureTemplate.calculateRelativePosition(settings, local).offset(seedPos);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProspectStructureTypes.ZIPLINE.get();
    }
}