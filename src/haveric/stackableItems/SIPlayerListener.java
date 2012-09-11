package haveric.stackableItems;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SIPlayerListener implements Listener {

    private StackableItems plugin;

    private String itemDisabledMessage;
    public SIPlayerListener(StackableItems si) {
        plugin = si;

        itemDisabledMessage = String.format("[%s] This item has been disabled.", plugin.getDescription().getName());
    }

    @EventHandler
    public void furnaceSmelt(FurnaceSmeltEvent event) {
        if (event.isCancelled()) {
            return;
        }

        int amt = 0;

        Furnace furnace = (Furnace) event.getBlock().getState();
        ItemStack result = furnace.getInventory().getResult();
        if (result == null) {
            amt = 0;
        } else {
            amt = result.getAmount() + 1;
        }

        int maxFurnaceSize = Config.getMaxFurnaceAmount();
        if (maxFurnaceSize > 64 && maxFurnaceSize <= 127) {

            // going to be a full furnace
            if (amt == 64) {
                int furnaceAmt = Config.getFurnaceAmount(furnace);
                if (furnaceAmt == maxFurnaceSize - 1) {
                    result.setAmount(furnaceAmt);
                    Config.clearFurnace(furnace);
                // increment virtual count
                } else {
                    if (furnaceAmt == -1) {
                        furnaceAmt = 64;
                    } else {
                        furnaceAmt++;
                    }

                    Config.setFurnaceAmount(furnace, furnaceAmt);

                    result.setAmount(62);
                }
            }
        } else if (maxFurnaceSize < 64) {
            if (amt == maxFurnaceSize) {
                //event.setCancelled(true);
                // TODO: Can we somehow stop the furnace burning so we can keep the fuel?
            }
        }
    }
/*
    @EventHandler (priority = EventPriority.HIGHEST)
    public void commandPreprocess(PlayerCommandPreprocessEvent event){
        String msg[] = event.getMessage().split(" ");

        int amt = 1;
        int data = 0;
        if (msg[0].equalsIgnoreCase("/item") || msg[0].equalsIgnoreCase("/i")){
            event.setCancelled(true);
            event.getPlayer().sendMessage("No items for you.");
            if (msg.length == 2 || msg.length == 3){

                String item[] = msg[1].split(":");
                event.getPlayer().sendMessage("item: " + item[0]);
                if (item.length == 2){
                    event.getPlayer().sendMessage("data: " + item[1]);
                }

                // TODO: deal with enchantments
                //String enchants[] = msg[1].split("|");
                //if (enchants.length == 2){
                //    event.getPlayer().sendMessage("Enchants: " + enchants[1]);
                //}

                if (msg.length == 3){
                    amt = Integer.parseInt(msg[2]);
                }
            }
        }
    }
*/

    @EventHandler
    public void craftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack craftedItem = event.getCurrentItem();

        if (craftedItem != null) {
            Material type = craftedItem.getType();

            int maxItems = SIItems.getItemMax(player, type, craftedItem.getDurability());

            if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                int cursorAmount = event.getCursor().getAmount();
                int recipeAmount = event.getRecipe().getResult().getAmount();

                if (event.isShiftClick()) {
                    CraftingInventory inventory = event.getInventory();

                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int freeSpaces = InventoryUtil.getFreeSpaces(player, craftedItem);
                        ItemStack clone = craftedItem.clone();

                        // custom repairing
                        if (amtCanCraft == 0 && ItemUtil.isRepairable(type)) {
                            // TODO: handle custom repairing to allow stacking
                            // TODO: don't let people repair two fully repaired items.. that's just stupid
                        } else if (freeSpaces > actualCraft) {
                            event.setCancelled(true);

                            InventoryUtil.removeFromCrafting(inventory, amtCanCraft);
                            clone.setAmount(actualCraft);
                            InventoryUtil.addItems(player, clone);
                        } else {
                            event.setCancelled(true);

                            InventoryUtil.removeFromCrafting(inventory, freeSpaces);
                            clone.setAmount(freeSpaces);
                            InventoryUtil.addItems(player, clone);
                        }
                    }
                } else if (event.isLeftClick() || event.isRightClick()) {
                    if (cursorAmount + recipeAmount > maxItems) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerFish(PlayerFishEvent event) {
        InventoryUtil.splitStack(event.getPlayer(), false);
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event) {
        InventoryUtil.splitStack(event.getPlayer(), true);
    }

    @EventHandler
    public void shootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            InventoryUtil.splitStack(player, false);
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            InventoryUtil.splitStack((Player) event.getDamager(), true);
        }
    }

    @EventHandler
    public void fillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();

        ItemStack holding = player.getInventory().getItemInHand();

        int amount = holding.getAmount();

        int slot = player.getInventory().getHeldItemSlot();

        if (amount > 1) {
            ItemStack clone = event.getItemStack().clone();

            InventoryUtil.replaceItem(player, slot, clone);
            InventoryUtil.updateInventory(player);

            event.setCancelled(true);
            event.getBlockClicked().setType(Material.AIR);

            InventoryUtil.addItems(player, new ItemStack(Material.BUCKET, amount - 1));
        }
    }

    @EventHandler
    public void emptyBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        ItemStack holding = player.getInventory().getItemInHand();
        Material type = holding.getType();

        int slot = player.getInventory().getHeldItemSlot();

        int amount = holding.getAmount();
        if (amount > 1 && (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.MILK_BUCKET)) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            InventoryUtil.replaceItem(player, slot, clone);
            InventoryUtil.addItems(player, new ItemStack(Material.BUCKET, 1));
        }
    }

    @EventHandler
    public void eatFood(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        PlayerClickData clickData = SIPlayers.getPlayerData(player.getName());

        double foodLevel = event.getFoodLevel();
        //plugin.log.info("Food: " + foodLevel + ", Last: " + clickData.getLastFoodLevel());
        if (foodLevel > clickData.getLastFoodLevel()) {
            clickData.setLastFoodLevel(foodLevel);

            if (clickData.getAmount() > 1 && clickData.getType() == Material.MUSHROOM_SOUP) {
                PlayerInventory inventory = player.getInventory();
                ItemStack itemAtSlot = inventory.getItem(clickData.getSlot());
                if (itemAtSlot != null && itemAtSlot.getType() == Material.MUSHROOM_SOUP) {
                    InventoryUtil.replaceItem(player, clickData.getSlot(), new ItemStack(Material.MUSHROOM_SOUP, clickData.getAmount() - 1));
                    //plugin.log.info("Left: " + (clickData.getAmount() - 1));

                    InventoryUtil.addItems(player, new ItemStack(Material.BOWL, 1));
                }
            }
        }
    }

    @EventHandler
    public void playerClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack holding = event.getItem();
            if (holding != null) {
                Material holdingType = holding.getType();
                Player player = event.getPlayer();

                int amount = holding.getAmount();

                PlayerClickData clickData = new PlayerClickData(player.getInventory().getHeldItemSlot(), holdingType, amount);
                SIPlayers.setPlayerData(player.getName(), clickData);
            }
        }

        // only prevent this after checking for consumption
        if (event.isCancelled()) {
            return;
        }


        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PlayerClickData clickData = SIPlayers.getPlayerData(event.getPlayer().getName());
            clickData.setLastBlock(event.getClickedBlock().getType());
            clickData.setLastBlockLocation(event.getClickedBlock().getLocation());

            ItemStack holding = event.getItem();
            if (holding != null) {
                Material holdingType = holding.getType();
                Player player = event.getPlayer();

                int amount = holding.getAmount();

                if (amount > 1 && holdingType == Material.FLINT_AND_STEEL) {
                    ItemStack move = holding.clone();
                    move.setAmount(amount - 1);

                    holding.setAmount(1);
                    InventoryUtil.addItems(player, move);
                }
            }
        }
    }


    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.getInventory().setMaxStackSize(127);
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();


        SlotType slotType = event.getSlotType();

        Inventory top = event.getView().getTopInventory();
        InventoryType topType = top.getType();


        if (cursor != null && clicked != null && slotType == SlotType.RESULT && topType == InventoryType.FURNACE) {
            Player player = (Player) event.getWhoClicked();

            int cursorAmount = cursor.getAmount();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();
            int clickedAmount = clicked.getAmount();

            int maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());

            if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                int freeSpaces = InventoryUtil.getFreeSpaces(player, clicked);

                ItemStack clone = clicked.clone();

                int maxFurnaceSize = Config.getMaxFurnaceAmount();
                if (maxFurnaceSize > 64 && maxFurnaceSize <= 127) {

                    PlayerClickData clickData = SIPlayers.getPlayerData(player.getName());
                    Material lastClicked = clickData.getLastBlock();
                    if (lastClicked == Material.FURNACE || lastClicked == Material.BURNING_FURNACE) {
                        Location loc = clickData.getLastBlockLocation();

                        int amt = Config.getFurnaceAmount(loc);
                        if (amt > -1) {
                            clone.setAmount(amt);

                            event.setCurrentItem(null);
                            event.setCursor(clone);
                            event.setResult(Result.ALLOW);
                        }
                    }
                // normal amounts in the furnace
                } else {
                    if (event.isShiftClick()) {
                        if (freeSpaces > clickedAmount) {
                            event.setCancelled(true);

                            event.setCurrentItem(null);
                            InventoryUtil.addItems(player, clone);
                        } else {
                            event.setCancelled(true);

                            int newAmount = clickedAmount - freeSpaces;
                            clone.setAmount(newAmount);
                            event.setCurrentItem(clone);

                            ItemStack clone2 = clicked.clone();
                            clone2.setAmount(freeSpaces);
                            InventoryUtil.addItems(player, clone2);
                        }
                    } else if (event.isLeftClick() || event.isRightClick()) {
                        if (cursorAmount + clickedAmount > maxItems) {
                            if (maxItems > 0 && cursorAmount == 0) {
                                if (clickedAmount > maxItems) {
                                    event.setCancelled(true);

                                    clone.setAmount(clickedAmount - maxItems);
                                    event.setCurrentItem(clone);

                                    ItemStack clone2 = clicked.clone();
                                    clone2.setAmount(maxItems);
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

            int maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());

            int rawSlot = event.getRawSlot();

            // we want to ignore creative players (for now) TODO: handle creative players
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            if (topType == InventoryType.MERCHANT) {
                // TODO: technically a result slot, handle accordingly when bukkit fixes the issue
                if (rawSlot == 2) {
                    return;
                }
            // TODO: might be able to remove this (except maxstacksize?)
            } else if (topType == InventoryType.ENCHANTING) {
                top.setMaxStackSize(1);
                if (rawSlot == 0) {
                    if (!event.isShiftClick()) {
                        return;
                    }
                }
            // TODO: Handle brewing like furnaces, moving the correct items into the correct slots
            } else if (topType == InventoryType.BREWING) {
                if (rawSlot <= 2) {
                    if (!event.isShiftClick()) {
                        return;
                    }
                }
            }

            int slot = event.getSlot();

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;

            boolean virtualClicked = false;
            boolean virtualCursor = false;

            VirtualItemStack clickedStack = null, cursorStack = null;


            if (Config.isVirtualItemsEnabled()) {
                clickedStack = VirtualItemConfig.getVirtualItemStack(player, slot);
                if (!clickedStack.isEmpty()) {
                    virtualClicked = true;
                }

                cursorStack = VirtualItemConfig.getVirtualItemStack(player, -1);
                if (!cursorStack.isEmpty()) {
                    virtualCursor = true;
                }
            }
            //Inventory bot = event.getView().getBottomInventory();

            //InventoryType botType = event.getView().getBottomInventory().getType();

            if (event.isShiftClick()) {
                if (rawSlot < top.getContents().length) {
                    InventoryUtil.moveItems(player, clicked, event, 0, 36, true);
                } else {
                    if (topType == InventoryType.CRAFTING) {
                        if (ItemUtil.isArmor(clickedType)) {
                            ItemStack armorSlot = null;
                            int moveSlot = -1;

                            PlayerInventory inventory = player.getInventory();

                            if (ItemUtil.isHelmet(clickedType)) {
                                armorSlot = inventory.getHelmet();
                                moveSlot = 39;
                            } else if (ItemUtil.isChestplate(clickedType)) {
                                armorSlot = inventory.getChestplate();
                                moveSlot = 38;
                            } else if (ItemUtil.isLeggings(clickedType)) {
                                armorSlot = inventory.getLeggings();
                                moveSlot = 37;
                            } else if (ItemUtil.isBoots(clickedType)) {
                                armorSlot = inventory.getBoots();
                                moveSlot = 36;
                            }

                            if (armorSlot == null && moveSlot > -1) {
                                int left = InventoryUtil.moveItems(player, clicked, event, moveSlot, moveSlot + 1, false);
                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                            } else {
                                InventoryUtil.swapInventory(player, clicked, event, rawSlot, 1);
                            }
                        } else {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 9);
                        }
                    } else if (topType == InventoryType.BREWING) {
                        boolean isBrewingIngredient = ItemUtil.isBrewingIngredient(clickedType);
                        boolean isPotion = clickedType == Material.POTION;

                        boolean moved = false;
                        if (isBrewingIngredient) {
                            ItemStack brewingSlot = top.getItem(3);

                            if (brewingSlot == null || ItemUtil.isSameItem(brewingSlot, clicked)) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 3, 4, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                        } else if (isPotion) {
                            ItemStack potionSlot1 = top.getItem(0);
                            ItemStack potionSlot2 = top.getItem(1);
                            ItemStack potionSlot3 = top.getItem(2);

                            if (potionSlot1 == null) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                            if (potionSlot2 == null) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 1, 2, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                            if (potionSlot3 == null) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 2, 3, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }

                        }
                        if (!moved) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 4);
                        }
                    } else if (topType == InventoryType.CHEST || topType == InventoryType.DISPENSER || topType == InventoryType.ENDER_CHEST) {
                        InventoryUtil.moveItems(player, clicked, event, top, true);
                    } else if (topType == InventoryType.WORKBENCH) {
                        boolean canMove = false;
                        for (int i = 1; i < 10 && !canMove; i++) {
                            if (top.getItem(i) == null) {
                                canMove = true;
                            }
                        }

                        InventoryUtil.moveItems(player, clicked, event, top, 1, 10, true);

                        if (!canMove) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 10);
                        }
                    // TODO Improve merchant shift click handling (Based on current recipe)
                    } else if (topType == InventoryType.MERCHANT) {
                        InventoryUtil.swapInventory(player, clicked, event, rawSlot, 3);
                    } else if (topType == InventoryType.ENCHANTING) {
                        boolean isEnchantable = ItemUtil.isEnchantable(clickedType);

                        boolean moved = false;
                        if (isEnchantable) {
                            ItemStack enchantSlot = top.getItem(0);

                            if (enchantSlot == null) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                        }

                        if (!moved) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 1);
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
                                    int left = InventoryUtil.moveItems(player, clicked, event, top, 1, 2, false);
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
                                    int left = InventoryUtil.moveItems(player, clicked, event, top, 0, 1, false);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        burnableMoved = false;
                                    }
                                }
                            }

                        }
                        // normal item;
                        if ((!fuelMoved && !burnableMoved) || (!isFuel && !isBurnable)) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 3);
                        }
                    }
                }
            } else if (event.isLeftClick()) {
                if (cursorEmpty && !slotEmpty && clickedAmount <= clickedType.getMaxStackSize() && maxItems < clickedAmount) {
                    if (!virtualClicked) {
                        //player.sendMessage("Pick up stack with empty hand. Less than max.");
                        if (clickedAmount <= maxItems) {
                            event.setCursor(clicked.clone());
                            event.setCurrentItem(null);
                            event.setResult(Result.ALLOW);
                        } else {
                            ItemStack clone = clicked.clone();
                            clone.setAmount(maxItems);
                            event.setCursor(clone);

                            ItemStack clone2 = clicked.clone();
                            clone2.setAmount(clickedAmount - maxItems);
                            event.setCurrentItem(clone2);
                            event.setResult(Result.ALLOW);
                        }
                    }
                // Pick up a stack with an empty hand
                } else if (cursorEmpty && !slotEmpty && clickedAmount > clickedType.getMaxStackSize()) {
                    if (virtualClicked) {
                        //player.sendMessage("Pick up stack with empty hand. (Virtual item)");
                        VirtualItemConfig.setVirtualItemStack(player, slot, null);
                        // Set cursor to the clicked stack
                        VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
                    } else {
                        //player.sendMessage("Pick up stack with empty hand. Greater than max.");
                        if (clickedAmount <= maxItems) {
                            event.setCursor(clicked.clone());
                            event.setCurrentItem(null);
                            event.setResult(Result.ALLOW);
                        } else {
                            ItemStack clone = clicked.clone();
                            clone.setAmount(maxItems);
                            event.setCursor(clone);

                            ItemStack clone2 = clicked.clone();
                            clone2.setAmount(clickedAmount - maxItems);
                            event.setCurrentItem(clone2);
                            event.setResult(Result.ALLOW);
                            InventoryUtil.updateInventory(player);
                        }
                    }
                // Drop a stack into an empty slot
                } else if (!cursorEmpty && slotEmpty && cursorAmount > cursorType.getMaxStackSize()) {
                    if (virtualCursor) {
                        //player.sendMessage("Drop a stack into an empty slot (virtual cursor)");
                        VirtualItemConfig.setVirtualItemStack(player, -1, null);
                        // Set slot to the cursor stack
                        VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                        if (cursorAmount > 64) {
                            event.setCursor(null);
                            event.setCurrentItem(cursor.clone());
                            event.setResult(Result.ALLOW);
                        }
                    } else {
                        //player.sendMessage("Drop a stack into an empty slot");
                        if (cursorAmount <= maxItems) {
                            event.setCurrentItem(cursor.clone());
                            event.setCursor(null);
                            event.setResult(Result.ALLOW);
                            InventoryUtil.updateInventory(player);
                        // More items than can fit in this slot
                        } else {
                            ItemStack toDrop = cursor.clone();
                            toDrop.setAmount(maxItems);
                            event.setCurrentItem(toDrop);

                            ItemStack toHold = cursor.clone();
                            toHold.setAmount(cursorAmount - maxItems);
                            event.setCursor(toHold);

                            event.setResult(Result.ALLOW);
                            InventoryUtil.updateInventory(player);
                        }
                    }
                // Combine two items
                } else if (!cursorEmpty && !slotEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    // Combine two virtual stacks
                    if (virtualCursor && virtualClicked) {
                        if (sameType) {
                            //player.sendMessage("Combine two virtual stacks");
                            while (clickedAmount < maxItems && cursorAmount > 0) {
                                clickedStack.addToFront(cursorStack.removeLast());
                                clickedAmount++;
                                cursorAmount--;
                            }
                            VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
                            if (cursorAmount > 0) {
                                VirtualItemConfig.setVirtualItemStack(player, -1, cursorStack);
                            } else {
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);
                            }
                        // swap stacks when not the same
                        } else {
                            //player.sendMessage("Swap two virtual stacks");
                            VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
                            VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                        }
                    // Add virtual stack to single item
                    } else if (virtualCursor) {
                        if (sameType) {
                            if (cursorAmount < maxItems) {
                                //player.sendMessage("Add virtual cursor to item");
                                cursorStack.addItemStack(clicked.clone());
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);
                                VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);

                                cursor.setAmount(clickedAmount + cursorAmount);
                                event.setCurrentItem(cursor.clone());
                                event.setCursor(null);

                                event.setResult(Result.ALLOW);
                            }
                        } else {
                            //player.sendMessage("Swap virtual cursor and item");
                            VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                            VirtualItemConfig.setVirtualItemStack(player, -1, null);
                        }
                    // Add cursor to virtual stack
                    } else if (virtualClicked) {
                        if (sameType) {
                            if (clickedAmount < maxItems) {
                                //player.sendMessage("Add cursor to virtual slot stack");
                                clickedStack.addToFront(cursor.clone());
                                VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);

                                event.setCursor(null);

                                cursor.setAmount(clickedAmount + cursorAmount);

                                event.setCurrentItem(cursor.clone());

                                event.setResult(Result.ALLOW);
                            }
                        } else {
                            //player.sendMessage("Swap cursor and virtual slot stack");
                            VirtualItemConfig.setVirtualItemStack(player, slot, null);
                            VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
                        }
                    // Add two normal items
                    } else {
                        if (sameType) {
                            if (ItemUtil.isSameItem(cursor, clicked)) {

                                int total = clickedAmount + cursorAmount;
                                if (total <= maxItems) {
                                    if (total > clicked.getMaxStackSize()) {
                                        //player.sendMessage("Combine two stacks fully");
                                        ItemStack s = new ItemStack(cursorType, total, cursorDur);
                                        s.addUnsafeEnchantments(cursor.getEnchantments());
                                        event.setCurrentItem(s);

                                        event.setCursor(null);
                                        event.setResult(Result.ALLOW);
                                    }
                                } else {
                                    //player.sendMessage("Combine two stacks partially");
                                    if (total - maxItems > maxItems) {
                                        event.setCancelled(true);
                                    } else {
                                        ItemStack s = new ItemStack(cursorType, maxItems, cursorDur);
                                        s.addUnsafeEnchantments(cursor.getEnchantments());
                                        event.setCurrentItem(s);

                                        ItemStack s2 = new ItemStack(cursorType, total - maxItems, cursorDur);
                                        s2.addUnsafeEnchantments(cursor.getEnchantments());
                                        event.setCursor(s2);

                                        event.setResult(Result.ALLOW);
                                        InventoryUtil.updateInventory(player);
                                    }
                                }

                            // Create a virtual stack out of two different items
                            } else if (Config.isVirtualItemsEnabled()) {
                                //player.sendMessage("Combine two items into a virtual stack.");
                                VirtualItemStack vis = new VirtualItemStack();
                                vis.addItemStack(cursor.clone());
                                vis.addItemStack(clicked.clone());
                                VirtualItemConfig.setVirtualItemStack(player, slot, vis);
                                event.setCursor(null);

                                cursor.setAmount(clickedAmount + cursorAmount);

                                event.setCurrentItem(cursor.clone());

                                event.setResult(Result.ALLOW);
                            // no virtual items so just swap them
                            } else {
                                //player.sendMessage("Swap two unstackable items");
                                event.setCurrentItem(cursor.clone());
                                event.setCursor(clicked.clone());

                                event.setResult(Result.ALLOW);
                            }
                        } else if (cursorAmount > 64) {
                            //player.sendMessage("Swap two items");

                            event.setCurrentItem(cursor.clone());
                            event.setCursor(clicked.clone());

                            event.setResult(Result.ALLOW);
                        }
                    }
                }
            } else if (event.isRightClick()) {
                if (!slotEmpty && !cursorEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    // Combine two virtual stacks
                    if (virtualCursor && virtualClicked) {
                        if (sameType) {
                            //player.sendMessage("RC:Combine two virtual stacks");
                            while (clickedAmount < maxItems && cursorAmount > 0) {
                                clickedStack.addToFront(cursorStack.removeLast());
                                clickedAmount++;
                                cursorAmount--;
                            }
                            VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
                            if (cursorAmount > 0) {
                                VirtualItemConfig.setVirtualItemStack(player, -1, cursorStack);
                            } else {
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);
                            }
                        // swap stacks when not the same
                        } else {
                            //player.sendMessage("RC:Swap two virtual stacks");
                            VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
                            VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                        }
                    // Add virtual stack to single item
                    } else if (virtualCursor) {
                        if (sameType) {
                            if (cursorAmount < maxItems) {
                                //player.sendMessage("RC:Add virtual cursor to item");
                                cursorStack.addItemStack(clicked.clone());
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);
                                VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);

                                cursor.setAmount(clickedAmount + cursorAmount);
                                event.setCurrentItem(cursor.clone());
                                event.setCursor(null);

                                event.setResult(Result.ALLOW);
                            }
                        } else {
                            //player.sendMessage("RC:Swap virtual cursor and item");
                            VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                            VirtualItemConfig.setVirtualItemStack(player, -1, null);
                        }
                    // Add cursor to virtual stack
                    } else if (virtualClicked) {
                        if (sameType) {
                            if (clickedAmount < maxItems) {
                                //player.sendMessage("RC:Add cursor to virtual slot stack");
                                clickedStack.addToFront(cursor.clone());
                                VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
                                VirtualItemConfig.setVirtualItemStack(player, -1, null);

                                event.setCursor(null);

                                cursor.setAmount(clickedAmount + cursorAmount);

                                event.setCurrentItem(cursor.clone());

                                event.setResult(Result.ALLOW);
                            }
                        } else {
                            //player.sendMessage("RC:Swap cursor and virtual slot stack");
                            VirtualItemConfig.setVirtualItemStack(player, slot, null);
                            VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
                        }
                    // Add two normal items
                    } else {
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
                                        event.setResult(Result.ALLOW);
                                    }
                                } else {
                                    event.setCancelled(true);
                                }

                            // Create a virtual stack out of two different items
                            } else if (Config.isVirtualItemsEnabled()) {
                                //player.sendMessage("RC:Combine two items into a virtual stack.");
                                VirtualItemStack vis = new VirtualItemStack();
                                vis.addItemStack(cursor.clone());
                                vis.addItemStack(clicked.clone());
                                VirtualItemConfig.setVirtualItemStack(player, slot, vis);
                                event.setCursor(null);

                                cursor.setAmount(clickedAmount + cursorAmount);

                                event.setCurrentItem(cursor.clone());

                                event.setResult(Result.ALLOW);
                            // no virtual items so just swap them
                            } else {
                                //player.sendMessage("RC:Swap two unstackable items");
                                event.setCurrentItem(cursor.clone());
                                event.setCursor(clicked.clone());

                                event.setResult(Result.ALLOW);
                            }
                        } else if (cursorAmount > 64) {
                            //player.sendMessage("RC:Swap two items");

                            event.setCurrentItem(cursor.clone());
                            event.setCursor(clicked.clone());

                            event.setResult(Result.ALLOW);
                        }
                    }
                //
                } else if (slotEmpty && !cursorEmpty) {
                    // Remove the last virtual itemstack
                    if (virtualCursor) {
                        ItemStack removed = cursorStack.removeLast();
                        event.setCurrentItem(removed);
                        cursor.setAmount(cursorAmount - removed.getAmount());
                        event.setCursor(cursor);

                        VirtualItemConfig.setVirtualItemStack(player, -1, cursorStack);

                        event.setResult(Result.ALLOW);
                    } else {

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
                            event.setCurrentItem(clone2);
                            event.setResult(Result.ALLOW);
                        } else {
                            clone.setAmount(maxItems);
                            event.setCursor(clone);

                            clone2.setAmount(clickedAmount - maxItems);
                            event.setCurrentItem(clone2);
                            event.setResult(Result.ALLOW);
                        }
                    }
                }
            //
            }
        // Throwing out a stack
        } else {
            // TODO: handle throwing out a virtual stack
        }
    }

    @EventHandler
    public void playerPicksUpItem(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();

        int freeSpaces = InventoryUtil.getFreeSpaces(player, stack);

        if (freeSpaces == 0) {
            event.setCancelled(true);
        } else {
            int maxItems = SIItems.getItemMax(event.getPlayer(), stack.getType(), stack.getDurability());
            if (maxItems == 0) {
                event.setCancelled(true);
            } else {
                InventoryUtil.addItems(player, stack);
                Random random = new Random();
                player.playSound(item.getLocation(), Sound.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                item.remove();

                event.setCancelled(true);
            }
        }
    }
}
