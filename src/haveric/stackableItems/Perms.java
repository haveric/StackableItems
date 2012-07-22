package haveric.stackableItems;

import net.milkbowl.vault.permission.Permission;

public class Perms {

    private static Permission perm = null;

    // TODO: make dynamic?
    private static String stack = "stackableitems.stack";

    private static String item = "stackableitems.item";

    public static void setPerm(Permission p) {
        perm = p;
    }

    public static Permission getPerm() {
        return perm;
    }

    public static boolean permEnabled() {
        return (perm != null);
    }

    public static void setStack(String newPerm) {
        stack = newPerm;
    }

    public static String getStackString() {
        return stack;
    }

    public static String getItemString() {
        return item;
    }

    public static boolean groupExists(String group) {
        for (String g : perm.getGroups()) {
            if (g.equals(group)) {
                return true;
            }
        }
        return false;
    }
}
