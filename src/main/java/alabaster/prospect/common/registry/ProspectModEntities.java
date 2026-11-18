package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.entity.grapple.GrapplingHookEntity;
import alabaster.prospect.common.entity.grapple.ZiplineEntity;
import alabaster.prospect.common.entity.prospector.WanderingProspector;
import alabaster.prospect.common.entity.prospector.ZombieProspector;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ProspectModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "prospect");

    public static final DeferredHolder<EntityType<?>, EntityType<SparkleNodeEntity>> SPARKLE_NODE =
            ENTITIES.register("sparkle_node",
                    () -> EntityType.Builder
                            .<SparkleNodeEntity>of(SparkleNodeEntity::new, MobCategory.MISC)
                            .sized(1f, 0.1f)
                            .clientTrackingRange(32)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "sparkle_node").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<WanderingProspector>> WANDERING_PROSPECTOR =
            ENTITIES.register("wandering_prospector",
                    () -> EntityType.Builder
                            .<WanderingProspector>of(WanderingProspector::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.95f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(10)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "wandering_prospector").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ZombieProspector>> ZOMBIE_PROSPECTOR =
            ENTITIES.register("zombie_prospector",
                    () -> EntityType.Builder
                            .<ZombieProspector>of(ZombieProspector::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .eyeHeight(1.74f)
                            .passengerAttachments(2.125f)
                            .ridingOffset(-0.7f)
                            .clientTrackingRange(8)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "zombie_prospector").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DynamiteEntity>> DYNAMITE =
            ENTITIES.register("dynamite",
                    () -> EntityType.Builder
                            .<DynamiteEntity>of(DynamiteEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "dynamite").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<GrapplingHookEntity>> GRAPPLING_HOOK =
            ENTITIES.register("grappling_hook",
                    () -> EntityType.Builder
                            .<GrapplingHookEntity>of(GrapplingHookEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "grappling_hook").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineEntity>> ZIPLINE =
            ENTITIES.register("zipline",
                    () -> EntityType.Builder
                            .<ZiplineEntity>of(ZiplineEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(Integer.MAX_VALUE)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "zipline").toString()));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WANDERING_PROSPECTOR.get(), Mob.createMobAttributes().build());
        event.put(ZOMBIE_PROSPECTOR.get(), ZombieProspector.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ZOMBIE_PROSPECTOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ZombieProspector::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityType.GLOW_SQUID, (SpawnPlacementType) null, null,
                ProspectModEntities::checkGroveGlowSquid, RegisterSpawnPlacementsEvent.Operation.OR);
    }

    private static boolean checkGroveGlowSquid(EntityType<GlowSquid> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos).is(Blocks.WATER) && level.getBiome(pos).is(ProspectBiomes.GLOWSHROOM_GROVE);
    }
}