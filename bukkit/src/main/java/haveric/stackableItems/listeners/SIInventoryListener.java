package haveric.stackableItems.listeners;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.config.FurnaceXPConfig;
import haveric.stackableItems.util.FurnaceUtil;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.Map;

import static haveric.stackableItems.util.InventoryUtil.OFFHAND_RAW_SLOT_ID;

public class SIInventoryListener implements Listener {

    private StackableItems plugin;

    private String itemDisabledMessage;
    public SIInventoryListener(StackableItems si) {
        plugin = si;

        itemDisabledMessage = String.format("[%s] This item has been disabled.", plugin.getDescription().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        if (plugin.supportsInventoryStackSize) {
            try {
                event.getInventory().setMaxStackSize(SIItems.ITEM_NEW_MAX);
            } catch (AbstractMethodError e) {
                plugin.supportsInventoryStackSize = false;
            }
        }

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        InventoryType.SlotType slotType = event.getSlotType();

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        InventoryType topType = top.getType();

        InventoryAction action = event.getAction();
        // Ignore drop events
        if (action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ONE_CURSOR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ClickType clickType = event.getClick();
        if (clickType == ClickType.NUMBER_KEY && slotType != InventoryType.SlotType.RESULT) {
            int hotbarButton = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

            if (clicked != null) {
                Material clickedType = clicked.getType();
                short clickedDur = clicked.getDurability();
                int clickedAmount = clicked.getAmount();

                boolean clickedEmpty = clickedType == Material.AIR;

                int hotbarAmount = 0;
                if (hotbarItem != null) {
                    hotbarAmount = hotbarItem.getAmount();
                }

                if (!clickedEmpty && hotbarItem == null) { // Moving clicked to an empty hotbar slot
                    int maxItems = InventoryUtil.getInventoryMax(player, null, player.getInventory(), clickedType, clickedDur, hotbarButton);
                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                        event.setResult(Event.Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        event.setCurrentItem(null);

                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        InventoryUtil.addItemsToPlayer(player, clone2, "");

                        event.setResult(Event.Result.ALLOW);
                    } // else let vanilla handle it
                } else if (clickedEmpty && hotbarItem != null) { // Moving hotbar to an empty clicked slot
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, null, top, clickedType, clickedDur, rawSlot);
                    int inventorySize = top.getSize();

                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), rawSlot, rawSlot + 1, null, "");
                        } else {
                            InventoryUtil.addItems(player, clicked.clone(), top, rawSlot, rawSlot + 1, null, "");
                        }

                        event.setResult(Event.Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(maxItems);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clone2, player.getInventory(), rawSlot, rawSlot + 1, null, "");
                        } else {
                            InventoryUtil.addItems(player, clone2, top, rawSlot, rawSlot + 1, null, "");
                        }
                    } // else let vanilla handle it
                } else if (!clickedEmpty && hotbarItem != null) { // Move clicked to hotbar. Move hotbar elsewhere
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, null, player.getInventory(), clickedType, clickedDur, hotbarButton);
                    int inventorySize = top.getSize();
                    int totalItems = clickedAmount + hotbarAmount;

                    if (rawSlot < inventorySize) {
                        if (ItemUtil.isSameItem(hotbarItem, clicked)) {
                            if (totalItems <= maxItems && totalItems > clickedType.getMaxStackSize()) {
                                event.setCurrentItem(null);
                                InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                                event.setResult(Event.Result.DENY);
                            } else if (totalItems > maxItems) {
                                event.setCurrentItem(null);
                                int extra = totalItems - maxItems;
                                int toAdd = maxItems - hotbarAmount;
                                ItemStack clone = clicked.clone();
                                clone.setAmount(toAdd);
                                InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                                ItemStack clone2 = clicked.clone();
                                clone2.setAmount(extra);
                                InventoryUtil.addItemsToPlayer(player, clone2, "");
                                event.setResult(Event.Result.DENY);
                            } // Else vanilla can handle it.
                            // Different Items
                        } else {
                            event.setCurrentItem(null);

                            ItemStack cloneHotbar = hotbarItem.clone();
                            if (clickedAmount > maxItems) {
                                ItemStack clone = clicked.clone();
                                clone.setAmount(maxItems);
                                InventoryUtil.replaceItem(player.getInventory(), hotbarButton, clone);

                                ItemStack clone2 = clicked.clone();
                                clone2.setAmount(clickedAmount - maxItems);
                                InventoryUtil.addItemsToPlayer(player, clone2, "");
                            } else {
                                ItemStack cloneClicked = clicked.clone();
                                InventoryUtil.replaceItem(player.getInventory(), hotbarButton, cloneClicked);
                            }
                            InventoryUtil.addItemsToPlayer(player, cloneHotbar, "");

                            event.setResult(Event.Result.DENY);
                        }
                    } // Else let vanilla move items between player slots
                }
            }
        } else if (clickType == ClickType.SWAP_OFFHAND && slotType != InventoryType.SlotType.RESULT) {
            // Swap "clicked" item with offhand item when "F" is pressed
            boolean isOffhandSlot = top instanceof CraftingInventory && event.getRawSlot() == OFFHAND_RAW_SLOT_ID;
            if (isOffhandSlot) { // Click on the offhand slot, do nothing
                if (clicked != null) {
                    Material clickedType = clicked.getType();
                    short clickedDur = clicked.getDurability();

                    // Cancel custom stack sizes to prevent vanilla from unstacking them
                    int maxPlayerInventory = SIItems.getItemMax(player, clickedType, clickedDur, topType);
                    if (maxPlayerInventory != SIItems.ITEM_DEFAULT) {
                        event.setCancelled(true);
                    }
                }
            } else { // Swap items
                PlayerInventory playerInventory = player.getInventory();
                ItemStack offhandItem = playerInventory.getItemInOffHand();

                boolean clickedEmpty = clicked == null || clicked.getType() == Material.AIR;
                boolean offhandEmpty = offhandItem.getType() == Material.AIR;

                int clickedSlot = event.getSlot();
                Inventory clickedInventory = event.getClickedInventory();
                if (clickedInventory == null) {
                    return;
                }

                if (!clickedEmpty && !offhandEmpty) {
                    Material clickedType = clicked.getType();
                    short clickedDur = clicked.getDurability();
                    int clickedAmount = clicked.getAmount();

                    Material offhandType = offhandItem.getType();
                    short offhandDur = offhandItem.getDurability();
                    int offhandAmount = offhandItem.getAmount();

                    int maxClickedInventory = InventoryUtil.getInventoryMax(player, null, clickedInventory, offhandType, offhandDur, clickedSlot);
                    int maxOffhand = InventoryUtil.getInventoryMax(player, null, player.getInventory(), clickedType, clickedDur, OFFHAND_RAW_SLOT_ID);

                    if (offhandAmount <= maxClickedInventory && clickedAmount <= maxOffhand) {
                        if (offhandAmount > offhandType.getMaxStackSize() || clickedAmount > clickedType.getMaxStackSize()) {
                            ItemStack offhandClone = offhandItem.clone();
                            ItemStack clickedClone = clicked.clone();

                            clickedInventory.setItem(clickedSlot, offhandClone);
                            playerInventory.setItemInOffHand(clickedClone);
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                    }
                } else if (clickedEmpty && !offhandEmpty) {
                    Material offhandType = offhandItem.getType();
                    short offhandDur = offhandItem.getDurability();
                    int offhandAmount = offhandItem.getAmount();
                    int maxClickedInventory = InventoryUtil.getInventoryMax(player, null, clickedInventory, offhandType, offhandDur, clickedSlot);

                    if (offhandAmount <= maxClickedInventory && offhandAmount > offhandType.getMaxStackSize()) {
                        playerInventory.setItemInOffHand(null);
                        clickedInventory.setItem(clickedSlot, offhandItem.clone());

                        event.setCancelled(true);
                    } else if (offhandAmount > maxClickedInventory) {
                        ItemStack clone = offhandItem.clone();
                        clone.setAmount(maxClickedInventory);
                        clickedInventory.setItem(clickedSlot, clone);

                        ItemStack clone2 = offhandItem.clone();
                        clone2.setAmount(offhandAmount - maxClickedInventory);
                        playerInventory.setItemInOffHand(clone2);

                        event.setCancelled(true);
                    }
                } else if (!clickedEmpty) {
                    Material clickedType = clicked.getType();
                    short clickedDur = clicked.getDurability();
                    int clickedAmount = clicked.getAmount();
                    int maxOffhand = InventoryUtil.getInventoryMax(player, null, player.getInventory(), clickedType, clickedDur, OFFHAND_RAW_SLOT_ID);

                    if (clickedAmount <= maxOffhand && clickedAmount > clickedType.getMaxStackSize()) {
                        playerInventory.setItemInOffHand(clicked.clone());
                        clickedInventory.setItem(clickedSlot, null);

                        event.setCancelled(true);
                    } else if (clickedAmount > maxOffhand) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxOffhand);
                        playerInventory.setItemInOffHand(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxOffhand);
                        clickedInventory.setItem(clickedSlot, clone2);

                        event.setCancelled(true);
                    }
                }
            }
        } else if (cursor != null && clicked != null && slotType == InventoryType.SlotType.RESULT && top instanceof FurnaceInventory) {
            Material clickedType = clicked.getType();
            boolean clickedEmpty = clickedType == Material.AIR;

            // Only deal with items in the result slot.
            if (!clickedEmpty) {
                InventoryHolder inventoryHolder = event.getInventory().getHolder();

                if (inventoryHolder instanceof Furnace furnace) {
                    int cursorAmount = cursor.getAmount();
                    Material cursorType = cursor.getType();

                    short clickedDur = clicked.getDurability();
                    int clickedAmount = clicked.getAmount();

                    boolean cursorEmpty = cursorType == Material.AIR;

                    int maxItems = InventoryUtil.getInventoryMax(player, null, top, clickedType, clickedDur, event.getRawSlot());

                    if (maxItems == 0) {
                        player.sendMessage(itemDisabledMessage);
                        event.setCancelled(true);
                    } else {
                        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, clicked);

                        ItemStack clone = clicked.clone();
                        ItemStack clone2 = clicked.clone();
                        int xpItems = 0;

                        int maxFurnaceSize = Config.getMaxBlockAmount(furnace, clickedType);
                        if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {
                            int amt = Config.getFurnaceAmount(furnace);
                            if (amt > -1) {
                                int maxPlayerInventory = SIItems.getItemMax(player, clickedType, clickedDur, topType);
                                // Don't touch default items
                                if (maxPlayerInventory == SIItems.ITEM_DEFAULT) {
                                    return;
                                }
                                if (maxPlayerInventory == SIItems.ITEM_INFINITE) {
                                    maxPlayerInventory = clickedType.getMaxStackSize();
                                }
                                if (event.isShiftClick()) {
                                    clone.setAmount(amt);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                    event.setCurrentItem(null);
                                    event.setResult(Event.Result.DENY);
                                    Config.clearFurnace(furnace);
                                    xpItems = amt;
                                } else if (cursorEmpty && event.isRightClick()) {
                                    // Give half of furnace amount to cursor
                                    int cursorHalf = (int) Math.round((amt + 0.5) / 2);
                                    if (cursorHalf > maxPlayerInventory) {
                                        cursorHalf = maxPlayerInventory;
                                    }
                                    int furnaceHalf = amt - cursorHalf;
                                    clone.setAmount(cursorHalf);
                                    event.setCursor(clone);

                                    clone2.setAmount(furnaceHalf);
                                    event.setCurrentItem(clone2);
                                    Config.clearFurnace(furnace);
                                    xpItems = cursorHalf;
                                } else if (event.isLeftClick() || event.isRightClick()) {
                                    // Any other click will stack on the cursor
                                    if (cursorEmpty || ItemUtil.isSameItem(clicked, cursor)) {
                                        int total = amt + cursorAmount;
                                        if (total <= maxPlayerInventory) {
                                            clone.setAmount(total);
                                            event.setCurrentItem(null);
                                            event.setCursor(clone);
                                            event.setResult(Event.Result.DENY);
                                            Config.clearFurnace(furnace);
                                            xpItems = amt;
                                        } else {
                                            int left = total - maxPlayerInventory;

                                            clone.setAmount(maxPlayerInventory);
                                            event.setCursor(clone);

                                            if (left < 64) {
                                                Config.clearFurnace(furnace);
                                                clone2.setAmount(left);
                                            } else {
                                                Config.setFurnaceAmount(furnace, left);
                                                clone2.setAmount(63);
                                            }
                                            event.setCurrentItem(clone2);

                                            event.setResult(Event.Result.DENY);
                                            xpItems = maxPlayerInventory - cursorAmount;
                                        }
                                    }
                                }
                                ItemStack xpClone = clicked.clone();
                                xpClone.setAmount(xpItems);
                                FurnaceXPConfig.giveFurnaceXP(player, xpClone);
                            }
                            InventoryUtil.updateInventory(player);
                            // normal amounts in the furnace
                        } else {
                            if (event.isShiftClick()) {
                                if (freeSpaces > clickedAmount) {
                                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), top, "");
                                    if (defaultStack > -1 && defaultStack < clone.getAmount()) {
                                        event.setCancelled(true);

                                        event.setCurrentItem(null);

                                        FurnaceXPConfig.giveFurnaceXP(player, clone);

                                        InventoryUtil.addItemsToPlayer(player, clone, "");
                                    }
                                } else {
                                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), top, "");
                                    if (defaultStack > -1 && defaultStack < clone2.getAmount()) {
                                        event.setCancelled(true);

                                        int newAmount = clickedAmount - freeSpaces;
                                        clone.setAmount(newAmount);
                                        event.setCurrentItem(clone);

                                        clone2.setAmount(freeSpaces);
                                        FurnaceXPConfig.giveFurnaceXP(player, clone2);

                                        InventoryUtil.addItemsToPlayer(player, clone2, "");
                                    }
                                }
                            } else if (event.isLeftClick() || event.isRightClick()) {
                                if (cursorAmount + clickedAmount > maxItems) {
                                    if (maxItems > 0 && cursorAmount == 0) {
                                        if (clickedAmount > maxItems) {
                                            event.setCancelled(true);

                                            clone.setAmount(clickedAmount - maxItems);
                                            event.setCurrentItem(clone);

                                            clone2.setAmount(maxItems);
                                            FurnaceXPConfig.giveFurnaceXP(player, clone2);

                                            event.setCursor(clone2);
                                        }
                                    } else {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // prevent clicks outside the inventory area or within result slots
        } else if (cursor != null && clicked != null && slotType != InventoryType.SlotType.RESULT) {
            Material cursorType = cursor.getType();
            short cursorDur = cursor.getDurability();
            int cursorAmount = cursor.getAmount();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();
            int clickedAmount = clicked.getAmount();

            int maxItems;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, null, top, cursorType, cursorDur, event.getRawSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, null, top, clickedType, clickedDur, event.getRawSlot());
            }

            int rawSlot = event.getRawSlot();

            // TODO: might be able to remove this (except maxstacksize?)
            if (topType == InventoryType.ENCHANTING) {
                if (rawSlot == 0) {
                    if (plugin.supportsInventoryStackSize) {
                        try {
                            top.setMaxStackSize(1);
                        } catch (AbstractMethodError e) {
                            plugin.supportsInventoryStackSize = false;
                        }
                    }
                    if (!event.isShiftClick()) {
                        return;
                    }
                } else if (rawSlot == 1) {
                    if (plugin.supportsInventoryStackSize) {
                        try {
                            top.setMaxStackSize(64);
                        } catch (AbstractMethodError e) {
                            plugin.supportsInventoryStackSize = false;
                        }
                    }
                }
            } else if (topType == InventoryType.BREWING) {
                if (rawSlot <= 2) {
                    if (!event.isShiftClick()) {
                        return;
                    }
                }
            }

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;


            // Creative Player Inventory is handled elsewhere
            if (player.getGameMode() == GameMode.CREATIVE && topType == InventoryType.PLAYER) {
                return;
            }

            if (clickType == ClickType.DOUBLE_CLICK) {
                if (!cursorEmpty && slotEmpty && maxItems != cursor.getMaxStackSize()) {
                    if (!InventoryUtil.canVanillaGatherItemsToCursor(player, top, cursor, maxItems)) {
                        event.setCancelled(true);
                        InventoryUtil.gatherItemsToCursor(player, top, cursor, maxItems);
                    }
                }
            } else if (event.isShiftClick()) {
                if (rawSlot < top.getSize()) {
                    // We only want to override if moving more than a vanilla stack will hold
                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, player.getInventory(), top, "");
                    if (defaultStack > -1 && clickedAmount > defaultStack) {
                        InventoryUtil.moveItemsToPlayer(player, clicked.clone(), event, 0, 36, true, top);
                    }
                } else {
                    if (topType == InventoryType.CRAFTING) {
                        PlayerInventory inventory = player.getInventory();
                        if (ItemUtil.isArmor(clickedType)) {
                            ItemStack armorSlot = null;
                            boolean moved = false;

                            ItemStack cloneArmor = clicked.clone();
                            cloneArmor.setAmount(1);
                            if (ItemUtil.isHelmet(clickedType)) {
                                armorSlot = inventory.getHelmet();
                                if (armorSlot == null) {
                                    inventory.setHelmet(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isChestplate(clickedType)) {
                                armorSlot = inventory.getChestplate();
                                if (armorSlot == null) {
                                    inventory.setChestplate(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isLeggings(clickedType)) {
                                armorSlot = inventory.getLeggings();
                                if (armorSlot == null) {
                                    inventory.setLeggings(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isBoots(clickedType)) {
                                armorSlot = inventory.getBoots();
                                if (armorSlot == null) {
                                    inventory.setBoots(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isOffhand(clickedType)) {
                                armorSlot = inventory.getItemInOffHand();
                                if (armorSlot.getType() == Material.AIR) {
                                    inventory.setItemInOffHand(cloneArmor);
                                    moved = true;
                                }
                            }

                            if ((armorSlot == null || armorSlot.getType() == Material.AIR) && moved) {
                                event.setCurrentItem(InventoryUtil.decrementStack(clicked));
                                event.setCancelled(true);
                            } else {
                                InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 9);
                            }
                        } else {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 9);
                        }
                    } else if (topType == InventoryType.BREWING) {
                        // TODO Prevent stacks from going into potion slots when shift clicking
                        boolean isBrewingIngredient = ItemUtil.isBrewingIngredient(clickedType);
                        boolean isPotion = ItemUtil.isPotion(clickedType);

                        boolean moved = false;
                        if (isBrewingIngredient) {
                            ItemStack brewingSlot = top.getItem(3);

                            if (brewingSlot == null || ItemUtil.isSameItem(brewingSlot, clicked)) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 3, 4, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                        } else if (isPotion) {
                            ItemStack potionSlot1 = top.getItem(0);
                            ItemStack potionSlot2 = top.getItem(1);
                            ItemStack potionSlot3 = top.getItem(2);

                            boolean movedAll = false;
                            if (potionSlot1 == null) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot2 == null && !movedAll) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot3 == null && !movedAll) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 2, 3, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }

                        }
                        if (!moved) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 4);
                        }
                    } else if (top instanceof AbstractHorseInventory abstractHorseInventory) {
                        ItemStack clickedClone = clicked.clone();
                        clickedClone.setAmount(1);

                        ItemStack specialSlot = null;
                        boolean moved = false;
                        if (abstractHorseInventory instanceof HorseInventory horseInventory) {
                            // Slot 0: saddle
                            // Slot 1: armor

                            if (clickedType == Material.SADDLE) {
                                specialSlot = horseInventory.getSaddle();
                                if (specialSlot == null) {
                                    horseInventory.setSaddle(clickedClone);
                                    moved = true;
                                }
                            } else if (ItemUtil.isHorseArmor(clickedType)) {
                                specialSlot = horseInventory.getArmor();
                                if (specialSlot == null) {
                                    horseInventory.setArmor(clickedClone);
                                    moved = true;
                                }
                            }
                        } else if (abstractHorseInventory instanceof LlamaInventory llamaInventory) {
                            // Slot 1: Decor

                            if (ItemUtil.isLlamaCarpet(clickedType)) {
                                specialSlot = llamaInventory.getDecor();
                                if (specialSlot == null) {
                                    llamaInventory.setDecor(clickedClone);
                                    moved = true;
                                }
                            }
                        } else {
                            // Slot 0: saddle

                            if (clickedType == Material.SADDLE) {
                                specialSlot = abstractHorseInventory.getSaddle();
                                if (specialSlot == null) {
                                    abstractHorseInventory.setSaddle(clickedClone);
                                    moved = true;
                                }
                            }
                        }

                        if ((specialSlot == null || specialSlot.getType() == Material.AIR) && moved) {
                            event.setCurrentItem(InventoryUtil.decrementStack(clicked));
                            event.setCancelled(true);
                        } else {
                            if (top.getSize() <= 2) { // No chest
                                InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 2);
                            } else { // Has chest
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 2, top.getSize(), true);
                                if (left > 0) {
                                    clicked.setAmount(left);
                                    InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, top.getSize());
                                }
                            }
                        }
                    } else if (topType == InventoryType.CHEST || topType == InventoryType.DISPENSER || topType == InventoryType.ENDER_CHEST
                            || topType == InventoryType.HOPPER || topType == InventoryType.DROPPER || topType == InventoryType.BARREL) {
                        // We only want to override if moving more than a vanilla stack will hold
                        int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");

                        if (defaultStack > -1 && clickedAmount > defaultStack) {
                            InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "inventory");
                        }
                    } else if (topType == InventoryType.SHULKER_BOX) {
                        // Shulker boxes can't go inside other shulker boxes
                        if (!ItemUtil.isShulkerBox(clicked.getType())) {
                            // We only want to override if moving more than a vanilla stack will hold
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");

                            if (defaultStack > -1 && clickedAmount > defaultStack) {
                                InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "inventory");
                            }
                        }
                        // This adds shift clicking from the player inventory to the workbench.
                    } else if (topType == InventoryType.WORKBENCH) {
                        int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 10, false);
                        if (left > 0) {
                            clicked.setAmount(left);
                        }

                        if (left == clickedAmount) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 10);
                        }
                        // TODO Improve merchant shift click handling (Based on current recipe)
                    } else if (topType == InventoryType.MERCHANT) {
                        InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 3);
                    } else if (topType == InventoryType.BEACON) {
                        ItemStack beaconSlot = top.getItem(0);
                        if (ItemUtil.isBeaconFuel(clickedType) && beaconSlot == null) {
                            InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "");
                        } else {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 1);
                        }
                    } else if (topType == InventoryType.ANVIL) {
                        ItemStack renameSlot = top.getItem(0);
                        ItemStack repairSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (renameSlot == null || ItemUtil.isSameItem(clicked, renameSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (repairSlot == null || ItemUtil.isSameItem(clicked, repairSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.ENCHANTING) {
                        if (clickedType == Material.LAPIS_LAZULI) {
                            // Let vanilla handle stacking lapis for now.
                        } else if (ItemUtil.isEnchantable(clickedType) && top.getItem(0) == null) {
                            // We only want to override if moving more than a vanilla stack will hold
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");
                            if (defaultStack > -1 && clickedAmount > defaultStack) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                            }
                        } else {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, top.getSize());
                        }
                    } else if (topType == InventoryType.FURNACE || topType == InventoryType.BLAST_FURNACE || topType == InventoryType.SMOKER) {
                        boolean isFuel = FurnaceUtil.isFuel(clickedType);
                        boolean isBurnable;

                        if (topType == InventoryType.SMOKER) {
                            isBurnable = FurnaceUtil.isSmokerBurnable(clickedType);
                        } else if (topType == InventoryType.BLAST_FURNACE) {
                            isBurnable = FurnaceUtil.isBlastFurnaceBurnable(clickedType);
                        } else {
                            isBurnable = FurnaceUtil.isFurnaceBurnable(clickedType);
                        }


                        // Furnace slots:
                        // 0 - Burnable
                        // 1 - Fuel
                        // 2 - Result
                        ItemStack burnable = top.getItem(0);
                        ItemStack fuel = top.getItem(1);

                        boolean fuelMoved = false;
                        if (isFuel) {
                            if (rawSlot >= 3 && rawSlot <= 38) {
                                if (fuel == null || ItemUtil.isSameItem(fuel,  clicked)) {
                                    fuelMoved = true;
                                    int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        fuelMoved = false;
                                    }
                                }
                            }
                        }

                        boolean burnableMoved = false;
                        if (!fuelMoved && isBurnable) {
                            if (rawSlot >= 3 && rawSlot <= 38) {
                                if (burnable == null || ItemUtil.isSameItem(burnable, clicked)) {
                                    burnableMoved = true;
                                    int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        burnableMoved = false;
                                    }
                                }
                            }
                        }

                        // normal item;
                        if ((!fuelMoved && !burnableMoved) || (!isFuel && !isBurnable)) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.LOOM) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);
                        ItemStack thirdSlot = top.getItem(2);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (thirdSlot == null || ItemUtil.isSameItem(clicked, thirdSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 4);
                        }
                    } else if (topType == InventoryType.CARTOGRAPHY) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.GRINDSTONE) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.STONECUTTER) {
                        ItemStack firstSlot = top.getItem(0);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll) {
                            InventoryUtil.swapInventoryAndHotbar(player, clicked.clone(), event, rawSlot, 2);
                        }
                    }
                }
            } else if (event.isLeftClick()) {
                // Pick up a stack with an empty hand
                if (cursorEmpty && !slotEmpty) {
                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCursor(clicked.clone());
                        event.setCurrentItem(null);
                        event.setResult(Event.Result.DENY);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        event.setCursor(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone2);
                        event.setResult(Event.Result.DENY);
                        InventoryUtil.updateInventory(player);
                    }

                    // Drop a stack into an empty slot
                } else if (!cursorEmpty && slotEmpty) {
                    boolean isShulkerInShulker = topType == InventoryType.SHULKER_BOX && ItemUtil.isShulkerBox(cursor.getType());

                    // Ignore armor slots and attempts to nest shulker boxes when dropping items, let default Minecraft handle them.
                    if (event.getSlotType() != InventoryType.SlotType.ARMOR && !isShulkerInShulker) {
                        if (cursorAmount <= maxItems) {
                            event.setCurrentItem(cursor.clone());
                            event.setCursor(null);
                            event.setResult(Event.Result.DENY);

                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            } else {
                                InventoryUtil.updateInventory(player);
                            }
                            // More items than can fit in this slot
                        } else {
                            ItemStack toDrop = cursor.clone();
                            toDrop.setAmount(maxItems);
                            event.setCurrentItem(toDrop);

                            ItemStack toHold = cursor.clone();
                            toHold.setAmount(cursorAmount - maxItems);
                            event.setCursor(toHold);

                            event.setResult(Event.Result.DENY);
                            InventoryUtil.updateInventory(player);
                        }
                    }
                    // Combine two items
                } else if (!cursorEmpty && !slotEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    if (sameType) {
                        if (ItemUtil.isSameItem(cursor, clicked)) {
                            int total = clickedAmount + cursorAmount;

                            if (total <= maxItems) {
                                if (total > clicked.getMaxStackSize()) {
                                    //player.sendMessage("Combine two stacks fully");
                                    ItemStack clone = cursor.clone();
                                    clone.setAmount(total);
                                    event.setCurrentItem(clone);

                                    event.setCursor(null);
                                    event.setResult(Event.Result.DENY);

                                    // These inventories need a 2 tick update for RecipeManager
                                    if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                        InventoryUtil.updateInventoryLater(player, 2);
                                    }
                                }
                            } else {
                                //player.sendMessage("Combine two stacks partially");
                                ItemStack clone = cursor.clone();
                                clone.setAmount(maxItems);
                                event.setCurrentItem(clone);

                                ItemStack clone2 = cursor.clone();
                                clone2.setAmount(total - maxItems);
                                event.setCursor(clone2);

                                event.setResult(Event.Result.DENY);
                                // These inventories need a 2 tick update for RecipeManager
                                if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                    InventoryUtil.updateInventoryLater(player, 2);
                                }
                            }
                        } else {
                            // Swap two unstackable items
                            //player.sendMessage("Swap two unstackable items");
                            event.setCurrentItem(cursor.clone());
                            event.setCursor(clicked.clone());

                            event.setResult(Event.Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("Swap two items");
                        event.setCurrentItem(cursor.clone());
                        event.setCursor(clicked.clone());

                        event.setResult(Event.Result.DENY);
                        // These inventories need a 2 tick update for RecipeManager
                        if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                            InventoryUtil.updateInventoryLater(player, 2);
                        }
                    }
                }
            } else if (event.isRightClick()) {
                if (!slotEmpty && !cursorEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    // Add two normal items
                    if (sameType) {
                        if (ItemUtil.isSameItem(cursor, clicked)) {
                            // Let vanilla handle bundles
                            if (clickedType == Material.BUNDLE) {
                                return;
                            }

                            int total = clickedAmount + 1;
                            if (total <= maxItems) {
                                if (total > clicked.getMaxStackSize()) {
                                    //player.sendMessage("RC:Drop single item");

                                    ItemStack clone = cursor.clone();
                                    clone.setAmount(total);

                                    event.setCurrentItem(clone);
                                    if (cursorAmount == 1) {
                                        event.setCursor(null);
                                    } else {
                                        cursor.setAmount(cursorAmount - 1);
                                    }
                                    event.setResult(Event.Result.DENY);
                                    // These inventories need a 2 tick update for RecipeManager
                                    if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                        InventoryUtil.updateInventoryLater(player, 2);
                                    }
                                }
                            } else {
                                event.setCancelled(true);
                            }
                        } else {
                            // Swap two unstackable Items
                            //player.sendMessage("RC:Swap two unstackable items");
                            event.setCurrentItem(cursor.clone());
                            event.setCursor(clicked.clone());

                            event.setResult(Event.Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("RC:Swap two items");
                        event.setCurrentItem(cursor.clone());
                        event.setCursor(clicked.clone());

                        event.setResult(Event.Result.DENY);
                        // These inventories need a 2 tick update for RecipeManager
                        if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                            InventoryUtil.updateInventoryLater(player, 2);
                        }
                    }
                    // pick up half a stack
                } else if (!slotEmpty && cursorEmpty && maxItems > -1) {
                    if (clickedAmount > maxItems) {
                        int maxPickup = (int) Math.round((clickedAmount + 0.5) / 2);

                        ItemStack clone = clicked.clone();
                        ItemStack clone2 = clicked.clone();

                        if (maxPickup < maxItems) {
                            clone.setAmount(maxPickup);
                            event.setCursor(clone);
                            clone2.setAmount(clickedAmount - maxPickup);

                        } else {
                            clone.setAmount(maxItems);
                            event.setCursor(clone);
                            clone2.setAmount(clickedAmount - maxItems);
                        }
                        event.setCurrentItem(clone2);
                        event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryDrag(InventoryDragEvent event) {
        ItemStack cursor = event.getOldCursor();
        ItemStack newCursor = event.getCursor();

        Player player = (Player) event.getWhoClicked();

        int cursorAmount = 0;
        if (newCursor != null) {
            cursorAmount = newCursor.getAmount();
        }

        Material cursorType = cursor.getType();
        int defaultStackAmount = cursorType.getMaxStackSize();
        short cursorDur = cursor.getDurability();

        InventoryView view = event.getView();
        Inventory inventory = event.getInventory();

        Map<Integer, ItemStack> items = event.getNewItems();

        int inventorySize = inventory.getSize();

        boolean deny = false;
        int numStacksToSplit = 0;
        int numToSplit = cursorAmount;
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            numStacksToSplit++;
            int slot = entry.getKey();
            ItemStack added = entry.getValue();
            int newAmount = added.getAmount();

            int maxSlot = InventoryUtil.getInventoryMax(player, null, inventory, cursorType, cursorDur, slot);

            if (newAmount > maxSlot && maxSlot > SIItems.ITEM_DEFAULT) {
                int extra = newAmount - maxSlot;
                numToSplit += extra;
                numStacksToSplit--;
                deny = true;
            } else if (newAmount >= defaultStackAmount && newAmount < maxSlot) {
                deny = true;

                int oldAmount = 0;
                ItemStack oldStack;
                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                        actualPlayerSlot -= 36;
                    } else if (actualPlayerSlot == OFFHAND_RAW_SLOT_ID) { // Handle shield/offhand
                        actualPlayerSlot = 40;
                    }
                    oldStack = player.getInventory().getItem(actualPlayerSlot);
                } else {
                    oldStack = inventory.getItem(slot);
                }
                if (oldStack != null) {
                    oldAmount = oldStack.getAmount();
                }
                numToSplit += newAmount - oldAmount;
            } else if (newAmount < defaultStackAmount && defaultStackAmount < maxSlot) {
                numToSplit += newAmount;
            }
        }

        if (deny) {
            event.setResult(Event.Result.DENY);

            int toAdd = 0;
            if (numStacksToSplit > 0) {
                toAdd = numToSplit / numStacksToSplit;
            }
            int left = numToSplit - (toAdd * numStacksToSplit);

            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                int slot = entry.getKey();
                ItemStack added = entry.getValue();
                int newAmount = added.getAmount();

                int maxSlot = InventoryUtil.getInventoryMax(player, null, inventory, cursorType, cursorDur, slot);
                if (maxSlot <= SIItems.ITEM_DEFAULT) {
                    maxSlot = added.getMaxStackSize();
                }

                ItemStack clone = cursor.clone();


                int cloneAmount = 0;
                if (defaultStackAmount >= maxSlot) {
                    if (newAmount > maxSlot) {
                        cloneAmount = maxSlot;
                    } else if (newAmount <= maxSlot) {
                        newAmount += toAdd;
                        if (newAmount > maxSlot) {
                            left += newAmount - maxSlot;
                            newAmount = maxSlot;
                        }
                        cloneAmount = newAmount;
                    }
                } else {
                    int oldAmount = 0;
                    ItemStack oldStack;
                    if (slot >= inventorySize) {
                        int rawPlayerSlot = slot - inventorySize;
                        if (inventory.getType() == InventoryType.CRAFTING) {
                            rawPlayerSlot -= 4; // Handle armor slots
                        }
                        int actualPlayerSlot = rawPlayerSlot + 9;
                        // Offset for hotbar
                        if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                            actualPlayerSlot -= 36;
                        } else if (actualPlayerSlot == OFFHAND_RAW_SLOT_ID) { // Handle shield/offhand
                            actualPlayerSlot = 40;
                        }
                        oldStack = player.getInventory().getItem(actualPlayerSlot);
                    } else {
                        oldStack = inventory.getItem(slot);
                    }
                    if (oldStack != null) {
                        oldAmount = oldStack.getAmount();
                    }

                    cloneAmount = oldAmount + toAdd;
                    if (cloneAmount > maxSlot) {
                        left += cloneAmount - maxSlot;
                        cloneAmount = maxSlot;
                    }
                }

                clone.setAmount(cloneAmount);

                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                        actualPlayerSlot -= 36;
                    } else if (actualPlayerSlot == OFFHAND_RAW_SLOT_ID) { // Handle shield/offhand
                        actualPlayerSlot = 40;
                    }
                    InventoryUtil.replaceItem(player.getInventory(), actualPlayerSlot, clone);
                } else {
                    InventoryUtil.replaceItem(inventory, slot, clone);
                }
            }

            ItemStack cursorClone = cursor.clone();
            cursorClone.setAmount(left);
            InventoryUtil.updateCursor(player, cursorClone);
            InventoryUtil.updateInventory(player);
        }
    }

/*
    // TODO: Handle Creative inventory
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void creativeClick(InventoryCreativeEvent event) {
        Inventory inventory = event.getInventory();

        if (plugin.supportsInventoryStackSize) {
            try {
                inventory.setMaxStackSize(SIItems.ITEM_NEW_MAX);
            } catch (AbstractMethodError e) {
                plugin.supportsInventoryStackSize = false;
            }
        }

        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();


        if (cursor != null && clicked != null) {
            Player player = (Player) event.getWhoClicked();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();

            Material cursorType = cursor.getType();
            short cursorDur = cursor.getDurability();

            int maxItems = 0;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, inventory, cursorType, cursorDur, event.getSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, inventory, clickedType, clickedDur, event.getSlot());
            }
            plugin.log.info("Max items: " + maxItems);
            plugin.log.info("ClickType: " + event.getClick());
            plugin.log.info("Shift?: " + event.isShiftClick());

            SlotType slotType = event.getSlotType();
            plugin.log.info("SlotType: " + slotType);
            plugin.log.info("Inv size: " + inventory.getSize());
            int rawSlot = event.getRawSlot();

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;
        }
    }
*/
}
