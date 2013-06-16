package haveric.stackableItems;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SIBlockBreak implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();

        Player player = event.getPlayer();

        // Handle breaking furnaces with larger stacks in them than normally allowed
        if (block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE) {
            int maxAmount = Config.getFurnaceAmount(block.getLocation());
            if (maxAmount > SIItems.ITEM_DEFAULT) {
                Furnace furnace = (Furnace) block.getState();
                ItemStack result = furnace.getInventory().getResult();

                ItemStack toDrop = result.clone();
                toDrop.setAmount(maxAmount - 63);

                block.getWorld().dropItem(block.getLocation(), toDrop);

                Config.clearFurnace(furnace);
            }
        }

        ItemStack hand = player.getItemInHand();

        // Handle splitting tool stacks when used to break blocks
        if (hand != null) {
            Material type = hand.getType();
            int maxItems = SIItems.getItemMax(player, type, hand.getDurability(), player.getInventory().getName());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            if (maxItems == SIItems.ITEM_INFINITE) {
                ItemStack clone = hand.clone();
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
}
