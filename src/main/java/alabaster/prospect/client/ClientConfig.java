package alabaster.prospect.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue GLOW_OVERLAY;
    public static final ModConfigSpec.BooleanValue RANGE_INDICATOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("miningHelmet");
        GLOW_OVERLAY = builder
                .comment("Whether the vignette glow overlay is shown while wearing a lit mining helmet underground.")
                .define("glowOverlay", true);
        builder.pop();

        builder.push("grappling");
        RANGE_INDICATOR = builder
                .comment("Whether brackets appear around the crosshair when a grappling hook would land on a block within rope length.")
                .define("rangeIndicator", true);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
