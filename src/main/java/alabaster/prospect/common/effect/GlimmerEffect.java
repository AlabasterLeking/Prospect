package alabaster.prospect.common.effect;

import alabaster.prospect.common.network.TopazGlintPayload;
import alabaster.prospect.common.registry.ProspectMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlimmerEffect extends MobEffect {

    private static final int INTERVAL = 40;
    private static final int RADIUS = 8;
    private static final int RADIUS_PER_LEVEL = 4;
    private static final int MAX_GLINTS = 12;

    public GlimmerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFDB710);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration < 0 || duration % INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return true;
        MobEffectInstance instance = player.getEffect(ProspectMobEffects.GLIMMER);
        if (instance != null && instance.isInfiniteDuration() && player.tickCount % INTERVAL != 0) return true;
        Level level = player.level();
        BlockPos center = player.blockPosition();
        int radius = RADIUS + RADIUS_PER_LEVEL * amplifier;
        List<BlockPos> ores = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                ores.add(pos.immutable());
            }
        }
        ores.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
        for (int i = 0; i < Math.min(MAX_GLINTS, ores.size()); i++) {
            PacketDistributor.sendToPlayer(player, new TopazGlintPayload(ores.get(i)));
        }
        return true;
    }
}