package haveric.stackableItems;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class VirtualItemConfig {

	static StackableItems plugin;
	
    private static FileConfiguration config;
    private static File configFile;
    
    // Defaults
    

    /**
     * Initializes the config file
     * @param ss The main class used to 
     */
    public static void init(StackableItems si){
    	plugin = si;
    	configFile = new File(plugin.getDataFolder() + "/virtualItems.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /** 
     * Sets up the default variables if they don't exist yet.
     * 
     */
    public static void setup(){
    	//int allItems = config.getInt(cfgAllItemsMax, ALL_ITEMS_MAX_DEFAULT); 
    	//config.set(cfgAllItemsMax, allItems);
    	
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
}
