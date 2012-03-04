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
    private static final boolean ALL_ITEMS_MAX_DEFAULT = false;
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
    	boolean freeze = config.getBoolean(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT); 
    	config.set(cfgAllItemsMax, freeze);
    	
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
	
	public static boolean getAllItemsMax(){
		return config.getBoolean(cfgAllItemsMax);
	}
	
	public static void setFreezeWater(boolean freeze){
		config.set(cfgAllItemsMax, freeze);
		saveConfig();
	}
	
	public static void setItemMax(Material mat, int max){
		config.set(mat.name(), max);
		saveConfig();
	}
	
	public static int getItemMax(Material mat){
		return config.getInt(mat.name(), ITEM_DEFAULT);
	}
}
