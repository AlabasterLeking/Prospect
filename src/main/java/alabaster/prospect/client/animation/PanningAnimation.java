package alabaster.prospect.client.animation;

import alabaster.prospect.common.item.PanItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class PanningAnimation {
    private PanningAnimation() {}

    public static final EnumProxy<HumanoidModel.ArmPose> PANNING_ARM_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) PanningAnimation::poseArms
    );

    public static final float SWISH_DEGREES = 22.0F;
    public static final float BOB_HEIGHT = 0.04F;
    private static final double SWISH_CYCLES = 3.0;

    private static final float ARM_PITCH = -0.75F;
    private static final float ARM_INWARD = 0.25F;
    private static final float ARM_SWAY = 0.1F;

    private static final float PAN_HEIGHT = 9.3F / 16.0F;
    private static final float PAN_FORWARD = -7.0F / 16.0F;
    private static final float PAN_TILT = -75.0F;
    private static final float PAN_SCALE = 0.5F;
    private static final float HAND_REACH = 6.6F / 16.0F;

    public static float progress(float elapsedTicks) {
        return elapsedTicks / PanItem.USE_DURATION;
    }

    public static float progress(LivingEntity entity) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return progress(PanItem.USE_DURATION - entity.getUseItemRemainingTicks() + partialTick);
    }

    public static float wave(float progress) {
        return (float) Math.sin(progress * Math.PI * 2.0 * SWISH_CYCLES);
    }

    public static boolean isPanning(LivingEntity entity, ItemStack stack) {
        return entity.isUsingItem()
                && stack.getItem() instanceof PanItem
                && entity.getItemInHand(entity.getUsedItemHand()) == stack;
    }

    public static void poseArms(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float sway = wave(progress(entity)) * ARM_SWAY;

        model.rightArm.xRot = ARM_PITCH;
        model.rightArm.yRot = -ARM_INWARD + sway;
        model.rightArm.zRot = 0.0F;

        model.leftArm.xRot = ARM_PITCH;
        model.leftArm.yRot = ARM_INWARD + sway;
        model.leftArm.zRot = 0.0F;
    }

    public static void transformThirdPersonPan(PoseStack poseStack, LivingEntity entity) {
        float wave = wave(progress(entity));

        poseStack.translate(-HAND_REACH * ARM_SWAY * wave, PAN_HEIGHT, PAN_FORWARD);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(wave * SWISH_DEGREES));
        poseStack.translate(0.0F, Math.abs(wave) * BOB_HEIGHT, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(PAN_TILT));
        poseStack.scale(PAN_SCALE, PAN_SCALE, PAN_SCALE);
    }
}