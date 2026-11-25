package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.world.phys.AABB;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SparkleNodeSpawnHandler {

    private static final int SPAWN_INTERVAL_TICKS = 20;
    private static final double NODE_COUNT_RADIUS = 32.0;
    private static final int CANDIDATES_PER_ATTEMPT = 8;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % SPAWN_INTERVAL_TICKS != 0) return;

        RandomSource random = level.random;
        float chance = Config.SPARKLE_NODE_CHANCE.get().floatValue();
        int maxNodes = Config.MAX_SPARKLE_NODES.get();
        if (chance <= 0.0F || maxNodes <= 0) return;

        Set<Long> visited = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            AABB nearby = player.getBoundingBox().inflate(NODE_COUNT_RADIUS, level.getHeight(), NODE_COUNT_RADIUS);
            if (level.getEntitiesOfClass(SparkleNodeEntity.class, nearby).size() >= maxNodes) continue;
            ChunkPos center = player.chunkPosition();

            for (int cx = center.x - 1; cx <= center.x + 1; cx++) {
                for (int cz = center.z - 1; cz <= center.z + 1; cz++) {
                    long key = ChunkPos.asLong(cx, cz);
                    if (!visited.add(key)) continue;

                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    if (random.nextFloat() >= chance) continue;

                    trySpawnInChunk(level, chunk.getPos(), random);
                }
            }
        }
    }

    private static void trySpawnInChunk(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();

        for (int i = 0; i < CANDIDATES_PER_ATTEMPT; i++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);

            int seaLevel = level.getSeaLevel();
            BlockPos candidate = findFluidSurface(level, x, z, seaLevel + 16, level.getMinBuildHeight());

            if (candidate != null) {
                if (SparkleNodeSpawner.trySpawn(level, candidate)) {
                    return;
                }
            }
        }
    }

    private static BlockPos findFluidSurface(ServerLevel level, int x, int z, int topY, int bottomY) {
        for (int y = topY; y >= bottomY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isFluidSurface(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isFluidSurface(ServerLevel level, BlockPos pos) {
        var fluid = level.getFluidState(pos);
        if (fluid.isEmpty()) return false;

        boolean isWater = fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER);
        boolean isLava = fluid.is(Fluids.LAVA) || fluid.is(Fluids.FLOWING_LAVA);
        if (!isWater && !isLava) return false;

        var aboveFluid = level.getFluidState(pos.above());
        return aboveFluid.isEmpty();
    }
}