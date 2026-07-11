package alabaster.prospect.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;

public class TopazGlowParticle extends GlowParticle {

    public TopazGlowParticle(ClientLevel level, double x, double y, double z,
                             double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TopazGlintRenderType.INSTANCE;
    }
}