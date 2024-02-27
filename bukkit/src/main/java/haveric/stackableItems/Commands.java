package haveric.stackableItems;

import haveric.stackableItems.config.Config;
import haveric.stackableItems.config.FurnaceXPConfig;
import haveric.stackableItems.util.FurnaceUtil;
import haveric.stackableItems.util.SIItems;
import haveric.stackableItems.uuidFetcher.UUIDFetcher;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class Commands implements TabExecutor {

    private StackableItems plugin;

    private static String cmdMain = "stackableitems";
    private final String cmdMainAlt = "si";
    private final String cmdHelp = "help";
    private final String cmdReload = "reload";
    private final String cmdPerms = "perms";
    private final String cmdPermsAlt = "perm";
    private final String cmdUpdate = "update";

    private final String cmdTypeDefault = "default";
    private final String cmdTypePlayer = "player";
    private final String cmdTypeGroup = "group";
    private final String cmdTypeInventory = "inventory";

    private final ChatColor msgColor = ChatColor.DARK_AQUA;
    private final ChatColor highlightColor = ChatColor.YELLOW;
    private final ChatColor defaultColor = ChatColor.WHITE;

    private String shortTitle = msgColor + "[" + ChatColor.GRAY + "SI" + msgColor + "] ";

    public Commands(StackableItems si) {
        plugin = si;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";

        boolean op = false;
        if (sender.isOp()) {
            op = true;
        }

        boolean hasAdminPerm = false;
        if (sender instanceof Player player) {
            hasAdminPerm = Perms.hasAdmin(player);
        }

        if (label.equalsIgnoreCase(cmdMain) || label.equalsIgnoreCase(cmdMainAlt)) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(cmdHelp))) {
                sender.sendMessage(title + "github.com/haveric/StackableItems - v" + plugin.getDescription().getVersion());
                sender.sendMessage("Commands: " + highlightColor + "/" + cmdMain + defaultColor + " or " + highlightColor + "/" + cmdMainAlt);

                String item = highlightColor + " <item:data>";

                if (op || hasAdminPerm) {
                    item += " " + highlightColor + "[amount]";
                    sender.sendMessage("/" + cmdMainAlt + " " + cmdReload + " - " + msgColor + "Reloads the config files");
                    sender.sendMessage("/" + cmdMainAlt + " " + cmdUpdate + " - " + msgColor + "Checks if there's a new version available.");
                    sender.sendMessage("Get or set stack amounts: " + highlightColor + "< >" + defaultColor + " = required, " + highlightColor + "[ ]" + defaultColor + " = optional");
                } else {
                    sender.sendMessage("Get stack amounts: " + highlightColor + "< >" + defaultColor + " = required");
                }
                sender.sendMessage(" /" + cmdMainAlt + highlightColor + " <world> " + defaultColor + cmdTypeDefault + item);
                sender.sendMessage(" /" + cmdMainAlt + highlightColor + " <world> " + defaultColor + cmdTypePlayer + highlightColor + " <playerName>" + item);
                sender.sendMessage(" /" + cmdMainAlt + highlightColor + " <world> " + defaultColor + cmdTypeGroup + highlightColor + " <groupName>" + item);
                sender.sendMessage(" /" + cmdMainAlt + highlightColor + " <world> " + defaultColor + cmdTypeInventory + highlightColor + " <inventoryName>" + item);
                sender.sendMessage("Replace" + highlightColor + " <world> " + defaultColor + "with a world name or" + highlightColor + " all " + defaultColor + "for all worlds.");
                sender.sendMessage("Replace" + highlightColor + " <playerName> " + defaultColor + "with a player's name.");
                sender.sendMessage("Replace" + highlightColor + " <groupName> " + defaultColor + "with a permission group's name.");
                sender.sendMessage("Replace" + highlightColor + " <inventoryName> " + defaultColor + "with an inventory's name.");
                sender.sendMessage("Replace" + highlightColor + " <item:data> " + defaultColor + "with a Material name or id and an optional data value. Examples: apple, iron_pickaxe, iron_sword:0");

                if (op || hasAdminPerm) {
                    sender.sendMessage("(Optional) Replace" + highlightColor + " [amount] " + defaultColor + "with an integer to set the config.");
                }
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
                    sender.sendMessage(Perms.getPermAdmin() + " - " + msgColor + "Allows use of admin commands.");
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You must be an op or have admin perms to see permission nodes.");
                }
            } else if (args.length == 1 && (args[0].equalsIgnoreCase(cmdUpdate))) {
                if (op || hasAdminPerm) {
                    Updater.query(sender);
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You must be an op or have admin perms to check for updates.");
                }
            } else if (args.length >= 3) {
                String world = args[0];
                String permType = args[1].toLowerCase();

                int argsForSet;
                String item;
                String permItem = null;
                if (permType.equals("default")) {
                    argsForSet = 4;
                    item = args[2];
                    permItem = "";
                } else {
                    argsForSet = 5;
                    item = args[3];
                    if (permType.equals("player")) {
                        UUID uuid;
                        try {
                            uuid = UUIDFetcher.getUUIDOf(args[2]);
                            if (uuid != null) {
                                permItem = "" + uuid;
                            }
                        } catch (Exception e) { }
                    } else {
                        permItem = args[2];
                    }
                }

                if (world.equalsIgnoreCase("all") || world.equalsIgnoreCase("allworlds") || world.equalsIgnoreCase("default")) {
                    world = "allWorlds";
                }

                Material mat;
                short dur = -1;
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
                    int max;
                    String msg = shortTitle + highlightColor;
                    // set value
                    if (args.length == argsForSet) {
                        if (op || hasAdminPerm) {
                            int numToSet = Integer.parseInt(args[argsForSet - 1]);
                            String displayName;
                            if (dur == SIItems.ITEM_DEFAULT) {
                                displayName = mat.name();
                            } else {
                                displayName = mat.name() + ":" + dur;
                            }

                            msg += displayName + msgColor;

                            max = SIItems.getMax(world, permType, permItem,  mat, dur);
                            msg += " for " + highlightColor + world + defaultColor + "-" + highlightColor + permType + msgColor;
                            if (numToSet == max) {
                                msg += " is already set to ";
                            } else {
                                SIItems.setMax(world, permType, permItem, mat, dur, numToSet);
                                msg += " set to ";
                            }
                            sender.sendMessage(msg + highlightColor + numToSet);

                        } else {
                            sender.sendMessage(shortTitle + ChatColor.RED + "You do not have permission to set config values.");
                        }
                    // get value
                    } else {
                        msg += mat.name() + msgColor;

                        max = SIItems.getMax(world, permType, permItem, mat, dur);
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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        int argsLength = args.length;

        List<String> validWorlds = new ArrayList<>();
        validWorlds.add("all");
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            validWorlds.add(world.getName());
        }

        List<String> validOptions = new ArrayList<>();
        String searchString = "";
        if (argsLength == 1) {
            searchString = args[0].toLowerCase().trim();

            validOptions.add(cmdReload);
            validOptions.add(cmdUpdate);
            validOptions.addAll(validWorlds);
        } else if (argsLength > 1) {
            searchString = args[1].toLowerCase().trim();

            if (argsLength == 2 && validWorlds.contains(args[0].toLowerCase())) {
                validOptions.add(cmdTypeDefault);
                validOptions.add(cmdTypePlayer);
                validOptions.add(cmdTypeGroup);
                validOptions.add(cmdTypeInventory);
            } else if (argsLength == 3) {
                searchString = args[2].toLowerCase().trim();

                String args1Lower = args[1].toLowerCase();
                if (args1Lower.equals(cmdTypeInventory)) {
                    InventoryType[] inventories = InventoryType.values();
                    for (InventoryType inventoryType : inventories) {
                        validOptions.add(inventoryType.toString().toLowerCase());
                    }
                } else if (args1Lower.equals(cmdTypeGroup)) {
                    String[] groups = Perms.getGroups();
                    validOptions.addAll(Arrays.asList(groups));
                } else if (args1Lower.equals(cmdTypePlayer)) {
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    for (Player player : players) {
                        validOptions.add(player.getName());
                    }
                } else if (args1Lower.equals(cmdTypeDefault)) {
                    addItems(validOptions);
                }
            } else if (argsLength == 4) {
                searchString = args[3].toLowerCase().trim();

                String args1Lower = args[1].toLowerCase();
                if (args1Lower.equals(cmdTypeInventory) || args1Lower.equals(cmdTypeGroup) || args1Lower.equals(cmdTypePlayer)) {
                    addItems(validOptions);
                }
            }
        }

        for (String string : validOptions) {
            String lower = string.toLowerCase();
            if (lower.contains(searchString)) {
                list.add(string);
            }
        }

        return list;
    }

    private void addItems(List<String> validItems) {
        Material[] materials = Material.values();
        for (Material material : materials) {
            validItems.add(material.toString().toLowerCase());
        }
    }

    public static String getMain() {
        return cmdMain;
    }
}
