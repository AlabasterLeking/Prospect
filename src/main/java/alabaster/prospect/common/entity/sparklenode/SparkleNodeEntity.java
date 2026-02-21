package alabaster.prospect.common.entity.sparklenode;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SparkleNodeEntity extends Entity {

    private static EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.INT);

    private int lifetime = 3600; // 3 minutes default

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();

        this.entityData.set(AGE, getAge() + 1);

        // CLIENT PARTICLES
        if (random.nextFloat() < 0.2f) {

            double spread = 0.8; // size of sparkle area

            double x = getX() + (random.nextDouble() - 0.5) * spread;
            double y = getY() + 0.02; // just above water
            double z = getZ() + (random.nextDouble() - 0.5) * spread;

            level().addParticle(
                    ParticleTypes.WAX_ON,
                    x, y, z,
                    0, 0.01, 0
            );
        }

        if (getAge() >= lifetime) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AGE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", this.getAge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(AGE, compound.getInt("Age"));
    }

    private int getAge() {
        return this.entityData.get(AGE);
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}