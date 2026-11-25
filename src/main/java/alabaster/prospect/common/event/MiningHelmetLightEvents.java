package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.HelmetLightBlock;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Prospect.MODID)
public class MiningHelmetLightEvents {

    private static final String LAMB_DYNAMIC_LIGHTS = "lambdynlights";

    private static final Map<UUID, GlobalPos> ACTIVE_LIGHTS = new ConcurrentHashMap<>();
    private static final Map<UUID, GlobalPos> LAST_EYE_POS = new ConcurrentHashMap<>();
    private static final Map<UUID, Flicker> FLICKERS = new ConcurrentHashMap<>();

    private static final int FULL_LIGHT_LEVEL = 15;
    private static final int UNDEAD_LIGHT_LEVEL = 11;
    private static final int FLICKER_DROP = 5;
    private static final float UNDEAD_BURST_CHANCE = 1.0F / 160.0F;
    private static final float WORN_BURST_CHANCE = 1.0F / 60.0F;
    private static final int MIN_DIPS = 1;
    private static final int MAX_DIPS = 3;
    private static final int MIN_HOLD_TICKS = 4;
    private static final int MAX_HOLD_TICKS = 8;
    private static final double LAMP_FORWARD = 0.3;
    private static final double LAMP_UP = 0.25;

    private static final BlockPos[] SEARCH_OFFSETS = {
            new BlockPos(0, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 2, 0),
    };

    public static boolean isClaimed(ServerLevel level, BlockPos pos) {
        return ACTIVE_LIGHTS.containsValue(GlobalPos.of(level.dimension(), pos));
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID id = entity.getUUID();
        if (!usesBlockLights() || entity.isDeadOrDying() || !isWearingLitHelmet(entity)) {
            release(level.getServer(), id);
            return;
        }

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        int light = lightLevel(entity, helmet, flickering(entity, helmet));
        GlobalPos eye = GlobalPos.of(level.dimension(), BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
        GlobalPos previous = ACTIVE_LIGHTS.get(id);
        if (eye.equals(LAST_EYE_POS.get(id)) && isStillPlaced(level, previous)) {
            setLight(level, previous.pos(), light);
            return;
        }
        LAST_EYE_POS.put(id, eye);

        BlockPos ownPos = previous != null && previous.dimension().equals(level.dimension()) ? previous.pos() : null;
        BlockPos placeAt = findPlaceablePosition(level, eye.pos(), ownPos);
        if (placeAt != null && placeAt.equals(ownPos) && isStillPlaced(level, previous)) {
            setLight(level, placeAt, light);
            return;
        }

        if (placeAt == null) {
            ACTIVE_LIGHTS.remove(id);
        } else {
            BlockState current = level.getBlockState(placeAt);
            boolean water = current.is(Blocks.WATER) || current.is(ProspectModBlocks.HELMET_LIGHT.get()) && current.getValue(HelmetLightBlock.WATERLOGGED);
            ACTIVE_LIGHTS.put(id, GlobalPos.of(level.dimension(), placeAt));
            level.setBlock(placeAt, ProspectModBlocks.HELMET_LIGHT.get().defaultBlockState().setValue(HelmetLightBlock.WATERLOGGED, water).setValue(HelmetLightBlock.LIGHT, light), HelmetLightBlock.QUIET_UPDATE);
        }

        if (previous != null && !previous.equals(ACTIVE_LIGHTS.get(id))) {
            clearLight(level.getServer(), previous);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        release(event.getEntity().getServer(), event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        release(event.getEntity().getServer(), event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        Entity.RemovalReason reason = event.getEntity().getRemovalReason();
        if (reason != null && reason.shouldDestroy()) {
            release(event.getEntity().getServer(), event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE_LIGHTS.clear();
        LAST_EYE_POS.clear();
        FLICKERS.clear();
    }

    public static boolean usesBlockLights() {
        return switch (Config.MINING_HELMET_LIGHT_MODE.get()) {
            case BLOCKS -> true;
            case DYNAMIC_LIGHTS -> false;
            case AUTO -> !ModList.get().isLoaded(LAMB_DYNAMIC_LIGHTS);
        };
    }

    private static boolean isWearingLitHelmet(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ProspectModTags.MINING_HELMET_LIGHT) && MiningHelmetItem.isLightOn(helmet);
    }

    private static boolean undeadEffects(LivingEntity entity) {
        return Config.UNDEAD_HELMET_FLICKER.get() && entity.getType().is(EntityTypeTags.UNDEAD);
    }

    private static float condition(ItemStack helmet) {
        if (!helmet.isDamageableItem()) return 1.0F;
        return 1.0F - (float) helmet.getDamageValue() / helmet.getMaxDamage();
    }

    private static int lightLevel(LivingEntity entity, ItemStack helmet, boolean flicker) {
        int light = undeadEffects(entity) ? UNDEAD_LIGHT_LEVEL : FULL_LIGHT_LEVEL;
        if (Config.HELMET_WEAR_DIMMING.get()) {
            int worn = Math.min(Config.HELMET_WORN_LIGHT_LEVEL.get(), light);
            light = Math.round(Mth.lerp(condition(helmet), worn, light));
        }
        if (flicker) light -= FLICKER_DROP;
        return Mth.clamp(light, 1, FULL_LIGHT_LEVEL);
    }

    private static float flickerChance(LivingEntity entity, ItemStack helmet) {
        float chance = undeadEffects(entity) ? UNDEAD_BURST_CHANCE : 0.0F;
        if (Config.HELMET_WEAR_FLICKER.get()) {
            float threshold = Config.HELMET_WEAR_FLICKER_THRESHOLD.get().floatValue();
            float condition = condition(helmet);
            if (threshold > 0.0F && condition < threshold) {
                chance = Math.max(chance, (1.0F - condition / threshold) * WORN_BURST_CHANCE);
            }
        }
        return chance;
    }

    private static boolean flickering(LivingEntity entity, ItemStack helmet) {
        UUID id = entity.getUUID();
        RandomSource random = entity.getRandom();
        Flicker flicker = FLICKERS.get(id);
        if (flicker == null) {
            float chance = flickerChance(entity, helmet);
            if (chance <= 0.0F || random.nextFloat() >= chance) return false;
            flicker = new Flicker(MIN_DIPS + random.nextInt(MAX_DIPS - MIN_DIPS + 1));
            FLICKERS.put(id, flicker);
        }
        if (--flicker.hold > 0) return flicker.dim;
        if (!flicker.dim && flicker.dips == 0) {
            FLICKERS.remove(id);
            return false;
        }
        flicker.dim = !flicker.dim;
        if (flicker.dim) {
            flicker.dips--;
            sputter(entity);
        }
        flicker.hold = MIN_HOLD_TICKS + random.nextInt(MAX_HOLD_TICKS - MIN_HOLD_TICKS + 1);
        return flicker.dim;
    }

    private static void sputter(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Vec3 lamp = entity.getEyePosition().add(Vec3.directionFromRotation(0.0F, entity.getYHeadRot()).scale(LAMP_FORWARD)).add(0.0, LAMP_UP, 0.0);
        level.sendParticles(ParticleTypes.SMOKE, lamp.x, lamp.y, lamp.z, 3, 0.03, 0.03, 0.03, 0.005);
    }

    private static void setLight(Level level, BlockPos pos, int light) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(HelmetLightBlock.LIGHT) != light) {
            level.setBlock(pos, state.setValue(HelmetLightBlock.LIGHT, light), HelmetLightBlock.QUIET_UPDATE);
        }
    }

    private static void release(@Nullable MinecraftServer server, UUID id) {
        LAST_EYE_POS.remove(id);
        FLICKERS.remove(id);
        GlobalPos pos = ACTIVE_LIGHTS.remove(id);
        if (pos != null && server != null) {
            clearLight(server, pos);
        }
    }

    private static boolean isStillPlaced(Level level, @Nullable GlobalPos light) {
        return light != null
                && light.dimension().equals(level.dimension())
                && level.getBlockState(light.pos()).is(ProspectModBlocks.HELMET_LIGHT.get());
    }

    @Nullable
    private static BlockPos findPlaceablePosition(Level level, BlockPos desired, @Nullable BlockPos ownPos) {
        for (BlockPos offset : SEARCH_OFFSETS) {
            BlockPos candidate = desired.offset(offset);
            BlockState state = level.getBlockState(candidate);
            if (state.is(ProspectModBlocks.HELMET_LIGHT.get())) {
                if (candidate.equals(ownPos)) return candidate;
                continue;
            }
            if (state.isAir() || isPlainWaterSource(state)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isPlainWaterSource(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSourceOfType(Fluids.WATER);
    }

    private static void clearLight(MinecraftServer server, GlobalPos light) {
        ServerLevel level = server.getLevel(light.dimension());
        if (level == null || !level.isLoaded(light.pos())) return;

        BlockState state = level.getBlockState(light.pos());
        if (!state.is(ProspectModBlocks.HELMET_LIGHT.get())) return;

        BlockState replacement = HelmetLightBlock.emptyStateFor(state);
        level.setBlock(light.pos(), replacement, replacement.isAir() ? HelmetLightBlock.QUIET_UPDATE : Block.UPDATE_ALL);
    }

    private static final class Flicker {
        private int dips;
        private int hold;
        private boolean dim;

        private Flicker(int dips) {
            this.dips = dips;
        }
    }
}