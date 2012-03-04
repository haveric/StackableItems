package haveric.stackableItems;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SIPlayerListener implements Listener{
	
	StackableItems plugin;
	
	public SIPlayerListener(StackableItems si) {
		plugin = si;
	}

	
	@EventHandler
	public void inventoryClick(InventoryClickEvent event){
		if (event.isCancelled()){
			return;
		}
		ItemStack cursor = event.getCursor();
		ItemStack clicked = event.getCurrentItem();
		plugin.log.info("Cursor: " + cursor);
		plugin.log.info("Clicked: " + clicked);
		
		
		if (cursor != null && clicked != null) {
			Material cursorType = cursor.getType();
			short cursorDur = cursor.getDurability();
			Material clickedType = clicked.getType();
			short clickedDur = clicked.getDurability();
			
			plugin.log.info("Cursor Type: " + cursorType + ", Dur: " + cursorDur);
			plugin.log.info("Clicked Type: " + clickedType + ", Dur: " + clickedDur);
			
			if (clickedType == Material.AIR && cursorType != Material.AIR){

			} else if (cursorType == clickedType && cursorDur == clickedDur && cursorType != Material.AIR){
				plugin.log.info("Getting here");
				int maxItems = Config.getItemMax(clicked.getType());
				if (maxItems > Config.ITEM_DEFAULT){
					int cursorAmount = cursor.getAmount();
					int clickedAmount = clicked.getAmount();
					
					if (clickedAmount + cursorAmount <= maxItems){
						if (clickedAmount + cursorAmount > clicked.getMaxStackSize()){
							event.setCurrentItem(new ItemStack(cursorType, clickedAmount + cursorAmount, cursorDur));
							
							// TODO: figure out an alternative or fix for this
							event.setCursor(new ItemStack(Material.DIRT, 0));
							event.setCancelled(true);
						}
					} else {
						event.setCurrentItem(new ItemStack(cursorType, maxItems, cursorDur));
						event.setCursor(new ItemStack(cursorType, clickedAmount + cursorAmount - maxItems, cursorDur));
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void playerPicksUpItem(PlayerPickupItemEvent event){
		if (event.isCancelled()){
			return;
		}
		Item item = event.getItem();
		
		int maxItems = Config.getItemMax(item.getItemStack().getType());
		event.getPlayer().sendMessage("Item: " + item.getItemStack().getType() + ", Max: " + maxItems);
		if (maxItems > Config.ITEM_DEFAULT){
			addItemsToInventory(event.getPlayer(), item);
			event.setCancelled(true);
		}
	}
	
	public void addItemsToInventory(Player player, Item entity){
		
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		ItemStack add = entity.getItemStack();
		
		Material addType = add.getType();
		short durability = add.getDurability();
		
		int maxAmount = Config.getItemMax(addType);
		int addAmount = add.getAmount();
		
		int canAdd;
		// add to existing stacks
		for(int i = 0; i < contents.length && addAmount > 0; i++){
			ItemStack item = contents[i];
			
			if (item != null){
				int free = item.getAmount();
				if (item != null && item.getType() == add.getType() && item.getDurability() == durability && free < maxAmount){
					canAdd = maxAmount - free;
					if (addAmount <= canAdd){
						item.setAmount(free + addAmount);
						addAmount = 0;
					} else {
						item.setAmount(maxAmount);
						addAmount -= maxAmount;
					}
				}
			}
		}
		
		boolean fullInventory = false;
		
		while (addAmount > 0 && !fullInventory){
			// check for empty slots
			int freeSlot = inventory.firstEmpty();
			if (freeSlot == -1){
				fullInventory = true;
			} else {
				if (addAmount <= maxAmount){
					inventory.setItem(freeSlot, new ItemStack(addType, addAmount, durability));
					addAmount = 0;
				} else {
					inventory.setItem(freeSlot, new ItemStack(addType, maxAmount, durability));
					addAmount -= maxAmount;
				}
			}
		}
		
		if (addAmount == 0){
			entity.remove();
		} else {
			entity.setItemStack(new ItemStack(addType, addAmount, durability));
		}
		
		
		player.sendMessage("Items left: " + addAmount);
	}
}
