package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Config {

	static StackableItems plugin;
	
	private static String cfgVirtualItems = "Virtual Items";
	private static String cfgAllItemsMax = "All items Max";
	
    private static FileConfiguration config;
    private static File configFile;
    
    private static FileConfiguration defaultItems;
    private static File defaultItemsFile;
    
    private static FileConfiguration configGroup;
    private static File configGroupFile;
    
    private static FileConfiguration configPlayer;
    private static File configPlayerFile;
    
    private static FileConfiguration configGroups;
    private static File configGroupsFile;
    
    //private static FileConfiguration configChest;
    //private static File configFileChest;
    
    
    // Defaults
    private static final int ALL_ITEMS_MAX_DEFAULT = -1;
    public static final int ITEM_DEFAULT = -1;
    
    private static final boolean VIRTUAL_ITEMS_DEFAULT = false;
    

    /**
     * Initializes the config file
     * @param ss The main class used to 
     */
    public static void init(StackableItems si){
    	plugin = si;
    	configFile = new File(plugin.getDataFolder() + "/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		
		defaultItemsFile = new File(plugin.getDataFolder() + "/defaultItems.yml");
		defaultItems = YamlConfiguration.loadConfiguration(defaultItemsFile);
		
		configGroupsFile = new File(plugin.getDataFolder() + "/groups.yml");
		configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
    }
    
    public static void reload(){
    	try {
			config.load(configFile);
			
			defaultItems.load(defaultItemsFile);
			
			configGroups.load(configGroupsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
    }
    /** 
     * Sets up the default variables if they don't exist yet.
     * 
     */
    public static void setup(){
    	boolean virtualItems = config.getBoolean(cfgVirtualItems, VIRTUAL_ITEMS_DEFAULT);
    	config.set(cfgVirtualItems, virtualItems);
    			
    	int allItems = defaultItems.getInt(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT); 
    	defaultItems.set(cfgAllItemsMax, allItems);
    	
    	saveConfig();
    	
    	saveCustomConfig(defaultItems, defaultItemsFile);
    }
    
    /**
     * Saves the configuration to the file.
     */
	private static void saveConfig(){
		try {
			config.save(configFile);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	
	private static void saveCustomConfig(FileConfiguration fileConfig, File file){
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static int getAllItemsMax(FileConfiguration fileConfig){
		return fileConfig.getInt(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT);
	}
	
	public static int getItemMax(Player player, Material mat, short dur){
		int max;
		
		ArrayList<String> itemGroups = getItemGroups(mat, dur);
		
		if (plugin.permEnabled() && plugin.getPerm().has(player, Perms.getStack())){
			
			String group = plugin.getPerm().getPrimaryGroup(player);
			configGroupFile = new File(plugin.getDataFolder() + "/" + group + ".yml");
			configPlayerFile = new File(plugin.getDataFolder() + "/" + player.getName() + ".yml");
			
			// load from a player.yml
			if (configPlayerFile.exists()){
				configPlayer = YamlConfiguration.loadConfiguration(configPlayerFile);
				
				max = getMaxFromConfig(configPlayer, mat, dur, itemGroups);
			
			// load from a group.yml
			} else if (configGroupFile.exists()){
				configGroup = YamlConfiguration.loadConfiguration(configGroupFile);

				max = getMaxFromConfig(configGroup, mat, dur, itemGroups);
			} else {
				max = getMaxFromConfig(defaultItems, mat, dur, itemGroups);
			}
		} else {
			max = getMaxFromConfig(defaultItems, mat, dur, itemGroups);
		}

		return max;
	}
	
	private static int getMaxFromConfig(FileConfiguration fileConfig, Material mat, short dur, ArrayList<String> itemGroups){
		int max;
		
		// Check for material name and durability
		max = fileConfig.getInt(mat.name() + " " + dur, ITEM_DEFAULT);

		// Check for group
		int size = itemGroups.size();
		if (max == ITEM_DEFAULT && size > 0){
			for (int i = 0; i < size; i++){
				int tempVal = fileConfig.getInt(itemGroups.get(i), ITEM_DEFAULT);
				if (tempVal > max){
					max = tempVal;
				}
			}
		}
		
		// Check for lowercase items
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.name().toLowerCase() + " " + dur, ITEM_DEFAULT);
		}
		
		// Check for item id and durability
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.getId() + " " + dur, ITEM_DEFAULT);
		}
		
		// no durability
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.name(), ITEM_DEFAULT);
		}
		// Check for lowercase
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.name().toLowerCase(), ITEM_DEFAULT);
		}
		
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.getId() + "", ITEM_DEFAULT);
		}
		
		// no individual item set, use the all items value
		if (max == ITEM_DEFAULT){
			max = getAllItemsMax(fileConfig);
		}
		
		// TODO: implement workaround to allow larger stacks after player leaving and logging back in.
		if (max > 127){
			max = 127;
		}
		
		return max;
	}
	
	public static boolean isVirtualItemsEnabled(){
		return config.getBoolean(cfgVirtualItems);
	}
	
	private static ArrayList<String> getItemGroups(Material mat, short dur){
		ArrayList<String> inGroup = new ArrayList<String>();
		ArrayList<String> saveList = new ArrayList<String>();
		for (String key : configGroups.getKeys(false)){
			List<String> items = configGroups.getStringList(key);
			int size = items.size();
			
			for (int i = 0; i < size; i++){
				String item = items.get(i).toUpperCase();
				if (item.equals(mat.name() + " " + dur) || item.equals(mat.getId() + " " + dur) || item.equals(mat.name()) || item.equals(mat.getId() + "")){
					inGroup.add(key);
					
					if (saveList.size() > 0){
						inGroup.addAll(saveList);
						saveList.clear();
					}
					break;
				}
			}
			
			if (size == 0){
				saveList.add(key);
			} else {
				saveList.clear();
			}
		}
		return inGroup;
	}
}
