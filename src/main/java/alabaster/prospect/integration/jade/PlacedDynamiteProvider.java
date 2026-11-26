package alabaster.prospect.integration.jade;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.PlacedDynamiteBlock;
import alabaster.prospect.common.block.PlacedDynamiteBundleBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PlacedDynamiteProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "placed_dynamite");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (state.hasProperty(PlacedDynamiteBlock.STICKS)) {
            tooltip.add(Component.translatable("jade.prospect.placed_dynamite.sticks", state.getValue(PlacedDynamiteBlock.STICKS), PlacedDynamiteBlock.MAX_STICKS).withStyle(ChatFormatting.GRAY));
        } else if (state.hasProperty(PlacedDynamiteBundleBlock.BUNDLES)) {
            tooltip.add(Component.translatable("jade.prospect.placed_dynamite.bundles", state.getValue(PlacedDynamiteBundleBlock.BUNDLES), PlacedDynamiteBundleBlock.MAX_BUNDLES).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
