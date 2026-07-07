package alabaster.prospect.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ProspectingPickaxeItem extends DiggerItem {

    public enum ProspectingTier {
        STONE(Tiers.STONE,         5,0.10f,false,false,40),
        IRON(Tiers.IRON,          12,0.05f,false,false,30),
        GOLD(Tiers.GOLD,          15,0.00f,false,true, 25),
        DIAMOND(Tiers.DIAMOND,    20,0.00f,false,false,20),
        NETHERITE(Tiers.NETHERITE,25,0.00f,false,true, 10);

        public final Tiers vanillaTier;
        public final int radius;
        public final float falsePositiveRate;
        public final boolean directional;
        public final boolean exactDistance;
        public final int cooldownTicks;

        ProspectingTier(Tiers vanillaTier, int radius, float falsePositiveRate, boolean directional, boolean exactDistance, int cooldownTicks) {
            this.vanillaTier = vanillaTier;
            this.radius = radius;
            this.falsePositiveRate = falsePositiveRate;
            this.directional = directional;
            this.exactDistance = exactDistance;
            this.cooldownTicks = cooldownTicks;
        }
    }

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

    public static int distanceIncrement = 3;

    private static final double PING_RANGE = 32.0;

    private final ProspectingTier prospectingTier;

    public ProspectingPickaxeItem(ProspectingTier prospectingTier, Properties properties) {
        super(
                prospectingTier.vanillaTier != null ? prospectingTier.vanillaTier : Tiers.IRON,
                BlockTags.MINEABLE_WITH_PICKAXE,
                properties
        );
        this.prospectingTier = prospectingTier;
    }

    public static Properties defaultProperties(ProspectingTier tier) {
        Tiers vt = tier.vanillaTier != null ? tier.vanillaTier : Tiers.IRON;
        return new Properties()
                .durability(vt.getUses())
                .attributes(DiggerItem.createAttributes(vt, 1.5f, -2.8f));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = ctx.getItemInHand();

        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = ctx.getClickedPos();
        Direction clickedFace = ctx.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (getOreColor(level, clickedState) != null) {
            announceArrival(player, level, clickedPos, clickedState);
            return InteractionResult.sidedSuccess(false);
        }

        List<BlockPos> candidates = prospectingTier.directional
                ? collectDirectional(level, clickedPos, clickedFace, prospectingTier.radius)
                : collectSpherical(level, clickedPos, prospectingTier.radius);

        OreHit hit = findNearestOre(level, clickedPos, candidates);

        hit = applyFalsePositive(hit, clickedPos, player.getRandom(),
                prospectingTier.falsePositiveRate, prospectingTier.radius);

        if (hit == null) {
            player.displayClientMessage(Component.translatable("tooltip.prospect.prospecting_pickaxe.no_ore")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
        } else {
            sendOreMessage(player, hit, prospectingTier);
            playPingSound(level, hit.pos(), hit.distance());
        }

        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        player.getCooldowns().addCooldown(stack.getItem(), prospectingTier.cooldownTicks);

        return InteractionResult.sidedSuccess(false);
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

    private static List<BlockPos> collectSpherical(Level level, BlockPos origin, int radius) {
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

    private static List<BlockPos> collectDirectional(Level level, BlockPos origin, Direction face, int radius) {
        List<BlockPos> result = new ArrayList<>();
        Direction inward = face.getOpposite();
        BlockPos cur = origin.relative(inward);
        for (int i = 0; i < radius; i++) {
            result.add(cur);
            cur = cur.relative(inward);
        }
        return result;
    }

    private static OreHit applyFalsePositive(OreHit realHit, BlockPos origin, RandomSource rng, float rate, int radius) {
        if (rate <= 0f || rng.nextFloat() >= rate) return realHit;

        String fakeTag = ORE_TAG_KEYS.get(rng.nextInt(ORE_TAG_KEYS.size() - 1));
        ChatFormatting fakeColor = ORE_COLORS.get(fakeTag);
        double fakeDist = 1 + rng.nextInt(radius);
        BlockPos fakePos = origin.offset(
                rng.nextInt(radius * 2 + 1) - radius,
                rng.nextInt(radius * 2 + 1) - radius,
                rng.nextInt(radius * 2 + 1) - radius
        );

        if (realHit == null || fakeDist < realHit.distance()) {
            return new OreHit(fakePos, fakeDist, fakeColor, "block." + fakeTag.replace("minecraft:", "minecraft.").replace("_ores", "").replace(":", ".") + "_ore");
        }

        return realHit;
    }

    private record OreHit(BlockPos pos, double distance, ChatFormatting color, String oreName) {}

    private static OreHit findNearestOre(Level level, BlockPos origin, List<BlockPos> candidates) {
        OreHit best = null;

        for (BlockPos pos : candidates) {
            BlockState state = level.getBlockState(pos);
            ChatFormatting color = getOreColor(level, state);
            if (color == null) continue;

            double dist = Math.sqrt(origin.distSqr(pos));
            if (best == null || dist < best.distance()) {
                String name = state.getBlock().getDescriptionId();
                best = new OreHit(pos, dist, color, name);
            }
        }

        return best;
    }

    private static ChatFormatting getOreColor(Level level, BlockState state) {
        for (Map.Entry<String, ChatFormatting> entry : ORE_COLORS.entrySet()) {
            TagKey<Block> tag = BlockTags.create(ResourceLocation.parse(entry.getKey()));
            if (state.is(tag)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void sendOreMessage(Player player, OreHit hit, ProspectingTier tier) {
        double dist = hit.distance();

        MutableComponent oreName = Component.translatable(hit.oreName())
                .withStyle(Style.EMPTY.withColor(hit.color()));

        MutableComponent message;

        if (tier.exactDistance) {
            int distInt = (int) Math.ceil(dist);
            MutableComponent distance = distanceComponent(distInt).withStyle(ChatFormatting.AQUA);
            message = Component.translatable("tooltip.prospect.prospecting_pickaxe.detected_exact", oreName, distance);
        } else {
            DistanceLabel label = getFuzzyLabel((int) Math.ceil(dist));
            MutableComponent strength = Component.translatable(label.key()).withStyle(label.color());
            message = Component.translatable("tooltip.prospect.prospecting_pickaxe.detected", oreName, strength);
        }

        player.displayClientMessage(message, true);
    }

    private static MutableComponent distanceComponent(int distInt) {
        String key = distInt == 1 ? "tooltip.prospect.prospecting_pickaxe.distance.block" : "tooltip.prospect.prospecting_pickaxe.distance.blocks";
        return Component.translatable(key, distInt);
    }

    private static DistanceLabel getFuzzyLabel(int distance) {
        int index = Math.min((distance - 1) / Math.max(distanceIncrement, 1), STRENGTH_KEYS.length - 1);
        return new DistanceLabel(STRENGTH_KEYS[index], LABEL_COLORS[index]);
    }

    public ProspectingTier getProspectingTier() {
        return prospectingTier;
    }
}