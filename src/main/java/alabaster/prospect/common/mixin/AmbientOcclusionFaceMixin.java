package alabaster.prospect.common.mixin;

import alabaster.prospect.common.block.TintedGlassPaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(targets = "net.minecraft.client.renderer.block.ModelBlockRenderer$AmbientOcclusionFace")
public abstract class AmbientOcclusionFaceMixin {

    @Unique
    private boolean prospect$paneInFront;

    @Inject(method = "calculate", at = @At("HEAD"), require = 0)
    private void prospect$checkPane(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, float[] shape, BitSet shapeFlags, boolean shade, CallbackInfo ci) {
        prospect$paneInFront = level.getBlockState(pos.relative(direction)).getBlock() instanceof TintedGlassPaneBlock;
    }

    @Inject(method = "blend(IIII)I", at = @At("HEAD"), cancellable = true, require = 0)
    private void prospect$blendAroundPane(int light0, int light1, int light2, int center, CallbackInfoReturnable<Integer> cir) {
        if (!prospect$paneInFront) return;
        int sky = Math.max(light0 >> 16 & 0xFFFF, Math.max(light1 >> 16 & 0xFFFF, light2 >> 16 & 0xFFFF));
        int block = Math.max(light0 & 0xFFFF, Math.max(light1 & 0xFFFF, light2 & 0xFFFF));
        int side = sky << 16 | block;
        if (light0 == 0) light0 = side;
        if (light1 == 0) light1 = side;
        if (light2 == 0) light2 = side;
        cir.setReturnValue(light0 + light1 + light2 + side >> 2 & 16711935);
    }
}