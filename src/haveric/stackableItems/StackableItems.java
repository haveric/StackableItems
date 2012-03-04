package haveric.stackableItems;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the <b>Stackable</b> plugin for Bukkit.
 * <p>
 * This plugin provide you the ability to choose the stack size of any item in 
 * the game. You have the possibility to increase a stack size, reduce it or 
 * even disable it. Also, set a stack size to zero removed completely any 
 * stack of this item from the inventory.
 *
 * @author DjDCH
 */
public class StackableItems extends JavaPlugin {
	
	final Logger log = Logger.getLogger("Minecraft");
	
	String pluginTitle;
	
	/** Contains the StackablePlayerListener instance. */
	protected final SIPlayerListener playerListener = new SIPlayerListener(this);

	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		ChatColor msgColor = ChatColor.DARK_AQUA;
		
		pluginTitle = msgColor + "[" + ChatColor.GRAY + getDescription().getName() + msgColor + "] ";
		
		// Register the plugin events
		pm.registerEvents(playerListener, this);
		
		Config.init(this);
		Config.setup();
		
		log.info(String.format("[%s] v%s Started",getDescription().getName(), getDescription().getVersion()));
	}
	
	/**
	 * Method execute when the plugin is disable.
	 */
	public void onDisable() {
		log.info(String.format("[%s] Disabled",getDescription().getName()));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {		
		String commandName = command.getName().toLowerCase();
		
		if (commandName.equals("stackable")) {
			if (!(sender instanceof Player)) {
				log.info("Unknown console command. Type \"help\" for help.");
				return true;
			}
		}
		return false;
	}
}
