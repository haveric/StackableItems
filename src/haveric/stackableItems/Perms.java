package haveric.stackableItems;

import haveric.stackableItems.config.Config;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

public final class Perms {

    private static Permission perm = null;
    private static StackableItems plugin = null;

    private static String admin = "stackableitems.admin";

    private Perms() { } // Private constructor for utility class

    public static void init(StackableItems si, Permission p) {
        plugin = si;
        perm = p;
    }

    private static boolean permEnabled() {
        return (perm != null);
    }

    public static boolean groupExists(String group) {
        boolean groupExists = false;

        if (permEnabled()) {
            try {
                for (String g : perm.getGroups()) {
                    if (g.equals(group)) {
                        groupExists = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // No groups
                if (Config.isDebugging()) {
                    plugin.log.warning("DEBUG: groupExists() - No group found.");
                }
            }
        }
        return groupExists;
    }

    public static boolean hasAdmin(Player player) {
        return player.hasPermission(admin);
    }

    public static String[] getPlayerGroups(Player player) {
        String[] groups = null;

        if (permEnabled()) {
            try {
                groups = perm.getPlayerGroups(player);
            } catch (Exception e) {
                // No groups
                if (Config.isDebugging()) {
                    plugin.log.warning("DEBUG: getPlayerGroups() - No group found.");
                }
            }
        }

        return groups;
    }

    public static String[] getGroups() {
        String[] groups = null;

        if (permEnabled()) {
            try {
                groups = perm.getGroups();
            } catch (Exception e) {
                // No groups
                if (Config.isDebugging()) {
                    plugin.log.warning("DEBUG: getgroups() - No group found.");
                }
            }
        }
        return groups;
    }

    public static String getPermAdmin() {
        return admin;
    }
}
