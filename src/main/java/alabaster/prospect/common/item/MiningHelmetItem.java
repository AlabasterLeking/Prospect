package alabaster.prospect.common.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class MiningHelmetItem extends Item implements Equipable {

    private static final String LIGHT_ON_KEY = "LightOn";

    public MiningHelmetItem(Properties properties) {
        super(properties.durability(128));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairCandidate) {
        return repairCandidate.is(Items.WHEAT);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    public static boolean isLightOn(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains(LIGHT_ON_KEY) || tag.getBoolean(LIGHT_ON_KEY);
    }

    public static void setLightOn(ItemStack stack, boolean on) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(LIGHT_ON_KEY, on);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean toggleLight(ItemStack stack) {
        boolean newState = !isLightOn(stack);
        setLightOn(stack, newState);
        return newState;
    }
}