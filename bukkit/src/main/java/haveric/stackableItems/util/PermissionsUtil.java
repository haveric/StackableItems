package haveric.stackableItems.util;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

public final class PermissionsUtil {

    private static Permission provider = null;
    private static StackableItems plugin = null;

    private static String admin = "stackableitems.admin";

    private PermissionsUtil() { } // Private constructor for utility class

    public static void init(StackableItems plugin, Permission provider) {
        PermissionsUtil.plugin = plugin;
        PermissionsUtil.provider = provider;
    }

    private static boolean isEnabled() {
        return (provider != null);
    }

    public static boolean groupExists(String group) {
        boolean groupExists = false;

        if (isEnabled()) {
            try {
                for (String g : provider.getGroups()) {
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

        if (isEnabled()) {
            try {
                groups = provider.getPlayerGroups(player);
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

        if (isEnabled()) {
            try {
                groups = provider.getGroups();
            } catch (Exception e) {
                // No groups
                if (Config.isDebugging()) {
                    plugin.log.warning("DEBUG: getgroups() - No group found.");
                }
            }
        }
        return groups;
    }

    public static String getAdminPermission() {
        return admin;
    }
}
