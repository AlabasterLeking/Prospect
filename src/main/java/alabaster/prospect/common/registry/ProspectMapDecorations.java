package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ProspectMapDecorations {
    private ProspectMapDecorations() {}

    private static final int ABANDONED_MINE_COLOR = 0x8B6A3E;
    private static final int PROSPECTOR_CAMP_COLOR = 0xB9B2A2;

    public static final DeferredRegister<MapDecorationType> MAP_DECORATIONS = DeferredRegister.create(Registries.MAP_DECORATION_TYPE, Prospect.MODID);

    public static final DeferredHolder<MapDecorationType, MapDecorationType> ABANDONED_MINE = MAP_DECORATIONS.register("abandoned_mine",
            () -> new MapDecorationType(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "abandoned_mine"), true, ABANDONED_MINE_COLOR, true, false));

    public static final DeferredHolder<MapDecorationType, MapDecorationType> PROSPECTOR_CAMP = MAP_DECORATIONS.register("prospector_camp",
            () -> new MapDecorationType(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "prospector_camp"), true, PROSPECTOR_CAMP_COLOR, true, false));
}