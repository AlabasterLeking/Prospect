package alabaster.prospect.common.item;

import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public enum DynamiteType {
    STANDARD(2.0F, 80, 1.0F, false),
    STICKY(2.0F, 80, 1.0F, true),
    BUNDLE(4.0F, 100, 0.7F, false);

    public final float explosionPower;
    public final int fuseTicks;
    public final float throwStrength;
    public final boolean sticky;

    DynamiteType(float explosionPower, int fuseTicks, float throwStrength, boolean sticky) {
        this.explosionPower = explosionPower;
        this.fuseTicks = fuseTicks;
        this.throwStrength = throwStrength;
        this.sticky = sticky;
    }

    public Item unlitItem() {
        return switch (this) {
            case STANDARD -> ProspectModItems.DYNAMITE.get();
            case STICKY -> ProspectModItems.STICKY_DYNAMITE.get();
            case BUNDLE -> ProspectModItems.DYNAMITE_BUNDLE.get();
        };
    }

    public Item litItem() {
        return switch (this) {
            case STANDARD -> ProspectModItems.LIT_DYNAMITE.get();
            case STICKY -> ProspectModItems.LIT_STICKY_DYNAMITE.get();
            case BUNDLE -> ProspectModItems.LIT_DYNAMITE_BUNDLE.get();
        };
    }

    @Nullable
    public Block placedBlock() {
        return switch (this) {
            case STANDARD -> ProspectModBlocks.PLACED_DYNAMITE.get();
            case BUNDLE -> ProspectModBlocks.PLACED_DYNAMITE_BUNDLE.get();
            case STICKY -> null;
        };
    }

    public static DynamiteType byId(int id) {
        DynamiteType[] values = values();
        return id >= 0 && id < values.length ? values[id] : STANDARD;
    }

    @Nullable
    public static DynamiteType fromStack(ItemStack stack) {
        if (stack.getItem() instanceof DynamiteItem dynamite) return dynamite.getType();
        if (stack.getItem() instanceof LitDynamiteItem lit) return lit.getType();
        return null;
    }
}