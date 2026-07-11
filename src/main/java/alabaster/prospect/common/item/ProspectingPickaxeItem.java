package alabaster.prospect.common.item;

import alabaster.prospect.common.prospecting.OreCandidate;
import alabaster.prospect.common.prospecting.ProspectingContext;
import alabaster.prospect.common.prospecting.ProspectingGemEffect;
import alabaster.prospect.common.prospecting.ProspectingGems;
import alabaster.prospect.common.prospecting.ProspectingSockets;
import alabaster.prospect.common.prospecting.QuartzGemEffect;
import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ProspectingPickaxeItem extends DiggerItem {

    private static final int BASE_RADIUS = 12;
    private static final int COOLDOWN_TICKS = 30;
    private static final double PING_RANGE = 32.0;
    public static int distanceIncrement = 3;

    private static final Map<String, ChatFormatting> ORE_COLORS = new LinkedHashMap<>();

    static {
        ORE_COLORS.put("minecraft:coal_ores",      ChatFormatting.DARK_GRAY);
        ORE_COLORS.put("minecraft:iron_ores",      ChatFormatting.WHITE);
        ORE_COLORS.put("minecraft:copper_ores",    ChatFormatting.GOLD);
        ORE_COLORS.put("minecraft:gold_ores",      ChatFormatting.YELLOW);
        ORE_COLORS.put("minecraft:redstone_ores",  ChatFormatting.RED);
        ORE_COLORS.put("minecraft:lapis_ores",     ChatFormatting.BLUE);
        ORE_COLORS.put("minecraft:diamond_ores",   ChatFormatting.AQUA);
        ORE_COLORS.put("minecraft:emerald_ores",   ChatFormatting.GREEN);
        ORE_COLORS.put("minecraft:quartz_ores",    ChatFormatting.WHITE);
        ORE_COLORS.put("minecraft:netherite_ores", ChatFormatting.DARK_RED);
        ORE_COLORS.put("prospect:ruby_ores",       ChatFormatting.RED);
        ORE_COLORS.put("prospect:sapphire_ores",   ChatFormatting.BLUE);
        ORE_COLORS.put("prospect:topaz_ores",      ChatFormatting.GOLD);
        ORE_COLORS.put("minecraft:ores",           ChatFormatting.GRAY);
    }

    private static final List<String> ORE_TAG_KEYS = new ArrayList<>(ORE_COLORS.keySet());

    private record DistanceLabel(String key, ChatFormatting color) {}

    private static final String[] STRENGTH_KEYS = {
            "tooltip.prospect.prospecting_pickaxe.strength.very_strong",
            "tooltip.prospect.prospecting_pickaxe.strength.strong",
            "tooltip.prospect.prospecting_pickaxe.strength.moderate",
            "tooltip.prospect.prospecting_pickaxe.strength.faint",
            "tooltip.prospect.prospecting_pickaxe.strength.very_faint"
    };
    private static final ChatFormatting[] LABEL_COLORS = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN
    };

    public ProspectingPickaxeItem(Properties properties) {
        super(Tiers.NETHERITE, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }

    public static Properties defaultProperties() {
        return new Properties()
                .durability(Tiers.NETHERITE.getUses())
                .attributes(DiggerItem.createAttributes(Tiers.NETHERITE, 1.5f, -2.8f));
    }

    private static List<ProspectingGemEffect> activeGems(ItemStack stack) {
        ProspectingSockets sockets = stack.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        List<ProspectingGemEffect> gems = new ArrayList<>();
        for (ResourceLocation id : sockets.gemIds()) {
            ProspectingGems.get(id).ifPresent(gems::add);
        }
        return gems;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = ctx.getItemInHand();
        BlockPos clickedPos = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        boolean hasQuartz = stack.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY)
                .has(ProspectingGems.QUARTZ);

        // Sneak+click sets/clears the Quartz lock instead of running a search.
        if (hasQuartz && player.isShiftKeyDown()) {
            String oreTag = getOreTag(level, clickedState);
            if (oreTag != null) {
                QuartzGemEffect.setLockedTag(stack, oreTag);
                player.displayClientMessage(Component.translatable("tooltip.prospect.prospecting_pickaxe.quartz_set", oreTag), true);
            } else {
                QuartzGemEffect.clearLockedTag(stack);
                player.displayClientMessage(Component.translatable("tooltip.prospect.prospecting_pickaxe.quartz_cleared"), true);
            }
            return InteractionResult.sidedSuccess(false);
        }

        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResult.PASS;
        }

        ProspectingContext gemCtx = new ProspectingContext(level, player, stack, clickedPos, ctx.getClickedFace());
        List<ProspectingGemEffect> gems = activeGems(stack);

        int radius = BASE_RADIUS;
        for (ProspectingGemEffect gem : gems) {
            radius = gem.modifyRadius(radius, gemCtx);
        }

        TagKey<Block> lockedTag = null;
        for (ProspectingGemEffect gem : gems) {
            TagKey<Block> lock = gem.lockedOreTag(stack);
            if (lock != null) {
                lockedTag = lock;
                break;
            }
        }

        ChatFormatting clickedColor = getOreColor(level, clickedState);
        if (clickedColor != null && (lockedTag == null || clickedState.is(lockedTag))) {
            announceArrival(player, level, clickedPos, clickedState);
            return InteractionResult.sidedSuccess(false);
        }

        List<BlockPos> candidatePositions = collectSpherical(clickedPos, radius);
        List<OreCandidate> candidates = new ArrayList<>();
        for (BlockPos pos : candidatePositions) {
            BlockState state = level.getBlockState(pos);
            String tagId = getOreTag(level, state);
            if (tagId == null) continue;
            if (lockedTag != null && !state.is(lockedTag)) continue;
            ChatFormatting color = ORE_COLORS.get(tagId);
            double dist = Math.sqrt(clickedPos.distSqr(pos));
            candidates.add(new OreCandidate(pos, dist, color, state.getBlock().getDescriptionId(), tagId));
        }

        OreCandidate best = null;
        for (OreCandidate candidate : candidates) {
            if (best == null || candidate.distanceFromOrigin() < best.distanceFromOrigin()) {
                best = candidate;
            }
        }
        for (ProspectingGemEffect gem : gems) {
            OreCandidate selected = gem.selectCandidate(candidates, best, gemCtx);
            if (selected != null) best = selected;
        }

        if (best == null) {
            player.displayClientMessage(Component.translatable("tooltip.prospect.prospecting_pickaxe.no_ore")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            for (ProspectingGemEffect gem : gems) {
                gem.onNoOreFound(gemCtx);
            }
        } else {
            sendFuzzyOreMessage(player, best);
            playPingSound(level, best.pos(), best.distanceFromOrigin());
            spawnProximityParticles(level, best);
            for (ProspectingGemEffect gem : gems) {
                gem.onHitFound(best, gemCtx);
            }
        }

        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);

        return InteractionResult.sidedSuccess(false);
    }

    private static void spawnProximityParticles(Level level, OreCandidate hit) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockState state = level.getBlockState(hit.pos());
        // Closer ore = more particles. Clamped so it never fully drops to
        // zero (still a hint something's there even at max range) or gets
        // excessive up close.
        int count = Mth.clamp(12 - (int) (hit.distanceFromOrigin() / 3), 2, 12);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                hit.pos().getX() + 0.5, hit.pos().getY() + 0.5, hit.pos().getZ() + 0.5,
                count, 0.25, 0.25, 0.25, 0.0);
    }

    private static void playPingSound(Level level, BlockPos pos, double distance) {
        float t = (float) Math.min(distance / PING_RANGE, 1.0);
        float pitch = 2.0f - 1.4f * t;
        float volume = 4.0f;
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, volume, pitch);
    }

    private static void announceArrival(Player player, Level level, BlockPos pos, BlockState state) {
        ChatFormatting color = getOreColor(level, state);
        MutableComponent oreName = Component.translatable(state.getBlock().getDescriptionId())
                .withStyle(Style.EMPTY.withColor(color));
        MutableComponent message = Component.translatable("tooltip.prospect.prospecting_pickaxe.found_it", oreName)
                .withStyle(ChatFormatting.GOLD);

        player.displayClientMessage(message, true);
        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    private static List<BlockPos> collectSpherical(BlockPos origin, int radius) {
        List<BlockPos> result = new ArrayList<>();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        mut.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (!mut.equals(origin)) {
                            result.add(mut.immutable());
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String getOreTag(Level level, BlockState state) {
        for (String tagId : ORE_TAG_KEYS) {
            TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(tagId));
            if (state.is(tag)) return tagId;
        }
        return null;
    }

    private static ChatFormatting getOreColor(Level level, BlockState state) {
        String tagId = getOreTag(level, state);
        return tagId == null ? null : ORE_COLORS.get(tagId);
    }

    private static void sendFuzzyOreMessage(Player player, OreCandidate hit) {
        MutableComponent oreName = Component.translatable(hit.oreTranslationKey())
                .withStyle(Style.EMPTY.withColor(hit.color()));

        DistanceLabel label = getFuzzyLabel((int) Math.ceil(hit.distanceFromOrigin()));
        MutableComponent strength = Component.translatable(label.key()).withStyle(label.color());
        Component message = Component.translatable("tooltip.prospect.prospecting_pickaxe.detected", oreName, strength);

        player.displayClientMessage(message, true);
    }

    private static DistanceLabel getFuzzyLabel(int distance) {
        int index = Math.min((distance - 1) / Math.max(distanceIncrement, 1), STRENGTH_KEYS.length - 1);
        return new DistanceLabel(STRENGTH_KEYS[index], LABEL_COLORS[index]);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        ProspectingSockets sockets = stack.getOrDefault(ProspectDataComponents.PROSPECTING_SOCKETS.get(), ProspectingSockets.EMPTY);
        if (sockets.gemIds().isEmpty()) return;

        tooltip.add(Component.translatable("tooltip.prospect.prospecting_pickaxe.gems_header")
                .withStyle(ChatFormatting.GRAY));

        for (ResourceLocation id : sockets.gemIds()) {
            Component gemName = Component.translatable("gem.prospect." + id.getPath())
                    .withStyle(ChatFormatting.WHITE);
            tooltip.add(Component.translatable("tooltip.prospect.prospecting_pickaxe.gem_entry", gemName)
                    .withStyle(ChatFormatting.GRAY));
            ProspectingGems.get(id).ifPresent(gem -> tooltip.addAll(gem.getTooltipLines(stack)));
        }
    }
}