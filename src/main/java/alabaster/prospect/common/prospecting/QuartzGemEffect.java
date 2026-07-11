package alabaster.prospect.common.prospecting;

import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public class QuartzGemEffect implements ProspectingGemEffect {

    public static Optional<String> getLockedTag(ItemStack stack) {
        return Optional.ofNullable(stack.get(ProspectDataComponents.QUARTZ_LOCK));
    }

    public static void setLockedTag(ItemStack stack, String tagId) {
        stack.set(ProspectDataComponents.QUARTZ_LOCK, tagId);
    }

    public static void clearLockedTag(ItemStack stack) {
        stack.remove(ProspectDataComponents.QUARTZ_LOCK);
    }

    @Override
    public TagKey<Block> lockedOreTag(ItemStack stack) {
        return getLockedTag(stack)
                .map(id -> BlockTags.create(ResourceLocation.parse(id)))
                .orElse(null);
    }

    @Override
    public List<Component> getTooltipLines(ItemStack stack) {
        return getLockedTag(stack)
                .map(id -> List.<Component>of(Component.translatable("tooltip.prospect.prospecting_pickaxe.quartz_locked", id)
                        .withStyle(ChatFormatting.WHITE)))
                .orElse(List.of(Component.translatable("tooltip.prospect.prospecting_pickaxe.quartz_unlocked")
                        .withStyle(ChatFormatting.DARK_GRAY)));
    }
}