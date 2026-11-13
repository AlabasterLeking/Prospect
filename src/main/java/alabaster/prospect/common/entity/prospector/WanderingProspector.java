package alabaster.prospect.common.entity.prospector;

import alabaster.prospect.common.entity.ai.AvoidDynamiteGoal;
import alabaster.prospect.common.item.MiningHelmetItem;
import alabaster.prospect.common.registry.ProspectModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WanderingProspector extends WanderingTrader {

    public static final String PACK_MULE_TAG = "prospect_pack_mule";

    private static final float VOICE_PITCH = 0.8F;
    private static final float CURE_DISCOUNT = 0.3F;
    private static final long RESTOCK_INTERVAL = 24000L;

    private long lastRestock = -1L;

    @Nullable
    private UUID curedBy;

    public WanderingProspector(EntityType<? extends WanderingProspector> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.getAvailableGoals().removeIf(wrapped -> wrapped.getGoal() instanceof UseItemGoal);
        goalSelector.addGoal(1, new AvoidDynamiteGoal(this, 0.5, SoundEvents.WANDERING_TRADER_NO));
        goalSelector.addGoal(7, new ProspectorPanGoal(this));
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH;
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = getOffers();
        addOffersFromItemListings(offers, ProspectorTrades.COMMON, 5);

        List<VillagerTrades.ItemListing> rare = new ArrayList<>(List.of(ProspectorTrades.RARE));
        while (!rare.isEmpty()) {
            MerchantOffer offer = rare.remove(random.nextInt(rare.size())).getOffer(this, random);
            if (offer != null) {
                offers.add(offer);
                return;
            }
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ProspectModItems.MINING_HELMET.get()));
        setDropChance(EquipmentSlot.HEAD, 0.0F);
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        restockIfDue();
        if (tickCount % 20 != 0) return;

        ItemStack helmet = getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof MiningHelmetItem)) return;

        boolean dark = level().isNight() || level().getBrightness(LightLayer.SKY, blockPosition()) < 8;
        if (MiningHelmetItem.isLightOn(helmet) != dark) {
            MiningHelmetItem.setLightOn(helmet, dark);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED && !level().isClientSide()) {
            for (Mule mule : level().getEntitiesOfClass(Mule.class, getBoundingBox().inflate(16.0), m -> m.getLeashHolder() == this)) {
                mule.discard();
            }
        }
        super.remove(reason);
    }

    public void markCured(@Nullable UUID curer) {
        curedBy = curer;
        setDespawnDelay(0);
        setPersistenceRequired();
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        super.setTradingPlayer(player);
        if (player == null) return;
        boolean curer = curedBy != null && curedBy.equals(player.getUUID());
        for (MerchantOffer offer : getOffers()) {
            offer.setSpecialPriceDiff(curer ? -Math.max(1, Math.round(offer.getBaseCostA().getCount() * CURE_DISCOUNT)) : 0);
        }
    }

    private void restockIfDue() {
        long now = level().getGameTime();
        if (lastRestock < 0) {
            lastRestock = now;
            return;
        }
        if (now - lastRestock < RESTOCK_INTERVAL || isTrading()) return;
        lastRestock = now;
        getOffers().forEach(MerchantOffer::resetUses);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("LastRestock", lastRestock);
        if (curedBy != null) {
            tag.putUUID("CuredBy", curedBy);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("LastRestock")) {
            lastRestock = tag.getLong("LastRestock");
        }
        if (tag.hasUUID("CuredBy")) {
            curedBy = tag.getUUID("CuredBy");
        }
    }
}