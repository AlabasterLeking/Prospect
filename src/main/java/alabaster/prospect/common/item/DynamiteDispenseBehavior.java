package alabaster.prospect.common.item;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DynamiteDispenseBehavior extends DefaultDispenseItemBehavior {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DynamiteDispenseBehavior behavior = new DynamiteDispenseBehavior();
            DispenserBlock.registerBehavior(ProspectModItems.DYNAMITE.get(), behavior);
            DispenserBlock.registerBehavior(ProspectModItems.LIT_DYNAMITE.get(), behavior);
        });
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        int fuse = stack.is(ProspectModItems.LIT_DYNAMITE.get()) ? LitDynamiteItem.getFuse(stack) : LitDynamiteItem.FUSE_TICKS;

        DynamiteEntity dynamite = new DynamiteEntity(level, position.x(), position.y(), position.z(), fuse);
        dynamite.shoot(direction.getStepX(), direction.getStepY() + 0.1F, direction.getStepZ(), 1.1F, 6.0F);
        level.addFreshEntity(dynamite);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().playSound(null, source.pos(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.6F, 1.4F);
    }
}