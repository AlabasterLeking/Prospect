package alabaster.prospect.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class TopazGlintParticleProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet sprites;

    public TopazGlintParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        TopazGlowParticle particle = new TopazGlowParticle(level, x, y, z, 0.0, 0.0, 0.0, sprites);
        particle.setColor(1.0f, 0.85f, 0.3f); // topaz gold
        particle.setParticleSpeed(dx * 0.01 / 2.0, dy * 0.01, dz * 0.01 / 2.0);
        particle.setLifetime(level.random.nextInt(30) + 10);
        return particle;
    }
}