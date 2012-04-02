package haveric.stackableItems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SIPlayerListener implements Listener{
	
	StackableItems plugin;
	
	public SIPlayerListener(StackableItems si) {
		plugin = si;
	}

	@EventHandler
	public void playerFish(PlayerFishEvent event){
		Player player = event.getPlayer();
		ItemStack holding = player.getItemInHand();
		int amount = holding.getAmount();
		if (amount > 1){
			if (!Config.isVirtualItemsEnabled()){
				ItemStack move = holding.clone();
				move.setAmount(amount-1);
				
				scheduleAddItems(player, move);
				holding.setAmount(1);
			}
		}
	}
	
	@EventHandler
	public void breakBlock(BlockBreakEvent event){
		Player player = event.getPlayer();
		ItemStack holding = player.getItemInHand();
		int amount = holding.getAmount();
		if (amount > 1 && ToolConfig.isTool(holding.getType())){
			if (!Config.isVirtualItemsEnabled()){
				ItemStack move = holding.clone();
				move.setAmount(amount-1);
				
				scheduleAddItems(player, move);
				holding.setAmount(1);
			}
			
		}
	}
	
	@EventHandler
	public void shootBow(EntityShootBowEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			ItemStack holding = player.getItemInHand();
			int amount = holding.getAmount();
			
			if (amount > 1){
				if (!Config.isVirtualItemsEnabled()){
					ItemStack move = holding.clone();
					move.setAmount(amount - 1);
					
					scheduleAddItems(player, move);
					holding.setAmount(1);
				}
			}
		}
	}
	
	@EventHandler
	public void entityDamage(EntityDamageByEntityEvent event){
		if (event.isCancelled()){
			return;
		}
		
		if (event.getDamager() instanceof Player){
			Player player = (Player) event.getDamager();
			ItemStack stack = player.getItemInHand();
			int amount = stack.getAmount();
			if (amount > 1 && ToolConfig.isTool(stack.getType())){
				ItemStack move = stack.clone();
				move.setAmount(amount-1);
				if (!Config.isVirtualItemsEnabled()){
					stack.setAmount(1);
					scheduleAddItems(player, move);
				}
				
			}
		}
	}
	

	@EventHandler
	public void fillBucket(PlayerBucketFillEvent event){
		Player player = event.getPlayer();
		int amount = player.getInventory().getItemInHand().getAmount();
		if (amount > 1){
			scheduleAddItems(player, new ItemStack(Material.BUCKET, amount - 1));
		}
		
	}
	
	@EventHandler
	public void emptyBucket(PlayerBucketEmptyEvent event){
		Player player = event.getPlayer();
		
		ItemStack holding = player.getInventory().getItemInHand();
		Material type = holding.getType();
		
		//player.sendMessage("Empty bucket, type: " + type);
		
		int slot = player.getInventory().getHeldItemSlot();
		
		int amount = holding.getAmount();
		if (amount > 1){
			if (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.MILK_BUCKET){
				ItemStack clone = holding.clone();
				clone.setAmount(amount - 1);
				
				scheduleReplaceItem(player, slot, clone);
				scheduleAddItems(player, new ItemStack(Material.BUCKET, 1));	
			}
		
		}
	}
	
	@EventHandler
	public void eatFood(FoodLevelChangeEvent event){
		Player player = (Player) event.getEntity();
		
		PlayerClickData clickData = SIPlayers.getPlayerData(player.getName());
		if (clickData.getAmount() > 1){
			if (clickData.getType() == Material.MUSHROOM_SOUP){
				PlayerInventory inventory = player.getInventory();
				ItemStack itemAtSlot = inventory.getItem(clickData.getSlot());
				if (itemAtSlot.getType() == Material.MUSHROOM_SOUP){
					scheduleReplaceItem(player, clickData.getSlot(), new ItemStack(Material.MUSHROOM_SOUP, clickData.getAmount()-1));
					scheduleAddItems(player, new ItemStack(Material.BOWL, clickData.getAmount()-1));
				}
			} else { 
				//player.sendMessage("Type: " + clickData.getType());
			}

		}
	}
	


	@EventHandler
	public void playerClick(PlayerInteractEvent event){
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
			ItemStack holding = event.getItem();
			if (holding != null){
				Material holdingType = holding.getType();
				Player player = event.getPlayer();
				
				int amount = holding.getAmount();
				
				PlayerClickData clickData = new PlayerClickData(player.getInventory().getHeldItemSlot(), holdingType, amount, holding.getDurability());
				SIPlayers.setPlayerData(player.getName(), clickData);
			}
		}
		
		// only prevent this after checking for consumption
		if (event.isCancelled()){
			return;
		}
		
		

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
			ItemStack holding = event.getItem();
			if (holding != null){
				Material holdingType = holding.getType();
				Player player = event.getPlayer();
				
				int amount = holding.getAmount();
				
				if (amount > 1){
					if (holdingType == Material.FLINT_AND_STEEL){
						ItemStack move = holding.clone();
						move.setAmount(amount-1);
						
						holding.setAmount(1);
						scheduleAddItems(player, move);
					}
				}
			}
		}
	}
	

	@EventHandler
	public void inventoryClick(InventoryClickEvent event){
		if (event.isCancelled()){
			return;
		}
		//event.getInventory().setMaxStackSize(1024);
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
			
			if (event.isShiftClick()){
				Inventory top = event.getView().getTopInventory();
				//Inventory bot = event.getView().getBottomInventory();
				InventoryType topType = top.getType();
				//InventoryType botType = event.getView().getBottomInventory().getType();
				
				if (topType.equals("CHEST")){
					
				} else {
					
				}
				// TODO: Handle moving large stacks
				// TODO: Handle stacking large stacks in other containers
				
				//player.sendMessage("Top: " + topType + ", Bot: " + botType + ", Slot: " + event.getSlotType());
			} else if (event.isLeftClick()){
				// Pick up a stack with an empty hand
				if (cursorEmpty && !slotEmpty && clickedAmount > clickedType.getMaxStackSize()){
					
					if (virtualClicked){
						//player.sendMessage("Pick up stack with empty hand. (Virtual item)");
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
						//player.sendMessage("Drop a stack into an empty slot (virtual cursor)");
						VirtualItemConfig.setVirtualItemStack(player, -1, null);
						// Set slot to the cursor stack
						VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
						if (cursorAmount > 64){
							
							event.setCursor(new ItemStack(Material.AIR));
							event.setCurrentItem(cursor.clone());
							
							event.setResult(Result.ALLOW);
							
						}
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
							//player.sendMessage("Combine two virtual stacks");
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
							//player.sendMessage("Swap two virtual stacks");
							VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
							VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
						}
					// Add virtual stack to single item
					} else if (virtualCursor){
						if (sameType){
							if (cursorAmount < maxItems){
								//player.sendMessage("Add virtual cursor to item");
								cursorStack.addItemStack(clicked.clone());
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
								VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
								
								cursor.setAmount(clickedAmount + cursorAmount);
								event.setCurrentItem(cursor.clone());
								event.setCursor(new ItemStack(Material.AIR));
								
								event.setResult(Result.ALLOW);
							}
						} else {
							//player.sendMessage("Swap virtual cursor and item");
							VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
							VirtualItemConfig.setVirtualItemStack(player, -1, null);
						}
					// Add cursor to virtual stack
					} else if (virtualClicked){
						if (sameType){
							if (clickedAmount < maxItems){
								//player.sendMessage("Add cursor to virtual slot stack");
								clickedStack.addToFront(cursor.clone());
								VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
								
								event.setCursor(new ItemStack(Material.AIR));
								
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
						boolean sameDur = cursorDur == clickedDur;
						boolean sameEnchants = cursor.getEnchantments().equals(clicked.getEnchantments());
						boolean noEnchants = cursor.getEnchantments() == null && clicked.getEnchantments() == null;
						
						
						if (sameType){
							if (sameDur && (sameEnchants || noEnchants)){
								if (maxItems > Config.ITEM_DEFAULT){
									
									int total = clickedAmount + cursorAmount;
									if (total <= maxItems){
										if (total > clicked.getMaxStackSize()){
											//player.sendMessage("Combine two stacks fully");
											ItemStack s = new ItemStack(cursorType, total, cursorDur);
											s.addUnsafeEnchantments(cursor.getEnchantments());
											event.setCurrentItem(s);
											
											event.setCursor(new ItemStack(Material.AIR));
											event.setResult(Result.ALLOW);
										}
									} else {
										//player.sendMessage("Combine two stacks partially");
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
								//player.sendMessage("Combine two items into a virtual stack.");
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
								//player.sendMessage("Swap two unstackable items");
								event.setCurrentItem(cursor.clone());
								event.setCursor(clicked.clone());
								
								event.setResult(Result.ALLOW);
							}
						} else if (cursorAmount > 64){
							//player.sendMessage("Swap two items");
							
							event.setCurrentItem(cursor.clone());
							event.setCursor(clicked.clone());
							
							event.setResult(Result.ALLOW);
						}
					}
				}
			} else if (event.isRightClick()){
				if (!slotEmpty && !cursorEmpty){
					if (virtualCursor && virtualClicked){
						
					} else if (virtualCursor && !virtualClicked){
						
					} else if (!virtualCursor && virtualClicked){
						
					} else if (!virtualCursor && !virtualClicked){
						
						boolean sameType = clickedType == cursorType;
						
						// Combine two virtual stacks
						if (virtualCursor && virtualClicked){
							if (sameType){
								//player.sendMessage("RC:Combine two virtual stacks");
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
								//player.sendMessage("RC:Swap two virtual stacks");
								VirtualItemConfig.setVirtualItemStack(player, -1, clickedStack);
								VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
							}
						// Add virtual stack to single item
						} else if (virtualCursor){
							if (sameType){
								if (cursorAmount < maxItems){
									//player.sendMessage("RC:Add virtual cursor to item");
									cursorStack.addItemStack(clicked.clone());
									VirtualItemConfig.setVirtualItemStack(player, -1, null);
									VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
									
									cursor.setAmount(clickedAmount + cursorAmount);
									event.setCurrentItem(cursor.clone());
									event.setCursor(new ItemStack(Material.AIR));
									
									event.setResult(Result.ALLOW);
								}
							} else {
								//player.sendMessage("RC:Swap virtual cursor and item");
								VirtualItemConfig.setVirtualItemStack(player, slot, cursorStack);
								VirtualItemConfig.setVirtualItemStack(player, -1, null);
							}
						// Add cursor to virtual stack
						} else if (virtualClicked){
							if (sameType){
								if (clickedAmount < maxItems){
									//player.sendMessage("RC:Add cursor to virtual slot stack");
									clickedStack.addToFront(cursor.clone());
									VirtualItemConfig.setVirtualItemStack(player, slot, clickedStack);
									VirtualItemConfig.setVirtualItemStack(player, -1, null);
									
									event.setCursor(new ItemStack(Material.AIR));
									
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
							boolean sameDur = cursorDur == clickedDur;
							boolean sameEnchants = cursor.getEnchantments().equals(clicked.getEnchantments());
							boolean noEnchants = cursor.getEnchantments() == null && clicked.getEnchantments() == null;
							
							
							if (sameType){
								if (sameDur && (sameEnchants || noEnchants)){
									if (maxItems > Config.ITEM_DEFAULT){
										
										int total = clickedAmount + cursorAmount;
										if (total <= maxItems){
											if (total > clicked.getMaxStackSize()){
												//player.sendMessage("RC:Combine two stacks fully");
												ItemStack s = new ItemStack(cursorType, total, cursorDur);
												s.addUnsafeEnchantments(cursor.getEnchantments());
												event.setCurrentItem(s);
												
												event.setCursor(new ItemStack(Material.AIR));
												event.setResult(Result.ALLOW);
											}
										} else {
											//player.sendMessage("RC:Combine two stacks partially");
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
									//player.sendMessage("RC:Combine two items into a virtual stack.");
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
									//player.sendMessage("RC:Swap two unstackable items");
									event.setCurrentItem(cursor.clone());
									event.setCursor(clicked.clone());
									
									event.setResult(Result.ALLOW);
								}
							} else if (cursorAmount > 64){
								//player.sendMessage("RC:Swap two items");
								
								event.setCurrentItem(cursor.clone());
								event.setCursor(clicked.clone());
								
								event.setResult(Result.ALLOW);
							}
						}
					}
				// 
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
				// pick up half a stack
				} else if (!slotEmpty && cursorEmpty){
					
				}
			//
			}
		// Throwing out a stack
		} else {
			// TODO: handle throwing out a virtual stack
		}
	}
/*
	private void scheduleUpdate(final HumanEntity player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
		    @SuppressWarnings("deprecation")
			@Override public void run() {
		      ((Player) player).updateInventory();
		    }
		});
	}
*/
	private void scheduleAddItems(final Player player, final ItemStack stack){
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override public void run() {
				addItemsToInventory(player, stack);
		    }
		});	
	}
	
	private void scheduleReplaceItem(final Player player, final int slot, final ItemStack stack) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override public void run() {
				player.getInventory().setItem(slot, stack);
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
		
		
		ItemStack clone = entity.getItemStack().clone();
		while (addAmount > 0 && !fullInventory){
			// check for empty slots
			int freeSlot = inventory.firstEmpty();
			if (freeSlot == -1){
				fullInventory = true;
			} else {
				if (addAmount <= maxAmount){
					clone.setAmount(addAmount);
					inventory.setItem(freeSlot, clone);
					addAmount = 0;
				} else {
					clone.setAmount(maxAmount);
					inventory.setItem(freeSlot, clone);
					addAmount -= maxAmount;
				}
			}
		}

		if (addAmount == 0){
			entity.remove();
		} else {
			entity.getItemStack().setAmount(addAmount);
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
		ItemStack clone = add.clone();
		while (addAmount > 0 && !fullInventory){
			// check for empty slots
			int freeSlot = inventory.firstEmpty();
			if (freeSlot == -1){
				fullInventory = true;
			} else {
				if (addAmount <= maxAmount){
					clone.setAmount(addAmount);
					inventory.setItem(freeSlot, clone);
					addAmount = 0;
				} else {
					clone.setAmount(maxAmount);
					inventory.setItem(freeSlot, clone);
					addAmount -= maxAmount;
				}
			}
		}
		
		if (addAmount == 0){
			
		} else {
			clone.setAmount(addAmount);
			player.getWorld().dropItemNaturally(player.getLocation(), clone);
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
