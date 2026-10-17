package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Prospect.MODID)
public class DynamiteEvents {

    private static final int MIN_CHAIN_FUSE = 10;
    private static final int CHAIN_FUSE_SPREAD = 20;

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        RandomSource random = level.getRandom();
        List<DynamiteEntity> chained = new ArrayList<>();
        Iterator<Entity> iterator = event.getAffectedEntities().iterator();

        while (iterator.hasNext()) {
            Entity entity = iterator.next();

            if (entity instanceof DynamiteEntity dynamite) {
                dynamite.shortenFuse(chainFuse(random));
                continue;
            }

            if (!(entity instanceof ItemEntity item)) continue;
            ItemStack stack = item.getItem();
            if (!stack.is(ProspectModItems.DYNAMITE.get()) && !stack.is(ProspectModItems.LIT_DYNAMITE.get())) continue;

            iterator.remove();
            for (int i = 0; i < stack.getCount(); i++) {
                DynamiteEntity dynamite = new DynamiteEntity(level, item.getX(), item.getY(), item.getZ(), chainFuse(random));
                dynamite.setDeltaMovement(random.nextGaussian() * 0.15, 0.2 + random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
                chained.add(dynamite);
            }
            item.discard();
        }

        chained.forEach(level::addFreshEntity);
    }

    private static int chainFuse(RandomSource random) {
        return MIN_CHAIN_FUSE + random.nextInt(CHAIN_FUSE_SPREAD);
    }
}