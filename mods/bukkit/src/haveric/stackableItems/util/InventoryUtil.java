package haveric.stackableItems.util;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.api.SIAddItemEvent;
import haveric.stackableItems.api.SIDropExcessEvent;
import haveric.stackableItems.config.Config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

    private static int getInventoryFreeSpaces(String worldName, ItemStack itemToCheck, Inventory inventory) {
        int free = 0;

        Material type = itemToCheck.getType();
        short durability = itemToCheck.getDurability();

        Iterator<ItemStack> iter = inventory.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ItemStack slot = iter.next();

            int maxAmount = getInventoryMax(null, worldName, inventory, type, durability, i);

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

        return free;
    }

    public static int getPlayerFreeSpaces(Player player, ItemStack itemToCheck) {
        return getFreeSpaces(player, itemToCheck, player.getInventory(), 0, 36);
    }

    private static int getFreeSpaces(Player player, ItemStack itemToCheck, Inventory inventory, int start, int end) {
        int free = 0;

        if (start < end && end <= inventory.getSize()) {
            Material type = itemToCheck.getType();
            short durability = itemToCheck.getDurability();

            Iterator<ItemStack> iter = inventory.iterator(start);
            int i = start;
            while (iter.hasNext() && i < end) {
                ItemStack slot = iter.next();

                int maxAmount = getInventoryMax(player, null, inventory, type, durability, i);

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

    public static int getAmountDefaultCanMove(Player player, ItemStack itemToCheck, Inventory inventory, Inventory fromInventory, String extraType) {
        int free = 0;

        if (canVanillaStackCorrectly(itemToCheck, inventory)) {
            Material type = itemToCheck.getType();
            short durability = itemToCheck.getDurability();

            if (extraType.equals("inventory")) {
                free = checkAddInventoryTTB(player, inventory, itemToCheck);

                if (free == 0) {
                    free = checkEmptyInventoryTTB(player, inventory, type, durability);
                }
            } else {
                boolean hotbarFirst = true;
                boolean leftToRight = false;
                boolean topToBottom = false;

                if (extraType.equals("pickup") || extraType.equals("swap")) {
                    leftToRight = true;
                    topToBottom = true;
                } else {
                    if (fromInventory != null) {
                        InventoryType fromType = fromInventory.getType();
                        if (fromType == InventoryType.WORKBENCH || fromType == InventoryType.ANVIL || fromType == InventoryType.FURNACE || fromType == InventoryType.CRAFTING || fromType == InventoryType.MERCHANT) {
                            hotbarFirst = false;
                            leftToRight = true;
                            topToBottom = true;
                        }
                    }
                }

                if (hotbarFirst) {
                    if (leftToRight) {
                        free = checkAddHotbarLTR(player, inventory, itemToCheck);
                    } else {
                        free = checkAddHotbarRTL(player, inventory, itemToCheck);
                    }
                }

                if (free == 0) {
                    if (topToBottom) {
                        free = checkAddInventoryTTB(player, inventory, itemToCheck);
                    } else {
                        free = checkAddInventoryBTT(player, inventory, itemToCheck);
                    }
                }

                if (!hotbarFirst && free == 0) {
                    if (leftToRight) {
                        free = checkAddHotbarLTR(player, inventory, itemToCheck);
                    } else {
                        free = checkAddHotbarRTL(player, inventory, itemToCheck);
                    }
                }

                // Check for an empty slot
                if (hotbarFirst && free == 0) {
                    if (leftToRight) {
                        free = checkEmptyHotbarLTR(player, inventory, type, durability);
                    } else {
                        free = checkEmptyHotbarRTL(player, inventory, type, durability);
                    }
                }

                if (free == 0) {
                    if (topToBottom) {
                        free = checkEmptyInventoryTTB(player, inventory, type, durability);
                    } else {
                        free = checkEmptyInventoryBTT(player, inventory, type, durability);
                    }
                }

                if (!hotbarFirst && free == 0) {
                    if (leftToRight) {
                        free = checkEmptyHotbarLTR(player, inventory, type, durability);
                    } else {
                        free = checkEmptyHotbarRTL(player, inventory, type, durability);
                    }
                }
            }

            // Handle situations where vanilla won't be able to help us.
            if (free == -2) {
                free = 0;
            }
        }

        return free;
    }

    private static int getAmountDefaultHelper(Player player, Inventory inventory, ItemStack itemToCheck, ItemStack slot, int i) {
        Material type = itemToCheck.getType();

        int free = 0;

        if (ItemUtil.isSameItem(slot, itemToCheck)) {
            short durability = itemToCheck.getDurability();
            int defaultMax = type.getMaxStackSize();
            int amt = slot.getAmount();
            int slotMax = getInventoryMax(player, null, inventory, type, durability, i);

            if (slotMax == defaultMax) {
                // Let vanilla always handle this
                free = -1;
            } else if (slotMax > defaultMax) {
                if (amt == slotMax) {
                    // Continue, slot is full and vanilla should ignore this
                    free = 0;
                } else if (amt >= defaultMax && amt < slotMax) {
                    // Vanilla can't handle this
                    free = -2;
                } else { // amt < defaultMax
                    // Let Vanilla handle this
                    free = defaultMax - amt;
                }
            } else if (slotMax < defaultMax) { // slotMax < defaultMax
                if (amt < slotMax) {
                    // Vanilla can only add up to slotMax
                    free = slotMax - amt;
                } else {
                    // Don't let Vanilla handle this
                    free = -2;
                }
            }
        }

        return free;
    }

    private static int checkAddHotbarLTR(Player player, Inventory inventory, ItemStack itemToCheck) {
        int free = 0;

        int i = 0;
        while (i <= 8 && free == 0) {
            ItemStack slot = inventory.getItem(i);
            free = getAmountDefaultHelper(player, inventory, itemToCheck, slot, i);
            i++;
        }

        return free;
    }

    private static int checkAddHotbarRTL(Player player, Inventory inventory, ItemStack itemToCheck) {
        int free = 0;
        int i = 8;

        while (i >= 0 && free == 0) {
            ItemStack slot = inventory.getItem(i);
            free = getAmountDefaultHelper(player, inventory, itemToCheck, slot, i);
            i--;
        }

        return free;
    }

    private static int checkAddInventoryTTB(Player player, Inventory inventory, ItemStack itemToCheck) {
        int free = 0;
        int i = 9;
        int imax = 35;

        if (inventory.getType() != InventoryType.PLAYER) {
            i = 0;
            imax = inventory.getSize() - 1;
        }

        while (i <= imax && free == 0) {
            ItemStack slot = inventory.getItem(i);
            free = getAmountDefaultHelper(player, inventory, itemToCheck, slot, i);
            i++;
        }

        return free;
    }

    private static int checkAddInventoryBTT(Player player, Inventory inventory, ItemStack itemToCheck) {
        int free = 0;
        int i = 35;
        int imin = 9;

        if (inventory.getType() != InventoryType.PLAYER) {
            i = inventory.getSize() - 1;
            imin = 0;
        }

        while (i >= imin && free == 0) {
            ItemStack slot = inventory.getItem(i);
            free = getAmountDefaultHelper(player, inventory, itemToCheck, slot, i);
            i--;
        }

        return free;
    }

    private static int checkEmptyHotbarLTR(Player player, Inventory inventory, Material type, short durability) {
        int free = 0;
        int i = 0;

        while (i <= 8 && free == 0) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                int slotMax = getInventoryMax(player, null, inventory, type, durability, i);
                free = slotMax;
            }
            i++;
        }

        return free;
    }

    private static int checkEmptyHotbarRTL(Player player, Inventory inventory, Material type, short durability) {
        int free = 0;
        int i = 8;

        while (i >= 0 && free == 0) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                int slotMax = getInventoryMax(player, null, inventory, type, durability, i);
                free = slotMax;
            }
            i--;
        }

        return free;
    }

    private static int checkEmptyInventoryTTB(Player player, Inventory inventory, Material type, short durability) {
        int free = 0;
        int i = 9;
        int imax = 35;

        if (inventory.getType() != InventoryType.PLAYER) {
            i = 0;
            imax = inventory.getSize() - 1;
        }

        while (i <= imax && free == 0) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                int slotMax = getInventoryMax(player, null, inventory, type, durability, i);
                free = slotMax;
            }
            i++;
        }

        return free;
    }

    private static int checkEmptyInventoryBTT(Player player, Inventory inventory, Material type, short durability) {
        int free = 0;
        int i = 35;
        int imin = 9;

        if (inventory.getType() != InventoryType.PLAYER) {
            i = inventory.getSize() - 1;
            imin = 0;
        }

        while (i <= imin && free == 0) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                int slotMax = getInventoryMax(player, null, inventory, type, durability, i);
                free = slotMax;
            }
            i--;
        }

        return free;
    }

    private static boolean canVanillaStackCorrectly(ItemStack item, Inventory inventory) {
        boolean canStack = true;
        Material type = item.getType();
        short dur = item.getDurability();

        int typeMaxDur = type.getMaxDurability();
        // if picking up an item above vanilla durability, we don't want vanilla to handle it
        if (dur > typeMaxDur) {
            canStack = false;
        } else {
            // If any items in the inventory have durability above vanilla durability, we don't want vanilla to handle them.
            Iterator<ItemStack> iter = inventory.iterator();
            while (iter.hasNext() && canStack) {
                ItemStack slot = iter.next();
                if (slot != null && type == slot.getType() && slot.getDurability() > typeMaxDur) {
                    canStack = false;
                }
            }
        }

        return canStack;
    }

    public static void addItemsToPlayer(Player player, ItemStack itemToAdd, String extraType) {
        addItems(player, itemToAdd, player.getInventory(), 0, 36, null, extraType);
    }

    public static void addItems(final Player player, final ItemStack itemToAdd, final Inventory inventory, final int start, final int end, final Inventory fromInventory, final String extraType) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                if (start < end && end <= inventory.getSize()) {
                    int addAmount = itemToAdd.getAmount();
                    int initialAdd = addAmount;

                    if (extraType.equals("inventory")) {
                        addAmount = addInventoryTTB(player, inventory, itemToAdd, addAmount, start, end, true);

                        if (addAmount > 0) {
                            addAmount = addInventoryTTB(player, inventory, itemToAdd, addAmount, start, end, false);
                        }
                    } else {
                        boolean hotbarFirst = true;
                        boolean leftToRight = false;
                        boolean topToBottom = false;

                        if (extraType.equals("pickup") || extraType.equals("swap")) {
                            leftToRight = true;
                            topToBottom = true;
                        } else {
                            if (fromInventory != null) {
                                InventoryType fromType = fromInventory.getType();
                                if (fromType == InventoryType.WORKBENCH || fromType == InventoryType.ANVIL || fromType == InventoryType.FURNACE || fromType == InventoryType.CRAFTING || fromType == InventoryType.MERCHANT) {
                                    hotbarFirst = false;
                                    leftToRight = true;
                                    topToBottom = true;
                                }
                            }
                        }

                        //Add to existing stacks
                        if (hotbarFirst) {
                            if (leftToRight) {
                                addAmount = addHotbarLTR(player, inventory, itemToAdd, addAmount, start, end, true);
                            } else {
                                addAmount = addHotbarRTL(player, inventory, itemToAdd, addAmount, start, end, true);
                            }
                        }

                        if (addAmount > 0) {
                            if (topToBottom) {
                                addAmount = addInventoryTTB(player, inventory, itemToAdd, addAmount, start, end, true);
                            } else {
                                addAmount = addInventoryBTT(player, inventory, itemToAdd, addAmount, start, end, true);
                            }
                        }

                        if (!hotbarFirst && addAmount > 0) {
                            if (leftToRight) {
                                addAmount = addHotbarLTR(player, inventory, itemToAdd, addAmount, start, end, true);
                            } else {
                                addAmount = addHotbarRTL(player, inventory, itemToAdd, addAmount, start, end, true);
                            }
                        }

                        // Add to empty slots
                        if (hotbarFirst && addAmount > 0) {
                            if (leftToRight) {
                                addAmount = addHotbarLTR(player, inventory, itemToAdd, addAmount, start, end, false);
                            } else {
                                addAmount = addHotbarRTL(player, inventory, itemToAdd, addAmount, start, end, false);
                            }
                        }

                        if (addAmount > 0) {
                            if (topToBottom) {
                                addAmount = addInventoryTTB(player, inventory, itemToAdd, addAmount, start, end, false);
                            } else {
                                addAmount = addInventoryBTT(player, inventory, itemToAdd, addAmount, start, end, false);
                            }
                        }

                        if (!hotbarFirst && addAmount > 0) {
                            if (leftToRight) {
                                addAmount = addHotbarLTR(player, inventory, itemToAdd, addAmount, start, end, false);
                            } else {
                                addAmount = addHotbarRTL(player, inventory, itemToAdd, addAmount, start, end, false);
                            }
                        }
                    }

                    ItemStack itemClone = itemToAdd.clone();
                    itemClone.setAmount(initialAdd - addAmount);
                    SIAddItemEvent addEvent = new SIAddItemEvent(player, itemClone, inventory);
                    Bukkit.getServer().getPluginManager().callEvent(addEvent);

                    if (addAmount > 0) {
                        // For some reason it is becoming air at this point in certain situations.
                        if (itemToAdd.getType() != Material.AIR) {
                            ItemStack clone = itemToAdd.clone();
                            clone.setAmount(addAmount);
                            player.getWorld().dropItemNaturally(player.getLocation(), clone.clone());

                            SIDropExcessEvent dropEvent = new SIDropExcessEvent(player, clone.clone(), inventory);
                            Bukkit.getServer().getPluginManager().callEvent(dropEvent);
                        }
                    }
                }
            }
        });
    }

    private static int addHotbarLTR(Player player, Inventory inventory, ItemStack itemToAdd, int addAmount, int start, int end, boolean partial) {
        if (start <= 8) {
            if (end > 8) {
                end = 8;
            }

            Material type = itemToAdd.getType();
            short durability = itemToAdd.getDurability();

            int i = start;
            while (i <= end && addAmount > 0) {
                if (partial) {
                    addAmount = addPartialLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                } else {
                    addAmount = addEmptyLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                }
                i++;
            }
        }

        return addAmount;
    }

    private static int addHotbarRTL(Player player, Inventory inventory, ItemStack itemToAdd, int addAmount, int start, int end, boolean partial) {
        if (start <= 8) {
            if (end > 8) {
                end = 9;
            }
            end--;

            Material type = itemToAdd.getType();
            short durability = itemToAdd.getDurability();

            int i = end;
            while (i >= start && addAmount > 0) {
                if (partial) {
                    addAmount = addPartialLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                } else {
                    addAmount = addEmptyLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                }
                i--;
            }
        }

        return addAmount;
    }

    private static int addInventoryTTB(Player player, Inventory inventory, ItemStack itemToAdd, int addAmount, int start, int end, boolean partial) {
        boolean validAdd = false;

        if (inventory.getType() == InventoryType.PLAYER) {
            if (end > 9) {
                if (start < 9) {
                    start = 9;
                }
                end--;
                validAdd = true;
            }
        } else {
            if (end > 0) {
                if (start < 0) {
                    start = 0;
                }
                end--;
                validAdd = true;
            }
        }

        if (validAdd) {
            Material type = itemToAdd.getType();
            short durability = itemToAdd.getDurability();

            int i = start;
            while (i <= end && addAmount > 0) {
                if (partial) {
                    addAmount = addPartialLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                } else {
                    addAmount = addEmptyLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                }
                i++;
            }
        }

        return addAmount;
    }

    private static int addInventoryBTT(Player player, Inventory inventory, ItemStack itemToAdd, int addAmount, int start, int end, boolean partial) {
        boolean validAdd = false;

        if (inventory.getType() == InventoryType.PLAYER) {
            if (end > 9) {
                if (start < 9) {
                    start = 9;
                }
                end--;
                validAdd = true;
            }
        } else {
            if (end > 0) {
                if (start < 0) {
                    start = 0;
                }
                end--;
                validAdd = true;
            }
        }

        if (validAdd) {
            Material type = itemToAdd.getType();
            short durability = itemToAdd.getDurability();

            int i = end;
            while (i >= start && addAmount > 0) {
                if (partial) {
                    addAmount = addPartialLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                } else {
                    addAmount = addEmptyLoopHelper(player, inventory, itemToAdd, type, durability, addAmount, i);
                }
                i--;
            }
        }

        return addAmount;
    }

    private static int addPartialLoopHelper(Player player, Inventory inventory, ItemStack itemToAdd, Material type, short durability, int addAmount, int i) {
        ItemStack slot = inventory.getItem(i);

        if (slot != null && ItemUtil.isSameItem(slot, itemToAdd)) {
            int slotAmount = slot.getAmount();

            int maxAmount = getInventoryMax(player, null, inventory, type, durability, i);
            // Handle infinite items
            if (maxAmount == SIItems.ITEM_INFINITE) {
                maxAmount = type.getMaxStackSize();
            }

            int canAdd = maxAmount - slotAmount;
            if (canAdd > 0) {
                // Add less than a full slot
                if (addAmount <= canAdd) {
                    slot.setAmount(slotAmount + addAmount);
                    inventory.setItem(i, slot);
                    addAmount = 0;
                // Fill the slot and leave the rest
                } else {
                    slot.setAmount(maxAmount);
                    inventory.setItem(i, slot);
                    addAmount -= canAdd;
                }
            }
        }

        return addAmount;
    }

    private static int addEmptyLoopHelper(Player player, Inventory inventory, ItemStack itemToAdd, Material type, short durability, int addAmount, int i) {
        ItemStack slot = inventory.getItem(i);

        if (slot == null) {
            int maxAmount = getInventoryMax(player, null, inventory, type, durability, i);

            // Handle infinite items
            if (maxAmount == SIItems.ITEM_INFINITE) {
                maxAmount = type.getMaxStackSize();
            }
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

        return addAmount;
    }

    // This should not be called on a player inventory
    public static void addItems(final Location location, final ItemStack itemToAdd, final Inventory inventory, final int maxAmount, boolean delay) {
        if (delay) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override public void run() {
                    addItemsHelper(location, itemToAdd, inventory, maxAmount);
                }
            });
        } else {
            addItemsHelper(location, itemToAdd, inventory, maxAmount);
        }
    }

    private static void addItemsHelper(final Location location, final ItemStack itemToAdd, final Inventory inventory, final int maxAmount) {
        int addAmount = itemToAdd.getAmount();

        // Add to existing stacks
        Iterator<ItemStack> iter = inventory.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ItemStack slot = iter.next();

            if (slot != null && ItemUtil.isSameItem(slot, itemToAdd)) {
                int slotAmount = slot.getAmount();

                int canAdd = maxAmount - slotAmount;
                if (canAdd > 0) {
                    // Add less than a full slot
                    if (addAmount <= canAdd) {
                        slot.setAmount(slotAmount + addAmount);
                        inventory.setItem(i, slot);
                        addAmount = 0;
                    // Fill the slot and leave the rest
                    } else {
                        slot.setAmount(maxAmount);
                        inventory.setItem(i, slot);
                        addAmount -= canAdd;
                    }
                }
            }
            i++;
        }

        // Reset the iterator to start
        iter = inventory.iterator();
        i = 0;
        // Add to empty slots
        while (iter.hasNext()) {
            ItemStack slot = iter.next();

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
            i++;
        }

        if (addAmount > 0) {
            ItemStack clone = itemToAdd.clone();
            clone.setAmount(addAmount);

            location.getWorld().dropItemNaturally(location, clone);
        }
    }

    /*
    public static void moveItemsFromHopper(final Location location, final ItemStack stack, final Inventory fromInventory, final Inventory toInventory, final int max) {
        int freeSpaces = getInventoryFreeSpaces(location.getWorld().getName(), stack, toInventory);
        if (freeSpaces > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override public void run() {
                    int freeSpaces2 = getInventoryFreeSpaces(location.getWorld().getName(), stack, toInventory);
                    if (freeSpaces2 > 0) {
                        fromInventory.removeItem(stack);
                        addItems(location, stack, toInventory, max, false);
                    }
                }
            });
        }
    }
    */

    public static int moveItemsToPlayer(Player player, ItemStack clicked, InventoryClickEvent event, int start, int end, boolean setLeft, Inventory fromInventory) {
        String extraType = "";

        if (fromInventory == null) {
            extraType = "swap";
        }

        return moveItems(player, clicked, event, player.getInventory(), start, end, setLeft, fromInventory, extraType);
    }

    public static int moveItemsToFullInventory(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, boolean setLeft, String extraType) {
        return moveItems(player, clicked, event, inventory, 0, inventory.getSize(), setLeft, null, extraType);
    }

    public static int moveItemsToInventory(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, int start, int end, boolean setLeft) {
        return moveItems(player, clicked, event, inventory, start, end, setLeft, null, "inventory");
    }

    private static int moveItems(Player player, ItemStack clicked, InventoryClickEvent event, Inventory inventory, int start, int end, boolean setLeft, Inventory fromInventory, String extraType) {
        event.setCancelled(true);
        ItemStack clone = clicked.clone();
        int free = getFreeSpaces(player, clone, inventory, start, end);

        int clickedAmount = clicked.getAmount();

        int left = 0;
        if (free >= clickedAmount) {
            addItems(player, clone, inventory, start, end, fromInventory, extraType);
            event.setCurrentItem(null);
        } else {
            left = clickedAmount - free;
            if (left > 0) {
                clone.setAmount(free);
                addItems(player, clone, inventory, start, end, fromInventory, extraType);

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

                char c = 'a';
                amt = checkItemInInventory(inventory, itemMap.get(c), amt);

                /*
                 * Check 'b' though 'i' for max >= 2 to 9
                 */
                int testMax = 2;
                while (testMax < 10) {
                    c++;

                    if (max >= testMax) {
                        amt = checkItemInInventory(inventory, itemMap.get(c), amt);
                    } else {
                        break;
                    }

                    testMax++;
                }
            } else if (rec instanceof ShapelessRecipe) {
                ShapelessRecipe shapeless = (ShapelessRecipe) rec;
                List<ItemStack> items = shapeless.getIngredientList();

                for (ItemStack i : items) {
                    amt = checkItemInInventory(inventory, i, amt);
                }
            }
            // TODO: Figure out if we need to handle FurnaceRecipes or not
            /*
            else {

            }
            */
        }

        if (amt == -1) {
            amt = 0;
        }
        return amt;
    }

    private static int checkItemInInventory(Inventory inventory, ItemStack ing, int amt) {
        if (amt != 0 && ing != null) {
            int ingAmount = ing.getAmount();
            int holdingAmount = 0;

            Iterator<ItemStack> iter = inventory.iterator();
            // Don't check the first slot as it is the result slot.
            iter.next();
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
        return amt;
    }

    public static void removeFromCrafting(final Player player, final CraftingInventory inventory, final int removeAmount) {
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

                        Material itemType = item.getType();
                        // Give back buckets when used in a recipe
                        if (itemType == Material.MILK_BUCKET || itemType == Material.WATER_BUCKET || itemType == Material.LAVA_BUCKET) {
                            addItemsToPlayer(player, new ItemStack(Material.BUCKET, removeAmount), "");
                        // Give back bowls if mushroom soup is ever used in a recipe
                        } else if (itemType == Material.MUSHROOM_SOUP) {
                            addItemsToPlayer(player, new ItemStack(Material.BOWL, removeAmount), "");
                        }
                    }
                    i++;
                }
            }
        });
    }

    public static void updateCursor(final Player player, final ItemStack newCursor) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                // Sanity check to make sure the new item is different;
                ItemStack oldCursor = player.getItemOnCursor();

                if ((newCursor != null && oldCursor != null && newCursor.getAmount() != oldCursor.getAmount()) || !ItemUtil.isSameItem(newCursor, oldCursor)) {
                    player.setItemOnCursor(newCursor);
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

    public static void updateInventoryLater(final Player player, final int ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override public void run() {
                player.updateInventory();
            }
        }, ticks);
    }

    public static int getInventoryMax(Player player, String worldName, Inventory inventory, Material mat, short dur, int slot) {
        InventoryType inventoryType = inventory.getType();

        GameMode gamemode = null;
        int maxAmount = 0;
        if (player == null) {
            maxAmount = SIItems.getInventoryMax(worldName, mat, dur, inventory.getType());
        } else {
            maxAmount = SIItems.getItemMax(player, mat, dur, inventoryType);
            int maxPlayerAmount = SIItems.getItemMax(player, mat, dur, player.getInventory().getType());

            // Handle player section of inventory separately from the container above it.
            if (slot >= inventory.getSize()) {
                maxAmount = maxPlayerAmount;
            }

            gamemode = player.getGameMode();
        }

        String invName = inventory.getName();

        if (inventoryType == InventoryType.CHEST && (invName.equalsIgnoreCase("Horse") || invName.equalsIgnoreCase("Undead horse") || invName.equalsIgnoreCase("Skeleton horse"))) {
            if (slot < 2) {
                maxAmount = 1;
            }
        } else if (inventoryType == InventoryType.CHEST && (invName.equalsIgnoreCase("Donkey") || invName.equalsIgnoreCase("Mule"))) {
            if (slot == 0) {
                maxAmount = 1;
            } else if (slot == 1) {
                maxAmount = 0;
            }
        } else if (inventoryType == InventoryType.FURNACE && !SIItems.isInventoryEnabled(worldName, inventory)) {
            if (slot >= 0 && slot < 3) {
                maxAmount = Config.getMaxFurnaceAmount(mat);
            }
        } else if (inventoryType == InventoryType.ENCHANTING) {
            if (slot == 0) {
                maxAmount = 1;
            } else if (slot == 1) {
                if (!(mat == Material.INK_SACK && dur == 4)) {
                    maxAmount = 0;
                }
            }
        } else if ((inventoryType == InventoryType.PLAYER && slot >= 36 && slot < 40 && gamemode != GameMode.CREATIVE) || (inventoryType == InventoryType.CRAFTING && slot >= 5 && slot < 9)) {
            maxAmount = 1;
        } else if (inventoryType == InventoryType.MERCHANT && !SIItems.isInventoryEnabled(worldName, inventory)) {
            if (slot >= 0 && slot < 2) {
                maxAmount = mat.getMaxStackSize();
            }
        } else if (inventoryType == InventoryType.BREWING && !SIItems.isInventoryEnabled(worldName, inventory)) {
            if (slot >= 0 && slot < 3) {
                maxAmount = 1;
            }
        } else if (((inventoryType == InventoryType.WORKBENCH && slot >= 1 && slot < 10) || (inventoryType == InventoryType.CRAFTING && slot >= 1 && slot < 5)) && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        } else if (inventoryType == InventoryType.ANVIL && slot < 2 && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        } else if (inventoryType == InventoryType.BEACON && slot == 0 && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = 1;
        } else if (inventoryType == InventoryType.ENDER_CHEST && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        } else if (inventoryType == InventoryType.HOPPER && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        } else if (inventoryType == InventoryType.DROPPER && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        } else if (inventoryType == InventoryType.DISPENSER && !SIItems.isInventoryEnabled(worldName, inventory)) {
            maxAmount = mat.getMaxStackSize();
        }

        // Handle infinite and default items
        if (maxAmount == SIItems.ITEM_INFINITE || maxAmount == SIItems.ITEM_DEFAULT) {
            maxAmount = mat.getMaxStackSize();
        }

        // Prevent Item loss when bukkit doesn't handle the inventory's max stack size
        int inventoryMax = inventory.getMaxStackSize();
        if (inventoryType != InventoryType.PLAYER && maxAmount > inventoryMax && slot < inventory.getSize()) {
            if (Config.isDebugging()) {
                plugin.log.info("Bukkit isn't handling max stack size for: " + inventoryType);
                plugin.log.info("  Max: " + maxAmount + ", inventoryMax: " + inventoryMax);
            }
            maxAmount = inventoryMax;
        }

        return maxAmount;
    }

    public static void splitStack(Player player, boolean toolCheck) {
        ItemStack holding = player.getItemInHand();
        if (holding != null) {
            int amount = holding.getAmount();

            if (amount > 1 && (!toolCheck || ItemUtil.isTool(holding.getType()))) {
                ItemStack move = holding.clone();
                move.setAmount(amount - 1);
                addItemsToPlayer(player, move, "");
                holding.setAmount(1);
            }
        }
    }

    public static void replaceItem(final Inventory inventory, final int slot, final ItemStack stack) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                ItemStack slotItem = inventory.getItem(slot);

                // Sanity check to make sure the new item is different;
                if ((stack != null && slotItem != null && stack.getAmount() != slotItem.getAmount()) || !ItemUtil.isSameItem(stack, slotItem)) {
                    inventory.setItem(slot, stack);
                }
            }
        });
    }

    public static void swapInventory(Player player, ItemStack toMove, InventoryClickEvent event, int rawSlot, int startSlot) {
        // move from main inventory to hotbar
        if (rawSlot >= startSlot && rawSlot <= startSlot + 26) {
            InventoryUtil.moveItemsToPlayer(player, toMove, event, 0, 9, true, null);
        // move from hotbar to main inventory
        } else if (rawSlot >= startSlot + 27 && rawSlot <= startSlot + 35) {
            InventoryUtil.moveItemsToPlayer(player, toMove, event, 9, 36, true, null);
        }
    }

    // TODO: IMPLEMENT
    public static boolean canVanillaMoveHopper(Inventory inventory, ItemStack cursorStack) {
        boolean canMove = false;

        return canMove;
    }

    public static boolean canVanillaGatherItemsToCursor(Player player, Inventory topInventory, ItemStack cursorStack, int max) {
        boolean canGather = false;

        int cursorAmount = cursorStack.getAmount();
        int maxVanillaStack = cursorStack.getMaxStackSize();
        if (cursorAmount < maxVanillaStack && cursorAmount < max) {
            int canStack = 0;

            if (max < maxVanillaStack) {
                canStack = max - cursorAmount;
            } else {
                canStack = maxVanillaStack - cursorAmount;
            }

            Inventory playerInventory = player.getInventory();

            Iterator<ItemStack> iterTop = topInventory.iterator();
            while (iterTop.hasNext() && canStack > -1) {
                ItemStack stack = iterTop.next();
                if (ItemUtil.isSameItem(stack, cursorStack)) {
                    int stackAmount = stack.getAmount();
                    if (stackAmount > maxVanillaStack) {
                        canStack = -1;
                    } else {
                        canStack -= stackAmount;
                    }
                }
            }

            Iterator<ItemStack> iterBot = playerInventory.iterator();
            while (iterBot.hasNext() && canStack > -1) {
                ItemStack stack = iterBot.next();
                if (ItemUtil.isSameItem(stack, cursorStack)) {
                    int stackAmount = stack.getAmount();
                    if (stackAmount > maxVanillaStack) {
                        canStack = -1;
                    } else {
                        canStack -= stackAmount;
                    }
                }
            }

            if (canStack >= 0) {
                canGather = true;
            }
        }

        return canGather;
    }

    public static void gatherItemsToCursor(Player player, Inventory topInventory, ItemStack cursorStack, int max) {
        int cursorAmount = cursorStack.getAmount();

        int canStack = max - cursorAmount;

        Inventory playerInventory = player.getInventory();

        Iterator<ItemStack> iterTop = topInventory.iterator();
        int i = 0;
        while (iterTop.hasNext() && canStack > 0) {
            ItemStack stack = iterTop.next();
            if (ItemUtil.isSameItem(stack, cursorStack)) {
                int stackAmount = stack.getAmount();

                if (stackAmount >= canStack) {
                    int left = stackAmount - canStack;
                    canStack = 0;

                    if (left == 0) {
                        topInventory.setItem(i, null);
                    } else {
                       stack.setAmount(left);
                    }
                } else {
                    canStack -= stackAmount;
                    topInventory.setItem(i, null);
                }
            }
            i++;
        }

        i = 0;
        Iterator<ItemStack> iterBot = playerInventory.iterator();
        while (iterBot.hasNext() && canStack > 0) {
            ItemStack stack = iterBot.next();

            if (ItemUtil.isSameItem(stack, cursorStack)) {
                int stackAmount = stack.getAmount();

                if (stackAmount >= canStack) {
                    int left = stackAmount - canStack;
                    canStack = 0;

                    if (left == 0) {
                        playerInventory.setItem(i, null);
                    } else {
                       stack.setAmount(left);
                    }
                } else {
                    canStack -= stackAmount;
                    playerInventory.setItem(i, null);
                }
            }
            i++;
        }

        int newCursorAmount = max - canStack;
        if (newCursorAmount > 0 && newCursorAmount <= max) {
            cursorStack.setAmount(newCursorAmount);
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
