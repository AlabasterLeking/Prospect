package alabaster.prospect.common.prospecting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ProspectingContext(Level level, Player player, ItemStack stack, BlockPos clickedPos, Direction clickedFace) {}