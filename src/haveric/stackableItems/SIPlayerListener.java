package haveric.stackableItems;

import java.util.List;

import net.minecraft.server.Packet22Collect;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SIPlayerListener implements Listener{
	
	StackableItems plugin;
	
	public SIPlayerListener(StackableItems si) {
		plugin = si;
	}

	@EventHandler
	public void furnaceSmelt(FurnaceSmeltEvent event){
		if (event.isCancelled()){
			return;
		}
		
		
		int amt = 0;

		Furnace furnace = (Furnace) event.getBlock().getState();
		ItemStack result = furnace.getInventory().getResult(); 
		if (result == null){
			amt = 0;
		} else {
			amt = result.getAmount()+1;
		}
		
		int maxFurnaceSize = Config.getMaxFurnaceAmount();
		if (maxFurnaceSize > 64 && maxFurnaceSize <= 127){
			
			// going to be a full furnace
			if (amt == 64){
				int furnaceAmt = Config.getFurnaceAmount(furnace);
				if (furnaceAmt == maxFurnaceSize - 1){
					result.setAmount(furnaceAmt);
					Config.clearFurnace(furnace);
				// increment virtual count
				} else {
					if (furnaceAmt == -1){
						furnaceAmt = 64;
					} else {
						furnaceAmt ++;
					}
					
					Config.setFurnaceAmount(furnace, furnaceAmt);
					
					result.setAmount(62);
				}
			}
		}
	}
	
/*
	@EventHandler (priority = EventPriority.HIGHEST)
	public void commandPreprocess(PlayerCommandPreprocessEvent event){
		String msg[] = event.getMessage().split(" ");

		int amt = 1;
		int data = 0;
		if (msg[0].equalsIgnoreCase("/item") || msg[0].equalsIgnoreCase("/i")){
			event.setCancelled(true);
			event.getPlayer().sendMessage("No items for you.");
			if (msg.length == 2 || msg.length == 3){
				
				String item[] = msg[1].split(":");
				event.getPlayer().sendMessage("item: " + item[0]);
				if (item.length == 2){
					event.getPlayer().sendMessage("data: " + item[1]);
				}
				
				// TODO: deal with enchantments
				//String enchants[] = msg[1].split("|");
				//if (enchants.length == 2){
				//	event.getPlayer().sendMessage("Enchants: " + enchants[1]);
				//}
				
				if (msg.length == 3){
					amt = Integer.parseInt(msg[2]);
				}
			}
		}
	}
*/
	
	@EventHandler
	public void craftItem(CraftItemEvent event){
		if (event.isCancelled()){
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		ItemStack craftedItem = event.getCurrentItem();
		
		if (Config.getItemMax(player, craftedItem.getType(), craftedItem.getDurability()) == 0){
			player.sendMessage(String.format("[%s] This item has been disabled.", plugin.getDescription().getName()));
			event.setCancelled(true);
		}
		
		
	}
	
	@EventHandler
	public void playerFish(PlayerFishEvent event){
		splitStack(event.getPlayer(), false);
	}
	
	@EventHandler
	public void breakBlock(BlockBreakEvent event){
		splitStack(event.getPlayer(), true);		
	}
	
	@EventHandler
	public void shootBow(EntityShootBowEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			splitStack(player, false);
		}
	}
	
	@EventHandler
	public void entityDamage(EntityDamageByEntityEvent event){
		if (event.getDamager() instanceof Player){
			splitStack((Player) event.getDamager(), true);			
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
					scheduleAddItems(player, new ItemStack(Material.BOWL, 1));
				}
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
			PlayerClickData clickData = SIPlayers.getPlayerData(event.getPlayer().getName());
			clickData.setLastBlock(event.getClickedBlock().getType());
			clickData.setLastBlockLocation(event.getClickedBlock().getLocation());
			
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
		event.getInventory().setMaxStackSize(127);
		ItemStack cursor = event.getCursor();
		ItemStack clicked = event.getCurrentItem();
		
		
		SlotType slotType = event.getSlotType();
		
		if (cursor != null && clicked != null && slotType == SlotType.RESULT){
			Inventory top = event.getView().getTopInventory();
			InventoryType topType = top.getType();
			
			if (topType == InventoryType.FURNACE){
				int maxFurnaceSize = Config.getMaxFurnaceAmount();
				if (maxFurnaceSize > 64 && maxFurnaceSize <= 127){
					
					PlayerClickData clickData = SIPlayers.getPlayerData(event.getWhoClicked().getName());
					Material lastClicked = clickData.getLastBlock();
					if (lastClicked == Material.FURNACE || lastClicked == Material.BURNING_FURNACE){
						Location loc = clickData.getLastBlockLocation();
						
						int amt = Config.getFurnaceAmount(loc);
						if (amt > -1){
							ItemStack clone = clicked.clone();
							clone.setAmount(amt);
							
							event.setCurrentItem(null);
							event.setCursor(clone);
							event.setResult(Result.ALLOW);
						}
					}
				}
			}
		// prevent clicks outside the inventory area or within result slots
		} else if (cursor != null && clicked != null && slotType != SlotType.RESULT && slotType != SlotType.ARMOR) {
			Inventory top = event.getView().getTopInventory();
			InventoryType topType = top.getType();
			
			int rawSlot = event.getRawSlot();
			Player player = (Player) event.getWhoClicked();
			
			// we want to ignore creative players (for now)
			if (player.getGameMode() == GameMode.CREATIVE){
				
				return;
			}
			if (topType == InventoryType.ENCHANTING){
				top.setMaxStackSize(1);
				if (rawSlot == 0){
					return;
				}
			} else if (topType == InventoryType.BREWING){
				if (rawSlot <= 3){
					return;
				}
			}
			
			Material cursorType = cursor.getType();
			short cursorDur = cursor.getDurability();
			int cursorAmount = cursor.getAmount();
			
			Material clickedType = clicked.getType();
			short clickedDur = clicked.getDurability();
			int clickedAmount = clicked.getAmount();
			
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
			Inventory bot = event.getView().getBottomInventory();

			InventoryType botType = event.getView().getBottomInventory().getType();
			/*
			if (event.isShiftClick()){

				
				
				player.sendMessage("Bot: " + botType + ", Raw: " + rawSlot + ", Slot: " + slot);
				
				if (botType == InventoryType.PLAYER){
					// In crafting area, move to main inventory
					int startNum = 0;
					int endNum = 0;
					boolean fromTop = false;

					// TODO: Handle stacking large stacks in other containers (chests)
					if (topType == InventoryType.CRAFTING || topType == InventoryType.DISPENSER){
						fromTop = true;
						endNum = 8;
					} else if (topType == InventoryType.WORKBENCH){
						fromTop = true;
						endNum = 9;
					} else if (topType == InventoryType.FURNACE){
						fromTop = true;
						endNum = 2;
					} else if (topType == InventoryType.BREWING){
						fromTop = true;
						endNum = 3;
					// TODO: Find a way to fix this
					// TODO: try cancelling and schedule add
					} else if (topType == InventoryType.CHEST){
						fromTop = true;
						//endNum = 26;
						
						endNum = top.getContents().length - 1;
						player.sendMessage("size: " + endNum);
						//scheduleAddItems(player, clicked.clone());
						//event.setCancelled(true);
					}
					

					
					if (fromTop){
						if (rawSlot >= startNum && rawSlot <= endNum){
							int addAmount = addToExistingStacks(player, clicked.clone(), false, true);
							
							if (addAmount > 0){
								ItemStack[] contents = bot.getContents();
								
								int length = contents.length;
								for(int i = 9; i < length && addAmount > 0; i++){
									ItemStack item = contents[i];
									
									if (item != null){
										if (item.getAmount() == 0 || item.getType() == Material.AIR){
											ItemStack clone = clicked.clone();
											clone.setAmount(addAmount);
											bot.setItem(i, clone);
											addAmount = 0;
										}
									}
								}
							}
							if (addAmount > 0){
								ItemStack clone = clicked.clone();
								clone.setAmount(addAmount);
								top.setItem(slot, clone);
							} else {
								top.setItem(slot, new ItemStack(Material.AIR, 0));
							}
						}
						scheduleUpdate(player);
					}
					player.sendMessage("SlotType: " + event.getSlotType());
					// In main inventory, move to hotbar
					if (rawSlot >= 9 && rawSlot <= 35){
						int addAmount = addToExistingStacks(player, clicked.clone(), true, false);
						player.sendMessage("Addamount: " + addAmount);
						if (addAmount > 0){
							int emptySlot = bot.firstEmpty();
							if (emptySlot < 9){
								if (addAmount > maxItems){
									
									bot.setItem(emptySlot, clicked.clone());
								}
							}
						}
						if (addAmount > 0){
							ItemStack clone = clicked.clone();
							clone.setAmount(addAmount);
							bot.setItem(slot, clone);
						} else {
							bot.setItem(slot, new ItemStack(Material.AIR, 0));
						}
					// In hotbar, move to main inventory
					} else if (rawSlot >= 36 && rawSlot <= 44){
						int addAmount = addToExistingStacks(player, clicked.clone(), false, true);
						if (addAmount > 0){
							ItemStack[] contents = bot.getContents();
							
							int length = contents.length;
							for(int i = 9; i < length && addAmount > 0; i++){
								ItemStack item = contents[i];
								
								if (item != null){
									if (item.getAmount() == 0){
										ItemStack clone = clicked.clone();
										clone.setAmount(addAmount);
										bot.setItem(i, clone);
										addAmount = 0;
									}
								}
							}
							
						}
						if (addAmount > 0){
							ItemStack clone = clicked.clone();
							clone.setAmount(addAmount);
							bot.setItem(slot, clone);
						} else {
							bot.setItem(slot, new ItemStack(Material.AIR, 0));
						}
					}
				}
			} else 
			*/
			if (event.isShiftClick()){
				player.sendMessage("Top: " + topType + ", Bot: " + botType + ", Raw: " + rawSlot);
				//event.setCancelled(true);
				
				if (topType == InventoryType.CHEST){
					if (rawSlot < top.getContents().length - 1){
						if (clicked.getAmount() > clickedType.getMaxStackSize()){
							scheduleAddItems(player, clicked.clone());
							
							event.setCurrentItem(null);
							
							//event.setResult(Result.ALLOW);
						}
						//
					}
				}
			} else
			
			if (event.isLeftClick()){
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
									
									int total = clickedAmount + 1;
									if (total <= maxItems){
										if (total > clicked.getMaxStackSize()){
											//player.sendMessage("RC:Drop single item");
											
											ItemStack clone = cursor.clone();
											clone.setAmount(total);
											
											event.setCurrentItem(clone);
											if (cursorAmount == 1){
												event.setCursor(new ItemStack(Material.AIR));
											} else {
												cursor.setAmount(cursorAmount-1);
											}
											event.setResult(Result.ALLOW);
										}
									} else {
										event.setCancelled(true);
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

	private void scheduleUpdate(final HumanEntity player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
		    @SuppressWarnings("deprecation")
			@Override public void run() {
		      ((Player) player).updateInventory();
		    }
		});
	}

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
	
	private void scheduleSetRemainingAmount(final Player player, final ItemStack stack, final int newAmount){
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override public void run() {
				List<Entity> entities = player.getNearbyEntities(2, 2, 2);
				
				boolean found = false;
				
				int size = entities.size();
				for (int i = 0; i < size && !found; i++){
					if (entities.get(i) instanceof Item){
						ItemStack currentStack = ((Item) entities.get(i)).getItemStack();
						if (currentStack.getType() == stack.getType() && currentStack.getDurability() == stack.getDurability() && currentStack.getEnchantments().equals(stack.getEnchantments())){
							currentStack.setAmount(newAmount);
							found = true;
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void playerPicksUpItem(PlayerPickupItemEvent event){
		if (event.isCancelled()){
			return;
		}
		
		Player player = event.getPlayer();
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		
		int numLeft = event.getRemaining();
		
		int maxItems = Config.getItemMax(event.getPlayer(), stack.getType(), stack.getDurability());
		if (maxItems == 0){
			event.setCancelled(true);
		} else if (maxItems > Config.ITEM_DEFAULT){
			
			int addAmount = addItemsToInventory(player, item);
			if (addAmount == 0){
				collectItem(player, item);
				
				if (numLeft > 0){
					ItemStack newStack = stack.clone();
					newStack.setAmount(numLeft);
					
					//scheduleSetRemainingAmount(player, originalStack, numLeft);
					player.getWorld().dropItem(item.getLocation(), newStack);
				}
				item.remove();
			}
			event.setCancelled(true);
		}
	
	}
	
	public void collectItem(Player player, Item item) {
		
        Packet22Collect packet = new Packet22Collect(((Entity) item).getEntityId(), player.getEntityId());
        ((CraftPlayer)player).getHandle().netServerHandler.sendPacket(packet);
    }
	
	public int addItemsToInventory(Player player, Item entity){
		
		Inventory inventory = player.getInventory();
		
		ItemStack add = entity.getItemStack();

		Material addType = add.getType();
		short durability = add.getDurability();
		
		int maxAmount = Config.getItemMax(player, addType, durability);
		if (maxAmount <= Config.ITEM_DEFAULT){
			maxAmount = addType.getMaxStackSize();
		}
		int addAmount = add.getAmount();
		// add to existing stacks
		addAmount = addToExistingStacks(player, add, false, false);
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

		return addAmount;
	}
	
	public void addItemsToInventory(Player player, ItemStack add){		
		Inventory inventory = player.getInventory();
		
		Material addType = add.getType();
		short durability = add.getDurability();
		
		int maxAmount = Config.getItemMax(player, addType, durability);
		if (maxAmount <= Config.ITEM_DEFAULT){
			maxAmount = addType.getMaxStackSize();
		}
		int addAmount = add.getAmount();

		// add to existing stacks
		addAmount = addToExistingStacks(player, add, false, false);

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
	
	private int addToExistingStacks(Player player, ItemStack add, boolean hotbarOnly, boolean mainInvOnly) {
		int canAdd;
		int maxAmount = Config.getItemMax(player, add.getType(), add.getDurability());
		if (maxAmount <= Config.ITEM_DEFAULT){
			maxAmount = add.getType().getMaxStackSize();
		}
		int addAmount = add.getAmount();
		
		ItemStack[] contents = player.getInventory().getContents();
		int length;
		int iStart = 0;
		if (hotbarOnly){
			length = 9;
		} else if (mainInvOnly){
			iStart = 9;
			length = 36;
		} else {
			length = contents.length;
		}
		
		for(int i = iStart; i < length && addAmount > 0; i++){
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
	
	public void splitStack(Player player, boolean toolCheck){
		ItemStack holding = player.getItemInHand();
		int amount = holding.getAmount();
		
		if (amount > 1){
			if (!toolCheck || ToolUtil.isTool(holding.getType())){
				if (!Config.isVirtualItemsEnabled()){
					ItemStack move = holding.clone();
					move.setAmount(amount-1);
					scheduleAddItems(player, move);
					holding.setAmount(1);
				}
			}
		}
	}
}
