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


    public Commands(StackableItems ss) {
        plugin = ss;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor msgColor = ChatColor.DARK_AQUA;
        ChatColor highlightColor = ChatColor.YELLOW;

        String title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";
        String shortTitle = msgColor + "[" + ChatColor.GRAY + "SI" + msgColor + "] ";

        boolean op = false;
        if (sender.isOp()) {
            op = true;
        }

        boolean canAdjust = false;
        if (Perms.getPerm().has(sender, Perms.getAdjustString())) {
            canAdjust = true;
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
            }

            else if (args.length == 2 || args.length == 3) {
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
                    int max = -1;
                    // set value
                    if (args.length == 3) {
                        if (op || canAdjust) {
                            int numToSet = Integer.parseInt(args[2]);
                            String displayName;
                            if (dur == -1) {
                                displayName = mat.name();
                            } else {
                                displayName = mat.name() + ":" + dur;
                            }

                            if (type.equals("default")) {
                                max = SIItems.getDefaultMax(mat, dur);
                                if (numToSet == max) {
                                    sender.sendMessage(shortTitle + highlightColor + displayName + msgColor + " for " + highlightColor + permType + msgColor + " is already set to " + highlightColor + numToSet);
                                } else {
                                    SIItems.setDefaultMax(mat, dur, numToSet);
                                    sender.sendMessage(shortTitle + highlightColor + displayName + msgColor + " for " + highlightColor + permType + msgColor + " set to " + highlightColor + numToSet);
                                }
                            } else if (type.equals("group") || type.equals("player")) {
                                max = SIItems.getMax(permType, mat, dur);
                                if (numToSet == max) {
                                    sender.sendMessage(shortTitle + highlightColor + displayName + msgColor + " for " + highlightColor + permType + msgColor + " is already set to " + highlightColor + numToSet);
                                } else {
                                    SIItems.setMax(permType, mat, dur, numToSet);
                                    sender.sendMessage(shortTitle + highlightColor + displayName + msgColor + " for " + highlightColor + permType + msgColor + " set to " + highlightColor + numToSet);
                                }
                            }
                        } else {
                            sender.sendMessage(shortTitle + ChatColor.RED + "You do not have permission to set config values.");
                        }
                    // get value
                    } else if (args.length == 2) {
                        if (type.equals("default")) {
                            max = SIItems.getDefaultMax(mat, dur);
                            if (max == -1) {
                                sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for Default. Vanilla value: " + highlightColor + mat.getMaxStackSize());
                            } else {
                                sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " for Default is: " + highlightColor + max);
                            }
                        } else if (type.equals("group")) {
                            max = SIItems.getMax(permType, mat, dur);
                            if (max == -1) {
                                max = SIItems.getDefaultMax(mat, dur);
                                if (max == -1) {
                                    sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for " + highlightColor + permType + msgColor + " or Default. Vanilla value: " + highlightColor + mat.getMaxStackSize());
                                } else {
                                    sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for " + highlightColor + permType + msgColor + ". Default value: " + highlightColor + max);
                                }
                            } else {
                                sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " for " + highlightColor + permType + msgColor + " is: " + highlightColor + max);
                            }
                        } else if (type.equals("player")) {
                            max = SIItems.getMax(permType, mat, dur);
                            if (max == -1) {
                                Player player = plugin.getServer().getPlayerExact(permType);
                                if (player != null) {
                                    String group = Perms.getPerm().getPrimaryGroup(player);
                                    max = SIItems.getMax(group, mat, dur);

                                    if (max == -1) {
                                        max = SIItems.getDefaultMax(mat, dur);
                                        if (max == -1) {
                                            sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for " + highlightColor + permType + msgColor + ", " + highlightColor + group + msgColor + " or Default. Vanilla value: " + highlightColor + mat.getMaxStackSize());
                                        } else {
                                            sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for " + highlightColor + permType + msgColor + " or " + highlightColor + group + msgColor + ". Default value: " + highlightColor + max);
                                        }
                                    } else {
                                        sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " not found for " + highlightColor + permType + msgColor + ". " + highlightColor + group + msgColor + " value: " + highlightColor + max);
                                    }
                                } else {
                                    sender.sendMessage(shortTitle + highlightColor + permType + msgColor + " does not exist.");
                                }
                            } else {
                                sender.sendMessage(shortTitle + highlightColor + mat.name() + msgColor + " for " + highlightColor + permType + msgColor + " is: " + highlightColor + max);
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
