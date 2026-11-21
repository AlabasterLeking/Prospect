package alabaster.prospect.common.item.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AltimeterItem extends Item {

    private static final int HIGHLANDS_Y = 128;
    private static final int SURFACE_DEPTH = 8;
    private static final int BEDROCK_HEIGHT = 5;
    private static final int NETHER_ROOF_Y = 128;
    private static final int LAVA_SEA_Y = 32;

    public AltimeterItem(Properties properties) {
        super(properties);
    }

    public static float needle(Level level, LivingEntity entity) {
        int min = level.getMinBuildHeight();
        int max = level.getMaxBuildHeight();
        return Math.clamp((float) (entity.getY() - min) / (max - min), 0.0F, 1.0F);
    }

    public static Component reading(Level level, LivingEntity entity) {
        int y = entity.getBlockY();
        int offset = y - level.getSeaLevel();
        if (offset == 0) {
            return Component.translatable("item.prospect.altimeter.sea_level", y);
        }
        String key = offset > 0 ? "item.prospect.altimeter.above" : "item.prospect.altimeter.below";
        return Component.translatable(key, y, Math.abs(offset));
    }

    @Nullable
    public static Component layer(Level level, LivingEntity entity) {
        String name = layerName(level, entity.getBlockY());
        return name == null ? null : Component.translatable("item.prospect.altimeter.layer", Component.translatable("item.prospect.altimeter.layer." + name));
    }

    @Nullable
    private static String layerName(Level level, int y) {
        if (level.dimensionType().hasCeiling()) {
            if (y >= NETHER_ROOF_Y) return "nether_roof";
            return y < LAVA_SEA_Y ? "lava_sea" : "nether";
        }
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().fixedTime().isPresent()) return null;
        if (y <= level.getMinBuildHeight() + BEDROCK_HEIGHT) return "bedrock";
        if (y < 0) return "deepslate";
        if (y < level.getSeaLevel() - SURFACE_DEPTH) return "underground";
        return y >= HIGHLANDS_Y ? "highlands" : "surface";
    }
}
