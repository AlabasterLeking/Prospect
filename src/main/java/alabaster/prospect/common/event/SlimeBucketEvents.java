package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.SlimeBucketItem;
import alabaster.prospect.common.network.SlimeChunkPayload;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Prospect.MODID)
public class SlimeBucketEvents {

    private static final long SLIME_CHUNK_SALT = 987234911L;
    private static final int CHECK_INTERVAL = 10;

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Slime slime) || slime instanceof MagmaCube) return;
        if (!slime.isAlive() || slime.getSize() != 1) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.BUCKET)) return;

        Player player = event.getEntity();
        Level level = player.level();
        player.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.0F);
        player.playSound(SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
        if (!level.isClientSide()) {
            ItemStack filled = SlimeBucketItem.capture(slime, ProspectModItems.SLIME_BUCKET.get());
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, filled, false));
            slime.discard();
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CHECK_INTERVAL != 0) return;
        boolean inSlimeChunk = isSlimeChunk(player.serverLevel(), player.chunkPosition());
        Boolean previous = LAST_SENT.get(player.getUUID());
        if (previous == null || previous != inSlimeChunk) {
            LAST_SENT.put(player.getUUID(), inSlimeChunk);
            PacketDistributor.sendToPlayer(player, new SlimeChunkPayload(inSlimeChunk));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    public static boolean isSlimeChunk(ServerLevel level, ChunkPos chunk) {
        return level.dimension() == Level.OVERWORLD
                && WorldgenRandom.seedSlimeChunk(chunk.x, chunk.z, level.getSeed(), SLIME_CHUNK_SALT).nextInt(10) == 0;
    }
}