package alabaster.prospect.integration.jade;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.PayloadRailBlock;
import alabaster.prospect.common.block.PayloadSignal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PayloadRailProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "payload_rail");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getBlockState().getValue(PayloadRailBlock.POWERED)) {
            tooltip.add(Component.translatable("jade.prospect.payload_rail.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        BlockPos pos = accessor.getPosition();
        AABB box = new AABB(pos.getX() + 0.2, pos.getY(), pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 0.8, pos.getZ() + 0.8);
        for (AbstractMinecart cart : accessor.getLevel().getEntitiesOfClass(AbstractMinecart.class, box)) {
            if (cart instanceof Container) {
                tooltip.add(Component.translatable("jade.prospect.payload_rail.cargo", cart.getDisplayName()).withStyle(ChatFormatting.GRAY));
                return;
            }
            int signal = PayloadSignal.read(cart);
            if (signal <= 0) continue;
            Entity rider = cart.getFirstPassenger();
            Component name = rider != null ? rider.getDisplayName() : cart.getDisplayName();
            tooltip.add(Component.translatable("jade.prospect.payload_rail.carrying", name, signal).withStyle(ChatFormatting.GRAY));
            return;
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
