package haveric.stackableItems.listeners;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.SIItems;

import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SIHopperListener implements Listener {
    /*
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void hopperMove(InventoryMoveItemEvent event) {
        if (Config.isHopperUsingStacks()) {
            Inventory toInventory = event.getDestination();
            InventoryHolder holder = toInventory.getHolder();
            if (holder != null) {
                Location location = null;
                if (holder instanceof Chest) {
                    Chest block = (Chest)holder;
                    location = block.getLocation();
                } else if (holder instanceof Hopper) {
                    Hopper block = (Hopper)holder;
                    location = block.getLocation();
                } else if (holder instanceof HopperMinecart) {
                    HopperMinecart block = (HopperMinecart)holder;
                    location = block.getLocation();
                } else if (holder instanceof StorageMinecart) {
                    StorageMinecart block = (StorageMinecart)holder;
                    location = block.getLocation();
                } else if (holder instanceof Beacon) {
                    Beacon block = (Beacon)holder;
                    location = block.getLocation();
                } else if (holder instanceof BrewingStand) {
                    BrewingStand block = (BrewingStand)holder;
                    location = block.getLocation();
                } else if (holder instanceof Dispenser) {
                    Dispenser block = (Dispenser)holder;
                    location = block.getLocation();
                } else if (holder instanceof Dropper) {
                    Dropper block = (Dropper)holder;
                    location = block.getLocation();
                } else if (holder instanceof Furnace) {
                    Furnace block = (Furnace)holder;
                    location = block.getLocation();
                }

                if (location != null) {
                    ItemStack stack = event.getItem();
                    int defaultMax = SIItems.getInventoryMax(location.getWorld().getName(), stack.getType(), stack.getDurability(), toInventory.getName());

                    // Don't touch default or infinite items.
                    if (defaultMax == SIItems.ITEM_DEFAULT || defaultMax == SIItems.ITEM_INFINITE) {
                        return;
                    }

                    if (defaultMax == 0) {
                        event.setCancelled(true);
                    } else if (defaultMax > 0){
                        if (!InventoryUtil.canVanillaMoveHopper(toInventory, stack)) {
                            Inventory fromInventory = event.getSource();
                            InventoryUtil.moveItemsFromHopper(location, stack.clone(), fromInventory, toInventory, defaultMax);

                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
     */
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hopperPickup(InventoryPickupItemEvent event) {
        Inventory inventory = event.getInventory();
        Hopper hopper = (Hopper) inventory.getHolder();
        String worldName = hopper.getWorld().getName();

        if (SIItems.isInventoryEnabled(worldName, inventory)) {
            Item item = event.getItem();
            ItemStack stack = item.getItemStack();


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
