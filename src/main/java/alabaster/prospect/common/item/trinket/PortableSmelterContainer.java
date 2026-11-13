package alabaster.prospect.common.item.trinket;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PortableSmelterContainer implements Container {

    private final ItemStack smelter;
    private final Player owner;
    private final InteractionHand hand;

    public PortableSmelterContainer(ItemStack smelter, Player owner, InteractionHand hand) {
        this.smelter = smelter;
        this.owner = owner;
        this.hand = hand;
    }

    @Override
    public int getContainerSize() {
        return PortableSmelterItem.SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return PortableSmelterItem.readItems(smelter).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return PortableSmelterItem.readItems(smelter).get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var items = PortableSmelterItem.readItems(smelter);
        ItemStack taken = items.get(slot).split(amount);
        PortableSmelterItem.writeItems(smelter, items);
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var items = PortableSmelterItem.readItems(smelter);
        ItemStack taken = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        PortableSmelterItem.writeItems(smelter, items);
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack.getItem() instanceof PortableSmelterItem) {
            owner.getInventory().placeItemBackInInventory(stack);
            stack = ItemStack.EMPTY;
        }
        var items = PortableSmelterItem.readItems(smelter);
        items.set(slot, stack);
        PortableSmelterItem.writeItems(smelter, items);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof PortableSmelterItem);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && player.getItemInHand(hand) == smelter;
    }

    @Override
    public void clearContent() {
        PortableSmelterItem.writeItems(smelter, PortableSmelterItem.emptyItems());
    }
}