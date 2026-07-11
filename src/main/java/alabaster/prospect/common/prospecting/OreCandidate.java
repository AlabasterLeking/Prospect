package alabaster.prospect.common.prospecting;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

public record OreCandidate(BlockPos pos, double distanceFromOrigin, ChatFormatting color, String oreTranslationKey, String oreTagId) {}