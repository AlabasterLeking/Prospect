package alabaster.prospect.common.mixin;

import alabaster.prospect.common.block.TintedGlassPaneBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    @WrapOperation(method = "calculateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"), require = 0)
    private boolean prospect$lightPaneFromSides(BlockState state, BlockGetter level, BlockPos pos, Operation<Boolean> original) {
        return state.getBlock() instanceof TintedGlassPaneBlock || original.call(state, level, pos);
    }
}