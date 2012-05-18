package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SIItems {

	//			   player	item			num
	static HashMap<String, HashMap<String, Integer>> itemsMap;
	//			   item		groups
	static HashMap<String, ArrayList<String>> itemGroups;
	
	static StackableItems plugin;
	
    private static FileConfiguration configItems;
    private static File configItemsFile;
	
    private static FileConfiguration configGroups;
    private static File configGroupsFile;
	
    private static FileConfiguration defaultItems;
    private static File defaultItemsFile;
    
    private static String cfgAllItemsMax = "ALL ITEMS MAX";
    
    public static final int ITEM_DEFAULT = -1;
    
	public static void init(StackableItems si){
		plugin = si;
		
		configGroupsFile = new File(plugin.getDataFolder() + "/groups.yml");
		configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
		if (configGroupsFile.length() == 0){
			Config.saveCustomConfig(configGroups, configGroupsFile);
		}
		
		defaultItemsFile = new File(plugin.getDataFolder() + "/defaultItems.yml");
		defaultItems = YamlConfiguration.loadConfiguration(configGroupsFile);
		
		setupDefaultItemsFile();
		
		reload();
	}
	
	private static void setupDefaultItemsFile(){
		if (defaultItemsFile.length() == 0){
			defaultItems.set(cfgAllItemsMax, ITEM_DEFAULT);
			Config.saveCustomConfig(defaultItems, defaultItemsFile);
		}
	}
	
	public static void reload(){
		itemsMap = new HashMap<String, HashMap<String, Integer>>();
		itemGroups = new HashMap<String, ArrayList<String>>();
		
		try {
			configGroups.load(configGroupsFile);
			
			defaultItems.load(defaultItemsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		addItemFiles("defaultItems");
		loadGroupItemFiles();
		loadPlayerItemFiles();
		loadItemGroups();
	}
	
	private static void loadGroupItemFiles(){
		String groups[] = Perms.getPerm().getGroups();
		
		for (String group : groups){
			addItemFiles(group);
		}
	}
	
	private static void loadPlayerItemFiles(){
		Player players[] = plugin.getServer().getOnlinePlayers();
		
		for (Player player : players){
			addItemFiles(player.getName());
		}
	}
	
	public static void addItemFiles(String groupOrPlayer){
		configItemsFile = new File(plugin.getDataFolder() + "/" + groupOrPlayer + ".yml");
		configItems = YamlConfiguration.loadConfiguration(configItemsFile);
		if (!itemsMap.containsKey(groupOrPlayer)){
			itemsMap.put(groupOrPlayer, new HashMap<String, Integer>());
		}
		for (String key : configItems.getKeys(false)){
			itemsMap.get(groupOrPlayer).put(key.toUpperCase(), configItems.getInt(key));
		}
	}
	
	public static void removeItemFiles(String groupOrPlayer){
		if (!itemsMap.get(groupOrPlayer).isEmpty()){
			itemsMap.get(groupOrPlayer).clear();
		}
		itemsMap.remove(groupOrPlayer);
	}
	
	
	private static void loadItemGroups(){
		List<String> saveList = new ArrayList<String>();
		for (String key : configGroups.getKeys(false)){
			List<String> items = configGroups.getStringList(key);
			int size = items.size();
			
			if (size == 0){
				saveList.add(key);
			} else {
				saveList.add(key);
				
				for (int i = 0; i < items.size(); i++){
					String item = items.get(i).toUpperCase();
					if (!itemGroups.containsKey(item)){
						itemGroups.put(item, new ArrayList<String>());
					}
					itemGroups.get(item).addAll(saveList);
				}
				saveList.clear();
			}
		}
	}
	
	public static int getItemMax(Player player, Material mat, short dur){
		int max = ITEM_DEFAULT;
		
		max = getMaxFromMap(player.getName(), mat, dur);
		if (max == ITEM_DEFAULT && Perms.permEnabled() && Perms.getPerm().has(player, Perms.getStack())){
			String group = Perms.getPerm().getPrimaryGroup(player);	
			max = getMaxFromMap(group, mat, dur);
		}
		if (max == ITEM_DEFAULT){
			max = getMaxFromMap("defaultItems", mat, dur);
		}
		
		return max;
	}
	
	private static int getMaxFromMap(String file, Material mat, short dur){
		int max = ITEM_DEFAULT;
		

		
		List<String> groups = null;
		if (itemGroups.containsKey(mat.name() + " " + dur)){
			groups = itemGroups.get(mat.name() + " " + dur);
		} else if (itemGroups.containsKey(mat.getId() + " " + dur)){
			groups = itemGroups.get(mat.getId() + " " + dur);
		} else if (itemGroups.containsKey(mat.name())){
			groups = itemGroups.get(mat.name());
		} else if (itemGroups.containsKey(mat.getId())){
			groups = itemGroups.get(mat.getId());
		}
		
		
		if (itemsMap.containsKey(file)){
			HashMap<String, Integer> subMap = itemsMap.get(file);
			if (groups != null){
				for (int i = 0; i < groups.size(); i++){
					if (subMap.containsKey(groups.get(i).toUpperCase())){
						max = subMap.get(groups.get(i).toUpperCase());
					}
				}
			}

			if (max == ITEM_DEFAULT){
				// check for material and durability
				if (subMap.containsKey(mat.name() + " " + dur)){
					max = subMap.get(mat.name() + " " + dur);
				// check for item id and durability
				} else if (subMap.containsKey(mat.getId() + " " + dur)){
					max = subMap.get(mat.getId() + " " + dur);
				// material name with no durability
				} else if (subMap.containsKey(mat.name())){
					max = subMap.get(mat.name());
				// item id with no durability
				} else if (subMap.containsKey(mat.getId())){
					max = subMap.get(mat.getId());
				// no individual item set, use the 'all items' value
				} else if (subMap.containsKey(cfgAllItemsMax)){
					max = subMap.get(cfgAllItemsMax);
				}
			}
			
			// TODO: implement workaround to allow larger stacks after player leaving and logging back in.
			if (max > 127){
				max = 127;
			}
		}
		
		return max;
	}
}
