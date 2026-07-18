package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.item.PanItem;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkleNodeEntity extends Entity {

    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IN_LAVA =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final Map<Integer, Boolean> CLIENT_HARVEST_LAVA = new HashMap<>();

    private int lifetime = 1200; // 1 minutes default

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        this.entityData.set(AGE, getAge() + 1);

        if (random.nextFloat() < 0.2f) {
            double spread = 0.9;
            double x = getX() + (random.nextDouble() - 0.5) * spread;
            double y = getY() + 0.02;
            double z = getZ() + (random.nextDouble() - 0.5) * spread;

            var particle = isInLava() ? ParticleTypes.SMALL_FLAME : ParticleTypes.WAX_OFF;
            level().addParticle(particle, x, y, z, 0, 0.01, 0);
        }

        if (getAge() >= lifetime) {
            discard();
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
        if (!(panStack.getItem() instanceof PanItem panItem)) return InteractionResult.PASS;

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
            serverPlayer.getPersistentData().putString("HarvestingHand", hand.name());
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    public static boolean isClientHarvestingLava(int playerEntityId) {
        return CLIENT_HARVEST_LAVA.getOrDefault(playerEntityId, false);
    }

    private ResourceLocation getBiomeLootTable(ServerLevel level) {
        var biome = level.getBiome(blockPosition());
        ResourceLocation biomeId = biome.unwrapKey()
                .map(k -> k.location())
                .orElse(ResourceLocation.fromNamespaceAndPath("minecraft", "plains"));

        return ResourceLocation.fromNamespaceAndPath("prospect", "gameplay/panning/" + biomeId.getPath());
    }

    public void harvestWith(Player player, ItemStack panStack, InteractionHand hand) {
        if (isInLava() && !(panStack.getItem() instanceof PanItem panItem && panItem.canHarvestLava())) {
            return;
        }

        // Damage the pan
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        panStack.hurtAndBreak(1, player, slot);

        if (!level().isClientSide && level() instanceof ServerLevel server) {
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
                    .withParameter(LootContextParams.DAMAGE_SOURCE, null)
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