package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.HelmetLightBlock;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.registry.ProspectModBlocks;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

        GlobalPos eye = GlobalPos.of(level.dimension(), BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
        GlobalPos previous = ACTIVE_LIGHTS.get(id);
        if (eye.equals(LAST_EYE_POS.get(id)) && isStillPlaced(level, previous)) return;
        LAST_EYE_POS.put(id, eye);

        BlockPos ownPos = previous != null && previous.dimension().equals(level.dimension()) ? previous.pos() : null;
        BlockPos placeAt = findPlaceablePosition(level, eye.pos(), ownPos);
        if (placeAt != null && placeAt.equals(ownPos) && isStillPlaced(level, previous)) return;

        if (placeAt == null) {
            ACTIVE_LIGHTS.remove(id);
        } else {
            BlockState current = level.getBlockState(placeAt);
            boolean water = current.is(Blocks.WATER) || current.is(ProspectModBlocks.HELMET_LIGHT.get()) && current.getValue(HelmetLightBlock.WATERLOGGED);
            ACTIVE_LIGHTS.put(id, GlobalPos.of(level.dimension(), placeAt));
            level.setBlock(placeAt, ProspectModBlocks.HELMET_LIGHT.get().defaultBlockState().setValue(HelmetLightBlock.WATERLOGGED, water), Block.UPDATE_CLIENTS);
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

    private static void release(@Nullable MinecraftServer server, UUID id) {
        LAST_EYE_POS.remove(id);
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
        level.setBlock(light.pos(), replacement, replacement.isAir() ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL);
    }
}