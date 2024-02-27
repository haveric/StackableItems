package haveric.stackableItems.listeners;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SIBlockListener implements Listener {
    private StackableItems plugin;

    public SIBlockListener(StackableItems si) {
        plugin = si;
    }
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        Player player = event.getPlayer();


        BlockState blockState = block.getState();
        // Handle breaking furnaces with larger stacks in them than normally allowed
        if (block instanceof Furnace furnace) {
            int maxAmount = Config.getFurnaceAmount(furnace);
            if (maxAmount > SIItems.ITEM_DEFAULT) {
                ItemStack result = furnace.getInventory().getResult();

                ItemStack toDrop = result.clone();
                toDrop.setAmount(maxAmount - 63);

                block.getWorld().dropItem(block.getLocation(), toDrop);

                Config.clearFurnace(furnace);
            }
        } else if (blockState instanceof ShulkerBox) {
            // Only need to override non-creative drops. Creative seems to handle overstacked items just fine
            if (player.getGameMode() != GameMode.CREATIVE) {
                ShulkerBox shulkerBox = (ShulkerBox) blockState;
                Inventory inventory = shulkerBox.getInventory();
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    ItemMeta meta = drop.getItemMeta();
                    if (meta instanceof BlockStateMeta blockMeta) {
                        BlockState itemBlockState = blockMeta.getBlockState();
                        if (itemBlockState instanceof ShulkerBox) {
                            boolean needsCustomDrop = false;
                            for (int i = 0; i < inventory.getSize(); i++) {
                                ItemStack originalItem = inventory.getItem(i);

                                if (originalItem != null) {
                                    if (originalItem.getAmount() > originalItem.getType().getMaxStackSize()) {
                                        needsCustomDrop = true;
                                        break;
                                    }
                                }
                            }

                            // Create a custom drop with the original stack values since vanilla doesn't like overstacked items
                            if (needsCustomDrop || inventory.isEmpty()) {
                                ItemStack newShulker = new ItemStack(block.getType());
                                BlockStateMeta newMeta = (BlockStateMeta) newShulker.getItemMeta();

                                if (newMeta != null) {
                                    event.setDropItems(false);

                                    if (!inventory.isEmpty()) {
                                        int[] itemCounts = new int[inventory.getSize()];
                                        List<ItemStack> nonEmptyStacks = new ArrayList<>();
                                        List<ItemStack> overstackedItems = new ArrayList<>();

                                        ShulkerBox newState = (ShulkerBox) newMeta.getBlockState();
                                        Inventory newInventory = newState.getInventory();
                                        for (int i = 0; i < inventory.getSize(); i++) {
                                            ItemStack originalItem = inventory.getItem(i);
                                            if (originalItem == null) {
                                                itemCounts[i] = 0;
                                            } else {
                                                itemCounts[i] = originalItem.getAmount();
                                                newInventory.setItem(i, originalItem.clone());

                                                if (originalItem.getAmount() > 64) {
                                                    overstackedItems.add(originalItem.clone());
                                                }

                                                nonEmptyStacks.add(originalItem.clone());
                                            }
                                        }

                                        newState.update();
                                        newMeta.setBlockState(newState);

                                        if (!overstackedItems.isEmpty()) {
                                            List<String> newLore = new ArrayList<>();
                                            newLore.add("(Contains SI overstacked items)");
                                            newMeta.setLore(newLore);
                                        }

                                        NamespacedKey key = new NamespacedKey(StackableItems.getPlugin(), "shulkerstackcounts");
                                        newMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, itemCounts);
                                    }

                                    if (meta.hasDisplayName()) {
                                        newMeta.setDisplayName(meta.getDisplayName());
                                    }
                                    newShulker.setItemMeta(newMeta);
                                    Location blockLocation = block.getLocation();

                                    Location dropLocation = new Location(blockLocation.getWorld(), blockLocation.getX() + .5, blockLocation.getY() + .5, blockLocation.getZ() + .5);
                                    block.getWorld().dropItem(dropLocation, newShulker);
                                }
                            }
                        }
                    }
                }
            }
        }

        ItemStack holding = player.getInventory().getItemInMainHand();

        // Handle splitting tool stacks when used to break blocks
        Material type = holding.getType();
        int maxItems = SIItems.getItemMax(player, type, holding.getDurability(), player.getInventory().getType());

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        if (maxItems == SIItems.ITEM_INFINITE) {
            ItemStack clone = holding.clone();
            PlayerInventory inventory = player.getInventory();
            InventoryUtil.replaceItem(inventory, inventory.getHeldItemSlot(), clone);
            InventoryUtil.updateInventory(player);
        } else {
            if (type == Material.SHEARS || type == Material.FLINT_AND_STEEL) {
                InventoryUtil.splitStackInMainHand(player, false);
            } else {
                InventoryUtil.splitStackInMainHand(player, true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();

        EquipmentSlot hand = event.getHand();
        ItemStack holding = event.getItemInHand();
        ItemStack clone = holding.clone();
        Player player = event.getPlayer();

        int maxItems = SIItems.getItemMax(player, clone.getType(), clone.getDurability(), player.getInventory().getType());
        if (ItemUtil.isShulkerBox(holding.getType())) {
            BlockStateMeta meta = (BlockStateMeta) holding.getItemMeta();
            if (meta != null) {
                NamespacedKey keyStackCounts = new NamespacedKey(StackableItems.getPlugin(), "shulkerstackcounts");

                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(keyStackCounts, PersistentDataType.INTEGER_ARRAY)) {
                    int[] itemCounts = container.get(keyStackCounts, PersistentDataType.INTEGER_ARRAY);
                    if (itemCounts != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                            Inventory shulkerInventory = shulkerBox.getInventory();
                            for (int i = 0; i < itemCounts.length; i++) {
                                int itemCount = itemCounts[i];
                                if (itemCount > 64) {
                                    ItemStack item = shulkerInventory.getItem(i);
                                    if (item != null) {
                                        item.setAmount(itemCount);
                                    }
                                }
                            }

                        }, 0);
                    }
                }
            }
        } else if (holding.getType() == Material.POWDER_SNOW_BUCKET) {
            ItemStack bucket = new ItemStack(Material.BUCKET);
            int maxBuckets = SIItems.getItemMax(player, bucket.getType(), bucket.getDurability(), player.getInventory().getType());
            if (clone.getAmount() > clone.getMaxStackSize() && (maxItems > SIItems.ITEM_DEFAULT || maxBuckets > SIItems.ITEM_DEFAULT)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    clone.setAmount(clone.getAmount() - 1);
                    if (hand == EquipmentSlot.HAND) {
                        player.getInventory().setItemInMainHand(clone);
                    } else {
                        player.getInventory().setItemInOffHand(clone);
                    }
                }, 0);

                InventoryUtil.addItemsToPlayer(player, bucket, "");
            }
        }

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }
        // Restore unlimited items
        if (maxItems == SIItems.ITEM_INFINITE) {
            if (hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(clone);
            } else {
                player.getInventory().setItemInOffHand(clone);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerIgniteBlock(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            // Only deal with players.
            if (player != null) {
                ItemStack holding = player.getInventory().getItemInMainHand();

                // Since repeatedly using flint and steel causes durability loss, reset durability on a new hit.
                ItemStack newStack = holding.clone();
                newStack.setDurability((short) 0);
                int maxItems = SIItems.getItemMax(player, newStack.getType(), newStack.getDurability(), player.getInventory().getType());

                // Don't touch default items.
                if (maxItems == SIItems.ITEM_DEFAULT) {
                    return;
                }
                // Handle unlimited flint and steel
                if (maxItems == SIItems.ITEM_INFINITE) {
                    player.getInventory().setItemInMainHand(newStack);
                    InventoryUtil.updateInventory(player);
                } else {
                    InventoryUtil.splitStackInMainHand(player, false);
                }
            }
        }
    }
}
