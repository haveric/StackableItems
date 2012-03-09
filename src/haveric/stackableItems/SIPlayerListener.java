package haveric.stackableItems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SIPlayerListener implements Listener{
	
	StackableItems plugin;
	
	public SIPlayerListener(StackableItems si) {
		plugin = si;
	}

	@EventHandler
	public void playerClick(PlayerInteractEvent event){
		if (event.isCancelled()){
			return;
		}
		//event.getPlayer().sendMessage("Action: " + event.getAction() + ", Name: " + event.getEventName());
		ItemStack holding = event.getItem();
		if (holding != null){
			int amount = holding.getAmount();
			
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && amount > 1){
				Material holdingType = holding.getType();
				Player player = event.getPlayer();
				
				if (holdingType == Material.BUCKET){
					Block up = event.getClickedBlock().getRelative(BlockFace.UP);
					if (up.getData() == 0){
						scheduleAddItems(player, Material.BUCKET, amount-1);
					}
				} else if (holdingType == Material.WATER_BUCKET){
					scheduleAddItems(player, Material.WATER_BUCKET, amount-1);
				} else if (holdingType == Material.LAVA_BUCKET){
					scheduleAddItems(player, Material.LAVA_BUCKET, amount-1);
				}
			}
		}
	}
	

	@EventHandler
	public void inventoryClick(InventoryClickEvent event){
		if (event.isCancelled()){
			return;
		}

		ItemStack cursor = event.getCursor();
		ItemStack clicked = event.getCurrentItem();
		
		// prevent clicks outside the inventory area
		if (cursor != null && clicked != null) {
			Material cursorType = cursor.getType();
			short cursorDur = cursor.getDurability();
			int cursorAmount = cursor.getAmount();
			
			Material clickedType = clicked.getType();
			short clickedDur = clicked.getDurability();
			int clickedAmount = clicked.getAmount();
			
			int maxItems = Config.getItemMax((Player)event.getView().getPlayer(), clickedType, clickedDur);
			
			if (event.isLeftClick() && cursorType != Material.AIR){
				if (clicked.getEnchantments().equals(cursor.getEnchantments())){
					if (clickedType == Material.AIR && cursorAmount > 1){
						ItemStack s = new ItemStack(cursorType, cursorAmount, cursorDur);
						s.addUnsafeEnchantments(cursor.getEnchantments());
						event.setCurrentItem(s);
						event.setCursor(new ItemStack(Material.AIR));
						event.setResult(Result.ALLOW);
						
						scheduleUpdate(event.getView().getPlayer());
					} else if (cursorType == clickedType && cursorDur == clickedDur){
						
						if (maxItems > Config.ITEM_DEFAULT){
		
							int total = clickedAmount + cursorAmount;
							if (total <= maxItems){
								if (total > clicked.getMaxStackSize()){
									ItemStack s = new ItemStack(cursorType, total, cursorDur);
									s.addUnsafeEnchantments(cursor.getEnchantments());
									event.setCurrentItem(s);
									
									event.setCursor(new ItemStack(Material.AIR));
									event.setResult(Result.ALLOW);
									scheduleUpdate(event.getView().getPlayer());
								}
							} else {
								ItemStack s = new ItemStack(cursorType, maxItems, cursorDur);
								s.addUnsafeEnchantments(cursor.getEnchantments());
								event.setCurrentItem(s);
								
								s = new ItemStack(cursorType, total - maxItems, cursorDur);
								s.addUnsafeEnchantments(cursor.getEnchantments());
								event.setCursor(s);
								
								event.setCancelled(true);
							}
						}
					} else {
						// Virtual Items
					}
				}
			} else if (event.isRightClick()){
				if (clickedType != Material.AIR && cursorType != Material.AIR && clickedAmount >= clicked.getMaxStackSize()){
					if (clickedAmount < maxItems){
						if (cursorAmount > 1){
							clicked.setAmount(clickedAmount + 1);
							event.setCurrentItem(clicked);
							
							cursor.setAmount(cursorAmount - 1);
							event.setCursor(cursor);

						} else {
							clicked.setAmount(clickedAmount + 1);
							event.setCurrentItem(clicked);
							
							event.setCursor(new ItemStack(Material.AIR, 0));
						}
						event.setResult(Result.ALLOW);
						scheduleUpdate(event.getView().getPlayer());
					} else {
						event.setCancelled(true);
					}
				}
				
			}
		}
	}
	
	private void scheduleUpdate(final HumanEntity player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
		    @SuppressWarnings("deprecation")
			@Override public void run() {
		      ((Player) player).updateInventory();
		    }
		});
	}
	
	private void scheduleAddItems(final Player player, final Material material, final int amount){
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override public void run() {
				addItemsToInventory(player, new ItemStack(material, amount));
		    }
		});	
	}
	
	@EventHandler
	public void playerPicksUpItem(PlayerPickupItemEvent event){
		if (event.isCancelled()){
			return;
		}
		Item item = event.getItem();
		
		int maxItems = Config.getItemMax(event.getPlayer(), item.getItemStack().getType(), item.getItemStack().getDurability());
		//event.getPlayer().sendMessage("Item: " + item.getItemStack().getType() + ", Max: " + maxItems);
		if (maxItems == 0){
			event.setCancelled(true);
		} else if (maxItems > Config.ITEM_DEFAULT){
			
			addItemsToInventory(event.getPlayer(), item);
			event.setCancelled(true);
		}
	}
	
	public void addItemsToInventory(Player player, Item entity){		
		Inventory inventory = player.getInventory();
		
		ItemStack add = entity.getItemStack();
		
		Material addType = add.getType();
		short durability = add.getDurability();
		
		int maxAmount = Config.getItemMax(player, addType, durability);
		int addAmount = add.getAmount();
		
		// add to existing stacks
		addAmount = addToExistingStacks(player, add);
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
	}
	
	public void addItemsToInventory(Player player, ItemStack add){		
		Inventory inventory = player.getInventory();
		
		Material addType = add.getType();
		short durability = add.getDurability();
		
		int maxAmount = Config.getItemMax(player, addType, durability);
		int addAmount = add.getAmount();
		
		// add to existing stacks
		addAmount = addToExistingStacks(player, add);
		
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
			
		} else {
			player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(addType, addAmount, durability));
		}
	}
	
	private int addToExistingStacks(Player player, ItemStack add) {
		int canAdd;
		int maxAmount = Config.getItemMax(player, add.getType(), add.getDurability());
		int addAmount = add.getAmount();
		
		ItemStack[] contents = player.getInventory().getContents();
		for(int i = 0; i < contents.length && addAmount > 0; i++){
			ItemStack item = contents[i];
						
			if (item != null){
				int free = item.getAmount();
				if (item.getType() == add.getType() && item.getDurability() == add.getDurability() && free < maxAmount){
					canAdd = maxAmount - free;
					if (addAmount <= canAdd){
						item.setAmount(free + addAmount);
						addAmount = 0;
						
					} else if (addAmount <= maxAmount){
						item.setAmount(maxAmount);
						addAmount -= canAdd;
						
					} else {
						item.setAmount(maxAmount);
						addAmount -= maxAmount;
					}
				}
			}
		}
		return addAmount;
	}
	
}
