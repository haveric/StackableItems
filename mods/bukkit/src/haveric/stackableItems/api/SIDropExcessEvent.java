package haveric.stackableItems.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SIDropExcessEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private ItemStack droppedItem;
    private Inventory inventory;

    public SIDropExcessEvent(Player player, ItemStack droppedItem, Inventory inventory) {
        this.player = player;
        this.droppedItem = droppedItem;
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItemDropped() {
        return droppedItem;
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
