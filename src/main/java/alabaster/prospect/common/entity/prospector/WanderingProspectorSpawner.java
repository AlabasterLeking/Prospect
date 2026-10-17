package alabaster.prospect.common.entity.prospector;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Prospect.MODID)
public class WanderingProspectorSpawner {

    public static final TagKey<Biome> FAVORED_BIOMES = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "wandering_prospector_favored"));

    private static final int TICK_DELAY = 1200;
    private static final int SPAWN_DELAY = 24000;
    private static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int DESPAWN_DELAY = 48000;
    private static final int UNDERGROUND_DEPTH = 20;
    private static final int CAVE_SEARCH_RADIUS = 24;

    private static int tickDelay = TICK_DELAY;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        if (!Config.ENABLE_WANDERING_PROSPECTOR.get()) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) return;
        if (--tickDelay > 0) return;
        tickDelay = TICK_DELAY;

        SpawnerData data = SpawnerData.get(level);
        data.spawnDelay -= TICK_DELAY;
        data.setDirty();
        if (data.spawnDelay > 0) return;

        data.spawnDelay = SPAWN_DELAY;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;

        int chance = data.spawnChance;
        data.spawnChance = Mth.clamp(chance + 25, MIN_SPAWN_CHANCE, MAX_SPAWN_CHANCE);
        if (level.random.nextInt(100) > chance) return;

        if (spawn(level)) {
            data.spawnChance = MIN_SPAWN_CHANCE;
        }
    }

    private static boolean spawn(ServerLevel level) {
        ServerPlayer player = level.getRandomPlayer();
        if (player == null || level.random.nextInt(10) == 0) return false;

        BlockPos playerPos = player.blockPosition();
        boolean underground = playerPos.getY() < level.getSeaLevel() - UNDERGROUND_DEPTH && !level.canSeeSky(playerPos);
        if (!underground && !level.getBiome(playerPos).is(FAVORED_BIOMES) && level.random.nextInt(3) != 0) return false;

        BlockPos pos = underground ? findCaveSpawnPosition(level, playerPos) : findSpawnPositionNear(level, playerPos, 48);
        if (pos == null || !hasEnoughSpace(level, pos)) return false;
        if (level.getBiome(pos).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) return false;

        WanderingProspector prospector = ProspectModEntities.WANDERING_PROSPECTOR.get().spawn(level, pos, MobSpawnType.EVENT);
        if (prospector == null) return false;

        if (!underground) {
            spawnPackMule(level, prospector);
        }
        prospector.setDespawnDelay(DESPAWN_DELAY);
        prospector.setWanderTarget(playerPos);
        prospector.restrictTo(playerPos, 16);
        return true;
    }

    private static void spawnPackMule(ServerLevel level, WanderingProspector prospector) {
        BlockPos pos = findSpawnPositionNear(level, prospector.blockPosition(), 4);
        if (pos == null) return;

        Mule mule = EntityType.MULE.create(level);
        if (mule == null) return;

        mule.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        mule.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        mule.setChest(true);
        mule.equipSaddle(new ItemStack(Items.SADDLE), null);
        mule.addTag(WanderingProspector.PACK_MULE_TAG);
        level.addFreshEntity(mule);
        mule.setLeashedTo(prospector, true);
    }

    private static BlockPos findSpawnPositionNear(ServerLevel level, BlockPos center, int maxDistance) {
        EntityType<WanderingProspector> type = ProspectModEntities.WANDERING_PROSPECTOR.get();
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + level.random.nextInt(maxDistance * 2) - maxDistance;
            int z = center.getZ() + level.random.nextInt(maxDistance * 2) - maxDistance;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, candidate, type)) {
                return candidate;
            }
        }
        return null;
    }

    private static BlockPos findCaveSpawnPosition(ServerLevel level, BlockPos center) {
        for (int i = 0; i < 20; i++) {
            BlockPos candidate = center.offset(
                    level.random.nextInt(CAVE_SEARCH_RADIUS * 2 + 1) - CAVE_SEARCH_RADIUS,
                    level.random.nextInt(13) - 6,
                    level.random.nextInt(CAVE_SEARCH_RADIUS * 2 + 1) - CAVE_SEARCH_RADIUS);
            if (candidate.distSqr(center) < 64 || level.canSeeSky(candidate)) continue;
            if (!level.getFluidState(candidate).isEmpty()) continue;

            BlockPos below = candidate.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (isOpenCave(level, candidate)) return candidate;
        }
        return null;
    }

    private static boolean isOpenCave(ServerLevel level, BlockPos pos) {
        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 2, 1))) {
            if (!level.getBlockState(check).isAir()) return false;
        }
        return true;
    }

    private static boolean hasEnoughSpace(BlockGetter level, BlockPos pos) {
        for (BlockPos check : BlockPos.betweenClosed(pos, pos.offset(1, 2, 1))) {
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static class SpawnerData extends SavedData {
        private static final String NAME = "prospect_wandering_prospector";

        private int spawnDelay = SPAWN_DELAY;
        private int spawnChance = MIN_SPAWN_CHANCE;

        public static SpawnerData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SpawnerData::new, SpawnerData::load, null), NAME);
        }

        private static SpawnerData load(CompoundTag tag, HolderLookup.Provider registries) {
            SpawnerData data = new SpawnerData();
            data.spawnDelay = tag.getInt("SpawnDelay");
            data.spawnChance = tag.getInt("SpawnChance");
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("SpawnDelay", spawnDelay);
            tag.putInt("SpawnChance", spawnChance);
            return tag;
        }
    }
}