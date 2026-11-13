package alabaster.prospect.common.item.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AltimeterItem extends Item {

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
}