package alabaster.prospect.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SparkParticleProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet sprites;

    public SparkParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        double lift = Math.abs(dy) + 0.06 + level.random.nextDouble() * 0.08;
        return new SparkParticle(level, x, y, z, dx, lift, dz, sprites);
    }
}