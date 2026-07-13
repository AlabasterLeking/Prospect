package alabaster.prospect.client.particle;

import alabaster.prospect.common.item.ProspectingPickaxeItem;
import alabaster.prospect.common.prospecting.ProspectingGems;
import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TopazGlintParticleProvider implements ParticleProvider<SimpleParticleType> {

    private static final double PROXIMITY_RANGE = 32.0;
    private final SpriteSet sprites;

    public TopazGlintParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        TopazGlowParticle particle = new TopazGlowParticle(level, x, y, z, 0.0, 0.0, 0.0, sprites);

        float brightness = 1.0f;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack held = findProspectingPickaxe(player);
            if (held != null) {
                ProspectingSockets sockets = held.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
                if (sockets.has(ProspectingGems.RUBY)) {
                    double dist = Math.sqrt(player.distanceToSqr(x, y, z));
                    double t = Math.min(dist / PROXIMITY_RANGE, 1.0);
                    brightness = (float) (0.5 + 0.5 * (1.0 - t));
                }
            }
        }

        particle.setColor(1.0f * brightness, 0.85f * brightness, 0.3f * brightness);
        particle.setParticleSpeed(dx * 0.01 / 2.0, dy * 0.01, dz * 0.01 / 2.0);
        particle.setLifetime(level.random.nextInt(30) + 10);
        return particle;
    }

    private static ItemStack findProspectingPickaxe(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ProspectingPickaxeItem) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof ProspectingPickaxeItem) return off;
        return null;
    }
}