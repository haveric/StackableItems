### StackableItems is a bukkit plugin that allows you to raise or lower the default stack amounts for any item.
#### Items can be disabled which disallows pickup and crafting.  Some items can also be set to be infinite which allows you to keep a certain item around no matter how much you use it.


#### For more info, visit [http://dev.bukkit.org/server-mods/stackableitems](http://dev.bukkit.org/server-mods/stackableitems) 

### Changelog:

#### Version 0.9.1.t3 (3/17/2013)
* Added Hopper support (Requires CraftBukkit build #2664)
* Ignore armor slots when dropping items


#### Version 0.9.1.t2 (3/12/2013)
* Removed dependency on VanishNoPacket. The event priority handles this and I get a cancelled event. 
* Fixed drinking from stacked milk buckets (Requires CraftBukkit build #2636 or higher)
* Custom XP Handling for Furnaces
    * Added furnaceXP.yml
    
#### Version 0.9.1.t1 (2/10/2013)
* Fixed dupe bug with crafting.

#### Version 0.9.1 (1/26/2013)
* Hopefully fixed getting permission groups when none exist
* Updated Metrics
* Fixed large stacks being able to be placed in inventories with smaller max stacks.
* Books work for shift clicking into enchantment tables.
* Fixed all book/metadata stacking.
* Removed message spam on furnaces when clicking on the results slot.
* Fixed Anvil dupe due to bukkit not recognizing the result slot as a result slot..
* Fixed shift clicking for crafting
* Fixed stacking across different stack amounts (ex: inventory: 100, crafting: 64)
* Buckets/bowls will be returned to inventory if used in a recipe
* Set some methods to HIGHEST priority. This should allow other plugins to take action before StackableItems tries to deal with some things.
* Checking for more cancelled events, which should also help plugin compatibility.
* Handle crafting when the result is greater than the normal stack amount.
* Fixed shift clicking an empty slot in an anvil (one ItemStack doesn't have a metadata while the other does)
* Since github dropped support for their version of downloads, I have set up a new folder structure under downloads:
    * release: always has the latest official version
    * dev: always has the most up to date version (could be the official or a development/test version)
    * old: storage of all old versions in case somebody wants to download an older version.

#### Version 0.9.0.1
* 1.4 support - Added Beacon and Anvil support

#### Version 0.9 (10/29/2012)
* Actually fixed stacking in brewing stands
* Fixed stacking in trading windows to work properly with Use_Stack_Amounts_In_Trading and only affect the trading slots.
* Fixed stacking issues with furnaces and enchantment tables
* Use default values for configs to prevent unnecessary file saving
* Replaced "ALL ITEMS MAX" with MIN and MAX
    * MIN sets the minimum amount that everything will stack to, but will not override higher default values. Ex: MIN: 2 would allow swords/armor/etc. to stack to 2, but dirt/cobble/etc would still stack to 64.
    * MAX sets the maximum amount that everything will stack to, but will not override lower default values. EX: MAX: 32 would allow dirt/cobble/etc to only stack to 32, but still allow tools to stack at 1 and snowballs to stack at 16.
    * If you want it to work as before, set both min and max to "ALL ITEMS MAX" (values above 64 only need min set as no default values go that high)
* VanishNoPacket support (If vanished and player has vanish.nopickup perm, items will not be picked up)
* Better support for default Perms
* Initial support for infinite items (Note: not every item is supported and may still be buggy)
    * Picking up and dropping items is not affected by this currently, only using the item (placing a block, hitting an enemy, etc)
    * Set the item to "infinite", "unlimited", or -2 (Example: DIAMOND_PICKAXE: infinite)
* Shears now split when used in a stack to prevent durability loss for the rest of the stack.
* Flint and Steel no longer uses durability on water, lava, and fire since you are never actually setting a fire.

#### Version 0.8.9 (9/23/2012)
* Use stacks in Brewing stand option was setting the crafting option again.
* Fixed repairing when using RepairRecipe plugin.
* Fixed some duplication bugs with crafting tables.
* Fixed duplication bug with brewing stands.
* Fixed a bug with default config using the group config.
* Added chestItems.yml to allow setting different stack amounts in chests.
* Fixed a bug with stacking that caused loss of items.

#### Version 0.8.8 (9/10/2012)
* Fixed some potential NPEs
* Fixed permissions to handle Vault properly when it is not found.
* Adding Java Version for Metrics to gather. This should tell me if I need to compile against an older version or can compile against a newer version of java.  If I don't get anything useful out of this, I'll remove it in the next version.
* Prevent written books as well as book and quills from stacking to prevent duplication. This is a temporary fix until bukkit implements a Book API
* Fixed some inconsistencies with stack amounts

#### Version 0.8.7 (8/26/2012):
* Added debug option.  If you want to help me test issues, set this to true.  If you start getting spammed with messages in the console, report those to github. At any point, you can set this to false to stop getting messages (after a "/si reload")
    * Will also be used from now on to prevent random message spam from my testing if I don't take them out before release.
* Adding Metrics support. To opt out, set "opt-out: true" in PluginMetrics/config.yml
* Removed dependency on CraftBukkit needed for the pop sound when picking up items thanks to the new Sounds API.
* Fixed bow repairing
* Fixed shift clicking in Enchantment Tables
    * If enchantment table is empty, first click will move 1 item to fill the slot.  Another click will switch it between main inventory and quickslots.
    * Items that cannot be enchanted will be moved between the main inventory and quickslots.
* Fixed shift clicking on stacks of armor in order to equip one.
* Fixed options to use stacks in merchant and crafting using the furnace option instead of their own.
* Added Use_Stack_Amounts_In_Brewing
* Fixed shift clicking in Brewing Stands and regular clicking in the ingredient slot.
* Improved shift clicking in Crafting window
* Fixed crafting recipes that use -1 durability for any durability

#### Version 0.8.6 (8/12/2012)
* Fixed(hopefully) a null pointer exception when eating food
* Shift clicking support in furnaces
    * Overrides default shift clicking to work as follows:
    * 1. Is the item a fuel? If so, check the fuel slot and move it there if you can. If not a fuel or fuel slot is full->
    * 2. Is the item used in any furnace recipe? If so, add it to the ingredient slot if possible. If not used in any recipes->
    * 3. Move the item between hotbar and main inventory.
    * Added lists/defaultFuels.txt and lists/customFuels.txt
    * /lists/defaultFuels.txt - This is a default fuel list that I provide and can update between versions (there is also a version # to this file which is listed at the top). This file will be used if customFuels.txt is empty
    * /lists/customFuels.txt - If an update comes out that adds more fuels and I disappear off the face of the planet or you have another plugin that uses something else as a fuel, this can be used to keep updated or add custom fuels. If there is anything in this file, it will override anything in defaultFuels.
* No longer overriding armor in crafting inventory
* No longer overriding repairing recipes
* Mushroom soup will not be fake consumed when you lose hunger
* Fixed bucket stacking
* Renamed config.yml to options.yml to hopefully avoid confusion in the future.
    * options renamed to avoid spaces
* Switched cmdMain and cmdMainAlt - Should fix conflicts with OpenInv
* Fixed picking up and dropping items causing them to be removed/glitchy.
* Fixed shift clicking in ender chests and villager trading.
* Any clicking on the villager trading Result slot will use vanilla behavior to prevent item loss/duplication. (until Bukkit implements a trading api)
* New config option: useStackAmountsInFurnace - True: Players can only put in the amount they are allowed, False: Furnaces hold their normal amount, no matter what players can stack to
    * Also useStackAmountsInTrading and useStackAmountsInCrafting. (False is defaulted for all three) (These may eventually turn into the way chests will be implemented, as in a unique file for each that allows setting all items as defaultItems.yml does)

#### Version 0.8.5 (7/28/2012)
* In-game commands for setting stack amounts and reloading now require players to be an op or have permission "- stackableitems.adjust"
* Shapeless recipe support for crafting

#### Version 0.8.4 (7/22/2012)
* Can no longer craft larger stacks than you're allowed to.
* Custom adding of items to inventory fixed
* Fixed infinite stack bug with large stacks for people without permissions to use large stacks.
* Items no longer bounce around when picking up if the stack size is normally bigger.
* Added /si as an alias for /stackableitems
* In-game commands to set/adjust stack amounts
* Fixed shift clicking for brewing stands

#### Version 0.8.3 (6/21/2012)
* Fix Shift Clicking (mostly)
* Fixed id's in config files
* Player's cannot pick up larger stacks than their permissions allow for.

#### Version 0.8.2 (6/3/2012)
* Fixed an exception when Vault wasn't found.
* Fixed unenchanted items combining with enchanted items when picked up

#### Version 0.8.1 (5/18/2012)
* Fixed file writing issues

#### Version 0.8 (5/14/2012)
* Fixed duplication bug with crafting
* Removed unnecessary defaultTools and customTools configs.
* Made config check player.yml before group.yml as player should be able to override group.
* Fix chest stacking maxing out at 64 (Client won't show the correct number above 64)
* Capping max items to 127 due to item loss after reloading. (I have an idea for a workaround, so it may come back)
* Fixed unstacking bug when a stack of tools was separated after max being set to normal (-1) again.
* Right clicking on a stack while holding another drops items one at a time as vanilla does.
* Fixed large stacks from being put in armor slots, enchantment tables, and brewing stands.
* Item groups
* Added /stackableitems reload command to reload config files
* All config/item files are loaded into the plugin now when users need them, which means less i/o and possibly faster
* Item files should now handle any case -> IRON_BOOTS, iron_boots, IrOn_BOOts should all work.
* Added optional furnace stack amount which allows furnaces to stack above 64 (65-127 or -1 for disabled). 
    * The client won't show it correctly, and the server can't smelt above 64 so it is faked (reverts to 63 and a counter saved) until picked up or the furnace is destroyed
* Now ignores creative mode so that items can be removed properly (temp fix)

#### Version 0.7.5 (4/16/2012)
* Prevent crafting of disabled items (set to 0)
* Play pickup sound when picking up stacks
* Fixed: Picking up items with a nearly full inventory will destroy any that don't fit
* Config now works with lowercase versions of items: 'DIRT: 0' as well 'dirt: 0'
* Fixed mushroom soup eating creating too many bowls.

#### Version 0.7.4 (4/1/2012)
* Fixed Fishing rod stacking
* Fixed items losing enchantments when picked up

#### Version 0.7.3 (3/28/2012)
* Fixed the other NPE when right clicking

#### Version 0.7.2 (3/27/2012)
* Fix for NPE when right clicking with an empty hand

#### Version 0.7.1 (3/26/2012)
* Changed how config files work: Moved items into it's own defaultItems.yml; config.yml should only have "Virtual Items:"
* Fixed bucket duplication bug
* Stacked filled buckets (water/lava) now split off an empty bucket when used allowing you to keep using the stack
* Mushroom soup can now be eaten while stacked (empty bowls will be split off)
* Fixed Right Clicking functionality
* Partially implemented virtual stacks
* Fixed tool stacking (except fishing rods)
* Added defaultTools.txt and customTools.txt to the plugin folder.  Use these to define custom tools


#### Version 0.7 (3/8/2012)
* Disabling an item (setting to 0) caused shadow items to fill inventory
* Added Vault soft dependency
* Added Permissions per group/player (Add a group.yml or player.yml, ie: admin.yml or haveric.yml)

#### Version 0.6.4 (3/8/2012)
* Removed test messages

#### Version 0.6.3 (3/7/2012)
* Fixed picking up multiple stacks causing amounts to be wrong
* Fixed right click functionality on a stack larger than its normal max size
* Enchanted items can now only be stacked with other equally enchanted items to prevent losing enchantments

#### Version 0.6.2 (3/6/2012)
* Added option to use item id's in config (just replace a name with an id)
* Fixed individual enchanted items losing enchantments
* Fixed bucket stacks when placing/picking up water/lava

#### Version 0.6.1 (3/4/2012)
* Fixed default Command /stackableitems
* Added durability checking

#### Version 0.6 (3/4/2012)
* Fixed "shadow" items. Thanks to Celtic_Minstrel!

#### Version 0.5 (3/4/2012)
* Initial Release
