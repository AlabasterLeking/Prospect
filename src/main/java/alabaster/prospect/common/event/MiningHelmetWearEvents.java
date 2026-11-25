package alabaster.prospect.common.event;

import alabaster.prospect.Config;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.MiningHelmetItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Prospect.MODID)
public class MiningHelmetWearEvents {

    private static final float DAMAGE_PER_DURABILITY = 4.0F;

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!Config.HELMET_TAKES_DAMAGE.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof MiningHelmetItem) || !helmet.canBeHurtBy(event.getSource())) return;
        float damage = event.getOriginalDamage() - event.getBlockedDamage();
        if (damage <= 0.0F) return;
        helmet.hurtAndBreak((int) Math.max(1.0F, damage / DAMAGE_PER_DURABILITY), entity, EquipmentSlot.HEAD);
    }
}