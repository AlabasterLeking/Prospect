package alabaster.prospect.common.utilities;

import alabaster.prospect.Prospect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ProspectTextUtils
{
    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable(Prospect.MODID + "." + key, args);
    }
}