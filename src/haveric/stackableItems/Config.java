package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

	static StackableItems plugin;
	
	private static String cfgVirtualItems = "Virtual Items";
	private static String cfgFurnaceAmount = "Furnace Amount";
	
    private static FileConfiguration config;
    private static File configFile;
    
    private static FileConfiguration configFurnaces;
    private static File configFurnacesFile;
    
    //private static FileConfiguration configChest;
    //private static File configFileChest;
    
    
    // Defaults
    public static final int ITEM_DEFAULT = -1;
    
    
    private static final boolean VIRTUAL_ITEMS_DEFAULT = false;
    private static final int FURNACE_AMOUNT_DEFAULT = -1;
    

    /**
     * Initializes the config file
     * @param ss The main class used to 
     */
    public static void init(StackableItems si){
    	plugin = si;
    	configFile = new File(plugin.getDataFolder() + "/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		
		
		configFurnacesFile = new File(plugin.getDataFolder() + "/data/furnaces.yml");
		configFurnaces = YamlConfiguration.loadConfiguration(configFurnacesFile);
    }
    
    public static void reload(){
    	try {
			config.load(configFile);
			
			configFurnaces.load(configFurnacesFile);
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
    			
    	int furnaceAmt = config.getInt(cfgFurnaceAmount, FURNACE_AMOUNT_DEFAULT);
    	
    	if (configFile.length() == 0){
    		config.set(cfgVirtualItems, virtualItems);
    		config.set(cfgFurnaceAmount, furnaceAmt);
    		saveConfig();
    	}
    	
    	if (configFurnacesFile.length() == 0){
    		saveCustomConfig(configFurnaces, configFurnacesFile);
    	}
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
	
	
	public static void saveCustomConfig(FileConfiguration fileConfig, File file){
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isVirtualItemsEnabled(){
		return config.getBoolean(cfgVirtualItems);
	}
	
	
	public static int getFurnaceAmount(Furnace furnace){
		return getFurnaceAmount(furnace.getLocation());
	}
	
	public static int getFurnaceAmount(Location loc){
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		int amt = configFurnaces.getInt(world + "." + x + "," + y + "," + z, ITEM_DEFAULT);
		
		return amt;
	}
	
	public static void setFurnaceAmount(Furnace furnace, int newAmt){
		Location loc = furnace.getLocation();
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		configFurnaces.set(world + "." + x + "," + y + "," + z, newAmt);
		
		saveCustomConfig(configFurnaces, configFurnacesFile);
	}
	
	public static void clearFurnace(Furnace furnace){
		clearFurnace(furnace.getLocation());
	}
	
	public static void clearFurnace(Location loc){
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		configFurnaces.set(world + "." + x + "," + y + "," + z, null);
		
		saveCustomConfig(configFurnaces, configFurnacesFile);
	}
	
	public static int getMaxFurnaceAmount(){
		return config.getInt(cfgFurnaceAmount, ITEM_DEFAULT);
	}
	
	public static void setMaxFurnaceAmount(int newAmt){
		config.set(cfgFurnaceAmount, newAmt);
		
		saveConfig();
	}
}
