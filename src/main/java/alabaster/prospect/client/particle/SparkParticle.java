package alabaster.prospect.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class SparkParticle extends TextureSheetParticle {

    private static final int FULL_BRIGHT = 0xF000F0;

    private final SpriteSet sprites;

    public SparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.gravity = 0.9F;
        this.friction = 0.9F;
        this.hasPhysics = true;
        this.lifetime = 6 + random.nextInt(8);
        this.quadSize = 0.05F + random.nextFloat() * 0.04F;
        updateColor();
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        setSpriteFromAge(sprites);
        updateColor();
        quadSize *= 0.93F;
        if (onGround) {
            xd *= 0.5;
            zd *= 0.5;
        }
    }

    private void updateColor() {
        float life = Mth.clamp((float) age / lifetime, 0.0F, 1.0F);
        setColor(1.0F, Mth.lerp(life, 0.95F, 0.35F), Mth.lerp(life, 0.7F, 0.05F));
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}