package haveric.stackableItems;

import haveric.stackableItems.vanish.Vanish;

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
import org.bukkit.event.player.PlayerShearEntityEvent;
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

    @EventHandler (priority = EventPriority.HIGHEST)
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void craftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack craftedItem = event.getCurrentItem();

        if (craftedItem != null) {
            Material type = craftedItem.getType();

            int maxItems = SIItems.getItemMax(player, type, craftedItem.getDurability(), false);
            // Handle infinite items for the crafted item
            if (maxItems == SIItems.ITEM_INFINITE) {
                maxItems = type.getMaxStackSize();

                CraftingInventory inventory = event.getInventory();

                // Handle infinite recipe items
                int inventSize = inventory.getSize();
                for (int i = 1; i < inventSize; i++) {
                    ItemStack temp = inventory.getItem(i);
                    if (temp != null) {
                        int maxSlot = SIItems.getItemMax(player, temp.getType(), temp.getDurability(), false);

                        if (maxSlot == SIItems.ITEM_INFINITE) {
                            ItemStack clone = temp.clone();
                            InventoryUtil.replaceItem(inventory, i, clone);
                        }
                    }
                }
            }

            if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                ItemStack cursor = event.getCursor();
                int cursorAmount = cursor.getAmount();
                ItemStack result = event.getRecipe().getResult();
                int recipeAmount = result.getAmount();

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

                            InventoryUtil.removeFromCrafting(player, inventory, amtCanCraft);
                            clone.setAmount(actualCraft);
                            InventoryUtil.addItems(player, clone);
                        } else {
                            event.setCancelled(true);

                            InventoryUtil.removeFromCrafting(player, inventory, freeSpaces);
                            clone.setAmount(freeSpaces);
                            InventoryUtil.addItems(player, clone);
                        }
                    }
                } else if (event.isLeftClick() || event.isRightClick()) {
                    if (cursorAmount + recipeAmount > maxItems) {
                        event.setCancelled(true);
                    } else {
                        // Only handle stacks that are above normal stack amounts.
                        if (cursorAmount + recipeAmount > result.getMaxStackSize()) {
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
                                cursorClone.setAmount(cursorAmount + recipeAmount);
                                event.setCursor(cursorClone);
                                event.setResult(Result.ALLOW);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerFish(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack clone = player.getItemInHand().clone();

        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), false);
        // Handle infinite fishing rods
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        } else {
            InventoryUtil.splitStack(player, false);
        }

    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void shootBow(EntityShootBowEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            ItemStack clone = event.getBow().clone();
            int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), false);

            // Handle infinite bows
            if (maxItems == SIItems.ITEM_INFINITE) {
                player.setItemInHand(clone);
                InventoryUtil.updateInventory(player);

                // TODO Handle infinite arrows
            } else {
                InventoryUtil.splitStack(player, false);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            ItemStack hold = player.getItemInHand();
            if (hold != null) {
                int maxItems = SIItems.getItemMax(player, hold.getType(), hold.getDurability(), false);

                // Handle infinite weapons
                if (maxItems == SIItems.ITEM_INFINITE) {
                    ItemStack clone = hold.clone();
                    PlayerInventory inventory = player.getInventory();
                    InventoryUtil.replaceItem(inventory, inventory.getHeldItemSlot(), clone);
                    InventoryUtil.updateInventory(player);
                } else {
                    InventoryUtil.splitStack(player, true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void fillBucket(PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack holding = player.getInventory().getItemInHand();

        int amount = holding.getAmount();

        int slot = player.getInventory().getHeldItemSlot();

        if (amount > 1) {
            ItemStack clone = event.getItemStack().clone();

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            InventoryUtil.updateInventory(player);

            event.setCancelled(true);
            event.getBlockClicked().setType(Material.AIR);

            InventoryUtil.addItems(player, new ItemStack(Material.BUCKET, amount - 1));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void emptyBucket(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack holding = player.getInventory().getItemInHand();
        Material type = holding.getType();

        int slot = player.getInventory().getHeldItemSlot();

        int amount = holding.getAmount();
        if (amount > 1 && (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.MILK_BUCKET)) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            InventoryUtil.addItems(player, new ItemStack(Material.BUCKET, 1));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void eatFood(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
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
                    InventoryUtil.replaceItem(player.getInventory(), clickData.getSlot(), new ItemStack(Material.MUSHROOM_SOUP, clickData.getAmount() - 1));
                    //plugin.log.info("Left: " + (clickData.getAmount() - 1));

                    InventoryUtil.addItems(player, new ItemStack(Material.BOWL, 1));
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
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
            Block block = event.getClickedBlock();

            ItemStack holding = event.getItem();
            if (holding != null) {
                if (holding.getType() == Material.FLINT_AND_STEEL) {
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
                        default:
                            break;
                    }
                }
            }
            Player player = event.getPlayer();
            PlayerClickData clickData = SIPlayers.getPlayerData(player.getName());

            Material blockType = block.getType();
            clickData.setLastBlock(blockType);
            clickData.setLastBlockLocation(block.getLocation());

            InventoryUtil.splitStack(player, true);
        }
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void inventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.getInventory().setMaxStackSize(SIItems.ITEM_NEW_MAX);

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

                int maxFurnaceSize = Config.getMaxFurnaceAmount(clickedType);
                if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {

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

            int maxItems = 0;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, top, cursorType, cursorDur, event.getRawSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, top, clickedType, clickedDur, event.getRawSlot());
            }

            int rawSlot = event.getRawSlot();

            // we want to ignore creative players (for now) TODO: handle creative players
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

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
            // TODO: Remove when Bukkit fixes anvil's result slot being a container
            } else if (topType == InventoryType.ANVIL) {
                if (rawSlot == 2) {
                    return;
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
                if (rawSlot < top.getSize()) {
                    InventoryUtil.moveItems(player, clicked, event, 0, 36, true);
                } else {
                    if (topType == InventoryType.CRAFTING) {
                        if (ItemUtil.isArmor(clickedType)) {
                            ItemStack armorSlot = null;
                            boolean moved = false;

                            PlayerInventory inventory = player.getInventory();

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
                                InventoryUtil.swapInventory(player, clicked, event, rawSlot, 9);
                            }

                        } else {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 9);
                        }
                    } else if (topType == InventoryType.BREWING) {
                        // TODO Prevent stacks from going into potion slots when shift clicking
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

                            boolean movedAll = false;
                            if (potionSlot1 == null) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot2 == null && !movedAll) {
                                int left = InventoryUtil.moveItems(player, clicked, event, top, 1, 2, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot3 == null && !movedAll) {
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
                        int left = InventoryUtil.moveItems(player, clicked, event, top, 1, 10, false);
                        if (left > 0) {
                            clicked.setAmount(left);
                        }


                        if (left == clickedAmount) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 10);
                        }
                    // TODO Improve merchant shift click handling (Based on current recipe)
                    } else if (topType == InventoryType.MERCHANT) {
                        InventoryUtil.swapInventory(player, clicked, event, rawSlot, 3);
                    } else if (topType == InventoryType.BEACON) {
                        ItemStack beaconSlot = top.getItem(0);
                        if (ItemUtil.isBeaconFuel(clickedType) && beaconSlot == null) {
                            InventoryUtil.moveItems(player, clicked, event, top, true);
                        } else {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 1);
                        }
                    } else if (topType == InventoryType.ANVIL) {
                        ItemStack renameSlot = top.getItem(0);
                        ItemStack repairSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (renameSlot == null || ItemUtil.isSameItem(clicked, renameSlot)) {
                            int left = InventoryUtil.moveItems(player, clicked, event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (repairSlot == null || ItemUtil.isSameItem(clicked, repairSlot))) {
                            int left = InventoryUtil.moveItems(player, clicked, event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked, event, rawSlot, 3);
                        }
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
                } else if (!cursorEmpty && slotEmpty) {
                    if (virtualCursor) {
                        //player.sendMessage("Drop a stack into an empty slot (virtual cursor)");
                        VirtualItemConfig.setVirtualItemStack(player, -1, null);
                        // Set slot to the cursor stack
                        VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
                        if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
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
                                        ItemStack clone = cursor.clone();
                                        clone.setAmount(total);
                                        event.setCurrentItem(clone);

                                        event.setCursor(null);
                                        event.setResult(Result.ALLOW);
                                    }
                                } else {
                                    //player.sendMessage("Combine two stacks partially");
                                    ItemStack clone = cursor.clone();
                                    clone.setAmount(maxItems);
                                    event.setCurrentItem(clone);

                                    ItemStack clone2 = cursor.clone();
                                    clone2.setAmount(total - maxItems);
                                    event.setCursor(clone2);

                                    event.setResult(Result.ALLOW);
                                    InventoryUtil.updateInventory(player);
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
                        } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
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
                        } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
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
                    }
                    /* TOOD: Finish handling Virtual stacks
                    else {

                    }
                    */
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
                        event.setResult(Result.ALLOW);
                    }
                }
            //
            }
        // Throwing out a stack
        }
        // TODO: handle throwing out a virtual stack
        /*
        else {

        }
        */
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerPicksUpItem(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();

        int freeSpaces = InventoryUtil.getFreeSpaces(player, stack);

        if (freeSpaces == 0 || Vanish.isPickupDisabled(player)) {
            event.setCancelled(true);
        } else {
            int maxItems = SIItems.getItemMax(event.getPlayer(), stack.getType(), stack.getDurability(), false);
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ItemStack clone = event.getItemInHand().clone();

        Player player = event.getPlayer();
        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), false);

        // Restore unlimited items
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerShearEntity(PlayerShearEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack clone = player.getItemInHand().clone();
        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), false);

        // Handle unlimited shears
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.setItemInHand(clone);
        } else {
            InventoryUtil.splitStack(player, false);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerIgniteBlock(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();

            ItemStack holding = player.getItemInHand();
            // Since repeatedly using flint and steel causes durability loss, reset durability on a new hit.
            ItemStack newStack = holding.clone();
            newStack.setDurability((short) 0);
            int maxItems = SIItems.getItemMax(player, newStack.getType(), newStack.getDurability(), false);

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
