package alabaster.prospect.client.sound;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DynamiteFuseSound extends AbstractTickableSoundInstance {

    private final Entity source;
    private final boolean held;

    public DynamiteFuseSound(Entity source, boolean held) {
        super(SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.source = source;
        this.held = held;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.3F;
        this.pitch = 1.3F;
        this.x = source.getX();
        this.y = source.getY();
        this.z = source.getZ();
    }

    public static boolean isHoldingLit(Entity entity) {
        return entity instanceof LivingEntity living
                && (living.getMainHandItem().is(ProspectModItems.LIT_DYNAMITE.get()) || living.getOffhandItem().is(ProspectModItems.LIT_DYNAMITE.get()));
    }

    @Override
    public void tick() {
        if (source.isRemoved() || (held && !isHoldingLit(source))) {
            stop();
            return;
        }
        x = source.getX();
        y = source.getY();
        z = source.getZ();
    }
}