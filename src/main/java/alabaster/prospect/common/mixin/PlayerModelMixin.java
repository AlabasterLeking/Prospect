package alabaster.prospect.common.mixin;

import alabaster.prospect.common.event.GrappleClientEvents;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Unique
    private static final float PROSPECT_RAISED = (float) -Math.PI * 0.95F;
    @Unique
    private static final float PROSPECT_GRIP = 0.12F;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void prospect$hangFromZipline(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player) || !GrappleClientEvents.isRiding(entity.getId())) return;
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        model.rightArm.xRot = PROSPECT_RAISED;
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = PROSPECT_GRIP;
        model.leftArm.xRot = PROSPECT_RAISED;
        model.leftArm.yRot = 0.0F;
        model.leftArm.zRot = -PROSPECT_GRIP;
        model.rightLeg.xRot = 0.1F;
        model.leftLeg.xRot = -0.1F;
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }
}
