package haveric.stackableItems;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

public final class Perms {

    private static Permission perm = null;
    private static StackableItems plugin = null;

    private static String stack = "stackableitems.stack";
    private static String adjust = "stackableitems.adjust";

    // Vanish nopickup
    private static String vanishNoPickup = "vanish.nopickup";

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

    public static boolean canStackInGroup(Player player) {
        return player.hasPermission(stack);
    }

    public static boolean canAdjust(Player player) {
        return player.hasPermission(adjust);
    }

    public static String getPrimaryGroup(Player player) {
        String primaryGroup = null;

        if (permEnabled()) {
            try {
                primaryGroup = perm.getPrimaryGroup(player);
            } catch (Exception e) {
                // No groups
                if (Config.isDebugging()) {
                    plugin.log.warning("DEBUG: getPrimaryGroup() - No group found.");
                }
            }
        }
        return primaryGroup;
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

    public static boolean canVanishPickup(Player player) {
        return player.hasPermission(vanishNoPickup);
    }

}
