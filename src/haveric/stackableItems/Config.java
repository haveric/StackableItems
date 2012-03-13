package haveric.stackableItems;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
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

		if (plugin.permEnabled() && plugin.getPerm().has(player, Perms.getStack())){
			
			String group = plugin.getPerm().getPrimaryGroup(player);
			
	    	configGroupFile = new File(plugin.getDataFolder() + "/" + group + ".yml");
	    	configPlayerFile = new File(plugin.getDataFolder() + "/" + player.getName() + ".yml");
	    	
	    	// load from a group.yml
	    	if (configGroupFile.exists()){
	    		configGroup = YamlConfiguration.loadConfiguration(configGroupFile);

	    		max = getMaxFromConfig(configGroup, mat, dur);
    		// load from a player.yml
	    	} else if (configPlayerFile.exists()){
	    		configPlayer = YamlConfiguration.loadConfiguration(configPlayerFile);
	    		
	    		max = getMaxFromConfig(configPlayer, mat, dur);
	    	} else {
	    		max = getMaxFromConfig(defaultItems, mat, dur);
	    	}
		} else {
			max = getMaxFromConfig(defaultItems, mat, dur);
		}

		return max;
	}
	
	private static int getMaxFromConfig(FileConfiguration fileConfig, Material mat, short dur){
		int max;
		// Check for material name and durability
		max = fileConfig.getInt(mat.name() + " " + dur, ITEM_DEFAULT);
		
		// Check for item id and durability
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.getId() + " " + dur, ITEM_DEFAULT);
		}
		
		// no durability
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.name(), ITEM_DEFAULT);
		}
		if (max == ITEM_DEFAULT){
			max = fileConfig.getInt(mat.getId() + "", ITEM_DEFAULT);
		}
		
		// no individual item set, use the all items value
		if (max == ITEM_DEFAULT){
			max = getAllItemsMax(fileConfig);
		}
		
		return max;
	}
	
	public static boolean isVirtualItemsEnabled(){
		return config.getBoolean(cfgVirtualItems);
	}
	
}
