package alabaster.prospect.common.entity.prospector;

import alabaster.prospect.Config;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LevelEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class ZombieProspector extends Zombie {

    public static final int MAX_NATURAL_SPAWN_Y = 32;
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING = SynchedEntityData.defineId(ZombieProspector.class, EntityDataSerializers.BOOLEAN);
    private static final byte CURE_EVENT = 16;
    private static final int CURE_TIME_MIN = 3600;
    private static final int CURE_TIME_SPREAD = 2401;

    private int conversionTime;
    @Nullable
    private UUID conversionStarter;

    public ZombieProspector(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    public static boolean checkSpawnRules(EntityType<ZombieProspector> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) return true;
        if (!Config.ZOMBIE_PROSPECTOR_SPAWNS.get()) return false;
        return pos.getY() <= MAX_NATURAL_SPAWN_Y && !level.canSeeSky(pos);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        float roll = random.nextFloat();
        Item pickaxe;
        if (roll < 0.03F) {
            pickaxe = ProspectModItems.PROSPECTING_PICKAXE.get();
        } else if (roll < 0.12F) {
            pickaxe = Items.GOLDEN_PICKAXE;
        } else if (roll < 0.45F) {
            pickaxe = Items.IRON_PICKAXE;
        } else {
            pickaxe = Items.STONE_PICKAXE;
        }
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(pickaxe));
        if (random.nextFloat() < 0.35F) {
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(ProspectModItems.MINING_HELMET.get()));
        }
    }

    @Override
    public void setBaby(boolean baby) {
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void copyHelmetFrom(ItemStack helmet) {
        if (!helmet.isEmpty()) {
            setItemSlot(EquipmentSlot.HEAD, helmet.copy());
            setDropChance(EquipmentSlot.HEAD, 1.0F);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONVERTING, false);
    }

    public boolean isConverting() {
        return entityData.get(DATA_CONVERTING);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GOLDEN_APPLE)) {
            if (!hasEffect(MobEffects.WEAKNESS) || isConverting()) return InteractionResult.CONSUME;
            stack.consume(1, player);
            if (!level().isClientSide()) {
                startConverting(player.getUUID(), random.nextInt(CURE_TIME_SPREAD) + CURE_TIME_MIN);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void startConverting(@Nullable UUID starter, int time) {
        conversionStarter = starter;
        conversionTime = time;
        entityData.set(DATA_CONVERTING, true);
        removeEffect(MobEffects.WEAKNESS);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, Math.min(level().getDifficulty().getId() - 1, 0)));
        level().broadcastEntityEvent(this, CURE_EVENT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == CURE_EVENT) {
            if (!isSilent()) {
                level().playLocalSound(getX(), getEyeY(), getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, getSoundSource(),
                        1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && isAlive() && isConverting()) {
            conversionTime--;
            if (conversionTime <= 0 && level() instanceof ServerLevel serverLevel) {
                finishConversion(serverLevel);
                return;
            }
        }
        super.tick();
    }

    private void finishConversion(ServerLevel level) {
        ItemStack helmet = getItemBySlot(EquipmentSlot.HEAD).copy();
        WanderingProspector prospector = convertTo(ProspectModEntities.WANDERING_PROSPECTOR.get(), false);
        if (prospector == null) return;
        EventHooks.finalizeMobSpawn(prospector, level, level.getCurrentDifficultyAt(prospector.blockPosition()), MobSpawnType.CONVERSION, null);
        if (!helmet.isEmpty()) {
            prospector.setItemSlot(EquipmentSlot.HEAD, helmet);
        }
        prospector.markCured(conversionStarter);
        prospector.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        EventHooks.onLivingConvert(this, prospector);
        if (!isSilent()) {
            level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_CONVERTED, blockPosition(), 0);
        }
        if (conversionStarter != null && level.getPlayerByUUID(conversionStarter) instanceof ServerPlayer player) {
            ProspectAdvancements.award(player, ProspectAdvancements.BACK_FROM_THE_DEAD);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !isConverting() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ConversionTime", isConverting() ? conversionTime : -1);
        if (conversionStarter != null) {
            tag.putUUID("ConversionPlayer", conversionStarter);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ConversionTime") && tag.getInt("ConversionTime") > -1) {
            startConverting(tag.hasUUID("ConversionPlayer") ? tag.getUUID("ConversionPlayer") : null, tag.getInt("ConversionTime"));
        }
    }
}