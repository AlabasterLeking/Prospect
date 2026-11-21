package alabaster.prospect.common.item;

import alabaster.prospect.client.animation.PanningAnimation;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import alabaster.prospect.common.registry.ProspectSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PanItem extends Item {

    public static final int USE_DURATION = 40;
    private static final int PARTICLE_INTERVAL_TICKS = 3;
    private static final int PARTICLES_PER_BURST = 5;
    private static final int SIFT_INTERVAL_TICKS = 8;
    private static final int DRIPS = 8;

    private final boolean canHarvestLava;
    private final ItemLike repairMaterial;

    public PanItem(Properties props, boolean canHarvestLava, ItemLike repairMaterial) {
        super(props);
        this.canHarvestLava = canHarvestLava;
        this.repairMaterial = repairMaterial;
    }

    public boolean canHarvestLava() {
        return canHarvestLava;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairCandidate) {
        return repairCandidate.is(repairMaterial.asItem());
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
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
            int siftElapsed = USE_DURATION - remainingUseDuration;
            if (siftElapsed % SIFT_INTERVAL_TICKS == 0 && player.level().getEntity(player.getPersistentData().getInt("HarvestingNode")) instanceof SparkleNodeEntity siftNode) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), siftNode.isInLava() ? ProspectSounds.PAN_SIFT_LAVA.get() : ProspectSounds.PAN_SIFT.get(),
                        SoundSource.PLAYERS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
            }
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

        boolean lava = SparkleNodeEntity.isClientHarvestingLava(player.getId());
        if (remainingUseDuration <= 1) {
            spawnDrips(player, level, lava);
            return;
        }
        int elapsed = USE_DURATION - remainingUseDuration;
        if (elapsed % PARTICLE_INTERVAL_TICKS != 0) return;
        spawnAmbientParticles(player, level, lava);
    }

    private void spawnDrips(Player player, Level level, boolean lava) {
        Vec3 origin = panOrigin(player);
        var particle = lava ? ParticleTypes.FALLING_LAVA : ParticleTypes.FALLING_WATER;
        for (int i = 0; i < DRIPS; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.6;
            double oz = (level.random.nextDouble() - 0.5) * 0.6;
            level.addParticle(particle, origin.x + ox, origin.y - 0.05, origin.z + oz, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 panOrigin(Player player) {
        return player.getEyePosition().add(player.getLookAngle().scale(1.2)).subtract(0, 0.6, 0);
    }

    private void spawnAmbientParticles(Player player, Level level, boolean lava) {
        Vec3 origin = panOrigin(player);

        var particle = lava ? ParticleTypes.LAVA : ParticleTypes.SPLASH;

        for (int i = 0; i < PARTICLES_PER_BURST; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.5;
            double oy = level.random.nextDouble() * 0.15;
            double oz = (level.random.nextDouble() - 0.5) * 0.5;
            double vx = (level.random.nextDouble() - 0.5) * 0.1;
            double vy = 0.08 + level.random.nextDouble() * 0.08;
            double vz = (level.random.nextDouble() - 0.5) * 0.1;

            level.addParticle(particle,
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
                float progress = PanningAnimation.progress(USE_DURATION - remaining + partialTick);
                float wave = PanningAnimation.wave(progress);

                poseStack.translate(0f, -0.5f, -1f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(20f));

                poseStack.mulPose(Axis.YP.rotationDegrees(wave * PanningAnimation.SWISH_DEGREES));
                poseStack.translate(0f, Math.abs(wave) * PanningAnimation.BOB_HEIGHT, 0f);

                poseStack.scale(1.35f, 1.35f, 1.35f);

                return true;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(
                    LivingEntity entity,
                    InteractionHand hand,
                    ItemStack stack
            ) {
                return entity.isUsingItem() && entity.getUseItem() == stack
                        ? PanningAnimation.PANNING_ARM_POSE.getValue()
                        : HumanoidModel.ArmPose.ITEM;
            }
        });
    }
}