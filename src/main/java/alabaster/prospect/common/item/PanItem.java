package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class PanItem extends Item {

    public PanItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE; // custom renderer handles the animation
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (!livingEntity.level().isClientSide) {
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
        }
    }
}