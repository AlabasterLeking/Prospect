package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.ai.AvoidDynamiteGoal;
import alabaster.prospect.common.entity.prospector.WanderingProspector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Prospect.MODID)
public class PackMuleEvents {

    private static final String DESPAWN_KEY = "prospect_pack_mule_despawn";
    private static final int DESPAWN_DELAY = 1200;
    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mule mule)) return;
        if (!mule.getTags().contains(WanderingProspector.PACK_MULE_TAG)) return;

        mule.goalSelector.addGoal(1, new AvoidDynamiteGoal(mule, 1.5, SoundEvents.MULE_ANGRY));
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mule mule)) return;
        if (mule.level().isClientSide() || mule.tickCount % CHECK_INTERVAL != 0) return;
        if (!mule.getTags().contains(WanderingProspector.PACK_MULE_TAG)) return;

        CompoundTag data = mule.getPersistentData();

        if (mule.isTamed() || mule.getLeashHolder() instanceof Player || mule.getFirstPassenger() instanceof Player) {
            mule.removeTag(WanderingProspector.PACK_MULE_TAG);
            data.remove(DESPAWN_KEY);
            return;
        }

        if (mule.getLeashHolder() instanceof WanderingProspector) {
            data.remove(DESPAWN_KEY);
            return;
        }

        int remaining = (data.contains(DESPAWN_KEY) ? data.getInt(DESPAWN_KEY) : DESPAWN_DELAY) - CHECK_INTERVAL;
        if (remaining <= 0) {
            mule.discard();
            return;
        }
        data.putInt(DESPAWN_KEY, remaining);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mule mule)) return;
        if (!mule.getTags().contains(WanderingProspector.PACK_MULE_TAG)) return;

        event.getDrops().removeIf(drop -> drop.getItem().is(Items.SADDLE));
    }
}