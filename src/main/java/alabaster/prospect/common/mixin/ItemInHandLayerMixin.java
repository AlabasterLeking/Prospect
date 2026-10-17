package alabaster.prospect.common.mixin;

import alabaster.prospect.client.animation.PanningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Shadow
    @Final
    private ItemInHandRenderer itemInHandRenderer;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void prospect$renderPanning(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!PanningAnimation.isPanning(entity, stack)) return;
        if (!(((RenderLayer<?, ?>) (Object) this).getParentModel() instanceof HumanoidModel<?> model)) return;

        ci.cancel();
        poseStack.pushPose();
        model.body.translateAndRotate(poseStack);
        PanningAnimation.transformThirdPersonPan(poseStack, entity);
        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}