package alabaster.prospect.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record HillsideTerrain(Optional<BoundingBox> fill, Optional<BoundingBox> carve, Optional<BoundingBox> line, List<Block> lining, float overgrowth, List<BoundingBox> cover, List<BoundingBox> backfill, List<BoundingBox> piles,
                              Optional<BoundingBox> rubbleArea, Optional<BoundingBox> rubbleExclude, float rubbleDensity, List<BlockPos> boulders, List<BoundingBox> paths, Optional<BoundingBox> grade, Optional<BoundingBox> bowl) {

    public static final MapCodec<HillsideTerrain> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BoundingBox.CODEC.optionalFieldOf("fill").forGetter(HillsideTerrain::fill),
            BoundingBox.CODEC.optionalFieldOf("carve").forGetter(HillsideTerrain::carve),
            BoundingBox.CODEC.optionalFieldOf("line").forGetter(HillsideTerrain::line),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().optionalFieldOf("lining", List.of()).forGetter(HillsideTerrain::lining),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("overgrowth", 0.0F).forGetter(HillsideTerrain::overgrowth),
            BoundingBox.CODEC.listOf().optionalFieldOf("cover", List.of()).forGetter(HillsideTerrain::cover),
            BoundingBox.CODEC.listOf().optionalFieldOf("backfill", List.of()).forGetter(HillsideTerrain::backfill),
            BoundingBox.CODEC.listOf().optionalFieldOf("piles", List.of()).forGetter(HillsideTerrain::piles),
            BoundingBox.CODEC.optionalFieldOf("rubble_area").forGetter(HillsideTerrain::rubbleArea),
            BoundingBox.CODEC.optionalFieldOf("rubble_exclude").forGetter(HillsideTerrain::rubbleExclude),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("rubble_density", 0.0F).forGetter(HillsideTerrain::rubbleDensity),
            BlockPos.CODEC.listOf().optionalFieldOf("boulders", List.of()).forGetter(HillsideTerrain::boulders),
            BoundingBox.CODEC.listOf().optionalFieldOf("paths", List.of()).forGetter(HillsideTerrain::paths),
            BoundingBox.CODEC.optionalFieldOf("grade").forGetter(HillsideTerrain::grade),
            BoundingBox.CODEC.optionalFieldOf("bowl").forGetter(HillsideTerrain::bowl)
    ).apply(instance, HillsideTerrain::new));

    private static final int MAX_FILL_DEPTH = 12;
    private static final int TAPER = 3;
    private static final int FILL_TAPER = 6;
    public static final int MARGIN = 14;
    private static final int GRADE_RADIUS = 6;
    private static final int GRADE_RAMP = 2;
    private static final int BOWL_RADIUS = 8;
    private static final int PATH_FRINGE = 2;
    private static final int[] PATH_CHANCE = {92, 45, 15};
    private static final int CLUSTER_SIZE = 3;
    private static final int CLUSTER_JITTER = 2;
    private static final double RIM_WORN_FRACTION = 0.55;
    private static final double SPREAD_JITTER = 0.3;
    private static final float MAX_DRESS_DENSITY = 0.6F;
    private static final int PLUG_HEIGHT = 4;

    public void prepare(WorldGenLevel level, BoundingBox chunk, Vec3i size, Function<BlockPos, BlockPos> toWorld) {
        bowl.ifPresent(area -> bowl(level, chunk, area, toWorld));
        if (!cover.isEmpty()) {
            cover(level, chunk, size, toWorld, new java.util.ArrayList<>());
        }
    }

    private static void bowl(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld) {
        BlockState[] ground = sampleGround(level, chunk, area, toWorld);
        for (int x = area.minX() - BOWL_RADIUS; x <= area.maxX() + BOWL_RADIUS; x++) {
            for (int z = area.minZ() - BOWL_RADIUS; z <= area.maxZ() + BOWL_RADIUS; z++) {
                BlockPos base = toWorld.apply(new BlockPos(x, area.maxY(), z));
                if (!chunk.isInside(base.getX(), chunk.minY(), base.getZ())) continue;
                int dx = Math.max(0, Math.max(area.minX() - x, x - area.maxX()));
                int dz = Math.max(0, Math.max(area.minZ() - z, z - area.maxZ()));
                int distance = spread(dx, dz, base);
                if (distance > BOWL_RADIUS) continue;
                BlockPos floor = base.above(distance);
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, base.getX(), base.getZ()) - 1;
                boolean cut = false;
                for (int y = top; y > floor.getY(); y--) {
                    BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (!state.getFluidState().isEmpty() || !isCarvable(state)) break;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    cut = true;
                }
                if (cut && distance > 0 && level.getBlockState(floor).is(BlockTags.DIRT) && level.getBlockState(floor.above()).isAir()) {
                    level.setBlock(floor, ground[0], Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    public void apply(WorldGenLevel level, BoundingBox chunk, BoundingBox structure, Vec3i size, RandomSource random, Function<BlockPos, BlockPos> toWorld) {
        List<BlockPos> surface = new java.util.ArrayList<>();
        carve.ifPresent(area -> carve(level, chunk, area, toWorld));
        grade.ifPresent(area -> grade(level, chunk, area, size, toWorld, surface));
        fill.ifPresent(area -> fill(level, chunk, area, toWorld, surface));
        line.ifPresent(area -> line(level, chunk, area, toWorld));
        if (!backfill.isEmpty()) {
            backfill(level, chunk, size, toWorld, surface);
        }
        if (!piles.isEmpty()) {
            piles(level, chunk, toWorld);
        }
        if (!paths.isEmpty()) {
            paths(level, chunk, toWorld);
        }
        dress(level, chunk, surface, toWorld);
        if (rubbleDensity > 0.0F && rubbleArea.isPresent()) {
            rubble(level, chunk, toWorld);
        }
        if (!boulders.isEmpty()) {
            boulders(level, chunk, toWorld);
        }
        if (overgrowth > 0.0F) {
            overgrow(level, chunk, structure, random);
        }
        bowl.ifPresent(area -> blendRim(level, chunk, area, toWorld));
    }

    private static void blendRim(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld) {
        BlockState[] ground = sampleGround(level, chunk, area, toWorld);
        if (ground[0].is(Blocks.COARSE_DIRT)) return;
        double cx = (area.minX() + area.maxX()) / 2.0;
        double cz = (area.minZ() + area.maxZ()) / 2.0;
        double outer = Math.min(area.maxX() - area.minX(), area.maxZ() - area.minZ()) / 2.0 + 1.0;
        double inner = outer * RIM_WORN_FRACTION;
        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minZ(); z <= area.maxZ(); z++) {
                double distance = Math.hypot(x - cx, z - cz);
                if (distance < inner) continue;
                BlockPos pos = toWorld.apply(new BlockPos(x, area.maxY(), z));
                if (!chunk.isInside(pos)) continue;
                double blend = Math.min(1.0, (distance - inner) / (outer - inner));
                if (hash(pos.getX(), pos.getZ(), 31) % 100 >= blend * 100) continue;
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.COARSE_DIRT) && !state.is(Blocks.DIRT) && !state.is(Blocks.ROOTED_DIRT)) continue;
                if (!level.getBlockState(pos.above()).isAir()) continue;
                level.setBlock(pos, ground[0], Block.UPDATE_CLIENTS);
            }
        }
    }

    private void overgrow(WorldGenLevel level, BoundingBox chunk, BoundingBox structure, RandomSource random) {
        BoundingBox area = structure.inflatedBy(1);
        for (BlockPos pos : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
            if (!chunk.isInside(pos) || !level.getBlockState(pos).isAir()) continue;
            if (random.nextFloat() < overgrowth) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (VineBlock.isAcceptableNeighbour(level, pos.relative(side), side)) {
                        level.setBlock(pos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side), true), Block.UPDATE_CLIENTS);
                        break;
                    }
                }
            }
            if (level.getBlockState(pos).isAir() && random.nextFloat() < overgrowth * 0.5F && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                level.setBlock(pos, Blocks.MOSS_CARPET.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static void carve(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld) {
        for (BlockPos local : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
            BlockPos pos = toWorld.apply(local);
            if (chunk.isInside(pos) && isCarvable(level.getBlockState(pos))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static void grade(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Vec3i size, Function<BlockPos, BlockPos> toWorld, List<BlockPos> surface) {
        BlockState[] ground = sampleGround(level, chunk, area, toWorld);
        for (int x = area.minX() - GRADE_RADIUS; x <= area.maxX() + GRADE_RADIUS; x++) {
            for (int z = area.minZ() - GRADE_RADIUS; z <= area.maxZ(); z++) {
                if (x >= 0 && x < size.getX() && z >= 0 && z < size.getZ()) continue;
                int dx = Math.max(0, Math.max(area.minX() - x, x - area.maxX()));
                BlockPos base = toWorld.apply(new BlockPos(x, area.maxY(), z));
                int distance = spread(dx, Math.max(0, area.minZ() - z), base) + Math.max(0, z - area.maxZ() + GRADE_RAMP);
                if (distance <= 0) continue;
                BlockPos column = base.above(distance);
                if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ()) - 1;
                boolean cut = false;
                for (int y = top; y > column.getY(); y--) {
                    BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (!state.getFluidState().isEmpty() || !isCarvable(state)) break;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    cut = true;
                }
                if (cut && level.getBlockState(column).is(BlockTags.DIRT) && level.getBlockState(column.above()).isAir()) {
                    level.setBlock(column, ground[0], Block.UPDATE_CLIENTS);
                    surface.add(column);
                }
            }
        }
    }

    private static void fill(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld, List<BlockPos> surface) {
        BlockState[] ground = sampleGround(level, chunk, area, toWorld);
        for (int x = area.minX() - FILL_TAPER; x <= area.maxX() + FILL_TAPER; x++) {
            for (int z = area.minZ() - FILL_TAPER; z <= area.maxZ() + FILL_TAPER; z++) {
                BlockPos base = toWorld.apply(new BlockPos(x, area.maxY(), z));
                int distance = spread(Math.max(0, Math.max(area.minX() - x, x - area.maxX())), Math.max(0, Math.max(area.minZ() - z, z - area.maxZ())), base);
                BlockPos pos = base.below(distance);
                for (int depth = 0; depth < MAX_FILL_DEPTH && pos.getY() > level.getMinBuildHeight(); depth++, pos = pos.below()) {
                    if (!chunk.isInside(pos)) break;
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.canBeReplaced() && state.getFluidState().isEmpty()) {
                        if (depth == 0) continue;
                        break;
                    }
                    level.setBlock(pos, depth == 0 ? ground[0] : ground[1], Block.UPDATE_CLIENTS);
                    if (depth == 0) surface.add(pos.immutable());
                }
            }
        }
    }

    private void cover(WorldGenLevel level, BoundingBox chunk, Vec3i size, Function<BlockPos, BlockPos> toWorld, List<BlockPos> surface) {
        BlockState[] ground = sampleGround(level, chunk, fill.orElse(cover.get(0)), toWorld);
        int centerX = size.getX() / 2;
        List<BlockPos> placed = new java.util.ArrayList<>();
        for (BoundingBox area : cover) {
            int fromX = area.minX() - (area.minX() <= centerX ? TAPER : 0);
            int toX = area.maxX() + (area.maxX() >= centerX ? TAPER : 0);
            for (int x = fromX; x <= toX; x++) {
                for (int z = area.minZ(); z <= area.maxZ() + TAPER; z++) {
                    int dx = x < area.minX() ? area.minX() - x : Math.max(0, x - area.maxX());
                    int dz = Math.max(0, z - area.maxZ());
                    int top = area.maxY() - spread(dx, dz, toWorld.apply(new BlockPos(x, 0, z)));
                    int bottom = top - MAX_FILL_DEPTH;
                    for (int y = top; y >= bottom; y--) {
                        BlockPos pos = toWorld.apply(new BlockPos(x, y, z));
                        if (!chunk.isInside(pos)) continue;
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.canBeReplaced()) break;
                        level.setBlock(pos, ground[1], Block.UPDATE_CLIENTS);
                        placed.add(pos.immutable());
                    }
                }
            }
        }
        for (BlockPos pos : placed) {
            if (level.getBlockState(pos.above()).isAir()) {
                level.setBlock(pos, ground[0], Block.UPDATE_CLIENTS);
                surface.add(pos);
            }
        }
    }

    private void backfill(WorldGenLevel level, BoundingBox chunk, Vec3i size, Function<BlockPos, BlockPos> toWorld, List<BlockPos> surface) {
        BlockState[] ground = sampleGround(level, chunk, fill.orElse(backfill.get(0)), toWorld);
        int centerX = size.getX() / 2;
        List<BlockPos> placed = new java.util.ArrayList<>();
        for (BoundingBox area : backfill) {
            boolean left = area.maxX() < centerX;
            int fromX = left ? area.minX() - TAPER : area.minX();
            int toX = left ? area.maxX() : area.maxX() + TAPER;
            for (int x = fromX; x <= toX; x++) {
                for (int z = area.minZ(); z <= area.maxZ() + TAPER; z++) {
                    int dx = x < area.minX() ? area.minX() - x : Math.max(0, x - area.maxX());
                    int dz = Math.max(0, z - area.maxZ());
                    BlockPos base = toWorld.apply(new BlockPos(x, area.maxY(), z));
                    BlockPos pos = base.below(spread(dx, dz, base));
                    plug(level, chunk, pos, ground, placed);
                    for (int depth = 0; depth < MAX_FILL_DEPTH && pos.getY() > level.getMinBuildHeight(); depth++, pos = pos.below()) {
                        if (!chunk.isInside(pos)) continue;
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.canBeReplaced() && state.getFluidState().isEmpty()) break;
                        level.setBlock(pos, ground[1], Block.UPDATE_CLIENTS);
                        placed.add(pos.immutable());
                    }
                }
            }
        }
        for (BlockPos pos : placed) {
            if (level.getBlockState(pos.above()).isAir()) {
                level.setBlock(pos, ground[0], Block.UPDATE_CLIENTS);
                surface.add(pos);
            }
        }
    }

    private static void plug(WorldGenLevel level, BoundingBox chunk, BlockPos top, BlockState[] ground, List<BlockPos> placed) {
        for (int up = 1; up <= PLUG_HEIGHT; up++) {
            BlockPos pos = top.above(up);
            if (!chunk.isInside(pos)) return;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (!isGround(state)) return;
            for (int y = 1; y < up; y++) {
                level.setBlock(top.above(y), ground[1], Block.UPDATE_CLIENTS);
                placed.add(top.above(y));
            }
            return;
        }
    }

    private void piles(WorldGenLevel level, BoundingBox chunk, Function<BlockPos, BlockPos> toWorld) {
        BlockState[] ground = sampleGround(level, chunk, fill.orElse(piles.get(0)), toWorld);
        for (BoundingBox area : piles) {
            double cx = (area.minX() + area.maxX()) / 2.0;
            double cz = (area.minZ() + area.maxZ()) / 2.0;
            double rx = (area.maxX() - area.minX()) / 2.0 + 0.75;
            double rz = (area.maxZ() - area.minZ()) / 2.0 + 0.75;
            int height = area.maxY() - area.minY() + 1;
            for (int x = area.minX(); x <= area.maxX(); x++) {
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    double d = Math.sqrt(Math.pow((x - cx) / rx, 2) + Math.pow((z - cz) / rz, 2));
                    if (d >= 1.0) continue;
                    int columnHeight = (int) Math.round(height * (1.0 - d) + Math.floorMod(x * 31 + z * 17, 3) * 0.3 - 0.3);
                    if (columnHeight <= 0) continue;
                    BlockPos column = toWorld.apply(new BlockPos(x, 0, z));
                    if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                    BlockPos base = surface(level, column);
                    if (!level.getFluidState(base).isEmpty()) continue;
                    for (int dy = 1; dy <= columnHeight; dy++) {
                        BlockPos pos = base.above(dy);
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.canBeReplaced()) break;
                        level.setBlock(pos, pileBlock(pos, ground), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private BlockState pileBlock(BlockPos pos, BlockState[] ground) {
        if (clustered(pos, 41, 5) == 0 || lining.isEmpty()) return ground[1];
        return lining.get(clustered(pos, 43, lining.size())).defaultBlockState();
    }

    private static int spread(int dx, int dz, BlockPos column) {
        if (dx <= 0 && dz <= 0) return 0;
        double jitter = (hash(column.getX(), column.getZ(), 17) % 5 - 2) * SPREAD_JITTER;
        return Math.max(1, (int) Math.round(Math.sqrt(dx * dx + dz * dz) + jitter));
    }

    private void dress(WorldGenLevel level, BoundingBox chunk, List<BlockPos> surface, Function<BlockPos, BlockPos> toWorld) {
        if (surface.isEmpty()) return;
        Optional<BoundingBox> around = fill.or(() -> grade).or(() -> backfill.stream().findFirst());
        if (around.isEmpty()) return;
        Map<BlockState, Integer> plants = new HashMap<>();
        int columns = 0;
        BoundingBox area = around.get();
        int ring = FILL_TAPER + 2;
        for (int x = area.minX() - ring; x <= area.maxX() + ring; x++) {
            for (int z = area.minZ() - ring; z <= area.maxZ() + ring; z++) {
                if (x >= area.minX() - FILL_TAPER && x <= area.maxX() + FILL_TAPER && z >= area.minZ() - FILL_TAPER && z <= area.maxZ() + FILL_TAPER) continue;
                BlockPos column = toWorld.apply(new BlockPos(x, 0, z));
                if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                BlockPos top = surface(level, column);
                if (!isGround(level.getBlockState(top))) continue;
                columns++;
                BlockState plant = level.getBlockState(top.above());
                if (isPlant(plant)) plants.merge(plant, 1, Integer::sum);
            }
        }
        if (plants.isEmpty() || columns == 0) return;
        int total = plants.values().stream().mapToInt(Integer::intValue).sum();
        float density = Math.min(plants.keySet().stream().anyMatch(state -> state.is(Blocks.SNOW)) ? 1.0F : MAX_DRESS_DENSITY, total / (float) columns);
        List<BlockState> choices = new java.util.ArrayList<>();
        plants.forEach((state, count) -> {
            for (int i = 0; i < count; i++) choices.add(state);
        });
        for (BlockPos pos : surface) {
            if (!chunk.isInside(pos.above())) continue;
            if (hash(pos.getX(), pos.getZ(), 23) % 1000 >= density * 1000) continue;
            if (!isGround(level.getBlockState(pos)) || !level.getBlockState(pos.above()).isAir()) continue;
            BlockState plant = choices.get(hash(pos.getX(), pos.getZ(), 29) % choices.size());
            if (plant.canSurvive(level, pos.above())) {
                level.setBlock(pos.above(), plant, Block.UPDATE_CLIENTS);
            }
        }
    }

    private static boolean isPlant(BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty() || state.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock) return false;
        return state.is(BlockTags.SMALL_FLOWERS) || state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH)
                || state.is(BlockTags.SAPLINGS) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.MOSS_CARPET) || state.is(Blocks.PINK_PETALS) || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM);
    }

    private static int clustered(BlockPos pos, int salt, int choices) {
        int jitter = hash(pos.getX(), pos.getZ(), salt + pos.getY()) % CLUSTER_JITTER;
        int cx = Math.floorDiv(pos.getX() + jitter, CLUSTER_SIZE);
        int cy = Math.floorDiv(pos.getY() + jitter, CLUSTER_SIZE);
        int cz = Math.floorDiv(pos.getZ() + jitter, CLUSTER_SIZE);
        return hash(cx + cy * 7919, cz, salt) % choices;
    }

    private static int hash(int x, int z, int salt) {
        int h = x * 734287 + z * 912931 + salt * 438289;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }

    private static BlockPos surface(WorldGenLevel level, BlockPos column) {
        return new BlockPos(column.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ()) - 1, column.getZ());
    }

    private static boolean openAbove(WorldGenLevel level, BlockPos top) {
        BlockState above = level.getBlockState(top.above());
        return above.isAir() || above.canBeReplaced();
    }

    private void paths(WorldGenLevel level, BoundingBox chunk, Function<BlockPos, BlockPos> toWorld) {
        for (BoundingBox area : paths) {
            for (int x = area.minX() - PATH_FRINGE; x <= area.maxX() + PATH_FRINGE; x++) {
                for (int z = area.minZ() - PATH_FRINGE; z <= area.maxZ() + PATH_FRINGE; z++) {
                    int edge = Math.max(Math.max(area.minX() - x, x - area.maxX()), Math.max(area.minZ() - z, z - area.maxZ()));
                    edge = Math.max(0, edge);
                    BlockPos column = toWorld.apply(new BlockPos(x, 0, z));
                    if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                    if (hash(column.getX(), column.getZ(), 13) % 100 >= PATH_CHANCE[edge]) continue;
                    BlockPos top = surface(level, column);
                    BlockState state = level.getBlockState(top);
                    if (state.is(Blocks.SNOW)) {
                        if (hash(column.getX(), column.getZ(), 17) % 3 > 0) level.setBlock(top, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        top = top.below();
                        state = level.getBlockState(top);
                    }
                    BlockState worn = worn(state, top, edge);
                    if (worn == null) continue;
                    BlockState above = level.getBlockState(top.above());
                    if (!above.isAir() && (worn.is(Blocks.DIRT_PATH) || (edge == 0 && above.canBeReplaced()))) {
                        level.setBlock(top.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    level.setBlock(top, worn, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static BlockState worn(BlockState state, BlockPos pos, int edge) {
        Block[] mix;
        if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE)) {
            mix = new Block[]{Blocks.SAND, Blocks.SMOOTH_SANDSTONE, Blocks.SAND, Blocks.SANDSTONE, Blocks.COARSE_DIRT};
        } else if (state.is(Blocks.RED_SAND) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.RED_SANDSTONE)) {
            mix = new Block[]{Blocks.RED_SAND, Blocks.COARSE_DIRT, Blocks.RED_SAND, Blocks.RED_SANDSTONE, Blocks.TERRACOTTA};
        } else if (state.is(BlockTags.DIRT)) {
            mix = edge == 0
                    ? new Block[]{Blocks.DIRT_PATH, Blocks.DIRT_PATH, Blocks.COARSE_DIRT, Blocks.DIRT_PATH, Blocks.GRAVEL, Blocks.ROOTED_DIRT, Blocks.COARSE_DIRT, Blocks.PACKED_MUD}
                    : new Block[]{Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.GRAVEL, Blocks.DIRT_PATH, Blocks.COARSE_DIRT};
        } else if (state.is(Blocks.GRAVEL) || state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            mix = new Block[]{Blocks.GRAVEL, Blocks.COBBLESTONE, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.ANDESITE};
        } else {
            return null;
        }
        return mix[clustered(pos, 19, mix.length)].defaultBlockState();
    }

    private void rubble(WorldGenLevel level, BoundingBox chunk, Function<BlockPos, BlockPos> toWorld) {
        BoundingBox area = rubbleArea.get();
        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minZ(); z <= area.maxZ(); z++) {
                if (rubbleExclude.isPresent() && rubbleExclude.get().isInside(x, rubbleExclude.get().minY(), z)) continue;
                BlockPos column = toWorld.apply(new BlockPos(x, 0, z));
                if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                if (hash(column.getX(), column.getZ(), 3) % 1000 >= rubbleDensity * 1000) continue;
                BlockPos top = surface(level, column);
                if (!isGround(level.getBlockState(top)) || !openAbove(level, top)) continue;
                int pick = hash(column.getX(), column.getZ(), 5) % 20;
                BlockState ground = level.getBlockState(top);
                if (ground.is(BlockTags.SAND)) {
                    boolean red = ground.is(Blocks.RED_SAND);
                    if (pick < 13) continue;
                    BlockState piece = pick < 16 ? (red ? Blocks.RED_SANDSTONE_SLAB : Blocks.SANDSTONE_SLAB).defaultBlockState()
                            : pick < 18 ? (red ? Blocks.RED_SANDSTONE : Blocks.SANDSTONE).defaultBlockState()
                              : (red ? Blocks.CUT_RED_SANDSTONE : Blocks.SMOOTH_SANDSTONE).defaultBlockState();
                    level.setBlock(top.above(), piece, Block.UPDATE_CLIENTS);
                    continue;
                }
                if (pick < 6) {
                    level.setBlock(top, Blocks.GRAVEL.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else if (pick < 9) {
                    level.setBlock(top, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else if (pick < 13) {
                    level.setBlock(top.above(), Blocks.STONE_BUTTON.defaultBlockState().setValue(net.minecraft.world.level.block.ButtonBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR), Block.UPDATE_CLIENTS);
                } else if (pick < 16) {
                    level.setBlock(top.above(), (pick % 2 == 0 ? Blocks.COBBLESTONE_SLAB : Blocks.STONE_SLAB).defaultBlockState(), Block.UPDATE_CLIENTS);
                } else if (pick < 18) {
                    level.setBlock(top.above(), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(top.above(), Blocks.ANDESITE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private void boulders(WorldGenLevel level, BoundingBox chunk, Function<BlockPos, BlockPos> toWorld) {
        BlockState[] rocks = lining.isEmpty()
                ? new BlockState[]{Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState()}
                : lining.stream().map(Block::defaultBlockState).toArray(BlockState[]::new);
        for (BlockPos local : boulders) {
            BlockPos center = toWorld.apply(new BlockPos(local.getX(), 0, local.getZ()));
            int size = 1 + hash(center.getX(), center.getZ(), 11) % 2;
            BlockPos base = surface(level, center);
            if (!level.getFluidState(base).isEmpty()) continue;
            for (int dx = -size; dx <= size; dx++) {
                for (int dz = -size; dz <= size; dz++) {
                    for (int dy = 0; dy <= size; dy++) {
                        double d = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.6);
                        if (d > size + 0.35 + (hash(center.getX() + dx, center.getZ() + dz, dy) % 3) * 0.15) continue;
                        BlockPos pos = base.offset(dx, dy, dz);
                        if (!chunk.isInside(pos)) continue;
                        BlockState state = level.getBlockState(pos);
                        if (dy > 0 && !state.isAir() && !state.canBeReplaced()) continue;
                        level.setBlock(pos, rocks[clustered(pos, 47, rocks.length)], Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static BlockState[] sampleGround(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld) {
        Map<BlockState, Integer> tops = new HashMap<>();
        Map<BlockState, Integer> unders = new HashMap<>();
        int ring = FILL_TAPER + 2;
        for (int x = area.minX() - ring; x <= area.maxX() + ring; x++) {
            for (int z = area.minZ() - ring; z <= area.maxZ() + ring; z++) {
                boolean outside = x < area.minX() - FILL_TAPER || x > area.maxX() + FILL_TAPER || z < area.minZ() - FILL_TAPER || z > area.maxZ() + FILL_TAPER;
                if (!outside) continue;
                BlockPos column = toWorld.apply(new BlockPos(x, 0, z));
                if (!chunk.isInside(column.getX(), chunk.minY(), column.getZ())) continue;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ()) - 1;
                BlockPos top = new BlockPos(column.getX(), y, column.getZ());
                BlockState topState = level.getBlockState(top);
                BlockState underState = level.getBlockState(top.below());
                if (isGround(topState)) tops.merge(topState, 1, Integer::sum);
                if (isGround(underState)) unders.merge(underState, 1, Integer::sum);
            }
        }
        BlockState top = mostCommon(tops, Blocks.GRASS_BLOCK.defaultBlockState());
        BlockState under = stable(mostCommon(unders, Blocks.DIRT.defaultBlockState()));
        return new BlockState[]{top, under};
    }

    private static BlockState stable(BlockState state) {
        if (!(state.getBlock() instanceof FallingBlock)) return state;
        if (state.is(Blocks.SAND) || state.is(Blocks.SUSPICIOUS_SAND)) return Blocks.SANDSTONE.defaultBlockState();
        if (state.is(Blocks.RED_SAND)) return Blocks.RED_SANDSTONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private static BlockState mostCommon(Map<BlockState, Integer> counts, BlockState fallback) {
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(fallback);
    }

    private void line(WorldGenLevel level, BoundingBox chunk, BoundingBox area, Function<BlockPos, BlockPos> toWorld) {
        if (lining.isEmpty()) return;
        for (BlockPos local : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
            BlockPos pos = toWorld.apply(local);
            if (!chunk.isInside(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.DIRT) || !touchesAir(level, pos)) continue;
            Block rock = lining.get(clustered(pos, 53, lining.size()));
            level.setBlock(pos, rock.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean touchesAir(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockPos neighbor = pos.relative(direction);
            if (level.getBlockState(neighbor).isAir() && !level.canSeeSky(neighbor)) return true;
        }
        return false;
    }

    private static boolean isGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE);
    }

    private static boolean isCarvable(BlockState state) {
        return !state.isAir() && (isGround(state) || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(Blocks.SNOW) || state.canBeReplaced());
    }
}