package alabaster.prospect.common.entity.sparklenode;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SparkleNodeEntity extends Entity {

    private int age = 0;
    private int lifetime = 20 * 60 * 3; // 3 minutes default

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        age++;

        if (!level().isClientSide) {
            if (age > lifetime) {
                discard();
            }
            return;
        }

        // CLIENT PARTICLES
        if (random.nextFloat() < 0.4f) {
            double x = getX() + (random.nextDouble() - 0.5) * 0.3;
            double y = getY() + 0.1;
            double z = getZ() + (random.nextDouble() - 0.5) * 0.3;

            level().addParticle(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    0, 0.01, 0
            );
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age");
        lifetime = tag.getInt("Lifetime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("Lifetime", lifetime);
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}