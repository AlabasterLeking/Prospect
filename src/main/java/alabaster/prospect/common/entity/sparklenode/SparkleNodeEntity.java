package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.prospecting.PanningTables;
import alabaster.prospect.common.registry.ProspectSounds;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import alabaster.prospect.common.registry.ProspectEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkleNodeEntity extends Entity {

    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IN_LAVA =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final Map<Integer, Boolean> CLIENT_HARVEST_LAVA = new HashMap<>();
    private static final int SHIMMER_CHANCE = 80;
    private static final float SPARKLE_CHANCE = 0.2F;
    private static final int FADE_TICKS = 200;
    private static final float MIN_FADE = 0.1F;

    private int lifetime = 1200; // 1 minutes default
    private UUID panner;

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && !beingPanned()) {
            this.entityData.set(AGE, getAge() + 1);
        }

        float fade = Math.max(MIN_FADE, Math.min(1.0F, (float) (lifetime - getAge()) / FADE_TICKS));
        if (random.nextFloat() < SPARKLE_CHANCE * fade) {
            double spread = 0.9;
            double x = getX() + (random.nextDouble() - 0.5) * spread;
            double y = getY() + 0.02;
            double z = getZ() + (random.nextDouble() - 0.5) * spread;

            var particle = isInLava() ? ParticleTypes.SMALL_FLAME : ParticleTypes.WAX_OFF;
            level().addParticle(particle, x, y, z, 0, 0.01, 0);
        }

        if (level().isClientSide() && random.nextFloat() * SHIMMER_CHANCE < fade) {
            level().playLocalSound(getX(), getY(), getZ(), ProspectSounds.SPARKLE_NODE_SHIMMER.get(), SoundSource.AMBIENT, 0.5F, 0.9F + random.nextFloat() * 0.3F, false);
        }

        if (!level().isClientSide() && getAge() >= lifetime) {
            fizzle();
            discard();
        }
    }

    private boolean beingPanned() {
        if (panner == null) return false;
        Player player = level().getPlayerByUUID(panner);
        if (player != null && player.isUsingItem() && player.getPersistentData().getInt("HarvestingNode") == getId()) return true;
        panner = null;
        return false;
    }

    private void fizzle() {
        if (!(level() instanceof ServerLevel server)) return;
        double y = getY() + 0.05;
        if (isInLava()) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), y, getZ(), 6, 0.25, 0.02, 0.25, 0.01);
            server.playSound(null, getX(), y, getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.3F, 1.6F + random.nextFloat() * 0.3F);
        } else {
            server.sendParticles(ParticleTypes.BUBBLE_POP, getX(), y, getZ(), 8, 0.3, 0.02, 0.3, 0.02);
            server.playSound(null, getX(), y, getZ(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.AMBIENT, 0.6F, 1.2F + random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AGE, 0);
        builder.define(IN_LAVA, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", getAge());
        compound.putBoolean("InLava", isInLava());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(AGE, compound.getInt("Age"));
        this.entityData.set(IN_LAVA, compound.getBoolean("InLava"));
    }

    private int getAge() {
        return this.entityData.get(AGE);
    }

    public void setInLava(boolean inLava) {
        this.entityData.set(IN_LAVA, inLava);
    }

    public boolean isInLava() {
        return this.entityData.get(IN_LAVA);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

        ItemStack panStack = player.getItemInHand(hand);
        if (!(panStack.getItem() instanceof PanItem panItem)) {
            if (!player.level().isClientSide) {
                boolean offhandPan = player.getOffhandItem().getItem() instanceof PanItem;
                player.displayClientMessage(Component.translatable(offhandPan ? "message.prospect.sparkle_node.main_hand" : "message.prospect.sparkle_node.need_tool")
                        .withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResult.PASS;
        }

        if (isInLava() && !panItem.canHarvestLava()) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("tooltip.prospect.pan.too_hot")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        // Only start use animation on client
        player.startUsingItem(hand);

        if (player.level().isClientSide) {
            CLIENT_HARVEST_LAVA.put(player.getId(), isInLava());
        } else if (player instanceof ServerPlayer serverPlayer) {
            // Record that the player is harvesting this node
            serverPlayer.getPersistentData().putInt("HarvestingNode", this.getId());
            panner = serverPlayer.getUUID();
            serverPlayer.getPersistentData().putString("HarvestingHand", hand.name());
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    public static boolean isClientHarvestingLava(int playerEntityId) {
        return CLIENT_HARVEST_LAVA.getOrDefault(playerEntityId, false);
    }

    private ResourceLocation getBiomeLootTable(ServerLevel level) {
        Holder<Biome> biome = level.getBiome(blockPosition());
        String table = PanningTables.resolve(level.getServer(), biome);
        return PanningTables.id(table != null ? table : biome.unwrapKey().map(key -> key.location().getPath()).orElse("plains"));
    }

    private static boolean isGem(ItemStack stack) {
        return stack.is(Tags.Items.GEMS) && !stack.is(Tags.Items.GEMS_QUARTZ);
    }

    private void playHarvestEffects(ServerLevel server, boolean gem) {
        boolean lava = isInLava();
        double x = getX();
        double y = getY() + 0.1;
        double z = getZ();
        if (lava) {
            server.sendParticles(ParticleTypes.LAVA, x, y, z, 6, 0.25, 0.05, 0.25, 0.0);
            server.playSound(null, x, y, z, SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.8F, 0.9F + random.nextFloat() * 0.2F);
        } else {
            server.sendParticles(ParticleTypes.SPLASH, x, y, z, 24, 0.3, 0.05, 0.3, 0.1);
            server.sendParticles(ParticleTypes.BUBBLE_POP, x, y, z, 8, 0.25, 0.02, 0.25, 0.02);
            server.playSound(null, x, y, z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.5F, 1.2F + random.nextFloat() * 0.2F);
        }
        if (gem) {
            server.sendParticles(ParticleTypes.WAX_OFF, x, y + 0.3, z, 10, 0.3, 0.2, 0.3, 0.6);
            server.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.1F + random.nextFloat() * 0.2F);
        }
    }

    public void harvestWith(Player player, ItemStack panStack, InteractionHand hand) {
        if (isInLava() && !(panStack.getItem() instanceof PanItem panItem && panItem.canHarvestLava())) {
            return;
        }

        // Damage the pan
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        panStack.hurtAndBreak(1, player, slot);

        if (!level().isClientSide && level() instanceof ServerLevel server) {
            if (player instanceof ServerPlayer serverPlayer) {
                ProspectAdvancements.award(serverPlayer, ProspectAdvancements.PAY_DIRT);
            }

            ResourceLocation tableId = getBiomeLootTable(server);
            ResourceKey<LootTable> lootKey =
                    ResourceKey.create(Registries.LOOT_TABLE, tableId);

            LootTable table = server.getServer()
                    .reloadableRegistries()
                    .getLootTable(lootKey);

            LootParams params = new LootParams.Builder(server)
                    .withParameter(LootContextParams.ORIGIN, position())
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, server.damageSources().generic())
                    .withLuck((panStack.getItem() instanceof PanItem pan ? pan.luck() : 0.0F) + player.getLuck())
                    .create(LootContextParamSets.ENTITY);

            Holder<Enchantment> panningLuck = server.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ProspectEnchantments.PANNING_LUCK);
            int luckLevel = panStack.getEnchantmentLevel(panningLuck);
            int rolls = 1 + luckLevel;

            List<ItemStack> allDrops = new ArrayList<>();
            for (int i = 0; i < rolls; i++) {
                var drops = table.getRandomItems(params);

                if (drops.isEmpty()) {
                    String fallbackName = isInLava() ? "nether_default" : "default";
                    ResourceKey<LootTable> defaultKey =
                            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("prospect", "gameplay/panning/" + fallbackName));
                    LootTable defaultTable = server.getServer()
                            .reloadableRegistries()
                            .getLootTable(defaultKey);
                    drops = defaultTable.getRandomItems(params);
                }

                allDrops.addAll(drops);
            }

            playHarvestEffects(server, allDrops.stream().anyMatch(SparkleNodeEntity::isGem));

            for (ItemStack lootStack : allDrops) {
                if (!player.getInventory().add(lootStack)) {
                    player.drop(lootStack, false);
                }
            }

            // Destroy the node
            discard();
        }
    }
}