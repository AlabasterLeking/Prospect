package alabaster.prospect.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class InstrumentReadings {

    private static final String[] DIRECTIONS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};
    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_HOUR = 1000;
    private static final int HOUR_OFFSET = 6;
    private static final int SUNRISE_START = 23000;
    private static final int DAY_START = 1000;
    private static final int SUNSET_START = 12000;
    private static final int NIGHT_START = 13800;

    private InstrumentReadings() {
    }

    @Nullable
    public static Component clock(Level level, ItemStack stack) {
        if (!stack.is(Items.CLOCK) || !ClientConfig.CLOCK_READOUT.get()) return null;
        if (!level.dimensionType().natural()) {
            return Component.translatable("item.prospect.clock.spinning");
        }
        long dayTime = level.getDayTime();
        int time = (int) (dayTime % TICKS_PER_DAY);
        long day = dayTime / TICKS_PER_DAY + 1;
        int hour = (time / TICKS_PER_HOUR + HOUR_OFFSET) % 24;
        int minute = (time % TICKS_PER_HOUR) * 60 / TICKS_PER_HOUR;
        int displayHour = hour % 12 == 0 ? 12 : hour % 12;
        String clockTime = String.format("%d:%02d", displayHour, minute);
        Component formatted = Component.translatable(hour < 12 ? "item.prospect.clock.am" : "item.prospect.clock.pm", clockTime);
        return Component.translatable("item.prospect.clock.reading", day, formatted, Component.translatable("item.prospect.clock.phase." + phase(time)));
    }

    private static String phase(int time) {
        if (time >= SUNRISE_START || time < DAY_START) return "sunrise";
        if (time < SUNSET_START) return "day";
        if (time < NIGHT_START) return "sunset";
        return "night";
    }

    public static List<Component> compass(Level level, Player player, ItemStack stack) {
        return compass(level, player, stack, false);
    }

    public static List<Component> compass(Level level, Player player, ItemStack stack, boolean tooltip) {
        boolean recovery = stack.is(Items.RECOVERY_COMPASS);
        if (!(recovery ? ClientConfig.RECOVERY_COMPASS_READOUT : ClientConfig.COMPASS_READOUT).get()) return List.of();
        Component target = compassTarget(level, player, stack);
        if (target == null) return List.of();
        Component facing = Component.translatable("item.prospect.compass.facing", facing(player));
        if (player.isReducedDebugInfo()) return List.of(facing, target);
        if (tooltip && recovery) {
            return List.of(position(level, player.getLastDeathLocation(), "death_position", "no_death"), target);
        }
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tooltip && tracker != null) {
            return List.of(position(level, tracker.target(), "lodestone_position", "no_lodestone"), target);
        }
        return List.of(Component.translatable("item.prospect.compass.position", player.getBlockX(), player.getBlockZ(), facing), target);
    }

    private static Component position(Level level, Optional<GlobalPos> target, String key, String missingKey) {
        if (target.isEmpty()) return Component.translatable("item.prospect.compass." + missingKey);
        BlockPos pos = target.get().pos();
        if (target.get().dimension() == level.dimension()) {
            return Component.translatable("item.prospect.compass." + key, pos.getX(), pos.getY(), pos.getZ());
        }
        ResourceLocation dimension = target.get().dimension().location();
        Component name = Component.translatableWithFallback("item.prospect.compass.dimension." + dimension.toLanguageKey(), dimension.toString());
        return Component.translatable("item.prospect.compass." + key + "_dimension", pos.getX(), pos.getY(), pos.getZ(), name);
    }

    @Nullable
    private static Component compassTarget(Level level, Player player, ItemStack stack) {
        if (stack.is(Items.RECOVERY_COMPASS)) {
            return target(level, player, player.getLastDeathLocation(), "last_death");
        }
        if (!stack.is(Items.COMPASS)) return null;
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker != null) {
            return target(level, player, tracker.target(), "lodestone");
        }
        if (!level.dimensionType().natural()) {
            return Component.translatable("item.prospect.compass.spinning");
        }
        return distance(player, level.getSharedSpawnPos(), "spawn");
    }

    private static Component target(Level level, Player player, Optional<GlobalPos> target, String kind) {
        if (target.isEmpty() || target.get().dimension() != level.dimension()) {
            return Component.translatable("item.prospect.compass.spinning");
        }
        return distance(player, target.get().pos(), kind);
    }

    private static Component distance(Player player, BlockPos pos, String kind) {
        double dx = pos.getX() + 0.5 - player.getX();
        double dz = pos.getZ() + 0.5 - player.getZ();
        int blocks = Mth.floor(Math.sqrt(dx * dx + dz * dz));
        return Component.translatable("item.prospect.compass." + kind, blocks);
    }

    private static Component facing(Player player) {
        int index = Math.floorMod(Math.round(Mth.wrapDegrees(player.getYRot()) / 45.0F), DIRECTIONS.length);
        return Component.translatable("item.prospect.compass.direction." + DIRECTIONS[index]);
    }
}