package alabaster.prospect.common.prospecting;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class RubyGemEffect implements ProspectingGemEffect {

    @Override
    public void onHitFound(OreCandidate hit, ProspectingContext ctx) {
        double distFromPlayer = Math.sqrt(ctx.player().distanceToSqr(
                hit.pos().getX() + 0.5, hit.pos().getY() + 0.5, hit.pos().getZ() + 0.5));

        MutableComponent oreName = Component.translatable(hit.oreTranslationKey())
                .withStyle(Style.EMPTY.withColor(hit.color()));

        ctx.player().displayClientMessage(
                Component.translatable("tooltip.prospect.prospecting_pickaxe.ruby_distance",
                        oreName, (int) Math.ceil(distFromPlayer)),
                true
        );
    }
}