package alabaster.prospect.client.renderer;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HookHeadRenderer {

    public static final ModelResourceLocation FLYING = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "item/grappling_hook_head"));
    public static final ModelResourceLocation LANDED = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "item/grappling_hook_head_landed"));

    private static final float SCALE = 0.5F;
    private static final Vector3f POINTING = new Vector3f(0.05F, 1.0F, 0.0F).normalize();
    private static final float TIP_X = 2.0F / 16.0F;
    private static final float TIP_Y = 6.5F / 16.0F;
    private static final float CENTER_Y = 3.0F / 16.0F;
    private static final float ROPE_X = -2.5F / 16.0F;
    private static final float ROPE_Y = -3.0F / 16.0F;

    private static ItemStack stack = ItemStack.EMPTY;

    private HookHeadRenderer() {}

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(FLYING);
        event.register(LANDED);
    }

    public static Vector3f into(Direction face) {
        return new Vector3f(-face.getStepX(), -face.getStepY(), -face.getStepZ());
    }

    public static void renderLanded(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Direction face, long seed) {
        renderLanded(poseStack, buffer, packedLight, into(face), seed);
    }

    public static void renderLanded(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Vector3f into, long seed) {
        poseStack.pushPose();
        poseStack.mulPose(rotation(into, seed));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-TIP_X, -TIP_Y, 0.0F);
        draw(LANDED, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    public static void renderFlying(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Vec3 motion, long seed) {
        Vector3f heading = motion.lengthSqr() < 1.0E-6 ? new Vector3f(0.0F, -1.0F, 0.0F) : motion.toVector3f().normalize();
        poseStack.pushPose();
        poseStack.mulPose(rotation(heading, seed));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0.0F, -CENTER_Y, 0.0F);
        draw(FLYING, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    public static Vec3 ropeOffset(Direction face, long seed) {
        return ropeOffset(into(face), seed);
    }

    public static Vec3 ropeOffset(Vector3f into, long seed) {
        Vector3f rope = new Vector3f(ROPE_X - TIP_X, ROPE_Y - TIP_Y, 0.0F).mul(SCALE).rotate(rotation(into, seed));
        return new Vec3(rope.x(), rope.y(), rope.z());
    }

    private static void draw(ModelResourceLocation location, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            stack = new ItemStack(ProspectModItems.GRAPPLING_HOOK.get());
        }
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(location);
        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);
    }

    private static Quaternionf rotation(Vector3f direction, long seed) {
        float roll = (float) Math.floorMod(seed, 360L) * Mth.DEG_TO_RAD;
        return new Quaternionf().rotationTo(POINTING, direction).premul(new Quaternionf().rotationAxis(roll, direction.x(), direction.y(), direction.z()));
    }
}