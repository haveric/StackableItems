package haveric.stackableItems.listeners;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.SIItems;

import org.bukkit.block.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SIHopperListener implements Listener {
/*
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void hopperMove(InventoryMoveItemEvent event) {
        Inventory fromInventory = event.getSource();
        Inventory toInventory = event.getDestination();
        InventoryHolder holder = toInventory.getHolder();
        if (holder instanceof Container) {
            Container container = (Container) holder;
            Location location = container.getLocation();

            ItemStack stack = event.getItem();
            int defaultMax = SIItems.getInventoryMax(location.getWorld().getName(), stack.getType(), stack.getDurability(), toInventory.getType());

            // Don't touch default or infinite items.
            if (defaultMax == SIItems.ITEM_DEFAULT || defaultMax == SIItems.ITEM_INFINITE) {
                return;
            }

            if (defaultMax == 0) {
                event.setCancelled(true);
            } else if (defaultMax > 0) {
                if (!InventoryUtil.canVanillaMoveHopper(toInventory, stack)) {
                    InventoryUtil.moveItemsFromHopper(location, stack.clone(), fromInventory, toInventory, defaultMax);

                    event.setCancelled(true);
                }
            }
        }
    }
*/
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hopperPickup(InventoryPickupItemEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        String worldName = null;

        if (holder instanceof Hopper hopper) {
            worldName = hopper.getWorld().getName();
        } else if (holder instanceof HopperMinecart hopperMinecart) {
            worldName = hopperMinecart.getWorld().getName();
        }

        if (worldName != null && SIItems.isInventoryEnabled(worldName, inventory)) {
            Item item = event.getItem();
            ItemStack stack = item.getItemStack().clone();


            int defaultMax = SIItems.getInventoryMax(item.getWorld().getName(), stack.getType(), stack.getDurability(), inventory.getType());

            // Don't touch default or infinite items.
            if (defaultMax == SIItems.ITEM_DEFAULT || defaultMax == SIItems.ITEM_INFINITE) {
                return;
            }

            // Bounce the item up off the hopper
            if (defaultMax == 0) {
                item.setVelocity(new Vector((Math.random() * .5) - .25, .5, (Math.random() * .5) - .25));
                event.setCancelled(true);
            } else if (defaultMax > 0) {
                item.remove();
                InventoryUtil.addItems(item.getLocation(), stack, inventory, defaultMax, true);
                event.setCancelled(true);
            }
        }
    }

}
