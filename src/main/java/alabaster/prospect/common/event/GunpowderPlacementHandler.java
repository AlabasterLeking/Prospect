package alabaster.prospect.common.event;

import alabaster.prospect.common.registry.ProspectModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class GunpowderPlacementHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (level.isClientSide) return;

        if (!stack.is(Items.GUNPOWDER)) return;

        BlockPos clickedPos = event.getPos();
        Direction face = event.getFace();

        if (face == null) return;

        BlockPos placePos = clickedPos.relative(face);

        // Normal placement
        if (level.isEmptyBlock(placePos)) {

            BlockState state = ProspectModBlocks.GUNPOWDER_FUSE.get().defaultBlockState();

            if (state.canSurvive(level, placePos)) {

                level.setBlock(placePos, state, 3);

                consume(stack, player);

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    private static void consume(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}