package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.prospector.WanderingProspector;
import alabaster.prospect.common.entity.prospector.ZombieProspector;
import alabaster.prospect.common.registry.ProspectModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Prospect.MODID)
public class ZombieProspectorEvents {

    @SubscribeEvent
    public static void onProspectorKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof WanderingProspector prospector)) return;
        if (!(event.getSource().getEntity() instanceof Zombie)) return;
        if (!(prospector.level() instanceof ServerLevel level)) return;

        Difficulty difficulty = level.getDifficulty();
        if (difficulty == Difficulty.PEACEFUL || difficulty == Difficulty.EASY) return;
        if (difficulty == Difficulty.NORMAL && level.random.nextBoolean()) return;

        ItemStack helmet = prospector.getItemBySlot(EquipmentSlot.HEAD).copy();
        ZombieProspector zombie = prospector.convertTo(ProspectModEntities.ZOMBIE_PROSPECTOR.get(), false);
        if (zombie == null) return;

        EventHooks.finalizeMobSpawn(zombie, level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION, null);
        zombie.copyHelmetFrom(helmet);
        EventHooks.onLivingConvert(prospector, zombie);
        if (!zombie.isSilent()) {
            level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, zombie.blockPosition(), 0);
        }
        event.setCanceled(true);
    }

}