package alabaster.prospect.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class GlowSporeParticle extends TextureSheetParticle {

    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float RISE = 0.0002F;
    private static final float SINK = -0.0004F;
    private static final float SWAY = 0.0006F;

    private final float drift;
    private final float peakAlpha;
    private final float phase;
    private final float swaySpeed;

    public GlowSporeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Vector3f color, SpriteSet sprites) {
        super(level, x, y, z);
        pickSprite(sprites);
        float shade = 0.85F + random.nextFloat() * 0.15F;
        setColor(color.x() * shade, color.y() * shade, color.z() * shade);
        this.xd = xd + (random.nextDouble() - 0.5) * 0.01;
        this.yd = yd + (yd < 0 ? 0.0 : random.nextDouble() * 0.004);
        this.zd = zd + (random.nextDouble() - 0.5) * 0.01;
        this.drift = yd < 0 ? SINK : RISE;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.hasPhysics = true;
        this.lifetime = 100 + random.nextInt(100);
        this.quadSize = 0.03F + random.nextFloat() * 0.035F;
        this.peakAlpha = 0.6F + random.nextFloat() * 0.4F;
        this.phase = random.nextFloat() * Mth.TWO_PI;
        this.swaySpeed = 0.04F + random.nextFloat() * 0.05F;
        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        float angle = phase + age * swaySpeed;
        xd += Mth.sin(angle) * SWAY;
        zd += Mth.cos(angle) * SWAY;
        yd += drift;
        float life = (float) age / lifetime;
        float fade = Math.min(1.0F, life * 6.0F) * Math.min(1.0F, (1.0F - life) * 4.0F);
        float twinkle = 0.8F + 0.2F * Mth.sin(angle * 3.0F);
        alpha = peakAlpha * fade * twinkle;
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
