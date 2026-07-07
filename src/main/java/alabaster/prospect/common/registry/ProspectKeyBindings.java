package alabaster.prospect.common.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ProspectKeyBindings {

    public static final KeyMapping TOGGLE_MINING_HELMET_LIGHT = new KeyMapping(
            "key.prospect.toggle_mining_helmet_light",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F),
            "key.categories.prospect"
    );
}