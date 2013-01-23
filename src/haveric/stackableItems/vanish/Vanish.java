package haveric.stackableItems.vanish;

import haveric.stackableItems.Perms;

import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

public class Vanish {

    private static VanishPlugin vanish = null;

    private Vanish() { } // Private constructor for utility class

    public static void setVanish(VanishPlugin newVanish) {
        vanish = newVanish;
    }

    private static boolean vanishEnabled() {
        return (vanish != null);
    }

    private static boolean isVanished(String playerName) {
        boolean vanished = false;

        if (vanishEnabled()) {
            VanishManager manager = vanish.getManager();
            if (manager != null) {
                vanished = manager.isVanished(playerName);
            }
        }

        return vanished;
    }

    public static boolean isPickupDisabled(Player player) {
        boolean pickupDisabled = false;

        if (isVanished(player.getName())) {
            pickupDisabled = Perms.canVanishPickup(player);
        }

        return pickupDisabled;
    }
}
