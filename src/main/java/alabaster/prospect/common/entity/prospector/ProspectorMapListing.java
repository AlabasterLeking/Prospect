package alabaster.prospect.common.entity.prospector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class ProspectorMapListing implements VillagerTrades.ItemListing {

    private static final int SEARCH_RADIUS = 50;

    private final Supplier<ItemCost> cost;
    private final TagKey<Structure> destination;
    private final String displayName;
    private final Holder<MapDecorationType> decoration;
    private final int maxUses;
    private final int xp;

    public ProspectorMapListing(Supplier<ItemCost> cost, TagKey<Structure> destination, String displayName, Holder<MapDecorationType> decoration, int maxUses, int xp) {
        this.cost = cost;
        this.destination = destination;
        this.displayName = displayName;
        this.decoration = decoration;
        this.maxUses = maxUses;
        this.xp = xp;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (!(trader.level() instanceof ServerLevel level)) return null;
        BlockPos target = level.findNearestMapStructure(destination, trader.blockPosition(), SEARCH_RADIUS, true);
        if (target == null) return null;
        ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", decoration);
        map.set(DataComponents.ITEM_NAME, Component.translatable(displayName));
        return new MerchantOffer(cost.get(), Optional.of(new ItemCost(Items.COMPASS)), map, maxUses, xp, 0.2F);
    }
}
