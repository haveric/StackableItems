package haveric.stackableItems;

import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StackableItems extends JavaPlugin {
	
	final Logger log = Logger.getLogger("Minecraft");
	private Commands commands = new Commands(this);
	
	protected final SIPlayerListener playerListener = new SIPlayerListener(this);
	
    
    // Vault  
    private Permission perm = null;
    
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		// Register the plugin events
		pm.registerEvents(playerListener, this);
		
		Config.init(this);
		
        // Vault
        setupVault();
        
		Config.setup();
		
		getCommand(Commands.getMain()).setExecutor(commands);
		log.info(String.format("[%s] v%s Started",getDescription().getName(), getDescription().getVersion()));
	}
	
	public void onDisable() {
		log.info(String.format("[%s] Disabled",getDescription().getName()));
	}
	
    private void setupVault() {
    	if(getServer().getPluginManager().getPlugin("Vault") == null){
    		log.info(String.format("[%s] Vault not found. Permissions disabled.",getDescription().getName()));
    		return;
    	}
        RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) {
            perm = permProvider.getProvider();
        }
    }
    
    public Permission getPerm(){
    	return perm;
    }
    
    public boolean permEnabled(){
    	return (perm != null);
    }
}
