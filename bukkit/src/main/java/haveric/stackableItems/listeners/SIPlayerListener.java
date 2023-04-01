package haveric.stackableItems.listeners;

import java.util.Random;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class SIPlayerListener implements Listener {

    private StackableItems plugin;

    private String itemDisabledMessage;
    public SIPlayerListener(StackableItems si) {
        plugin = si;

        itemDisabledMessage = String.format("[%s] This item has been disabled.", plugin.getDescription().getName());
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void furnaceSmelt(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        Furnace furnace = (Furnace) block.getState();
        ItemStack result = furnace.getInventory().getResult();
        if (result != null) {
            int amt = result.getAmount() + 1;

            int maxFurnaceSize = Config.getMaxBlockAmount(furnace, result.getType());
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

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void craftItem(CraftItemEvent event) {
        ItemStack craftedItem = event.getCurrentItem();

        if (craftedItem != null) {
            Player player = (Player) event.getWhoClicked();
            Material type = craftedItem.getType();
            CraftingInventory inventory = event.getInventory();

            int maxItems = SIItems.getItemMax(player, type, craftedItem.getDurability(), inventory.getType());

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
                        int maxSlot = SIItems.getItemMax(player, temp.getType(), temp.getDurability(), inventory.getType());

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
                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int hotbarButton = event.getHotbarButton();
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
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                        } else {
                            ItemStack toAdd = result.clone();
                            toAdd.setAmount(maxItems - hotbarAmount);
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                            ItemStack rest = result.clone();
                            rest.setAmount(total - maxItems);
                            InventoryUtil.addItemsToPlayer(player, rest, "");
                        }
                    }
                } else if (event.isShiftClick()) {
                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, craftedItem);
                        ItemStack clone = craftedItem.clone();
                        // Avoid crafting when there is nothing being crafted
                        if (clone.getType() != Material.AIR) {
                            // custom repairing
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), null, "craft");
                            if (amtCanCraft == 0 && ItemUtil.isRepairable(type)) {
                                // TODO: handle custom repairing to allow stacking
                                // TODO: don't let people repair two fully repaired items.. that's just stupid
                            } else if (freeSpaces > actualCraft) {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < actualCraft) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, amtCanCraft);
                                    clone.setAmount(actualCraft);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                }
                            } else {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < freeSpaces) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, freeSpaces);
                                    clone.setAmount(freeSpaces);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                }
                            }
                        }
                    }
                } else if (event.isLeftClick() || event.isRightClick()) {
                    if (ItemUtil.isSameItem(result, cursor)) {
                        int total = cursorAmount + recipeAmount;

                        if (total > maxItems) {
                            event.setCancelled(true);
                        } else {
                            // Only handle stacks that are above normal stack amounts.
                            if (total > result.getMaxStackSize()) {
                                int numCanHold = maxItems - cursorAmount;

                                int craftTimes = numCanHold / recipeAmount;
                                int canCraft = InventoryUtil.getCraftingAmount(event.getInventory(), event.getRecipe());

                                int actualCraft = Math.min(craftTimes, canCraft);

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
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getInventory().getItemInMainHand();

        ItemStack clone = holding.clone();

        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getType());

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        EquipmentSlot hand = event.getHand();

        // Handle infinite fishing rods
        if (maxItems == SIItems.ITEM_INFINITE) {
            if (hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(clone);
            } else {
                player.getInventory().setItemInOffHand(clone);
            }
        } else {
            InventoryUtil.splitStackInHand(player, false, hand);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void shootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack clone = event.getBow().clone();

            int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getType());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite bows
            if (maxItems == SIItems.ITEM_INFINITE) {
                player.getInventory().setItemInMainHand(clone);
                InventoryUtil.updateInventory(player);
            } else {
                InventoryUtil.splitStackInMainHand(player, false);
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

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack holding = player.getInventory().getItemInMainHand();

            int maxItems = SIItems.getItemMax(player, holding.getType(), holding.getDurability(), player.getInventory().getType());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite weapons
            if (maxItems == SIItems.ITEM_INFINITE) {
                PlayerInventory inventory = player.getInventory();
                InventoryUtil.replaceItem(inventory, inventory.getHeldItemSlot(), holding.clone());
                InventoryUtil.updateInventory(player);
            } else {
                InventoryUtil.splitStackInMainHand(player, true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack holding;
        if (hand == EquipmentSlot.HAND) {
            holding = player.getInventory().getItemInMainHand();
        } else {
            holding = player.getInventory().getItemInOffHand();
        }

        int amount = holding.getAmount();
        if (amount > 1) {
            ItemStack toAdd = event.getItemStack();
            int maxItems = SIItems.getItemMax(player, toAdd.getType(), toAdd.getDurability(), player.getInventory().getType());

            // Let Vanilla handle filling buckets for default value
            if (maxItems != SIItems.ITEM_DEFAULT) {
                ItemStack clone = holding.clone();
                clone.setAmount(amount - 1);

                int slot;
                if (hand == EquipmentSlot.HAND) {
                    slot = player.getInventory().getHeldItemSlot();
                } else {
                    slot = 40;
                }
                InventoryUtil.replaceItem(player.getInventory(), slot, clone);
                InventoryUtil.addItemsToPlayer(player, toAdd, "");

                event.setCancelled(true);

                Block clickedBlock = event.getBlockClicked();

                Material bucketType = toAdd.getType();
                if (bucketType == Material.WATER_BUCKET) {
                    BlockData data = clickedBlock.getBlockData();
                    if (data instanceof Waterlogged) {
                        Waterlogged waterloggedData = (Waterlogged) data;
                        waterloggedData.setWaterlogged(false);
                        clickedBlock.setBlockData(waterloggedData);
                    } else {
                        clickedBlock.setType(Material.AIR);
                    }
                } else {
                    clickedBlock.setType(Material.AIR);
                }

                InventoryUtil.updateInventory(player);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void emptyBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack holding;
        if (hand == EquipmentSlot.HAND) {
            holding = player.getInventory().getItemInMainHand();
        } else {
            holding = player.getInventory().getItemInOffHand();
        }

        int amount = holding.getAmount();
        if (amount > 1) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            int slot;
            if (hand == EquipmentSlot.HAND) {
                slot = player.getInventory().getHeldItemSlot();
            } else {
                slot = 40;
            }

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            InventoryUtil.addItemsToPlayer(player, event.getItemStack(), "");
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void consumeItem(PlayerItemConsumeEvent event) {
        ItemStack consumedItem = event.getItem();
        int amt = consumedItem.getAmount();

        if (amt > 1) {
            Player player = event.getPlayer();
            Material type = consumedItem.getType();

            if (type == Material.MILK_BUCKET) {
                InventoryUtil.addItemsToPlayer(player, new ItemStack(Material.BUCKET), "");
            } else if (type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW || type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW) {
                ItemStack clone = consumedItem.clone();
                clone.setAmount(amt - 1);

                int heldSlot;
                if (event.getHand() == EquipmentSlot.HAND) {
                    heldSlot = player.getInventory().getHeldItemSlot();
                } else {
                    heldSlot = 40;
                }
                InventoryUtil.replaceItem(player.getInventory(), heldSlot, clone);
                InventoryUtil.addItemsToPlayer(player, new ItemStack(Material.BOWL), "");
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        // Right click air is cancelled for some reason, even when it succeeds
        if (action != Action.RIGHT_CLICK_AIR && (event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY)) {
            return;
        }

        ItemStack holding = event.getItem();
        Player player = event.getPlayer();
        boolean anyRightClick = action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR;

        if (holding != null) {
            if (anyRightClick && holding.getType() == Material.GLASS_BOTTLE) {
                Block targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);

                if (targetBlock != null && targetBlock.getType() == Material.WATER) {
                    ItemStack toAdd = new ItemStack(Material.POTION);
                    PotionMeta meta = (PotionMeta) toAdd.getItemMeta();
                    if (meta != null) {
                        meta.setBasePotionData(new PotionData(PotionType.WATER));
                        toAdd.setItemMeta(meta);
                    }

                    int maxItems = SIItems.getItemMax(player, toAdd.getType(), toAdd.getDurability(), player.getInventory().getType());

                    // Let Vanilla handle filling bottles for default value
                    if (maxItems != SIItems.ITEM_DEFAULT) {
                        int amount = holding.getAmount();
                        int slot = player.getInventory().getHeldItemSlot();

                        ItemStack clone = holding.clone();
                        clone.setAmount(amount - 1);

                        InventoryUtil.replaceItem(player.getInventory(), slot, clone);
                        InventoryUtil.addItemsToPlayer(player, toAdd, "");

                        event.setCancelled(true);

                        InventoryUtil.updateInventory(player);
                    }
                }
            } else if (action == Action.RIGHT_CLICK_BLOCK && holding.getType() == Material.FLINT_AND_STEEL && Config.isPreventWastedFASEnabled()) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {
                    Material placedType = clickedBlock.getRelative(event.getBlockFace()).getType();

                    switch (placedType) {
                        case WATER:
                        case LAVA:
                        case FIRE:
                            event.setUseItemInHand(Result.DENY);
                            event.setUseInteractedBlock(Result.DENY);
                            break;
                        default:
                            break;
                    }

                    InventoryUtil.updateInventory(player);
                }
            } else if (anyRightClick && ItemUtil.isEquippableViaSwap(holding.getType())) {
                int maxItems = SIItems.getItemMax(player, holding.getType(), holding.getDurability(), player.getInventory().getType());

                // Let Vanilla handle swapping armors
                if (maxItems != SIItems.ITEM_DEFAULT && holding.getAmount() > 1) {
                    event.setCancelled(true);
                }
            }

            InventoryUtil.splitStackInMainHand(player, true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerPicksUpItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();

        int maxItems = SIItems.getItemMax(player, stack.getType(), stack.getDurability(), player.getInventory().getType());

        // Don't touch default items
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, stack);

        if (freeSpaces == 0 || maxItems == 0) {
            event.setCancelled(true);
        } else {
            // We only want to override if moving more than a vanilla stack will hold
            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, stack, player.getInventory(), null, "pickup");

            if (defaultStack > -1 && (stack.getAmount() > defaultStack || stack.getAmount() > stack.getMaxStackSize())) {
                InventoryUtil.addItemsToPlayer(player, stack.clone(), "pickup");
                Random random = new Random();
                Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;
                player.playSound(item.getLocation(), pickupSound, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                item.remove();

                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getInventory().getItemInMainHand();

        ItemStack clone = holding.clone();
        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getType());
        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        // Handle unlimited shears
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.getInventory().setItemInMainHand(clone);
        } else {
            InventoryUtil.splitStackInMainHand(player, false);
        }
    }
}
