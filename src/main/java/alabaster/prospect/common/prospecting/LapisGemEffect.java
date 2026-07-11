package alabaster.prospect.common.prospecting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class LapisGemEffect implements ProspectingGemEffect {

    private static final int XP_COST = 5;

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        if (ctx.player().experienceLevel < XP_COST) {
            ctx.player().displayClientMessage(
                    Component.translatable("tooltip.prospect.prospecting_pickaxe.lapis_not_enough_xp", XP_COST),
                    true
            );
            return;
        }

        BlockPos playerPos = ctx.player().blockPosition();
        BlockPos ore = hit.pos();
        int dx = ore.getX() - playerPos.getX();
        int dy = ore.getY() - playerPos.getY();
        int dz = ore.getZ() - playerPos.getZ();

        String ns = dz < 0 ? "north" : "south";
        String ew = dx < 0 ? "west" : "east";
        String ud = dy < 0 ? "down" : "up";

        ctx.player().giveExperienceLevels(-XP_COST);

        ctx.player().displayClientMessage(
                Component.translatable("tooltip.prospect.prospecting_pickaxe.lapis_directions",
                        Math.abs(dx), ew, Math.abs(dz), ns, Math.abs(dy), ud),
                false
        );
    }
}