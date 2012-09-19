package haveric.stackableItems;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public final class InventoryUtil {

    private static StackableItems plugin;

    private InventoryUtil() { } // Private constructor for utility class

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
    */
    public static int getFreeSpaces(Player player, ItemStack itemToCheck) {
        return getFreeSpaces(player, itemToCheck, player.getInventory(), 0, 36);
    }

    public static int getFreeSpaces(Player player, ItemStack itemToCheck, Inventory inventory, int start, int end) {
        int free = 0;

        if (start < end && end <= inventory.getContents().length) {
            Material type = itemToCheck.getType();
            short durability = itemToCheck.getDurability();

            Iterator<ItemStack> iter = inventory.iterator(start);
            int i = start;
            while (iter.hasNext() && i < end) {
                ItemStack slot = iter.next();

                int maxAmount = getInventoryMax(player, inventory, type, durability, i);

                if (slot == null) {
                    free += maxAmount;
                } else if (ItemUtil.isSameItem(slot, itemToCheck)) {
                    int freeInSlot = maxAmount - slot.getAmount();
                    if (freeInSlot > 0) {
                        free += freeInSlot;
                    }
                }
                i++;
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

                    int addAmount = itemToAdd.getAmount();

                    // Add to existing stacks
                    Iterator<ItemStack> iter = inventory.iterator(start);
                    int i = start;
                    while (iter.hasNext() && i < end && addAmount > 0) {
                        ItemStack slot = iter.next();

                        if (slot != null && ItemUtil.isSameItem(slot, itemToAdd)) {
                            int slotAmount = slot.getAmount();

                            int maxAmount = getInventoryMax(player, inventory, type, durability, i);

                            int canAdd = maxAmount - slotAmount;
                            if (canAdd > 0) {
                                if (addAmount <= canAdd) {
                                    slot.setAmount(slotAmount + addAmount);
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
                        i++;
                    }
                    // Reset the iterator to start
                    iter = inventory.iterator(start);
                    i = start;
                    // Add to empty slots
                    while (iter.hasNext() && i < end && addAmount > 0) {
                        ItemStack slot = iter.next();

                        if (slot == null) {
                            int maxAmount = getInventoryMax(player, inventory, type, durability, i);
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
                        i++;
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

    public static int moveItems(Player player, ItemStack clicked, InventoryClickEvent event, int start, int end, boolean setLeft) {
        return moveItems(player, clicked, event, player.getInventory(), start, end, setLeft);
    }

    public static int moveItems(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, boolean setLeft) {
        return moveItems(player, clicked, event, inventory, 0, inventory.getContents().length, setLeft);
    }

    public static int moveItems(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, int start, int end, boolean setLeft) {
        event.setCancelled(true);
        ItemStack clone = clicked.clone();
        int free = getFreeSpaces(player, clone, inventory, start, end);

        int clickedAmount = clicked.getAmount();

        int left = 0;
        if (free >= clickedAmount) {
            addItems(player, clone, inventory, start, end);
            event.setCurrentItem(null);
        } else {
            left = clickedAmount - free;
            if (left > 0) {
                clone.setAmount(free);
                addItems(player, clone, inventory, start, end);

                if (setLeft) {
                    ItemStack clone2 = clicked.clone();
                    clone2.setAmount(left);
                    event.setCurrentItem(clone2);
                }
            }
        }
        updateInventory(player);
        return left;
    }

    public static int getCraftingAmount(Inventory inventory, Recipe recipe) {
        int amt = -1;

        List<Recipe> recipes = plugin.getServer().getRecipesFor(recipe.getResult());

        Iterator<Recipe> iter = recipes.iterator();
        while (iter.hasNext() && amt <= 0) {
            Recipe rec = iter.next();

            if (rec instanceof ShapedRecipe) {
                ShapedRecipe shaped = (ShapedRecipe) rec;
                Map<Character, ItemStack> itemMap = shaped.getIngredientMap();

                String[] shape = shaped.getShape();
                int width = shape.length;
                int height = shape[0].length();

                int max = width * height;
                amt = checkItemInInventory(inventory, itemMap.get('a'), amt);
                if (max >= 2) {
                    amt = checkItemInInventory(inventory, itemMap.get('b'), amt);
                }
                if (max >= 3) {
                    amt = checkItemInInventory(inventory, itemMap.get('c'), amt);
                }
                if (max >= 4) {
                    amt = checkItemInInventory(inventory, itemMap.get('d'), amt);
                }
                if (max >= 5) {
                    amt = checkItemInInventory(inventory, itemMap.get('e'), amt);
                }
                if (max >= 6) {
                    amt = checkItemInInventory(inventory, itemMap.get('f'), amt);
                }
                if (max >= 7) {
                    amt = checkItemInInventory(inventory, itemMap.get('g'), amt);
                }
                if (max >= 8) {
                    amt = checkItemInInventory(inventory, itemMap.get('h'), amt);
                }
                if (max == 9) {
                    amt = checkItemInInventory(inventory, itemMap.get('i'), amt);
                }
            } else if (rec instanceof ShapelessRecipe) {
                ShapelessRecipe shapeless = (ShapelessRecipe) rec;
                List<ItemStack> items = shapeless.getIngredientList();

                for (ItemStack i : items) {
                    amt = checkItemInInventory(inventory, i, amt);
                }

            // TODO: Figure out if we need to handle FurnaceRecipes or not
            } else {

            }
        }

        if (amt == -1) {
            amt = 0;
        }
        return amt;
    }

    private static int checkItemInInventory(Inventory inventory, ItemStack ing, int amt) {
        if (amt != 0) {
            if (ing != null) {
                int ingAmount = ing.getAmount();

                int holdingAmount = 0;

                Iterator<ItemStack> iter = inventory.iterator();

                while (iter.hasNext()) {
                    ItemStack item = iter.next();
                    if (item != null && ItemUtil.isSameItem(item, ing, true)) {
                        int temp = item.getAmount();

                        if (holdingAmount == 0 || holdingAmount > temp) {
                            holdingAmount = temp;
                        }
                    }
                }

                // TODO: re-evaluate if the double is necessary
                int craftAmount = (int) Math.floor(holdingAmount / (double) ingAmount);

                if (amt == -1 || amt > craftAmount) {
                    amt = craftAmount;
                }
            }
        }
        return amt;
    }

    public static void removeFromCrafting(final CraftingInventory inventory, final int removeAmount) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {

                Iterator<ItemStack> iter = inventory.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    ItemStack item = iter.next();

                    if (item != null) {
                        int itemAmount = item.getAmount();
                        if (itemAmount == removeAmount) {
                            inventory.setItem(i, null);
                        } else {
                            int newAmount = itemAmount - removeAmount;
                            item.setAmount(newAmount);
                            inventory.setItem(i, item);
                        }
                    }
                    i++;
                }
            }
        });
    }

    public static void updateInventory(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override public void run() {
                player.updateInventory();
            }
        });
    }

    public static int getInventoryMax(Player player, Inventory inventory, Material mat, short dur, int slot) {
        int maxAmount = SIItems.getItemMax(player, mat, dur);

        if (inventory.getType() == InventoryType.FURNACE && !Config.isFurnaceUsingStacks()) {
            int maxFurnaceSize = Config.getMaxFurnaceAmount();
            if (maxFurnaceSize > 64 && maxFurnaceSize <= 127) {
                maxAmount = maxFurnaceSize;
            } else {
                maxAmount = 64;
            }
        } else if (inventory.getType() == InventoryType.ENCHANTING) {
            maxAmount = 1;
        } else if ((inventory.getType() == InventoryType.PLAYER && slot >= 36 && slot < 40) || (inventory.getType() == InventoryType.CRAFTING && slot >= 5 && slot < 9)) {
            maxAmount = 1;
        } else if (inventory.getType() == InventoryType.MERCHANT && !Config.isMerchantUsingStacks()) {
            maxAmount = 64;
        } else if (!Config.isCraftingUsingStacks()) {
            if ((inventory.getType() == InventoryType.WORKBENCH && slot >= 1 && slot < 10) || (inventory.getType() == InventoryType.CRAFTING && slot >= 1 && slot < 5)) {
                maxAmount = 64;
            }
        } else if (inventory.getType() == InventoryType.BREWING && !Config.isBrewingUsingStacks()) {
            if (slot >= 0 && slot < 3) {
                maxAmount = 1;
            } else {
                maxAmount = 64;
            }
        }

        return maxAmount;
    }

    public static void splitStack(Player player, boolean toolCheck) {
        ItemStack holding = player.getItemInHand();
        int amount = holding.getAmount();

        if (amount > 1 && (!toolCheck || ItemUtil.isTool(holding.getType()))) {
            if (!Config.isVirtualItemsEnabled()) {
                ItemStack move = holding.clone();
                move.setAmount(amount - 1);
                InventoryUtil.addItems(player, move);
                holding.setAmount(1);
            }
        }
    }

    public static void replaceItem(final Player player, final int slot, final ItemStack stack) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                player.getInventory().setItem(slot, stack);
            }
        });
    }

    public static void swapInventory(Player player, ItemStack toMove, InventoryClickEvent event, int rawSlot, int startSlot) {
        // move from main inventory to hotbar
        if (rawSlot >= startSlot && rawSlot <= startSlot + 26) {
            InventoryUtil.moveItems(player, toMove, event, 0, 9, true);
        // move from hotbar to main inventory
        } else if (rawSlot >= startSlot + 27 && rawSlot <= startSlot + 35) {
            InventoryUtil.moveItems(player, toMove, event, 9, 36, true);
        }
    }

    public static ItemStack decrementStack(ItemStack stack) {
        ItemStack clone = stack.clone();
        int amount = stack.getAmount();
        if (amount > 1) {
            clone.setAmount(amount - 1);
        } else {
            clone = null;
        }

        return clone;
    }
}
