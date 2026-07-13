package alabaster.prospect.common.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

/**
 * Deliberately plain Item + Equipable, NOT ArmorItem. Extending ArmorItem
 * would give correct armor stats "for free" but also hijacks worn-item
 * rendering: vanilla's HumanoidArmorLayer renders armor by looking up the
 * item's ArmorMaterial and drawing one of its fixed vanilla texture layers
 * on the standard humanoid shape, completely bypassing this item's own
 * custom 3D "head" perspective model. Staying a plain Item keeps the custom
 * worn model intact, and as a side effect also blocks armor trim
 * application entirely, since trims only apply to genuine ArmorItems.
 */
public class MiningHelmetItem extends Item implements Equipable {

    private static final String LIGHT_ON_KEY = "LightOn";

    public MiningHelmetItem(Properties properties) {
        super(properties.durability(128).attributes(createIronHelmetAttributes()));
    }

    private static ItemAttributeModifiers createIronHelmetAttributes() {
        ArmorMaterial material = ArmorMaterials.IRON.value();
        ArmorItem.Type type = ArmorItem.Type.HELMET;

        int defense = material.getDefense(type);
        float toughness = material.toughness();
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
        ResourceLocation id = ResourceLocation.withDefaultNamespace("armor." + type.getName());

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ARMOR, new AttributeModifier(id, defense, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), slotGroup);

        float knockbackResistance = material.knockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, knockbackResistance, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        }

        return builder.build();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairCandidate) {
        return repairCandidate.is(Items.IRON_INGOT);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    public static boolean isLightOn(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains(LIGHT_ON_KEY) || tag.getBoolean(LIGHT_ON_KEY);
    }

    public static void setLightOn(ItemStack stack, boolean on) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(LIGHT_ON_KEY, on);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean toggleLight(ItemStack stack) {
        boolean newState = !isLightOn(stack);
        setLightOn(stack, newState);
        return newState;
    }
}