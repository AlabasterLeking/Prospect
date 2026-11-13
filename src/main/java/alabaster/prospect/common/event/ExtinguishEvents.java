package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.ExtinguishedLight;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Prospect.MODID)
public class ExtinguishEvents {

    private static final double SPLASH_HORIZONTAL = 4.0;
    private static final double SPLASH_VERTICAL = 2.0;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion) || !(potion.level() instanceof ServerLevel level)) return;
        PotionContents contents = potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!contents.is(Potions.WATER)) return;
        AABB area = potion.getBoundingBox().inflate(SPLASH_HORIZONTAL, SPLASH_VERTICAL, SPLASH_HORIZONTAL);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(area.minX, area.minY, area.minZ), BlockPos.containing(area.maxX, area.maxY, area.maxZ))) {
            ExtinguishedLight.douse(level, pos.immutable(), level.getBlockState(pos));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item) || item.level().isClientSide() || !item.isInWater()) return;
        ItemStack stack = item.getItem();
        Item doused = stack.is(Items.TORCH) ? ProspectModItems.EXTINGUISHED_TORCH.get() : stack.is(Items.LANTERN) ? ProspectModItems.EXTINGUISHED_LANTERN.get() : null;
        if (doused == null) return;
        item.setItem(stack.transmuteCopy(doused, stack.getCount()));
        item.level().playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.4F, 2.0F);
    }
}