package alabaster.prospect.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue GLOW_OVERLAY;
    public static final ModConfigSpec.BooleanValue RANGE_INDICATOR;
    public static final ModConfigSpec.BooleanValue ROPE_TENSION;
    public static final ModConfigSpec.BooleanValue CLOCK_READOUT;
    public static final ModConfigSpec.BooleanValue COMPASS_READOUT;
    public static final ModConfigSpec.BooleanValue RECOVERY_COMPASS_READOUT;

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
        ROPE_TENSION = builder
                .comment("Whether the camera tightens slightly when a grappling rope snaps taut or strains during a fast swing.")
                .define("ropeTension", true);
        builder.pop();

        builder.push("instruments");
        CLOCK_READOUT = builder
                .comment("Whether holding a Clock shows the day and time above the hotbar. Disable if another mod already does this.")
                .define("clockReadout", true);
        COMPASS_READOUT = builder
                .comment("Whether holding a Compass shows your coordinates and facing above the hotbar, with the distance to its target in the tooltip. Disable if another mod already does this.")
                .define("compassReadout", true);
        RECOVERY_COMPASS_READOUT = builder
                .comment("Whether holding a Recovery Compass shows your coordinates and facing above the hotbar, with your last death location in the tooltip. Disable if another mod already does this.")
                .define("recoveryCompassReadout", true);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}