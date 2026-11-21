package alabaster.prospect.common.utilities;

import alabaster.prospect.Prospect;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ProspectAdvancements {
    private ProspectAdvancements() {}

    public static final String TRIGGERED = "triggered";

    public static final String ROOT = "root";
    public static final String DEAD_END = "dead_end";
    public static final String TRIAL_BY_FIRE = "trial_by_fire";
    public static final String BRIGHT_IDEA = "bright_idea";
    public static final String ROCKHOUND = "rockhound";
    public static final String GEM_COLLECTOR = "gem_collector";
    public static final String STRUCK_A_DEAL = "struck_a_deal";
    public static final String FIRE_IN_THE_HOLE = "fire_in_the_hole";
    public static final String HOT_POTATO = "hot_potato";
    public static final String PAY_DIRT = "pay_dirt";
    public static final String CATCHING_AIR = "catching_air";
    public static final String LIFTOFF = "liftoff";
    public static final String BACK_FROM_THE_DEAD = "back_from_the_dead";
    public static final String HOOKED_ON_A_FEELING = "hooked_on_a_feeling";
    public static final String ZIPPEE = "zippee";
    public static final String GET_OVER_HERE = "get_over_here";
    public static final String SLIME_TIME = "slime_time";
    public static final String SUGAR_RUSH = "sugar_rush";
    public static final String ROCK_BOTTOM = "rock_bottom";
    public static final String HEAD_IN_THE_CLOUDS = "head_in_the_clouds";
    public static final String SHATTERPROOF = "shatterproof";

    public static void award(ServerPlayer player, String name) {
        AdvancementHolder advancement = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name));
        if (advancement != null) {
            player.getAdvancements().award(advancement, TRIGGERED);
        }
    }
}