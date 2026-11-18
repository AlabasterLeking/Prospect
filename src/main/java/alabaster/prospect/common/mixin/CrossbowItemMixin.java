package alabaster.prospect.common.mixin;

import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    @Inject(method = "getSupportedHeldProjectiles()Ljava/util/function/Predicate;", at = @At("RETURN"), cancellable = true)
    private void prospect$heldGrapplingHook(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        cir.setReturnValue(cir.getReturnValue().or(stack -> stack.is(ProspectModItems.GRAPPLING_HOOK.get())));
    }

    @Inject(method = "getAllSupportedProjectiles()Ljava/util/function/Predicate;", at = @At("RETURN"), cancellable = true)
    private void prospect$inventoryGrapplingHook(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        cir.setReturnValue(cir.getReturnValue().or(stack -> stack.is(ProspectModItems.GRAPPLING_HOOK.get())));
    }
}