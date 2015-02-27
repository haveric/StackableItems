package haveric.stackableItems.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SIAddItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private ItemStack addedItem;
    private Inventory inventory;

    public SIAddItemEvent(Player player, ItemStack addedItem, Inventory inventory) {
        this.player = player;
        this.addedItem = addedItem;
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItemAdded() {
        return addedItem;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
