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
    private static String cmdMainAlt = "si";
    private static String cmdHelp = "help";
    private static String cmdReload = "reload";

    private ChatColor msgColor = ChatColor.DARK_AQUA;
    private ChatColor highlightColor = ChatColor.YELLOW;

    private String title;
    private String shortTitle = msgColor + "[" + ChatColor.GRAY + "SI" + msgColor + "] ";

    public Commands(StackableItems si) {
        plugin = si;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";

        boolean op = false;
        if (sender.isOp()) {
            op = true;
        }

        boolean canAdjust = false;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            canAdjust = Perms.canAdjust(player);
        }

        if (commandLabel.equalsIgnoreCase(cmdMain) || commandLabel.equalsIgnoreCase(cmdMainAlt)) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(cmdHelp))) {
                sender.sendMessage(title + "github.com/haveric/StackableItems - v" + plugin.getDescription().getVersion());

                if (op || canAdjust) {
                    sender.sendMessage("/" + cmdMain + " " + cmdReload + " - " + msgColor + "Reloads the config files");
                    sender.sendMessage("/" + cmdMain + " <player/group/default> item:dur [amt] - " + msgColor + "Get/set a player/group's max items");
                } else {
                    sender.sendMessage("/" + cmdMain + " <player/group/default> item:dur - " + msgColor + "Get a player/group's max items");
                }

            } else if (args.length == 1 && args[0].equalsIgnoreCase(cmdReload)) {
                if (op || canAdjust) {
                    Config.reload();
                    SIItems.reload();
                    FurnaceUtil.reload();
                    sender.sendMessage(title + "Configuration files reloaded.");
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You do not have permission to reload the config.");
                }
            } else if (args.length == 2 || args.length == 3) {
                String type;
                String permType = args[0];

                if (permType.equalsIgnoreCase("default") || permType.equalsIgnoreCase("defaultitems")) {
                    type = "default";
                } else {
                    if (Perms.groupExists(permType)) {
                        type = "groupOrPlayer";
                    } else {
                        type = "player";
                    }
                }
                Material mat;
                short dur = -1;
                String item = args[1];
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
                    if (args.length == 3) {

                        if (op || canAdjust) {
                            int numToSet = Integer.parseInt(args[2]);
                            String displayName;
                            if (dur == SIItems.DUR_MATCH_ANY) {
                                displayName = mat.name();
                            } else {
                                displayName = mat.name() + ":" + dur;
                            }

                            msg += displayName + msgColor;

                            if (type.equals("default")) {
                                max = SIItems.getDefaultMax(mat, dur);
                                msg += " for " + highlightColor + permType + msgColor;
                                if (numToSet == max) {
                                    msg += " is already set to ";
                                } else {
                                    SIItems.setDefaultMax(mat, dur, numToSet);
                                    msg += " set to ";
                                }
                                sender.sendMessage(msg + highlightColor + numToSet);
                            } else { // group or player
                                max = SIItems.getMax(permType, mat, dur);
                                msg += " for " + highlightColor + permType + msgColor;
                                if (numToSet == max) {
                                    msg += " is already set to ";
                                } else {
                                    SIItems.setMax(permType, mat, dur, numToSet);
                                    msg += " set to ";
                                }
                                sender.sendMessage(msg + highlightColor + numToSet);
                            }
                        } else {
                            sender.sendMessage(shortTitle + ChatColor.RED + "You do not have permission to set config values.");
                        }
                    // get value
                    } else if (args.length == 2) {
                        msg += mat.name() + msgColor;
                        if (type.equals("default")) {
                            max = SIItems.getDefaultMax(mat, dur);
                            if (max == SIItems.ITEM_DEFAULT) {
                                msg += " not found for Default. Vanilla value: " + highlightColor + mat.getMaxStackSize();
                            } else {
                                msg += " for Default is: " + highlightColor + max;
                            }
                            sender.sendMessage(msg);
                        } else if (type.equals("group")) {
                            max = SIItems.getMax(permType, mat, dur);
                            if (max == SIItems.ITEM_DEFAULT) {
                                max = SIItems.getDefaultMax(mat, dur);
                                msg += " not found for " + highlightColor + permType + msgColor;
                                if (max == SIItems.ITEM_DEFAULT) {
                                    msg += " or Default. Vanilla value: " + highlightColor + mat.getMaxStackSize();
                                } else {
                                    msg += ". Default value: " + highlightColor + max;
                                }
                                sender.sendMessage(msg);
                            } else {
                                sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " for " + highlightColor + permType + msgColor + " is: " + highlightColor + max);
                            }
                        } else { // player
                            max = SIItems.getMax(permType, mat, dur);
                            if (max == SIItems.ITEM_DEFAULT) {
                                Player player = plugin.getServer().getPlayerExact(permType);
                                if (player == null) {
                                    sender.sendMessage(msg + " does not exist.");
                                } else {
                                    String group = Perms.getPrimaryGroup(player);
                                    if (group == null) {
                                        msg += " does not exist.";
                                    } else {
                                        max = SIItems.getMax(group, mat, dur);

                                        msg += " not found for " + highlightColor + permType + msgColor;
                                        if (max == -SIItems.ITEM_DEFAULT) {
                                            max = SIItems.getDefaultMax(mat, dur);

                                            if (max == SIItems.ITEM_DEFAULT) {
                                                msg += ", " + highlightColor + group + msgColor + " or Default. Vanilla value: " + highlightColor + mat.getMaxStackSize();
                                            } else {
                                                msg += " or " + highlightColor + group + msgColor + ". Default value: " + highlightColor + max;
                                            }

                                        } else {
                                            msg += ". " + highlightColor + group + msgColor + " value: " + highlightColor + max;
                                        }
                                    }
                                    sender.sendMessage(msg);
                                }
                            } else {
                                sender.sendMessage(msg + " for " + highlightColor + permType + msgColor + " is: " + highlightColor + max);
                            }
                        }
                    }
                }
            }
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
