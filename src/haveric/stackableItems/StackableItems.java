package haveric.stackableItems;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StackableItems extends JavaPlugin {
	
	final Logger log = Logger.getLogger("Minecraft");
	private Commands commands = new Commands(this);
	
	protected final SIPlayerListener playerListener = new SIPlayerListener(this);

	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		// Register the plugin events
		pm.registerEvents(playerListener, this);
		
		Config.init(this);
		Config.setup();
		
		getCommand(Commands.getMain()).setExecutor(commands);
		log.info(String.format("[%s] v%s Started",getDescription().getName(), getDescription().getVersion()));
	}
	
	public void onDisable() {
		log.info(String.format("[%s] Disabled",getDescription().getName()));
	}
}
