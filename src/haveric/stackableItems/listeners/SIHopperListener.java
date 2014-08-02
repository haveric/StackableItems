package haveric.stackableItems.listeners;

import org.bukkit.event.Listener;

public class SIHopperListener implements Listener{
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

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void hopperPickup(InventoryPickupItemEvent event) {
        if (Config.isHopperUsingStacks()) {
            Item item = event.getItem();
            ItemStack stack = item.getItemStack();
            Inventory inventory = event.getInventory();

            int defaultMax = SIItems.getInventoryMax(item.getWorld().getName(), stack.getType(), stack.getDurability(), inventory.getName());

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
                StackableItems.log.info("Hopper Pickup");
                InventoryUtil.addItems(item.getLocation(), stack, inventory, defaultMax, true);
                event.setCancelled(true);
            }
        }
    }
    */
}
