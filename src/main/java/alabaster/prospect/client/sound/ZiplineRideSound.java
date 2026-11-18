package alabaster.prospect.client.sound;

import alabaster.prospect.common.event.GrappleClientEvents;
import alabaster.prospect.common.registry.ProspectSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ZiplineRideSound extends AbstractTickableSoundInstance {

    private static final float MAX_VOLUME = 0.7F;
    private static final float FADE = 0.3F;

    private final Player rider;

    public ZiplineRideSound(Player rider) {
        super(ProspectSounds.ZIPLINE_RIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.rider = rider;
        looping = true;
        delay = 0;
        volume = 0.0F;
        follow();
    }

    private void follow() {
        x = rider.getX();
        y = rider.getY() + rider.getBbHeight();
        z = rider.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (rider.isRemoved() || !GrappleClientEvents.isRiding(rider.getId())) {
            stop();
            return;
        }
        follow();
        double speed = Math.sqrt(Mth.square(rider.getX() - rider.xo) + Mth.square(rider.getY() - rider.yo) + Mth.square(rider.getZ() - rider.zo));
        float target = (float) Mth.clamp(speed * 0.8, 0.0, MAX_VOLUME);
        volume = Mth.lerp(FADE, volume, target);
        pitch = 0.8F + (float) Mth.clamp(speed, 0.0, 1.0) * 0.6F;
    }
}
