package alabaster.prospect.integration.curios;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Predicate;

public final class CuriosCompat {

    public static final String MODID = "curios";

    private CuriosCompat() {
    }

    public static void register() {
        CuriosApi.registerCurio(ProspectModItems.PORTABLE_SMELTER.get(), new ICurioItem() {
            @Override
            public void curioTick(SlotContext slotContext, ItemStack stack) {
                LivingEntity entity = slotContext.entity();
                stack.getItem().inventoryTick(stack, entity.level(), entity, -1, false);
            }
        });
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inventory -> inventory.findFirstCurio(filter))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}
