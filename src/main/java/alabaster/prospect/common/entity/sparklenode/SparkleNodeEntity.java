package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.item.PanItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class SparkleNodeEntity extends Entity {

    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(SparkleNodeEntity.class, EntityDataSerializers.INT);

    private int lifetime = 3600; // 3 minutes default

    public SparkleNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        this.entityData.set(AGE, getAge() + 1);

        if (random.nextFloat() < 0.2f) {
            double spread = 0.8;
            double x = getX() + (random.nextDouble() - 0.5) * spread;
            double y = getY() + 0.02;
            double z = getZ() + (random.nextDouble() - 0.5) * spread;

            level().addParticle(ParticleTypes.WAX_ON, x, y, z, 0, 0.01, 0);
        }

        if (getAge() >= lifetime) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AGE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", getAge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(AGE, compound.getInt("Age"));
    }

    private int getAge() {
        return this.entityData.get(AGE);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack panStack = player.getItemInHand(hand);
        if (!(panStack.getItem() instanceof PanItem)) return InteractionResult.PASS;

        // Only start use animation on client
        player.startUsingItem(hand);

        // Record that the player is harvesting this node
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getPersistentData().putInt("HarvestingNode", this.getId());
            serverPlayer.getPersistentData().putString("HarvestingHand", hand.name());
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private ResourceLocation getBiomeLootTable(ServerLevel level) {
        var biome = level.getBiome(blockPosition());
        ResourceLocation biomeId = biome.unwrapKey()
                .map(k -> k.location())
                .orElse(ResourceLocation.fromNamespaceAndPath("minecraft", "plains"));

        return ResourceLocation.fromNamespaceAndPath("prospect", "gameplay/panning/" + biomeId.getPath());
    }

    public void harvestWith(Player player, ItemStack panStack, InteractionHand hand) {
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

            var drops = table.getRandomItems(params);

            if (drops.isEmpty()) {
                ResourceKey<LootTable> defaultKey =
                        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("prospect", "gameplay/panning/default"));
                LootTable defaultTable = server.getServer()
                        .reloadableRegistries()
                        .getLootTable(defaultKey);
                drops = defaultTable.getRandomItems(params);
            }

            for (ItemStack lootStack : drops) {
                if (!player.getInventory().add(lootStack)) {
                    player.drop(lootStack, false);
                }
            }

            // Destroy the node
            discard();
        }
    }
}