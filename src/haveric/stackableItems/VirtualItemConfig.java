package haveric.stackableItems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
     * Saves the configuration to the file.
     */
	private static void saveConfig(){
		try {
			config.save(configFile);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static VirtualItemStack getVirtualItemStack(Player player, int slot) {
		String loc = player.getWorld().getName() + "." + player.getName()+ ".";
		if (slot == -1){
			loc += "cursor"; 
		} else {
			loc += "slot" + slot;
		}
		ArrayList<ItemStack> items = (ArrayList<ItemStack>) config.getList(loc, null);
		
		VirtualItemStack vis = new VirtualItemStack(items);

		return vis;
	}
	
	public static void setVirtualItemStack(Player player, int slot, VirtualItemStack vis){
		
		String loc = player.getWorld().getName() + "." + player.getName()+ ".";
		if (slot == -1){
			loc += "cursor"; 
		} else {
			loc += "slot" + slot;
		}
		
		if (vis == null || vis.isEmpty()){
			config.set(loc, null);
		} else {
			config.set(loc,vis.getList());
		}
		saveConfig();
	}
}
