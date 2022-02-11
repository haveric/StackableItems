package haveric.stackableItems.util;

public class StringUtil {

    public static boolean equalsAnyIgnoreCase(String string, String... others) {
        if (string == null) {
            string = "";
        }
        for (String other : others) {
            if (string.equalsIgnoreCase(other)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsAny(String string, String... others) {
        if (string == null) {
            string = "";
        }
        for (String other : others) {
            if (string.equals(other)) {
                return true;
            }
        }
        return false;
    }
}
