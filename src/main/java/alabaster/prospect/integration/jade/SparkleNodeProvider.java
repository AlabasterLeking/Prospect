package alabaster.prospect.integration.jade;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.prospecting.PanningTables;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum SparkleNodeProvider implements IEntityComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "sparkle_node");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof SparkleNodeEntity node)) return;
        boolean lava = node.isInLava();
        ResourceLocation biome = node.level().getBiome(node.blockPosition()).unwrapKey().map(key -> key.location()).orElse(null);
        String table = PanningTables.clientTables().stream()
                .filter(entry -> biome != null && entry.biomes().contains(biome))
                .map(PanningTables.Table::name)
                .findFirst()
                .orElse(lava ? PanningTables.NETHER_DEFAULT : PanningTables.DEFAULT);
        tooltip.add(Component.translatable("jade.prospect.sparkle_node.table", tableName(table)).withStyle(ChatFormatting.GRAY));
        PanItem pan = heldPan(accessor.getPlayer());
        if (pan == null) {
            tooltip.add(Component.translatable("jade.prospect.sparkle_node.needs_pan").withStyle(ChatFormatting.DARK_GRAY));
        } else if (lava && !pan.canHarvestLava()) {
            tooltip.add(Component.translatable("tooltip.prospect.pan.too_hot").withStyle(ChatFormatting.GOLD));
        }
    }

    private static PanItem heldPan(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof PanItem pan) return pan;
        }
        return null;
    }

    private static Component tableName(String name) {
        StringBuilder fallback = new StringBuilder();
        for (String word : name.split("_")) {
            if (word.isEmpty()) continue;
            if (!fallback.isEmpty()) fallback.append(' ');
            fallback.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.translatableWithFallback("jei.prospect.panning.table." + name, fallback.toString());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
