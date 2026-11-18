package alabaster.prospect.common.mixin;

import alabaster.prospect.common.block.TintedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {

    @Inject(method = "isEmptyShape", at = @At("HEAD"), cancellable = true, require = 0)
    private static void prospect$paneLightShape(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof TintedGlassPaneBlock) {
            cir.setReturnValue(false);
        }
    }
}