package haveric.stackableItems;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

	static StackableItems plugin;
	
	private static String cfgAllItemsMax = "All items Max";
    public static FileConfiguration config;
    public static File configFile;
    
    // Defaults
    private static final int ALL_ITEMS_MAX_DEFAULT = -1;
    public static final int ITEM_DEFAULT = -1;
    

    /**
     * Initializes the config file
     * @param ss The main class used to 
     */
    public static void init(StackableItems si){
    	plugin = si;
    	configFile = new File(plugin.getDataFolder() + "/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /** 
     * Sets up the default variables if they don't exist yet.
     * 
     */
    public static void setup(){
    	int allItems = config.getInt(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT); 
    	config.set(cfgAllItemsMax, allItems);
    	
    	saveConfig();
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
	
	public static int getAllItemsMax(){
		return config.getInt(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT);
	}
	
	public static void setAllItemsMax(int numMax){
		config.set(cfgAllItemsMax, numMax);
		saveConfig();
	}
	
	public static int getItemMax(Material mat, short dur){
		int max;
		max = config.getInt(mat.name() + " " + dur, ITEM_DEFAULT);
		if (max == ITEM_DEFAULT){
			max = config.getInt(mat.name(), getAllItemsMax());
		}
		
		return max;
	}
}
