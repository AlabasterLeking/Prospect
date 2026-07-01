package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Prospect.MODID)
public class MiningHelmetLightEvents {

    private static final Map<UUID, BlockPos> ACTIVE_LIGHTS = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> LAST_EYE_POS = new ConcurrentHashMap<>();
    public static final int LIGHT_LEVEL = 15;

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

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity raw = event.getEntity();
        if (!(raw instanceof LivingEntity entity)) return;

        Level level = entity.level();
        if (level.isClientSide()) return;

        UUID id = entity.getUUID();
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        boolean wearing = helmet.is(ProspectModTags.MINING_HELMET_LIGHT);

        if (!wearing) {
            BlockPos active = ACTIVE_LIGHTS.remove(id);
            if (active != null) {
                clearLight(level, active);
            }
            LAST_EYE_POS.remove(id);
            return;
        }

        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        BlockPos lastEyePos = LAST_EYE_POS.get(id);

        if (eyePos.equals(lastEyePos)) return;
        LAST_EYE_POS.put(id, eyePos);

        BlockPos previousLight = ACTIVE_LIGHTS.remove(id);
        if (previousLight != null) {
            clearLight(level, previousLight);
        }

        BlockPos placeAt = findPlaceablePosition(level, eyePos);
        if (placeAt != null) {
            BlockState toPlace = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL);
            if (isWaterSource(level.getBlockState(placeAt))) {
                toPlace = toPlace.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            level.setBlock(placeAt, toPlace, 3);
            ACTIVE_LIGHTS.put(id, placeAt);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID id = player.getUUID();
        LAST_EYE_POS.remove(id);
        BlockPos pos = ACTIVE_LIGHTS.remove(id);
        if (pos != null) {
            clearLight(player.level(), pos);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        UUID id = entity.getUUID();
        LAST_EYE_POS.remove(id);
        BlockPos pos = ACTIVE_LIGHTS.remove(id);
        if (pos != null) {
            clearLight(entity.level(), pos);
        }
    }

    private static BlockPos findPlaceablePosition(Level level, BlockPos desired) {
        for (BlockPos offset : SEARCH_OFFSETS) {
            BlockPos candidate = desired.offset(offset);
            BlockState state = level.getBlockState(candidate);
            if (state.isAir() || isWaterSource(state)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isWaterSource(BlockState state) {
        return state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource();
    }

    private static void clearLight(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.LIGHT)) return;
        boolean wasWaterlogged = state.getValue(BlockStateProperties.WATERLOGGED);
        level.setBlock(pos, wasWaterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
    }
}