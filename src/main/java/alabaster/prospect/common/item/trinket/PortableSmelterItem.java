package alabaster.prospect.common.item.trinket;

import alabaster.prospect.common.registry.ProspectDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class PortableSmelterItem extends Item {

    public static final int SLOTS = 3;
    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int OUTPUT = 2;

    public PortableSmelterItem(Properties properties) {
        super(properties);
    }

    public static NonNullList<ItemStack> emptyItems() {
        return NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    }

    public static NonNullList<ItemStack> readItems(ItemStack smelter) {
        NonNullList<ItemStack> items = emptyItems();
        smelter.getOrDefault(ProspectDataComponents.SMELTER_ITEMS.get(), ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    public static void writeItems(ItemStack smelter, NonNullList<ItemStack> items) {
        smelter.set(ProspectDataComponents.SMELTER_ITEMS.get(), ItemContainerContents.fromItems(items));
    }

    public static SmelterState readState(ItemStack smelter) {
        return smelter.getOrDefault(ProspectDataComponents.SMELTER_STATE.get(), SmelterState.EMPTY);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            PortableSmelterContainer container = new PortableSmelterContainer(stack, player, hand);
            ContainerData data = new ContainerData() {
                @Override
                public int get(int index) {
                    SmelterState state = readState(stack);
                    return switch (index) {
                        case 0 -> state.litTime();
                        case 1 -> state.litDuration();
                        case 2 -> state.cookingProgress();
                        case 3 -> state.cookingTotalTime();
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                }

                @Override
                public int getCount() {
                    return 4;
                }
            };
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new FurnaceMenu(id, inventory, container, data), stack.getHoverName()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        SmelterState state = readState(stack);
        NonNullList<ItemStack> items = readItems(stack);
        ItemStack input = items.get(INPUT);
        ItemStack fuel = items.get(FUEL);
        if (!state.isLit() && (input.isEmpty() || fuel.isEmpty())) {
            if (state.cookingProgress() > 0) {
                stack.set(ProspectDataComponents.SMELTER_STATE.get(), new SmelterState(0, state.litDuration(), 0, state.cookingTotalTime()));
            }
            return;
        }

        int litTime = Math.max(0, state.litTime() - 1);
        int litDuration = state.litDuration();
        int progress = state.cookingProgress();
        int total = state.cookingTotalTime();
        boolean itemsChanged = false;

        Optional<RecipeHolder<SmeltingRecipe>> recipe = input.isEmpty() ? Optional.empty()
                : serverLevel.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), serverLevel);
        ItemStack result = recipe.map(holder -> holder.value().assemble(new SingleRecipeInput(input), serverLevel.registryAccess())).orElse(ItemStack.EMPTY);
        boolean canSmelt = !result.isEmpty() && canAccept(items.get(OUTPUT), result);

        if (litTime == 0 && canSmelt && !fuel.isEmpty()) {
            int burn = fuel.getBurnTime(RecipeType.SMELTING);
            if (burn > 0) {
                litTime = burn;
                litDuration = burn;
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty()) {
                    items.set(FUEL, remainder);
                }
                itemsChanged = true;
            }
        }

        if (litTime > 0 && canSmelt) {
            total = recipe.get().value().getCookingTime();
            progress++;
            if (progress >= total) {
                progress = 0;
                ItemStack output = items.get(OUTPUT);
                if (output.isEmpty()) {
                    items.set(OUTPUT, result.copy());
                } else {
                    output.grow(result.getCount());
                }
                input.shrink(1);
                itemsChanged = true;
                float experience = recipe.get().value().getExperience();
                if (entity instanceof Player player && experience > 0) {
                    int whole = (int) experience;
                    if (serverLevel.random.nextFloat() < experience - whole) whole++;
                    if (whole > 0) player.giveExperiencePoints(whole);
                }
            }
        } else if (progress > 0) {
            progress = Math.max(0, progress - 2);
        }

        if (itemsChanged) {
            writeItems(stack, items);
        }
        SmelterState next = new SmelterState(litTime, litDuration, progress, total);
        if (!next.equals(state)) {
            stack.set(ProspectDataComponents.SMELTER_STATE.get(), next);
        }
    }

    private static boolean canAccept(ItemStack output, ItemStack result) {
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NonNullList<ItemStack> items = readItems(stack);
        if (readState(stack).isLit()) {
            tooltip.add(Component.translatable("item.prospect.portable_smelter.lit").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        for (int i = 0; i < SLOTS; i++) {
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                tooltip.add(Component.translatable("item.prospect.portable_smelter.slot", item.getCount(), item.getHoverName()).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
    }
}