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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SIBlockListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        Player player = event.getPlayer();


        BlockState blockState = block.getState();
        // Handle breaking furnaces with larger stacks in them than normally allowed
        if (block instanceof Furnace) {
            Furnace furnace = (Furnace) block;
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
                    if (meta instanceof BlockStateMeta) {
                        BlockStateMeta blockMeta = (BlockStateMeta) meta;
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
        int maxItems = SIItems.getItemMax(player, type, ItemUtil.getDurability(holding), player.getInventory().getType());

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
                InventoryUtil.splitStack(player, false);
            } else {
                InventoryUtil.splitStack(player, true);
            }
        }
    }
}
