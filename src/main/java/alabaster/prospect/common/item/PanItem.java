package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PanItem extends Item {

    public PanItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        // Server-side harvest logic
        if (!level.isClientSide) {
            if (remainingUseDuration <= 1) {
                int nodeId = player.getPersistentData().getInt("HarvestingNode");
                String handStr = player.getPersistentData().getString("HarvestingHand");
                InteractionHand hand = handStr.equals("MAIN_HAND") ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                Entity entityTarget = player.level().getEntity(nodeId);
                if (entityTarget instanceof SparkleNodeEntity node) {
                    node.harvestWith(player, stack, hand);
                }

                player.getPersistentData().remove("HarvestingNode");
                player.getPersistentData().remove("HarvestingHand");
            }
            return;
        }

        // Particles
        int nodeId = player.getPersistentData().getInt("HarvestingNode");
        Entity target = player.level().getEntity(nodeId);
        if (target instanceof SparkleNodeEntity) {
            spawnWaterParticles(player, level);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.startUsingItem(hand);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    net.minecraft.client.player.LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack stack,
                    float partialTick,
                    float equipProgress,
                    float swingProgress
            ) {
                if (!player.isUsingItem()) return false;

                int useTime = player.getUseItemRemainingTicks();
                float progress = (getUseDuration(stack, player) - useTime + partialTick) / 40f;

                // Step 1: Translate pan to be directly in front of player, slightly lower
                poseStack.translate(0.0F, -0.3F, -0.8F); // X=0 for exact center

                // Step 2: Lay the pan flat
                poseStack.mulPose(Axis.XP.rotationDegrees(90f)); // horizontal
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f)); // flip bottom to face player

                // Step 3: Swish side-to-side
                float swish = (float)Math.sin(progress * 6F) * 15F;
                poseStack.mulPose(Axis.YP.rotationDegrees(swish));

                // Step 4: Slight bob for realism
                float bob = (float)Math.sin(progress * 12F) * 0.03F;
                poseStack.translate(0F, bob, 0F);

                // Step 5: Scale pan slightly bigger
                poseStack.scale(1.3F, 1.3F, 1.3F);

                return true;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(
                    LivingEntity entity,
                    InteractionHand hand,
                    ItemStack stack
            ) {
                if (entity.isUsingItem()) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                return HumanoidModel.ArmPose.ITEM;
            }
        });
    }

    private void spawnWaterParticles(Player player, Level level) {
        double distance = 0.8;
        double height = player.getEyeHeight() - 0.2;

        double dx = -Math.sin(Math.toRadians(player.getYRot())) * distance;
        double dz = Math.cos(Math.toRadians(player.getYRot())) * distance;
        double x = player.getX() + dx;
        double y = player.getY() + height;
        double z = player.getZ() + dz;

        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.4;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.4;

            level.addParticle(
                    ParticleTypes.FALLING_WATER,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0.0, 0.05, 0.0
            );
        }
    }
}