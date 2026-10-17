package alabaster.prospect;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    public static ModConfigSpec COMMON_CONFIG;
    private static final Map<String, ModConfigSpec.BooleanValue> ITEMS = new HashMap<>();

    public static ModConfigSpec.BooleanValue ENABLE_MINING_HELMET_OVERLAY;
    public static ModConfigSpec.BooleanValue ENABLE_GUNPOWDER_FUSE_PLACEMENT;
    public static ModConfigSpec.BooleanValue ENABLE_WANDERING_PROSPECTOR;
    public static ModConfigSpec.EnumValue<HelmetLightMode> MINING_HELMET_LIGHT_MODE;

    public enum HelmetLightMode {
        AUTO,
        BLOCKS,
        DYNAMIC_LIGHTS
    }

    public Config() {
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        ENABLE_MINING_HELMET_OVERLAY = COMMON_BUILDER
                .comment("Whether the vignette glow overlay is shown while wearing a lit mining helmet underground.")
                .define("enableMiningHelmetOverlay", true);

        ENABLE_GUNPOWDER_FUSE_PLACEMENT = COMMON_BUILDER
                .comment("Whether right-clicking gunpowder against a block places a gunpowder fuse.")
                .define("enableGunpowderFusePlacement", true);

        ENABLE_WANDERING_PROSPECTOR = COMMON_BUILDER
                .comment("Whether the Wandering Prospector periodically spawns near players in the Overworld.")
                .define("enableWanderingProspector", true);

        MINING_HELMET_LIGHT_MODE = COMMON_BUILDER
                .comment("How the mining helmet lights up its surroundings.",
                        "AUTO: use LambDynamicLights when it is installed, otherwise place invisible light blocks.",
                        "BLOCKS: always place invisible light blocks, which every player sees.",
                        "DYNAMIC_LIGHTS: never place light blocks; only players running LambDynamicLights see the light.")
                .defineEnum("miningHelmetLightMode", HelmetLightMode.AUTO);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}