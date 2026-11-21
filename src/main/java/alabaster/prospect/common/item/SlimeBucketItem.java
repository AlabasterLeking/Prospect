package alabaster.prospect.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SlimeBucketItem extends Item {

    public static final String NAME_KEY = "CustomName";
    public static final String HEALTH_KEY = "Health";
    private static final String NO_AI_KEY = "NoAI";
    private static final String SILENT_KEY = "Silent";
    private static final String NO_GRAVITY_KEY = "NoGravity";
    private static final String GLOWING_KEY = "Glowing";
    private static final String INVULNERABLE_KEY = "Invulnerable";
    public static final int DANCE_FRAMES = 6;
    public static final int DANCE_TICKS_PER_FRAME = 3;

    private static boolean dancing;

    public SlimeBucketItem(Properties properties) {
        super(properties);
    }

    public static boolean isDancing() {
        return dancing;
    }

    public static void setDancing(boolean value) {
        dancing = value;
    }

    public static ItemStack capture(Slime slime, Item bucket) {
        ItemStack filled = new ItemStack(bucket);
        CompoundTag tag = new CompoundTag();
        if (slime.hasCustomName()) {
            tag.putString(NAME_KEY, Component.Serializer.toJson(slime.getCustomName(), slime.registryAccess()));
        }
        tag.putFloat(HEALTH_KEY, slime.getHealth());
        if (slime.isNoAi()) tag.putBoolean(NO_AI_KEY, true);
        if (slime.isSilent()) tag.putBoolean(SILENT_KEY, true);
        if (slime.isNoGravity()) tag.putBoolean(NO_GRAVITY_KEY, true);
        if (slime.hasGlowingTag()) tag.putBoolean(GLOWING_KEY, true);
        if (slime.isInvulnerable()) tag.putBoolean(INVULNERABLE_KEY, true);
        filled.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(tag));
        return filled;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (!level.isClientSide() && context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("message.prospect.slime_bucket.peaceful"), true);
            }
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (!release(server, pos, stack, context.getRotation(), player)) return InteractionResult.PASS;
        if (player != null) {
            player.setItemInHand(context.getHand(), BucketItem.getEmptySuccessItem(stack, player));
        }
        return InteractionResult.CONSUME;
    }

    public static boolean release(ServerLevel server, BlockPos pos, ItemStack stack, float yRot, @Nullable Player player) {
        Slime slime = EntityType.SLIME.create(server);
        if (slime == null) return false;

        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yRot, 0.0F);
        slime.setSize(1, true);
        CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(NAME_KEY)) {
            slime.setCustomName(Component.Serializer.fromJson(tag.getString(NAME_KEY), server.registryAccess()));
        }
        if (tag.contains(HEALTH_KEY)) {
            slime.setHealth(tag.getFloat(HEALTH_KEY));
        }
        slime.setNoAi(tag.getBoolean(NO_AI_KEY));
        slime.setSilent(tag.getBoolean(SILENT_KEY));
        slime.setNoGravity(tag.getBoolean(NO_GRAVITY_KEY));
        slime.setGlowingTag(tag.getBoolean(GLOWING_KEY));
        slime.setInvulnerable(tag.getBoolean(INVULNERABLE_KEY));
        slime.setPersistenceRequired();
        server.addFreshEntity(slime);

        server.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        server.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        return true;
    }
}