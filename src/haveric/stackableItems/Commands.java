package haveric.stackableItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private StackableItems plugin;

    private static String cmdMain = "stackableitems";
    private String cmdMainAlt = "si";
    private String cmdHelp = "help";
    private String cmdReload = "reload";
    private String cmdPerms = "perms";
    private String cmdPermsAlt = "perm";

    private ChatColor msgColor = ChatColor.DARK_AQUA;
    private ChatColor highlightColor = ChatColor.YELLOW;
    private ChatColor defaultColor = ChatColor.WHITE;

    private String shortTitle = msgColor + "[" + ChatColor.GRAY + "SI" + msgColor + "] ";

    public Commands(StackableItems si) {
        plugin = si;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";

        boolean op = false;
        if (sender.isOp()) {
            op = true;
        }

        boolean hasAdminPerm = false;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            hasAdminPerm = Perms.hasAdmin(player);
        }

        if (commandLabel.equalsIgnoreCase(cmdMain) || commandLabel.equalsIgnoreCase(cmdMainAlt)) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(cmdHelp))) {
                sender.sendMessage(title + "github.com/haveric/StackableItems - v" + plugin.getDescription().getVersion());
                sender.sendMessage("Commands: " + highlightColor + "/" + cmdMain + defaultColor + " or " + highlightColor + "/" + cmdMainAlt);
                if (op || hasAdminPerm) {
                    sender.sendMessage("/" + cmdMainAlt + " " + cmdReload + " - " + msgColor + "Reloads the config files");
                    sender.sendMessage("/" + cmdMainAlt + highlightColor + " world type" + defaultColor + " item:dur [amt] - " + msgColor + "Get/set a player's max items");
                } else {
                    sender.sendMessage("/" + cmdMainAlt + highlightColor + " world type" + defaultColor + " item:dur - " + msgColor + "Get a player's max items");
                }
                sender.sendMessage("Types: " + highlightColor + "playerName" + defaultColor + ", " + highlightColor + "inventoryName" + defaultColor + ", " + highlightColor + "permissionGroup" + defaultColor + ", " + highlightColor + "default");

            } else if (args.length == 1 && args[0].equalsIgnoreCase(cmdReload)) {
                if (op || hasAdminPerm) {
                    Config.reload();
                    SIItems.reload();
                    FurnaceUtil.reload();
                    FurnaceXPConfig.reload();
                    sender.sendMessage(title + "Configuration files reloaded.");
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You do not have permission to reload the config.");
                }
            } else if (args.length == 1 && (args[0].equalsIgnoreCase(cmdPerms) || args[0].equalsIgnoreCase(cmdPermsAlt))) {
                if (op || hasAdminPerm) {
                    sender.sendMessage(title + "Permission nodes:");
                    sender.sendMessage(Perms.getPermStack() + " - " + msgColor + "Allows a permission group to stack items.");
                    sender.sendMessage(Perms.getPermAdmin() + " - " + msgColor + "Allows use of admin commands.");
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You must be an op or have admin perms to see permission nodes.");
                }
            } else if (args.length == 3 || args.length == 4) {
                String world = args[0];
                String permType = args[1];

                if (world.equalsIgnoreCase("all") || world.equalsIgnoreCase("allworlds") || world.equalsIgnoreCase("default")) {
                    world = "allWorlds";
                }

                if (permType.equalsIgnoreCase("default")) {
                    permType = "default";
                }

                Material mat;
                short dur = -1;
                String item = args[2];
                String matName;

                if (item.contains(":")) {
                    String[] itemDur = item.split(":");
                    matName = itemDur[0];
                    mat = Material.matchMaterial(matName);
                    dur = (short) Integer.parseInt(itemDur[1]);
                } else {
                    matName = item;
                    mat = Material.matchMaterial(matName);
                }

                if (mat == null) {
                    sender.sendMessage(shortTitle + "No material found matching " + highlightColor + matName);
                } else {
                    int max = SIItems.ITEM_DEFAULT;
                    String msg = shortTitle + highlightColor;
                    // set value
                    if (args.length == 4) {
                        if (op || hasAdminPerm) {
                            int numToSet = Integer.parseInt(args[3]);
                            String displayName;
                            if (dur == SIItems.ITEM_DEFAULT) {
                                displayName = mat.name();
                            } else {
                                displayName = mat.name() + ":" + dur;
                            }

                            msg += displayName + msgColor;


                            max = SIItems.getMax(world + "." + permType, mat, dur);
                            msg += " for " + highlightColor + world + defaultColor + "-" + highlightColor + permType + msgColor;
                            if (numToSet == max) {
                                msg += " is already set to ";
                            } else {
                                SIItems.setMax(world + "." + permType, mat, dur, numToSet);
                                msg += " set to ";
                            }
                            sender.sendMessage(msg + highlightColor + numToSet);

                        } else {
                            sender.sendMessage(shortTitle + ChatColor.RED + "You do not have permission to set config values.");
                        }
                    // get value
                    } else if (args.length == 3) {
                        msg += mat.name() + msgColor;

                        max = SIItems.getMax(world + "." + permType, mat, dur);
                        if (max == SIItems.ITEM_DEFAULT) {
                            msg += " not found for " + highlightColor + world + defaultColor + "-" + highlightColor + permType;
                        } else {
                            msg += " for " + world + "-" + permType + " is: " + highlightColor + max;
                        }
                        sender.sendMessage(msg);

                    }
                }
            }
        }
        return false;
    }

    public static String getMain() {
        return cmdMain;
    }
}
