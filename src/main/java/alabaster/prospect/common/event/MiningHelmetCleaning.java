package alabaster.prospect.common.event;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public class MiningHelmetCleaning {

    public static void register() {
        CauldronInteraction.WATER.map().put(ProspectModItems.MINING_HELMET.get(), (state, level, pos, player, hand, stack) -> {
            if (!stack.has(DataComponents.DYED_COLOR)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide()) {
                stack.remove(DataComponents.DYED_COLOR);
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return ItemInteractionResult.SUCCESS;
        });
    }
}