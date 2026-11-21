package alabaster.prospect.common.item;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SlimeBucketDispenseBehavior extends OptionalDispenseItemBehavior {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(ProspectModItems.SLIME_BUCKET.get(), new SlimeBucketDispenseBehavior()));
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(direction);
        boolean open = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
        setSuccess(open && level.getDifficulty() != Difficulty.PEACEFUL && SlimeBucketItem.release(level, pos, stack, direction.toYRot(), null));
        return isSuccess() ? new ItemStack(Items.BUCKET) : stack;
    }
}
