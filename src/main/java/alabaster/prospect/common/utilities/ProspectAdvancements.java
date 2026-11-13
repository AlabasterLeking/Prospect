package alabaster.prospect.common.utilities;

import alabaster.prospect.Prospect;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ProspectAdvancements {
    private ProspectAdvancements() {}

    public static final String FIRE_IN_THE_HOLE = "fire_in_the_hole";
    public static final String HOT_POTATO = "hot_potato";
    public static final String PAY_DIRT = "pay_dirt";
    public static final String CATCHING_AIR = "catching_air";
    public static final String LIFTOFF = "liftoff";
    public static final String BACK_FROM_THE_DEAD = "back_from_the_dead";

    public static void award(ServerPlayer player, String name) {
        AdvancementHolder advancement = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "triggered");
        }
    }
}