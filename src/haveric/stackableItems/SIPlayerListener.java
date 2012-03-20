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
		event.getInventory().setMaxStackSize(1024);
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
			
			Player player = (Player)event.getView().getPlayer();
			int maxItems = Config.getItemMax(player, clickedType, clickedDur);
			
			int slot = event.getSlot();
			
			//boolean normallyStackable = clickedType.getMaxStackSize() != 1;
			
			boolean cursorEmpty = cursorType == Material.AIR;
			boolean slotEmpty = clickedType == Material.AIR; 
			
			boolean virtualClicked = false;
			boolean virtualCursor = false;
			
			VirtualItemStack clickedStack = null, cursorStack = null;
			
			
			
			
			if (Config.isVirtualItemsEnabled()){
				clickedStack = VirtualItemConfig.getVirtualItemStack(player, slot);
				if (!clickedStack.isEmpty()){
					virtualClicked = true;
				}
				
				cursorStack = VirtualItemConfig.getVirtualItemStack(player, -1);
				if (!cursorStack.isEmpty()){
					virtualCursor = true;
				}
			}
			
			if (event.isLeftClick()){
				// Pick up a stack with an empty hand
				if (cursorEmpty && !slotEmpty && clickedAmount > clickedType.getMaxStackSize()){
					
					if (virtualClicked){
						player.sendMessage("Pick up stack with empty hand. (Virtual item)");
						VirtualItemConfig.setVirtualItemStack(player, slot, null);
						// Set cursor to the clicked stack
						VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
					} else {
						//player.sendMessage("Pick up stack with empty hand.");
						event.setCursor(clicked.clone());
						event.setCurrentItem(new ItemStack(Material.AIR));
						event.setResult(Result.ALLOW);
					}
				// Drop a stack into an empty slot
				} else if (!cursorEmpty && slotEmpty && cursorAmount > cursorType.getMaxStackSize()){
					if (virtualCursor){
						player.sendMessage("Drop a stack into an empty slot (virtual cursor)");
						VirtualItemConfig.setVirtualItemStack(player, -1, null);
						// Set slot to the cursor stack
						VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
					} else {
						//player.sendMessage("Drop a stack into an empty slot");
						event.setCurrentItem(cursor.clone());
						event.setCursor(new ItemStack(Material.AIR));
						event.setResult(Result.ALLOW);
					}
				// Combine two items
				} else if (!cursorEmpty && !slotEmpty){
					boolean sameType = clickedType == cursorType;
					
					// Combine two virtual stacks
					if (virtualCursor && virtualClicked){
						if (sameType){
							player.sendMessage("Combine two virtual stacks");
							while (clickedAmount < maxItems && cursorAmount > 0){
								clickedStack.addToFront(cursorStack.removeLast());
								clickedAmount ++;
								cursorAmount --;
							}
							VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
							if (cursorAmount > 0){
								VirtualItemConfig.setVirtualItemStack(player, -1, cursorStack);
							} else {
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
							}
						// swap stacks when not the same
						} else {
							player.sendMessage("Swap two virtual stacks");
							VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
							VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
						}
					// Add virtual stack to single item
					} else if (virtualCursor){
						if (sameType){
							if (cursorAmount < maxItems){
								player.sendMessage("Add virtual cursor to item");
								cursorStack.addItemStack(clicked.clone());
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
								VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
								
								cursor.setAmount(clickedAmount + cursorAmount);
								event.setCurrentItem(cursor.clone());
								event.setCursor(new ItemStack(Material.AIR));
								
								event.setResult(Result.ALLOW);
							}
						} else {
							player.sendMessage("Swap virtual cursor and item");
							VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
							VirtualItemConfig.setVirtualItemStack(player, -1, null);
						}
					// Add cursor to virtual stack
					} else if (virtualClicked){
						if (sameType){
							if (clickedAmount < maxItems){
								player.sendMessage("Add cursor to virtual slot stack");
								clickedStack.addToFront(cursor.clone());
								VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
								
								event.setCursor(new ItemStack(Material.AIR));
								
								cursor.setAmount(clickedAmount + cursorAmount);
								
								event.setCurrentItem(cursor.clone());
								
								event.setResult(Result.ALLOW);
							}
						} else {
							player.sendMessage("Swap cursor and virtual slot stack");
							VirtualItemConfig.setVirtualItemStack(player, slot, null);
							VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
						}
					// Add two normal items
					} else {
						boolean sameDur = cursorDur == clickedDur;
						boolean sameEnchants = cursor.getEnchantments().equals(clicked.getEnchantments());
						boolean noEnchants = cursor.getEnchantments() == null && clicked.getEnchantments() == null;
						
						
						if (sameType){
							if (sameDur && (sameEnchants || noEnchants)){
								if (maxItems > Config.ITEM_DEFAULT){
									
									int total = clickedAmount + cursorAmount;
									if (total <= maxItems){
										if (total > clicked.getMaxStackSize()){
											player.sendMessage("Combine two stacks fully");
											ItemStack s = new ItemStack(cursorType, total, cursorDur);
											s.addUnsafeEnchantments(cursor.getEnchantments());
											event.setCurrentItem(s);
											
											event.setCursor(new ItemStack(Material.AIR));
											event.setResult(Result.ALLOW);
											scheduleUpdate(player);
										}
									} else {
										player.sendMessage("Combine two stacks partially");
										ItemStack s = new ItemStack(cursorType, maxItems, cursorDur);
										s.addUnsafeEnchantments(cursor.getEnchantments());
										event.setCurrentItem(s);
										
										s = new ItemStack(cursorType, total - maxItems, cursorDur);
										s.addUnsafeEnchantments(cursor.getEnchantments());
										event.setCursor(s);
										
										event.setResult(Result.ALLOW);
									}
								}
							// Create a virtual stack out of two different items
							} else if (Config.isVirtualItemsEnabled()){
								player.sendMessage("Combine two items into a virtual stack.");
								VirtualItemStack vis = new VirtualItemStack();
								vis.addItemStack(cursor.clone());
								vis.addItemStack(clicked.clone());
								VirtualItemConfig.setVirtualItemStack(player, slot, vis);
								event.setCursor(new ItemStack(Material.AIR));
								
								cursor.setAmount(clickedAmount + cursorAmount);
								
								event.setCurrentItem(cursor.clone());
								
								event.setResult(Result.ALLOW);
							// no virtual items so just swap them
							} else {
								player.sendMessage("Swap two unstackable items");
								event.setCurrentItem(cursor.clone());
								event.setCursor(clicked.clone());
								
								event.setResult(Result.ALLOW);
							}
						} else if (cursorAmount > 64){
							player.sendMessage("Swap two items");
							
							event.setCurrentItem(cursor.clone());
							event.setCursor(clicked.clone());
							
							event.setResult(Result.ALLOW);
						}
					}
				}
			} else if (event.isRightClick()){
				if (!slotEmpty && !cursorEmpty){
					
				} else if (slotEmpty && !cursorEmpty){
					// Remove the last virtual itemstack
					if (virtualCursor){
						ItemStack removed = cursorStack.removeLast();
						event.setCurrentItem(removed);
						cursor.setAmount(cursorAmount - removed.getAmount());
						event.setCursor(cursor);
						
						
						VirtualItemConfig.setVirtualItemStack(player, -1, cursorStack);
						
						event.setResult(Result.ALLOW);
					} else {
						
					}
				}
			}
			/* TODO: redo
			else if (event.isRightClick()){
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
						scheduleUpdate(player);
					} else {
						event.setCancelled(true);
					}
				// remove last from virtual stack
				} else if (clickedType == Material.AIR && cursorType != Material.AIR){
					
				}
				
			}
			*/
		// Throwing out a stack
		} else {
			// TODO: handle throwing out a virtual stack
		}
	}
			
			/*
			// Combine items
			} else if (event.isLeftClick() && cursorType != Material.AIR){
				
				// same type and not normally stackable (Virtual items)
				if (clickedType == cursorType && clickedType.getMaxStackSize() == 1){
					VirtualItemStack vis = VirtualItemConfig.getVirtualItemStack(player, slot);
					vis.addItemStack(clicked.clone());
					vis.addItemStack(cursor.clone());
					
					clicked.setAmount(clickedAmount+1);
					event.setCursor(new ItemStack(Material.AIR));
					
					
					VirtualItemConfig.setVirtualItemStack(player, slot, vis);
					
					
					event.setResult(Result.ALLOW);
				} else if (clicked.getEnchantments().equals(cursor.getEnchantments())){
					// moving a stack to an empty slot
					if (clickedType == Material.AIR && cursorAmount > 1){
						ItemStack s = new ItemStack(cursorType, cursorAmount, cursorDur);
						s.addUnsafeEnchantments(cursor.getEnchantments());
						event.setCurrentItem(s);
						event.setCursor(new ItemStack(Material.AIR));
						event.setResult(Result.ALLOW);
						
						scheduleUpdate(player);
					// combining two alike stack
					} else if (cursorType == clickedType && cursorDur == clickedDur){
						
						
					}
				}
			*/
			/*
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
					scheduleUpdate(player);
				} else {
					event.setCancelled(true);
				}
			// remove last from virtual stack
			} else if (clickedType == Material.AIR && cursorType != Material.AIR){
				
			}
			
		}
	}
	*/
	
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
