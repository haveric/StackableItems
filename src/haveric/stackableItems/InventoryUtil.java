package haveric.stackableItems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    private static StackableItems plugin;

    public static void init(StackableItems si) {
        plugin = si;
    }
    /*
    public static int getFreeSpaces(Player player, ItemStack itemToCheck, Inventory inventory){
        return getFreeSpaces(player, itemToCheck, inventory, 0, inventory.getContents().length);
    }

    public static int getFreeSpaces(Player player, ItemStack itemToCheck, int start, int end){
        return getFreeSpaces(player, itemToCheck, player.getInventory(), start, end);
    }

    public static int getFreeSpaces(Player player, ItemStack itemToCheck){
        return getFreeSpaces(player, itemToCheck, player.getInventory(), 0, 36);
    }
    */
    public static int getFreeSpaces(Player player, ItemStack itemToCheck, Inventory inventory, int start, int end) {
        int free = 0;

        if (start < end && end <= inventory.getContents().length) {
            Material type = itemToCheck.getType();
            short durability = itemToCheck.getDurability();

            int maxAmount = SIItems.getItemMax(player, type, durability);
            if (maxAmount <= Config.ITEM_DEFAULT) {
                maxAmount = type.getMaxStackSize();
            }

            for (int i = start; i < end; i++) {
                ItemStack slot = inventory.getItem(i);

                if (slot == null) {
                    free += maxAmount;
                } else if (slot.getType() == type && slot.getDurability() == durability) {
                    boolean sameEnchants = slot.getEnchantments().equals(itemToCheck.getEnchantments());
                    boolean noEnchants = slot.getEnchantments() == null && itemToCheck.getEnchantments() == null;

                    if (sameEnchants || noEnchants) {
                        int freeInSlot = maxAmount - slot.getAmount();
                        if (freeInSlot > 0) {
                            free += freeInSlot;
                        }
                    }
                }
            }
        }
        return free;
    }

    /*
    public static void addItems(Player player, ItemStack itemToAdd, Inventory inventory){
        addItems(player, itemToAdd, inventory, 0, inventory.getContents().length);
    }

    public static void addItems(Player player, ItemStack itemToAdd, int start, int end){
        addItems(player, itemToAdd, player.getInventory(), start, end);
    }
    */

    public static void addItems(Player player, ItemStack itemToAdd) {
        addItems(player, itemToAdd, player.getInventory(), 0, 36);
    }

    public static void addItems(final Player player, final ItemStack itemToAdd, final Inventory inventory, final int start, final int end) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                if (start < end && end <= inventory.getContents().length) {
                    Material type = itemToAdd.getType();
                    short durability = itemToAdd.getDurability();

                    int maxAmount = SIItems.getItemMax(player, type, durability);
                    if (maxAmount <= Config.ITEM_DEFAULT) {
                        maxAmount = type.getMaxStackSize();
                    }

                    int addAmount = itemToAdd.getAmount();
                    // Add to existing stacks
                    for (int i = start; i < end && addAmount > 0; i++) {
                        ItemStack slot = inventory.getItem(i);
                        if (slot != null) {
                            if (slot.getType() == type && slot.getDurability() == durability) {
                                boolean sameEnchants = slot.getEnchantments().equals(itemToAdd.getEnchantments());
                                boolean noEnchants = slot.getEnchantments() == null && itemToAdd.getEnchantments() == null;

                                if (sameEnchants || noEnchants) {
                                    int free = slot.getAmount();

                                    int canAdd = maxAmount - free;
                                    if (addAmount <= canAdd) {
                                        slot.setAmount(free + addAmount);
                                        inventory.setItem(i, slot);
                                        addAmount = 0;
                                    } else if (addAmount <= maxAmount) {
                                        slot.setAmount(maxAmount);
                                        inventory.setItem(i, slot);
                                        addAmount -= canAdd;
                                    } else {
                                        slot.setAmount(maxAmount);
                                        inventory.setItem(i, slot);
                                        addAmount -= maxAmount;
                                    }
                                }
                            }
                        }
                    }

                    // Add to empty slots
                    for (int i = start; i < end && addAmount > 0; i++) {
                        ItemStack slot = inventory.getItem(i);
                        if (slot == null) {
                            if (addAmount >= maxAmount) {
                                itemToAdd.setAmount(maxAmount);
                                inventory.setItem(i, itemToAdd.clone());
                                addAmount -= maxAmount;
                            } else if (addAmount > 0) {
                                itemToAdd.setAmount(addAmount);
                                inventory.setItem(i, itemToAdd.clone());
                                addAmount = 0;
                            }
                        }
                    }
                    if (addAmount > 0) {
                        ItemStack clone = itemToAdd.clone();
                        clone.setAmount(addAmount);
                        player.getWorld().dropItemNaturally(player.getLocation(), clone);
                    }
                }
            }
        });
    }

    public static void moveItems(Player player, ItemStack clicked, InventoryClickEvent event, int start, int end) {
        moveItems(player, clicked, event, player.getInventory(), start, end);
    }

    public static void moveItems(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory) {
        moveItems(player, clicked, event, inventory, 0, inventory.getContents().length);
    }

    public static void moveItems(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, int start, int end) {
        event.setCancelled(true);
        ItemStack clone = clicked.clone();
        int free = getFreeSpaces(player, clone, inventory, start, end);

        int clickedAmount = clicked.getAmount();

        if (free >= clickedAmount) {
            addItems(player, clone, inventory, start, end);
            event.setCurrentItem(null);
        } else {
            int left = clickedAmount - free;
            if (left > 0) {
                clone.setAmount(free);
                addItems(player, clone, inventory, start, end);

                ItemStack clone2 = clicked.clone();
                clone2.setAmount(left);
                event.setCurrentItem(clone2);
            }
        }
    }
}
