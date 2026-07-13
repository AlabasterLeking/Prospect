package alabaster.prospect.common.event;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.ProspectingPickaxeItem;
import alabaster.prospect.common.prospecting.ProspectingGemItems;
import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.LinkedHashSet;

@EventBusSubscriber(modid = Prospect.MODID)
public class GrindstoneEvents {

    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onRightClickGrindstone(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return; // avoid double-firing for both hands

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.GRINDSTONE)) return;

        ItemStack held = event.getEntity().getItemInHand(event.getHand());
        if (!(held.getItem() instanceof ProspectingPickaxeItem)) return;

        ResourceLocation toRemove = lastSocketedGem(held);
        if (toRemove == null) return; // no gems socketed, let normal grindstone interaction happen

        event.setCanceled(true); // don't open the grindstone UI for this interaction

        ProspectingSockets sockets = held.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        held.set(ProspectDataComponents.PROSPECTING_SOCKETS.get(), withoutGem(sockets, toRemove));

        float durabilityFraction = 1.0f;
        if (held.isDamageableItem()) {
            durabilityFraction = 1.0f - ((float) held.getDamageValue() / held.getMaxDamage());
        }

        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 particlePos = Vec3.atCenterOf(pos).add(0, 0.5, 0);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    12, 0.3, 0.3, 0.3, 0.05);
        }

        if (RANDOM.nextFloat() < durabilityFraction) {
            spawnFlyingGem(level, pos, ProspectingGemItems.getItem(toRemove));
        }
    }

    private static void spawnFlyingGem(Level level, BlockPos pos, ItemStack gemStack) {
        Vec3 origin = Vec3.atCenterOf(pos).add(0, 0.6, 0);
        ItemEntity entity = new ItemEntity(level, origin.x, origin.y, origin.z, gemStack);
        entity.setDeltaMovement(
                (RANDOM.nextDouble() - 0.5) * 0.35,
                0.35 + RANDOM.nextDouble() * 0.15,
                (RANDOM.nextDouble() - 0.5) * 0.35
        );
        level.addFreshEntity(entity);
    }

    private static ResourceLocation lastSocketedGem(ItemStack stack) {
        ProspectingSockets sockets = stack.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        if (sockets.gemIds().isEmpty()) return null;
        ResourceLocation last = null;
        for (ResourceLocation id : sockets.gemIds()) {
            last = id;
        }
        return last;
    }

    private static ProspectingSockets withoutGem(ProspectingSockets sockets, ResourceLocation gemId) {
        var copy = new LinkedHashSet<>(sockets.gemIds());
        copy.remove(gemId);
        return new ProspectingSockets(copy);
    }
}