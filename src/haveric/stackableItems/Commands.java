package haveric.stackableItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor{

	StackableItems plugin;
	
	static String cmdMain = "stackableitems";
	static String cmdHelp = "help";
	static String cmdReload = "reload";
	
	public Commands(StackableItems ss){
		plugin = ss;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		ChatColor msgColor = ChatColor.DARK_AQUA;
		//ChatColor highlightColor = ChatColor.YELLOW;
		
		String title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";
		
		if (commandLabel.equalsIgnoreCase(cmdMain)){
			if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(cmdHelp))){
				sender.sendMessage(title+"github.com/haveric/StackableItems - v" + plugin.getDescription().getVersion());
				//sender.sendMessage("/" + cmdMain + " " + cmdReload + " - " + msgColor + "Reloads the config files");
				//sender.sendMessage("/" + cmdMain + " <player/group/default> item [amt] - " + msgColor + "Get/set a player/group's max items");
			// TODO: reload functionality
			}
			/*
			else if (args.length == 1 && args[0].equalsIgnoreCase(cmdReload)){
				
			} else if (args.length == 2 || args.length == 3){
				String fileString = "";
				if (args[0].equalsIgnoreCase("default")){
					fileString = "defaultItems.yml";
				} else {
					fileString = args[0];
				}
				
				Material.getMaterial(args[1]);
				
				// set value
				if (args.length == 3){
					int numToSet = Integer.parseInt(args[2]);
				// get value
				} else { 
					
				}
			}
			*/
		}
		return false;
	}
	
	public static String getMain() {
		return cmdMain;
	}

	public static void setMain(String cmd) {
		cmdMain = cmd;
	}

	public static String getHelp() {
		return cmdHelp;
	}

	public static void setHelp(String cmd) {
		cmdHelp = cmd;
	}
}
