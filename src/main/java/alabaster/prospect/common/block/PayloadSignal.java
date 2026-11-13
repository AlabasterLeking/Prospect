package alabaster.prospect.common.block;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class PayloadSignal {

    public static final int PLAYER = 15;
    public static final int VILLAGER = 12;
    public static final int ANIMAL = 9;
    public static final int BABY = 6;
    public static final int HOSTILE = 3;
    public static final int OTHER = 1;
    public static final int TNT = 15;

    private PayloadSignal() {}

    public static int read(AbstractMinecart cart) {
        if (cart instanceof MinecartTNT) return TNT;
        if (cart instanceof Container container) {
            return container.isEmpty() ? 0 : Math.max(1, AbstractContainerMenu.getRedstoneSignalFromContainer(container));
        }
        int signal = 0;
        for (Entity passenger : cart.getPassengers()) {
            signal = Math.max(signal, rider(passenger));
        }
        return signal;
    }

    private static int rider(Entity passenger) {
        if (passenger instanceof Player) return PLAYER;
        if (passenger instanceof LivingEntity living && living.isBaby()) return BABY;
        if (passenger instanceof AbstractVillager) return VILLAGER;
        if (passenger instanceof Enemy) return HOSTILE;
        if (passenger instanceof Animal) return ANIMAL;
        return OTHER;
    }
}