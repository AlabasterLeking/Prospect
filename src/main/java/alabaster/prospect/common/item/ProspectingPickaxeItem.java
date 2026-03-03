package alabaster.prospect.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
import java.util.ArrayDeque;
import java.util.Queue;

public class ProspectingPickaxeItem extends DiggerItem {

    public enum ProspectingTier {
        STONE(Tiers.STONE,         5,  0.50f, false, false, 60),
        IRON(Tiers.IRON,          12,  0.10f, false, false, 40),
        GOLD(Tiers.GOLD,          15,  0.00f, false,  true, 40),
        DIAMOND(Tiers.DIAMOND,    20,  0.00f, false, false, 20),
        NETHERITE(Tiers.NETHERITE, 25, 0.00f, false, true,  20);

        public final Tiers vanillaTier;
        public final int radius;
        public final float falsePositiveRate;
        public final boolean directional;
        public final boolean exactDistance;
        public final int cooldownTicks;

        ProspectingTier(Tiers vanillaTier, int radius, float falsePositiveRate, boolean directional, boolean exactDistance, int cooldownTicks) {
            this.vanillaTier       = vanillaTier;
            this.radius            = radius;
            this.falsePositiveRate = falsePositiveRate;
            this.directional       = directional;
            this.exactDistance     = exactDistance;
            this.cooldownTicks     = cooldownTicks;
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
        ORE_COLORS.put("minecraft:ores",           ChatFormatting.GRAY);
    }

    private static final List<String> ORE_TAG_KEYS = new ArrayList<>(ORE_COLORS.keySet());

    private static final record DistanceLabel(int maxDist, String label, ChatFormatting color) {}

    private static final List<DistanceLabel> DISTANCE_LABELS = List.of(
            new DistanceLabel(5,               "Very Strong", ChatFormatting.RED),
            new DistanceLabel(10,              "Strong",      ChatFormatting.GOLD),
            new DistanceLabel(15,              "Moderate",    ChatFormatting.YELLOW),
            new DistanceLabel(20,              "Faint",       ChatFormatting.GREEN),
            new DistanceLabel(Integer.MAX_VALUE, "Very Faint", ChatFormatting.DARK_GREEN)
    );

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

        BlockPos clickedPos = ctx.getClickedPos();
        Direction clickedFace = ctx.getClickedFace();

        if (player.getCooldowns().isOnCooldown(ctx.getItemInHand().getItem())) {
            return InteractionResult.PASS;
        }

        List<BlockPos> candidates = prospectingTier.directional
                ? collectDirectional(level, clickedPos, clickedFace, prospectingTier.radius)
                : collectSpherical(level, clickedPos, prospectingTier.radius);

        Set<BlockPos> clickedVein = collectVein(level, clickedPos);

        OreHit hit = findNearestOre(level, clickedPos, candidates, clickedVein);

        hit = applyFalsePositive(hit, clickedPos, player.getRandom(),
                prospectingTier.falsePositiveRate, prospectingTier.radius);

        if (hit == null) {
            player.displayClientMessage(Component.literal("No ores detected nearby.")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
        } else {
            sendOreMessage(player, hit, prospectingTier);
        }

        ItemStack stack = ctx.getItemInHand();
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        player.getCooldowns().addCooldown(stack.getItem(), prospectingTier.cooldownTicks);

        return InteractionResult.sidedSuccess(level.isClientSide());
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

    private static Set<BlockPos> collectVein(Level level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        if (getOreColor(level, originState) == null) return Set.of();

        Block targetBlock = originState.getBlock();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).getBlock() == targetBlock) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    private static OreHit findNearestOre(Level level, BlockPos origin, List<BlockPos> candidates, Set<BlockPos> excludedVein) {
        OreHit best = null;

        for (BlockPos pos : candidates) {
            if (excludedVein.contains(pos)) continue;

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
        int dist = (int) Math.ceil(hit.distance());

        MutableComponent oreName = Component.translatable(hit.oreName())
                .withStyle(Style.EMPTY.withColor(hit.color()));

        MutableComponent message;

        if (tier.exactDistance) {
            message = Component.literal("Detected ")
                    .append(oreName)
                    .append(Component.literal(" at exactly ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(dist + " block" + (dist == 1 ? "" : "s") + " away.")
                            .withStyle(ChatFormatting.AQUA));
        } else {
            DistanceLabel label = getFuzzyLabel(dist);
            MutableComponent strength = Component.literal(label.label())
                    .withStyle(label.color());

            message = Component.literal("Ore signal: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(strength)
                    .append(Component.literal(" – ").withStyle(ChatFormatting.GRAY))
                    .append(oreName);
        }

        player.displayClientMessage(message, true);
    }

    private static DistanceLabel getFuzzyLabel(int distance) {
        for (DistanceLabel label : DISTANCE_LABELS) {
            if (distance <= label.maxDist()) return label;
        }
        return DISTANCE_LABELS.get(DISTANCE_LABELS.size() - 1);
    }

    public ProspectingTier getProspectingTier() {
        return prospectingTier;
    }
}