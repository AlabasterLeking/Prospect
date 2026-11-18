package alabaster.prospect.client.renderer;

import alabaster.prospect.common.entity.grapple.GrapplingHookEntity;
import alabaster.prospect.common.entity.grapple.RopeWrap;
import alabaster.prospect.common.event.GrappleClientEvents;
import alabaster.prospect.common.registry.ProspectModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GrapplingHookRenderer extends EntityRenderer<GrapplingHookEntity> {

    public GrapplingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(GrapplingHookEntity hook, Frustum frustum, double camX, double camY, double camZ) {
        if (super.shouldRender(hook, frustum, camX, camY, camZ)) return true;
        Entity owner = hook.getOwner();
        return owner != null && frustum.isVisible(new AABB(hook.position(), owner.position()).inflate(2.0));
    }

    @Override
    public void render(GrapplingHookEntity hook, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        long seed = hook.getUUID().getLeastSignificantBits();
        Vector3f into = attachedDirection(hook, partialTick);
        if (hook.isAnchored()) {
            HookHeadRenderer.renderLanded(poseStack, buffer, packedLight, hook.getHitFace(), seed);
        } else if (into != null) {
            HookHeadRenderer.renderLanded(poseStack, buffer, packedLight, into, seed);
        } else {
            HookHeadRenderer.renderFlying(poseStack, buffer, packedLight, hook.getDeltaMovement(), seed);
        }

        if (hook.getOwner() instanceof Player owner && GrapplingHookEntity.holding(owner, hook.isThrown())) {
            Vec3 origin = hook.getPosition(partialTick);
            if (hook.isAnchored()) {
                origin = origin.add(HookHeadRenderer.ropeOffset(hook.getHitFace(), seed));
            } else if (into != null) {
                origin = origin.add(HookHeadRenderer.ropeOffset(into, seed));
            }
            Vec3 hand = RopeHand.position(owner, holdsLauncherInMainHand(owner, hook.isThrown()), partialTick);
            List<Vec3> path = new ArrayList<>();
            path.add(origin);
            RopeWrap wrap = hook.isAnchored() ? GrappleClientEvents.wraps(hook) : null;
            if (wrap != null) {
                path.addAll(wrap.points());
            }
            path.add(hand);
            double pathLength = 0.0;
            for (int i = 0; i < path.size() - 1; i++) {
                pathLength += path.get(i).distanceTo(path.get(i + 1));
            }
            float sag = 0.0F;
            double length = GrappleClientEvents.ropeLength(hook);
            if (length < 0.0) {
                length = hook.getCurrentLength();
            }
            if (length > 0.0) {
                sag = (float) Math.max(0.0, length - pathLength) * 0.5F;
            }
            Vec3 base = hook.getPosition(partialTick);
            for (int i = 0; i < path.size() - 1; i++) {
                Vec3 from = path.get(i);
                poseStack.pushPose();
                Vec3 shift = from.subtract(base);
                poseStack.translate(shift.x, shift.y, shift.z);
                RopeRenderer.render(poseStack, buffer, hook.level(), from, path.get(i + 1), i == path.size() - 2 ? sag : 0.0F);
                poseStack.popPose();
            }
        }
        super.render(hook, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static Vector3f attachedDirection(GrapplingHookEntity hook, float partialTick) {
        if (hook.getHookedEntityId() < 0 || !(hook.getOwner() instanceof Player owner)) return null;
        Vec3 direction = hook.getPosition(partialTick).subtract(owner.getPosition(partialTick).add(0.0, GrapplingHookEntity.ROPE_HEIGHT, 0.0));
        return direction.lengthSqr() < 1.0E-6 ? null : direction.toVector3f().normalize();
    }

    private static boolean holdsLauncherInMainHand(Player player, boolean thrown) {
        ItemStack main = player.getMainHandItem();
        return thrown ? main.is(ProspectModItems.GRAPPLING_HOOK.get()) : main.getItem() instanceof CrossbowItem;
    }

    @Override
    public ResourceLocation getTextureLocation(GrapplingHookEntity hook) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}