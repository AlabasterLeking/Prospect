package alabaster.prospect.client.particle;

import alabaster.prospect.common.block.GlowColor;
import alabaster.prospect.common.particle.GlowSporeOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.Nullable;

public class GlowSporeParticleProvider implements ParticleProvider<GlowSporeOptions> {

    private static final GlowColor[] COLORS = GlowColor.values();

    private final SpriteSet sprites;

    public GlowSporeParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Nullable
    @Override
    public Particle createParticle(GlowSporeOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        GlowColor color = options.color().orElseGet(() -> COLORS[level.random.nextInt(COLORS.length)]);
        return new GlowSporeParticle(level, x, y, z, dx, dy, dz, color.particleColor(), sprites);
    }
}
