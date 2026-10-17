package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.entity.prospector.WanderingProspector;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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

    public static final DeferredHolder<EntityType<?>, EntityType<DynamiteEntity>> DYNAMITE =
            ENTITIES.register("dynamite",
                    () -> EntityType.Builder
                            .<DynamiteEntity>of(DynamiteEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "dynamite").toString()));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WANDERING_PROSPECTOR.get(), Mob.createMobAttributes().build());
    }
}