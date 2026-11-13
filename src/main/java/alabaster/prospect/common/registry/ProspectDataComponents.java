package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.item.trinket.SmelterState;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ProspectDataComponents {
    private ProspectDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Prospect.MODID);

    public static final Supplier<DataComponentType<ProspectingSockets>> PROSPECTING_SOCKETS = DATA_COMPONENTS.register(
            "prospecting_sockets",
            () -> DataComponentType.<ProspectingSockets>builder()
                    .persistent(ProspectingSockets.CODEC)
                    .networkSynchronized(ProspectingSockets.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<Integer>> FUSE = DATA_COMPONENTS.register(
            "fuse",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final Supplier<DataComponentType<String>> QUARTZ_LOCK = DATA_COMPONENTS.register(
            "quartz_lock",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static final Supplier<DataComponentType<ItemContainerContents>> SMELTER_ITEMS = DATA_COMPONENTS.register(
            "smelter_items",
            () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );

    public static final Supplier<DataComponentType<SmelterState>> SMELTER_STATE = DATA_COMPONENTS.register(
            "smelter_state",
            () -> DataComponentType.<SmelterState>builder()
                    .persistent(SmelterState.CODEC)
                    .networkSynchronized(SmelterState.STREAM_CODEC)
                    .build()
    );
}