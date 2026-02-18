package alabaster.prospect.common.registry;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProspectModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "prospect");

    public static final DeferredHolder<EntityType<?>, EntityType<SparkleNodeEntity>> SPARKLE_NODE =
            ENTITIES.register("sparkle_node",
                    () -> EntityType.Builder
                            .<SparkleNodeEntity>of(SparkleNodeEntity::new, MobCategory.MISC)
                            .sized(1f, 1f)
                            .clientTrackingRange(32)
                            .updateInterval(3)
                            .build(ResourceLocation.fromNamespaceAndPath("prospect", "sparkle_node").toString()));
}
