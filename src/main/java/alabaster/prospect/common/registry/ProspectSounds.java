package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProspectSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Prospect.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GRAPPLING_HOOK_REEL = register("item.grappling_hook.reel");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_RIDE = register("entity.zipline.ride");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARKLE_NODE_SHIMMER = register("entity.sparkle_node.shimmer");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAN_SIFT = register("item.pan.sift");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAN_SIFT_LAVA = register("item.pan.sift_lava");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name)));
    }
}
