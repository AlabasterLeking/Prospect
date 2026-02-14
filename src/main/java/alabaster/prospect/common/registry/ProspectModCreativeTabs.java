package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProspectModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Prospect.MODID);

    public static final Supplier<CreativeModeTab> TAB_PROSPECT = CREATIVE_TABS.register(Prospect.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.prospect"))
                    .icon(() -> new ItemStack(Items.COBBLESTONE))
                    .displayItems((parameters, output) -> ProspectModItems.CREATIVE_TAB_ITEMS.forEach((item) -> output.accept(item.get())))
                    .build());
}