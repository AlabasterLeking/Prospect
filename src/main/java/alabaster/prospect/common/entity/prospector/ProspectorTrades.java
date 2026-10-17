package alabaster.prospect.common.entity.prospector;

import alabaster.prospect.common.prospecting.ProspectingGems;
import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.registry.ProspectDataComponents;
import alabaster.prospect.common.registry.ProspectEnchantments;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProspectorTrades {
    private ProspectorTrades() {}

    private static final ResourceLocation[] BASE_SOCKET_GEMS = {
            ProspectingGems.QUARTZ, ProspectingGems.DIAMOND, ProspectingGems.EMERALD, ProspectingGems.LAPIS, ProspectingGems.AMETHYST
    };

    public static final VillagerTrades.ItemListing[] COMMON = {
            (trader, random) -> sell(new ItemStack(ProspectModItems.COPPER_PAN.get()), 3, 4),
            (trader, random) -> sell(new ItemStack(ProspectModItems.IRON_PAN.get()), 6, 2),
            (trader, random) -> sell(new ItemStack(ProspectModItems.PROSPECTING_PICKAXE.get()), 10, 1),
            (trader, random) -> sell(new ItemStack(Items.RAW_COPPER, 8), 1, 8),
            (trader, random) -> sell(new ItemStack(Items.RAW_IRON, 4), 2, 6),
            (trader, random) -> sell(new ItemStack(Items.RAW_GOLD, 3), 3, 4),
            (trader, random) -> sell(new ItemStack(Items.RAIL, 8), 1, 8),
            (trader, random) -> sell(new ItemStack(Items.GUNPOWDER, 4), 1, 8),
            (trader, random) -> sell(new ItemStack(ProspectModItems.DYNAMITE.get(), 4), 3, 6),
            ProspectorTrades::dyedMiningHelmet,
            ProspectorTrades::foreignGem
    };

    public static final VillagerTrades.ItemListing[] RARE = {
            ProspectorTrades::mineshaftMap,
            (trader, random) -> new MerchantOffer(new ItemCost(ProspectModItems.RUBY.get(), 3), new ItemStack(ProspectModItems.GOLDEN_PAN.get()), 1, 1, 0.05F),
            ProspectorTrades::panningLuckBook,
            ProspectorTrades::socketedPickaxe
    };

    private static MerchantOffer sell(ItemStack result, int emeralds, int maxUses) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), result, maxUses, 1, 0.05F);
    }

    private static MerchantOffer dyedMiningHelmet(Entity trader, RandomSource random) {
        ItemStack helmet = new ItemStack(ProspectModItems.MINING_HELMET.get());
        ItemStack dyed = DyedItemColor.applyDyes(helmet, List.of(DyeItem.byColor(DyeColor.byId(random.nextInt(16)))));
        return sell(dyed.isEmpty() ? helmet : dyed, 8, 1);
    }

    private static MerchantOffer foreignGem(Entity trader, RandomSource random) {
        Holder<Biome> biome = trader.level().getBiome(trader.blockPosition());
        List<Item> gems = new ArrayList<>();
        if (!biome.is(Tags.Biomes.IS_DRY)) gems.add(ProspectModItems.RUBY.get());
        if (!biome.is(Tags.Biomes.IS_COLD)) gems.add(ProspectModItems.SAPPHIRE.get());
        if (!biome.is(Tags.Biomes.IS_JUNGLE)) gems.add(ProspectModItems.TOPAZ.get());
        if (gems.isEmpty()) return null;
        return sell(new ItemStack(gems.get(random.nextInt(gems.size()))), 8, 3);
    }

    private static MerchantOffer mineshaftMap(Entity trader, RandomSource random) {
        if (!(trader.level() instanceof ServerLevel level)) return null;

        BlockPos target = level.findNearestMapStructure(StructureTags.MINESHAFT, trader.blockPosition(), 100, true);
        if (target == null) return null;

        ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.prospect.mineshaft"));

        return new MerchantOffer(new ItemCost(ProspectModItems.TOPAZ.get(), 2), Optional.of(new ItemCost(Items.EMERALD, 4)), map, 1, 1, 0.05F);
    }

    private static MerchantOffer panningLuckBook(Entity trader, RandomSource random) {
        Optional<Holder.Reference<Enchantment>> enchantment = trader.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ProspectEnchantments.PANNING_LUCK);
        if (enchantment.isEmpty()) return null;

        int level = 1 + random.nextInt(enchantment.get().value().getMaxLevel());
        ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment.get(), level));
        return new MerchantOffer(new ItemCost(ProspectModItems.SAPPHIRE.get(), level + 1), book, 1, 1, 0.05F);
    }

    private static MerchantOffer socketedPickaxe(Entity trader, RandomSource random) {
        ResourceLocation gem = BASE_SOCKET_GEMS[random.nextInt(BASE_SOCKET_GEMS.length)];
        ItemStack pickaxe = new ItemStack(ProspectModItems.PROSPECTING_PICKAXE.get());
        pickaxe.set(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY.with(gem));

        List<Item> prices = List.of(ProspectModItems.RUBY.get(), ProspectModItems.SAPPHIRE.get(), ProspectModItems.TOPAZ.get());
        Item price = prices.get(random.nextInt(prices.size()));
        int count = gem.equals(ProspectingGems.DIAMOND) ? 6 : 4;

        return new MerchantOffer(new ItemCost(price, count), Optional.of(new ItemCost(Items.EMERALD, 8)), pickaxe, 1, 1, 0.05F);
    }
}