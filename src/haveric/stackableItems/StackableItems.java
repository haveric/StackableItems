package haveric.stackableItems;

import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StackableItems extends JavaPlugin {
	
	final Logger log = Logger.getLogger("Minecraft");
	private Commands commands = new Commands(this);
    
    
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		// Register the plugin events
		pm.registerEvents(new SIPlayerListener(this), this);
		pm.registerEvents(new SIBlockBreak(), this);
		pm.registerEvents(new PlayerJoinQuit(), this);
		
		Config.init(this);
		VirtualItemConfig.init(this);
		
		
        // Vault
        setupVault();
        
        SIItems.init(this);
        
        SIPlayers.setup();
        
		Config.setup();
		
		getCommand(Commands.getMain()).setExecutor(commands);
	}
	
	public void onDisable() {

	}
	
    private void setupVault() {
    	if(getServer().getPluginManager().getPlugin("Vault") == null){
    		log.info(String.format("[%s] Vault not found. Permissions disabled.",getDescription().getName()));
    		return;
    	}
        RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) {
        	Perms.setPerm(permProvider.getProvider());
        }
    }
}
