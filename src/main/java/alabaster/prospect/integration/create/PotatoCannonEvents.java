package alabaster.prospect.integration.create;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.item.DynamiteType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Prospect.MODID)
public class PotatoCannonEvents {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof Projectile projectile)) return;
        ItemStack stack = CreateCompat.potatoItem(projectile);
        if (stack.isEmpty()) return;
        DynamiteType type = typeFor(stack);
        if (type == null) return;
        event.setCanceled(true);
        DynamiteEntity dynamite = new DynamiteEntity(level, projectile.getX(), projectile.getY(), projectile.getZ(), type.fuseTicks, type);
        Entity owner = projectile.getOwner();
        if (owner != null) dynamite.setOwner(owner);
        Vec3 motion = projectile.getDeltaMovement();
        dynamite.shoot(motion.x, motion.y, motion.z, (float) motion.length(), 0.0F);
        level.addFreshEntity(dynamite);
        level.playSound(null, dynamite.getX(), dynamite.getY(), dynamite.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, dynamite.getX(), dynamite.getY(), dynamite.getZ(), 4, 0.05, 0.05, 0.05, 0.02);
            server.sendParticles(ParticleTypes.SMOKE, dynamite.getX(), dynamite.getY(), dynamite.getZ(), 6, 0.08, 0.08, 0.08, 0.02);
        }
    }

    @Nullable
    private static DynamiteType typeFor(ItemStack stack) {
        for (DynamiteType type : DynamiteType.values()) {
            if (type != DynamiteType.BUNDLE && stack.is(type.unlitItem())) return type;
        }
        return null;
    }
}