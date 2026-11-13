package alabaster.prospect.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class SlimeBucketItem extends Item {

    public static final String NAME_KEY = "CustomName";
    public static final String HEALTH_KEY = "Health";
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
        filled.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(tag));
        return filled;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(context.getClickedFace());
        Slime slime = EntityType.SLIME.create(server);
        if (slime == null) return InteractionResult.PASS;

        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0.0F);
        slime.setSize(1, true);
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(NAME_KEY)) {
            slime.setCustomName(Component.Serializer.fromJson(tag.getString(NAME_KEY), server.registryAccess()));
        }
        if (tag.contains(HEALTH_KEY)) {
            slime.setHealth(tag.getFloat(HEALTH_KEY));
        }
        slime.setPersistenceRequired();
        server.addFreshEntity(slime);

        Player player = context.getPlayer();
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        if (player != null) {
            player.setItemInHand(context.getHand(), BucketItem.getEmptySuccessItem(stack, player));
        }
        return InteractionResult.CONSUME;
    }
}