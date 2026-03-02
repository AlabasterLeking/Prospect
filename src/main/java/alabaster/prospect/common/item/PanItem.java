package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PanItem extends Item {

    private static final int USE_DURATION = 40;
    private static final int PARTICLE_INTERVAL_TICKS = 3;
    private static final int PARTICLES_PER_BURST = 5;

    public PanItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (!level.isClientSide) {
            if (remainingUseDuration <= 1) {
                int nodeId = player.getPersistentData().getInt("HarvestingNode");
                String handStr = player.getPersistentData().getString("HarvestingHand");
                InteractionHand hand = handStr.equals("MAIN_HAND")
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;

                Entity entityTarget = player.level().getEntity(nodeId);
                if (entityTarget instanceof SparkleNodeEntity node) {
                    node.harvestWith(player, stack, hand);
                }

                player.getPersistentData().remove("HarvestingNode");
                player.getPersistentData().remove("HarvestingHand");
            }
            return;
        }

        int elapsed = USE_DURATION - remainingUseDuration;
        if (elapsed % PARTICLE_INTERVAL_TICKS != 0) return;
        spawnWaterParticles(player, level);
    }

    private void spawnWaterParticles(Player player, Level level) {
        Vec3 look = player.getLookAngle();
        Vec3 origin = player.getEyePosition()
                .add(look.scale(1.2))
                .subtract(0, 0.6, 0);

        for (int i = 0; i < PARTICLES_PER_BURST; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.5;
            double oy = level.random.nextDouble() * 0.15;
            double oz = (level.random.nextDouble() - 0.5) * 0.5;
            double vx = (level.random.nextDouble() - 0.5) * 0.1;
            double vy = 0.08 + level.random.nextDouble() * 0.08;
            double vz = (level.random.nextDouble() - 0.5) * 0.1;

            level.addParticle(ParticleTypes.SPLASH,
                    origin.x + ox, origin.y + oy, origin.z + oz,
                    vx, vy, vz);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack stack,
                    float partialTick,
                    float equipProgress,
                    float swingProgress
            ) {
                if (!player.isUsingItem()) return false;

                int remaining = player.getUseItemRemainingTicks();
                float progress = (USE_DURATION - remaining + partialTick) / (float) USE_DURATION;

                poseStack.translate(0f, -0.5f, -1f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(20f));

                float swishAngle = (float) Math.sin(progress * (float) (Math.PI * 6)) * 22f;
                poseStack.mulPose(Axis.YP.rotationDegrees(swishAngle));

                float bob = (float) Math.abs(Math.sin(progress * (float) (Math.PI * 6))) * 0.04f;
                poseStack.translate(0f, bob, 0f);

                poseStack.scale(1.35f, 1.35f, 1.35f);

                return true;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(
                    LivingEntity entity,
                    InteractionHand hand,
                    ItemStack stack
            ) {
                return entity.isUsingItem()
                        ? HumanoidModel.ArmPose.BOW_AND_ARROW
                        : HumanoidModel.ArmPose.ITEM;
            }
        });
    }
}