package alabaster.prospect.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class RopeHand {

    private RopeHand() {}

    public static Vec3 position(Player player, boolean mainHand, float partialTick) {
        boolean right = (player.getMainArm() == HumanoidArm.RIGHT) == mainHand;
        int side = right ? 1 : -1;
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot()) * Mth.DEG_TO_RAD;
            Vec3 look = player.getViewVector(partialTick);
            Vec3 rightVector = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
            return player.getEyePosition(partialTick).add(look.scale(0.6)).add(rightVector.scale(0.3 * side)).add(0.0, -0.25, 0.0);
        }
        float bodyYaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(bodyYaw);
        double cos = Mth.cos(bodyYaw);
        double sideOffset = side * 0.35 * player.getScale();
        double forwardOffset = 0.6 * player.getScale();
        double crouch = player.isCrouching() ? -0.1875 : 0.0;
        return player.getEyePosition(partialTick).add(-cos * sideOffset - sin * forwardOffset, crouch - 0.45 * player.getScale(), -sin * sideOffset + cos * forwardOffset);
    }
}
