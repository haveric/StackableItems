package haveric.stackableItems.listeners;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.config.FurnaceXPConfig;
import haveric.stackableItems.util.FurnaceUtil;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

import java.util.Map;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SIPlayerListener implements Listener {

    private StackableItems plugin;

    private String itemDisabledMessage;
    public SIPlayerListener(StackableItems si) {
        plugin = si;

        itemDisabledMessage = String.format("[%s] This item has been disabled.", plugin.getDescription().getName());
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void furnaceSmelt(FurnaceSmeltEvent event) {
        int amt = 0;

        Furnace furnace = (Furnace) event.getBlock().getState();
        ItemStack result = furnace.getInventory().getResult();
        if (result != null) {
            amt = result.getAmount() + 1;

            int maxFurnaceSize = Config.getMaxFurnaceAmount(result.getType());
            if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {

                // going to be a full furnace
                if (amt == SIItems.ITEM_DEFAULT_MAX) {
                    int furnaceAmt = Config.getFurnaceAmount(furnace);
                    if (furnaceAmt == maxFurnaceSize - 1) {
                        result.setAmount(furnaceAmt);
                        Config.clearFurnace(furnace);
                    // increment virtual count
                    } else {
                        if (furnaceAmt == -1) {
                            furnaceAmt = SIItems.ITEM_DEFAULT_MAX;
                        } else {
                            furnaceAmt++;
                        }

                        Config.setFurnaceAmount(furnace, furnaceAmt);

                        result.setAmount(62);
                    }
                }
            }
        }
        // TODO: Handle a max furnace amount of less than 64 items
        /*
        else if (maxFurnaceSize < SIItems.ITEM_DEFAULT_MAX) {
            if (amt == maxFurnaceSize) {
                //event.setCancelled(true);
                // TODO: Can we somehow stop the furnace burning so we can keep the fuel?
            }
        }
        */
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void craftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack craftedItem = event.getCurrentItem();

        if (craftedItem != null) {
            Material type = craftedItem.getType();

            CraftingInventory inventory = event.getInventory();

            int maxItems = SIItems.getItemMax(player, type, craftedItem.getDurability(), inventory.getName());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite items for the crafted item
            if (maxItems == SIItems.ITEM_INFINITE) {

                // Handle infinite recipe items
                int inventSize = inventory.getSize();
                for (int i = 1; i < inventSize; i++) {
                    ItemStack temp = inventory.getItem(i);
                    if (temp != null) {
                        int maxSlot = SIItems.getItemMax(player, temp.getType(), temp.getDurability(), inventory.getName());

                        if (maxSlot == SIItems.ITEM_INFINITE) {
                            ItemStack clone = temp.clone();
                            InventoryUtil.replaceItem(inventory, i, clone);
                        }
                    }
                }
            } else if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                ItemStack cursor = event.getCursor();
                int cursorAmount = cursor.getAmount();
                ItemStack result = event.getRecipe().getResult();
                int recipeAmount = result.getAmount();

                if (event.getClick() == ClickType.NUMBER_KEY) {
                    int hotbarButton = event.getHotbarButton();

                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                        int hotbarAmount = 0;
                        if (hotbarItem != null) {
                            hotbarAmount = hotbarItem.getAmount();
                        }
                        int total = hotbarAmount + recipeAmount;

                        event.setResult(Result.DENY);
                        InventoryUtil.removeFromCrafting(player, inventory, 1);
                        if (total <= maxItems) {
                            ItemStack toAdd = result.clone();
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, false, null);
                        } else {
                            ItemStack toAdd = result.clone();
                            toAdd.setAmount(maxItems - hotbarAmount);
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, false, null);

                            ItemStack rest = result.clone();
                            rest.setAmount(total - maxItems);
                            InventoryUtil.addItems(player, rest, false, null);
                        }
                    }
                } else if (event.isShiftClick()) {
                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int freeSpaces = InventoryUtil.getFreeSpaces(player, craftedItem);
                        ItemStack clone = craftedItem.clone();
                        // Avoid crafting when there is nothing being crafted
                        if (clone.getType() != Material.AIR) {
                            // custom repairing
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), null);

                            if (amtCanCraft == 0 && ItemUtil.isRepairable(type)) {
                                // TODO: handle custom repairing to allow stacking
                                // TODO: don't let people repair two fully repaired items.. that's just stupid
                            } else if (freeSpaces > actualCraft) {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < actualCraft) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, amtCanCraft);
                                    clone.setAmount(actualCraft);
                                    InventoryUtil.addItems(player, clone, true, null);
                                }
                            } else {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < freeSpaces) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, freeSpaces);
                                    clone.setAmount(freeSpaces);
                                    InventoryUtil.addItems(player, clone, true, null);
                                }
                            }
                        }
                    }
                } else if (event.isLeftClick() || event.isRightClick()) {
                    int total = cursorAmount + recipeAmount;
                    if (total > maxItems) {
                        event.setCancelled(true);
                    } else {
                        // Only handle stacks that are above normal stack amounts.
                        if (total > result.getMaxStackSize()) {
                            int numCanHold = maxItems - cursorAmount;
                            int craftTimes = numCanHold / recipeAmount;
                            int canCraft = InventoryUtil.getCraftingAmount(event.getInventory(), event.getRecipe());

                            int actualCraft = 0;
                            if (craftTimes <= canCraft) {
                                actualCraft = craftTimes;
                            } else {
                                actualCraft = canCraft;
                            }

                            if (actualCraft > 0) {
                                ItemStack cursorClone = cursor.clone();

                                // Remove one stack from the crafting grid
                                InventoryUtil.removeFromCrafting(player, event.getInventory(), 1);

                                // Add one set of items to the cursor
                                cursorClone.setAmount(total);
                                event.setCursor(cursorClone);
                                event.setResult(Result.DENY);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        ItemStack clone = player.getItemInHand().clone();

        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getName());

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        // Handle infinite fishing rods
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        } else {
            InventoryUtil.splitStack(player, false);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void shootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            ItemStack clone = event.getBow().clone();
            int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getName());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite bows
            if (maxItems == SIItems.ITEM_INFINITE) {
                player.setItemInHand(clone);
                InventoryUtil.updateInventory(player);
            } else {
                InventoryUtil.splitStack(player, false);
            }

            // TODO: Handle Infinite arrows
            //  Arrows shouldn't be able to be picked up... similar to how the Infinite enchantment works
            //  Perhaps setting the Infinite enchantment temporarily, although I don't like that option
            /*
            int maxArrows = SIItems.getItemMax(player, Material.ARROW, (short) 0, false);
            if (maxArrows == SIItems.ITEM_INFINITE) {
                InventoryUtil.addItems(player, new ItemStack(Material.ARROW));
                InventoryUtil.updateInventory(player);
            }
            */
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            ItemStack hold = player.getItemInHand();
            if (hold != null) {
                int maxItems = SIItems.getItemMax(player, hold.getType(), hold.getDurability(), player.getInventory().getName());

                // Don't touch default items.
                if (maxItems == SIItems.ITEM_DEFAULT) {
                    return;
                }

                // Handle infinite weapons
                if (maxItems == SIItems.ITEM_INFINITE) {
                    PlayerInventory inventory = player.getInventory();
                    InventoryUtil.replaceItem(inventory, inventory.getHeldItemSlot(), hold.clone());
                    InventoryUtil.updateInventory(player);
                } else {
                    InventoryUtil.splitStack(player, true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void fillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();

        ItemStack holding = player.getInventory().getItemInHand();

        int amount = holding.getAmount();

        int slot = player.getInventory().getHeldItemSlot();

        if (amount > 1) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            ItemStack toAdd = event.getItemStack();
            InventoryUtil.addItems(player, toAdd, false, null);

            event.setCancelled(true);

            Material bucketType = toAdd.getType();
            if (bucketType != Material.MILK_BUCKET) {
                event.getBlockClicked().setType(Material.AIR);
            }

            InventoryUtil.updateInventory(player);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void emptyBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        ItemStack holding = player.getInventory().getItemInHand();
        int amount = holding.getAmount();
        if (amount > 1) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            InventoryUtil.addItems(player, event.getItemStack(), false, null);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void consumeItem(PlayerItemConsumeEvent event) {
        ItemStack consumedItem = event.getItem();
        Material type = consumedItem.getType();

        int amt = consumedItem.getAmount();
        Player player = event.getPlayer();

        if (amt > 1) {
            if (type == Material.MILK_BUCKET) {
                InventoryUtil.addItems(player, new ItemStack(Material.BUCKET), false, null);
            } else if (type == Material.MUSHROOM_SOUP) {
                int heldSlot = player.getInventory().getHeldItemSlot();

                InventoryUtil.replaceItem(player.getInventory(), heldSlot, new ItemStack(Material.MUSHROOM_SOUP, amt - 1));
                InventoryUtil.addItems(player, new ItemStack(Material.BOWL), false, null);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            ItemStack holding = event.getItem();
            if (holding != null && holding.getType() == Material.FLINT_AND_STEEL) {
                Material placedType = block.getRelative(event.getBlockFace()).getType();

                switch(placedType) {
                    case STATIONARY_WATER:
                    case WATER:
                    case STATIONARY_LAVA:
                    case LAVA:
                    case FIRE:
                        event.setUseItemInHand(Result.DENY);
                        event.setUseInteractedBlock(Result.DENY);
                        break;
                }
            }

            InventoryUtil.splitStack(event.getPlayer(), true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
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

        Inventory inventory = event.getInventory();

        Map<Integer, ItemStack> items = event.getNewItems();

        int inventorySize = inventory.getSize();

        boolean deny = false;
        int numStacksToSplit = 0;
        int numToSplit = cursorAmount;
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            numStacksToSplit ++;
            int slot = entry.getKey();
            ItemStack added = entry.getValue();
            int newAmount = added.getAmount();

            int maxSlot = InventoryUtil.getInventoryMax(player, inventory, cursorType, cursorDur, slot);
            plugin.log.info("Max: " + maxSlot + ", New: " + newAmount);
            if (newAmount > maxSlot && maxSlot > SIItems.ITEM_DEFAULT) {
                int extra = newAmount - maxSlot;
                numToSplit += extra;
                numStacksToSplit--;
                deny = true;
            } else if (newAmount >= defaultStackAmount && newAmount < maxSlot) {
                deny = true;

                int oldAmount = 0;
                ItemStack oldStack = null;
                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36) {
                        actualPlayerSlot -= 36;
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
            event.setResult(Result.DENY);

            int toAdd = 0;
            if (numStacksToSplit > 0) {
                toAdd = (int) Math.floor(numToSplit / numStacksToSplit);
            }
            int left = numToSplit - (toAdd * numStacksToSplit);
            //plugin.log.info("Num To Split: " + numToSplit + ", Stacks to split: " + numStacksToSplit + ", Left: " + left);
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                int slot = entry.getKey();
                ItemStack added = entry.getValue();
                int newAmount = added.getAmount();

                int maxSlot = InventoryUtil.getInventoryMax(player, inventory, cursorType, cursorDur, slot);
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
                    ItemStack oldStack = null;
                    if (slot >= inventorySize) {
                        int rawPlayerSlot = slot - inventorySize;
                        if (inventory.getType() == InventoryType.CRAFTING) {
                            rawPlayerSlot -= 4; // Handle armor slots
                        }
                        int actualPlayerSlot = rawPlayerSlot + 9;
                        // Offset for hotbar
                        if (actualPlayerSlot >= 36) {
                            actualPlayerSlot -= 36;
                        }
                        oldStack = player.getInventory().getItem(actualPlayerSlot);
                    } else {
                        oldStack = inventory.getItem(slot);
                    }
                    if (oldStack != null) {
                        oldAmount = oldStack.getAmount();
                    }

                    //plugin.log.info("old: " + oldAmount + ", new: " + newAmount + ", ToAdd: " + toAdd + ", default: " + defaultStackAmount + ", max: " + maxSlot);

                    cloneAmount = oldAmount + toAdd;
                    if (cloneAmount > maxSlot) {
                        left += cloneAmount - maxSlot;
                        cloneAmount = maxSlot;
                    }
                    /*
                    if (newAmount + toAdd > defaultStackAmount) {
                        plugin.log.info("Full Stack");
                        newAmount += toAdd;
                        if (newAmount > maxSlot) {
                            left += newAmount - maxSlot;
                            newAmount = maxSlot;
                        }
                        cloneAmount = newAmount;
                    } else {
                        plugin.log.info("Replace");
                        cloneAmount = toAdd;
                    }
                    */
                }

                clone.setAmount(cloneAmount);
                //plugin.log.info("Clone: " + clone + ", Amt: " + cloneAmount);
                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36) {
                        actualPlayerSlot -= 36;
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
        inventory.setMaxStackSize(SIItems.ITEM_NEW_MAX);

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

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void inventoryClick(InventoryClickEvent event) {
        event.getInventory().setMaxStackSize(SIItems.ITEM_NEW_MAX);

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();


        SlotType slotType = event.getSlotType();

        Inventory top = event.getView().getTopInventory();
        InventoryType topType = top.getType();

        String topName = top.getName();
        // Let Vanilla handle the saddle and armor slots for horses
        if (event.getRawSlot() < 2 && topType == InventoryType.CHEST && (topName.equalsIgnoreCase("Horse") || topName.equalsIgnoreCase("Donkey") || topName.equalsIgnoreCase("Mule")
                                                  || topName.equalsIgnoreCase("Undead horse") || topName.equalsIgnoreCase("Skeleton horse"))) {
            return;
        }

        ClickType clickType = event.getClick();
        //plugin.log.info("Click: " + clickType);

        if (clickType == ClickType.NUMBER_KEY && slotType != SlotType.RESULT) {
            Player player = (Player) event.getWhoClicked();
            int hotbarButton = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

            if (clicked != null) {
                Material clickedType = clicked.getType();
                short clickedDur = clicked.getDurability();
                int clickedAmount = clicked.getAmount();
                //maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());

                boolean clickedEmpty = clickedType == Material.AIR;

                int hotbarAmount = 0;
                if (hotbarItem != null) {
                    hotbarAmount = hotbarItem.getAmount();
                    //maxHotbarItems = InventoryUtil.getInventoryMax(player, player.getInventory(), hotbarType, hotbarDur, hotbarButton);
                }

                // Moving clicked to an empty hotbar slot
                if (!clickedEmpty && hotbarItem == null) {
                    int maxItems = InventoryUtil.getInventoryMax(player, player.getInventory(), clickedType, clickedDur, hotbarButton);

                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, false, null);
                        event.setResult(Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        event.setCurrentItem(null);

                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, false, null);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        InventoryUtil.addItems(player, clone2, false, null);

                        event.setResult(Result.ALLOW);
                    } // else let vanilla handle it
                // Moving hotbar to an empty clicked slot
                } else if (clickedEmpty && hotbarItem != null) {
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, rawSlot);
                    int inventorySize = top.getSize();

                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), rawSlot, rawSlot + 1, false, null);
                        } else {
                            InventoryUtil.addItems(player, clicked.clone(), top, rawSlot, rawSlot + 1, false, null);
                        }

                        event.setResult(Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(maxItems);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clone2, player.getInventory(), rawSlot, rawSlot + 1, false, null);
                        } else {
                            InventoryUtil.addItems(player, clone2, top, rawSlot, rawSlot + 1, false, null);
                        }
                    } // else let vanilla handle it
                // Move clicked to hotbar. Move hotbar elsewhere
                } else if (!clickedEmpty && hotbarItem != null) {
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, player.getInventory(), clickedType, clickedDur, hotbarButton);
                    int inventorySize = top.getSize();
                    int totalItems = clickedAmount + hotbarAmount;

                    if (rawSlot < inventorySize) {
                        if (ItemUtil.isSameItem(hotbarItem, clicked)) {
                            if (totalItems <= maxItems && totalItems > clickedType.getMaxStackSize()) {
                                event.setCurrentItem(null);
                                InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, false, null);
                                event.setResult(Result.DENY);
                            } else if (totalItems > maxItems) {
                                event.setCurrentItem(null);
                                int extra = totalItems - maxItems;
                                int toAdd = maxItems - hotbarAmount;
                                ItemStack clone = clicked.clone();
                                clone.setAmount(toAdd);
                                InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, false, null);

                                ItemStack clone2 = clicked.clone();
                                clone2.setAmount(extra);
                                InventoryUtil.addItems(player, clone2, false, null);
                                event.setResult(Result.DENY);
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
                                InventoryUtil.addItems(player, clone2, false, null);
                            } else {
                                ItemStack cloneClicked = clicked.clone();
                                InventoryUtil.replaceItem(player.getInventory(), hotbarButton, cloneClicked);
                            }
                            InventoryUtil.addItems(player, cloneHotbar, false, null);

                            event.setResult(Result.DENY);
                        }
                    } // Else let vanilla move items between player slots
                }
            }

        } else if (cursor != null && clicked != null && slotType == SlotType.RESULT && topType == InventoryType.FURNACE) {
            Player player = (Player) event.getWhoClicked();

            int cursorAmount = cursor.getAmount();
            Material cursorType = cursor.getType();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();
            int clickedAmount = clicked.getAmount();

            boolean cursorEmpty = cursorType == Material.AIR;

            int maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());

            if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                int freeSpaces = InventoryUtil.getFreeSpaces(player, clicked);

                ItemStack clone = clicked.clone();
                ItemStack clone2 = clicked.clone();
                int xpItems = 0;

                int maxFurnaceSize = Config.getMaxFurnaceAmount(clickedType);
                if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {
                    InventoryHolder inventoryHolder = event.getInventory().getHolder();

                    if (inventoryHolder instanceof Furnace) {
                        Furnace furnace = (Furnace) inventoryHolder;

                        Location blockLocation = furnace.getBlock().getLocation();
                        int amt = Config.getFurnaceAmount(blockLocation);
                        if (amt > -1) {
                            int maxPlayerInventory = SIItems.getItemMax(player, clickedType, clickedDur, topType.name());
                            // Don't touch default items
                            if (maxPlayerInventory == SIItems.ITEM_DEFAULT) {
                                return;
                            }
                            if (maxPlayerInventory == SIItems.ITEM_INFINITE) {
                                maxPlayerInventory = clickedType.getMaxStackSize();
                            }
                            if (event.isShiftClick()) {
                                clone.setAmount(amt);
                                InventoryUtil.addItems(player, clone, true, null);
                                event.setCurrentItem(null);
                                event.setResult(Result.DENY);
                                Config.clearFurnace(blockLocation);
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
                                Config.clearFurnace(blockLocation);
                                xpItems = cursorHalf;
                            } else if (event.isLeftClick() || event.isRightClick()) {
                                // Any other click will stack on the cursor
                                if (cursorEmpty || ItemUtil.isSameItem(clicked, cursor)) {
                                    int total = amt + cursorAmount;
                                    if (total <= maxPlayerInventory) {
                                        clone.setAmount(total);
                                        event.setCurrentItem(null);
                                        event.setCursor(clone);
                                        event.setResult(Result.DENY);
                                        Config.clearFurnace(blockLocation);
                                        xpItems = amt;
                                    } else {
                                        int left = total - maxPlayerInventory;

                                        clone.setAmount(maxPlayerInventory);
                                        event.setCursor(clone);

                                        if (left < 64) {
                                            Config.clearFurnace(blockLocation);
                                            clone2.setAmount(left);
                                        } else {
                                            Config.setFurnaceAmount(blockLocation, left);
                                            clone2.setAmount(63);
                                        }
                                        event.setCurrentItem(clone2);

                                        event.setResult(Result.DENY);
                                        xpItems = maxPlayerInventory - cursorAmount;
                                    }
                                }
                            }
                            ItemStack xpClone = clicked.clone();
                            xpClone.setAmount(xpItems);
                            FurnaceXPConfig.giveFurnaceXP(player, xpClone);
                        }
                    }
                    InventoryUtil.updateInventory(player);
                // normal amounts in the furnace
                } else {
                    if (event.isShiftClick()) {
                        if (freeSpaces > clickedAmount) {
                            event.setCancelled(true);

                            event.setCurrentItem(null);

                            FurnaceXPConfig.giveFurnaceXP(player, clone);

                            InventoryUtil.addItems(player, clone, true, null);
                        } else {
                            event.setCancelled(true);

                            int newAmount = clickedAmount - freeSpaces;
                            clone.setAmount(newAmount);
                            event.setCurrentItem(clone);

                            clone2.setAmount(freeSpaces);
                            FurnaceXPConfig.giveFurnaceXP(player, clone2);

                            InventoryUtil.addItems(player, clone2, true, null);
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
        // prevent clicks outside the inventory area or within result slots
        } else if (cursor != null && clicked != null && slotType != SlotType.RESULT) {
            Player player = (Player) event.getWhoClicked();

            Material cursorType = cursor.getType();
            short cursorDur = cursor.getDurability();
            int cursorAmount = cursor.getAmount();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();
            int clickedAmount = clicked.getAmount();

            int maxItems = 0;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, top, cursorType, cursorDur, event.getRawSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());
            }

            int rawSlot = event.getRawSlot();

            // TODO: might be able to remove this (except maxstacksize?)
             if (topType == InventoryType.ENCHANTING) {
                top.setMaxStackSize(1);
                if (rawSlot == 0) {
                    if (!event.isShiftClick()) {
                        return;
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


            if (event.isShiftClick()) {
                if (rawSlot < top.getSize()) {
                    // We only want to override if moving more than a vanilla stack will hold
                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, player.getInventory(), top);
                    if (defaultStack > -1 && clickedAmount > defaultStack) {
                        InventoryUtil.moveItems(player, clicked.clone(), event, 0, 36, true, true, top);
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
                            }

                            if (armorSlot == null && moved) {
                                event.setCurrentItem(InventoryUtil.decrementStack(clicked));
                                event.setCancelled(true);
                            } else {
                                InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 9);
                            }
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 9);
                        }
                    } else if (topType == InventoryType.BREWING) {
                        // TODO Prevent stacks from going into potion slots when shift clicking
                        boolean isBrewingIngredient = ItemUtil.isBrewingIngredient(clickedType);
                        boolean isPotion = clickedType == Material.POTION;

                        boolean moved = false;
                        if (isBrewingIngredient) {
                            ItemStack brewingSlot = top.getItem(3);

                            if (brewingSlot == null || ItemUtil.isSameItem(brewingSlot, clicked)) {
                                int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 3, 4, false, false, null);

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
                                int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 0, 1, false, false, null);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot2 == null && !movedAll) {
                                int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 1, 2, false, false, null);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot3 == null && !movedAll) {
                                int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 2, 3, false, false, null);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }

                        }
                        if (!moved) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 4);
                        }
                    } else if (topType == InventoryType.CHEST && (topName.equalsIgnoreCase("Horse") || topName.equalsIgnoreCase("Donkey") || topName.equalsIgnoreCase("Mule")
                            || topName.equalsIgnoreCase("Undead horse") || topName.equalsIgnoreCase("Skeleton horse"))) {
                        // No chest
                        if (top.getSize() < 2) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 2);
                        // Has chest
                        } else {
                            int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 2, top.getSize(), true, false, null);

                            if (left > 0) {
                                clicked.setAmount(left);
                            }
                        }
                    } else if (topType == InventoryType.CHEST || topType == InventoryType.DISPENSER || topType == InventoryType.ENDER_CHEST
                            || topType == InventoryType.HOPPER || topType == InventoryType.DROPPER) {

                        // We only want to override if moving more than a vanilla stack will hold
                        int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null);
                        if (defaultStack > -1 && clickedAmount > defaultStack) {
                            InventoryUtil.moveItems(player, clicked.clone(), event, top, true, false, null);
                        }
                    // This adds shift clicking from the player inventory to the workbench.
                    } else if (topType == InventoryType.WORKBENCH) {
                        int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 1, 10, false, false, null);
                        if (left > 0) {
                            clicked.setAmount(left);
                        }

                        if (left == clickedAmount) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 10);
                        }
                    // TODO Improve merchant shift click handling (Based on current recipe)
                    } else if (topType == InventoryType.MERCHANT) {
                        InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                    } else if (topType == InventoryType.BEACON) {
                        ItemStack beaconSlot = top.getItem(0);
                        if (ItemUtil.isBeaconFuel(clickedType) && beaconSlot == null) {
                            InventoryUtil.moveItems(player, clicked.clone(), event, top, true, false, null);
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 1);
                        }
                    } else if (topType == InventoryType.ANVIL) {
                        ItemStack renameSlot = top.getItem(0);
                        ItemStack repairSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (renameSlot == null || ItemUtil.isSameItem(clicked, renameSlot)) {
                            int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 0, 1, false, false, null);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (repairSlot == null || ItemUtil.isSameItem(clicked, repairSlot))) {
                            int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 1, 2, false, false, null);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.ENCHANTING) {
                        if (ItemUtil.isEnchantable(clickedType) && top.getItem(0) == null) {
                            // We only want to override if moving more than a vanilla stack will hold
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null);
                            if (defaultStack > -1 && clickedAmount > defaultStack) {
                                int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 0, 1, false, false, null);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                            }
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 1);
                        }
                    } else if (topType == InventoryType.FURNACE) {
                        boolean isFuel = FurnaceUtil.isFuel(clickedType);
                        boolean isBurnable = FurnaceUtil.isBurnable(clickedType);

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
                                    int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 1, 2, false, false, null);
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
                                    int left = InventoryUtil.moveItems(player, clicked.clone(), event, top, 0, 1, false, false, null);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        burnableMoved = false;
                                    }
                                }
                            }

                        }
                        // normal item;
                        if ((!fuelMoved && !burnableMoved) || (!isFuel && !isBurnable)) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    }
                }
            } else if (event.isLeftClick()) {
                // Pick up a stack with an empty hand
                if (cursorEmpty && !slotEmpty) {
                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCursor(clicked.clone());
                        event.setCurrentItem(null);
                        event.setResult(Result.DENY);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        event.setCursor(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone2);
                        event.setResult(Result.DENY);
                        InventoryUtil.updateInventory(player);
                    }

                // Drop a stack into an empty slot
                } else if (!cursorEmpty && slotEmpty) {
                    // Ignore armor slots when dropping items, let default Minecraft handle them.
                    if (event.getSlotType() != SlotType.ARMOR) {
                        if (cursorAmount <= maxItems) {
                            event.setCurrentItem(cursor.clone());
                            event.setCursor(null);
                            event.setResult(Result.DENY);

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

                            event.setResult(Result.DENY);
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
                                    event.setResult(Result.DENY);

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

                                event.setResult(Result.DENY);
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

                            event.setResult(Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("Swap two items");
                        event.setCurrentItem(cursor.clone());
                        event.setCursor(clicked.clone());

                        event.setResult(Result.DENY);
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
                                    event.setResult(Result.DENY);
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

                            event.setResult(Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("RC:Swap two items");
                        event.setCurrentItem(cursor.clone());
                        event.setCursor(clicked.clone());

                        event.setResult(Result.DENY);
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
                        event.setResult(Result.DENY);
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerPicksUpItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();

        int freeSpaces = InventoryUtil.getFreeSpaces(player, stack);

        int maxItems = SIItems.getItemMax(event.getPlayer(), stack.getType(), stack.getDurability(), player.getInventory().getName());
        // Don't touch default items
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        if (freeSpaces == 0 || maxItems == 0) {
            event.setCancelled(true);
        } else {
            // We only want to override if moving more than a vanilla stack will hold
            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, stack, player.getInventory(), null);
            if (defaultStack > -1 && stack.getAmount() > defaultStack) {
                InventoryUtil.addItems(player, stack.clone(), false, null);
                Random random = new Random();
                player.playSound(item.getLocation(), Sound.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                item.remove();

                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        ItemStack clone = event.getItemInHand().clone();

        Player player = event.getPlayer();
        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getName());

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }
        // Restore unlimited items
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();

        ItemStack clone = player.getItemInHand().clone();
        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getName());
        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        // Handle unlimited shears
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        } else {
            InventoryUtil.splitStack(player, false);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerIgniteBlock(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            // Only deal with players.
            if (player != null) {
                ItemStack holding = player.getItemInHand();
                // Since repeatedly using flint and steel causes durability loss, reset durability on a new hit.
                ItemStack newStack = holding.clone();
                newStack.setDurability((short) 0);
                int maxItems = SIItems.getItemMax(player, newStack.getType(), newStack.getDurability(), player.getInventory().getName());

                // Don't touch default items.
                if (maxItems == SIItems.ITEM_DEFAULT) {
                    return;
                }
                // Handle unlimited flint and steel
                if (maxItems == SIItems.ITEM_INFINITE) {
                    player.setItemInHand(newStack);
                    InventoryUtil.updateInventory(player);
                } else {
                    InventoryUtil.splitStack(player, false);
                }
            }
        }
    }
}
