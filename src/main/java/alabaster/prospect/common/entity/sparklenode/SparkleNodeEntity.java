package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.item.PanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SparkleNodeEntity extends Entity {

    private static EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.INT);

    private int lifetime = 3600; // 3 minutes default

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        this.entityData.set(AGE, getAge() + 1);

        if (random.nextFloat() < 0.2f) {

            double spread = 0.8;

            double x = getX() + (random.nextDouble() - 0.5) * spread;
            double y = getY() + 0.02;
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
    public boolean isPickable() {
        return false;
    }

    public boolean mayInteract(Level level, BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {

        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (item instanceof PanItem panItem) {
            this.discard();
            itemStack.hurtAndBreak(1, player, Objects.requireNonNull(panItem.getEquipmentSlot(itemStack)));
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }
}