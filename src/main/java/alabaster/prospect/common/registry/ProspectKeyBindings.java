package alabaster.prospect.common.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
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

    private static final IKeyConflictContext GRAPPLING = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return KeyConflictContext.IN_GAME.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this;
        }
    };

    public static final KeyMapping GRAPPLE_REEL_IN = grapple("key.prospect.grapple_reel_in", InputConstants.KEY_LCONTROL);

    public static final KeyMapping GRAPPLE_LET_OUT = grapple("key.prospect.grapple_let_out", InputConstants.KEY_LSHIFT);

    private static KeyMapping grapple(String name, int key) {
        return new KeyMapping(name, GRAPPLING, InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories.prospect") {
            @Override
            public boolean isActiveAndMatches(InputConstants.Key keyCode) {
                return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey()) && isConflictContextAndModifierActive();
            }

            @Override
            public boolean isConflictContextAndModifierActive() {
                return getKeyConflictContext().isActive() && (getKeyModifier() == KeyModifier.NONE || getKeyModifier().isActive(getKeyConflictContext()));
            }
        };
    }
}