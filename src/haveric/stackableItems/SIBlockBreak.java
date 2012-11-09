package haveric.stackableItems;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SIBlockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

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
    }
}
