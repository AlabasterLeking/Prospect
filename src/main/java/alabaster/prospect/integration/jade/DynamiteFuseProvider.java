package alabaster.prospect.integration.jade;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DynamiteFuseProvider implements IEntityComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "dynamite_fuse");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof DynamiteEntity dynamite)) return;
        String seconds = String.format("%.1f", Math.max(0, dynamite.getFuse()) / 20.0F);
        tooltip.add(Component.translatable("jade.prospect.dynamite.fuse", seconds).withStyle(ChatFormatting.RED));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
