package haveric.stackableItems.util;

import org.bukkit.Sound;

public class SoundUtil {
    public static Sound getSound(String newSound, String oldSound) {
        Sound sound = null;

        if (Version.has19Support()) {
            // set known sounds to make sure Enum isn't changing on us
            if (newSound.equals("ENTITY_EXPERIENCE_ORB_PICKUP")) {
                sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            } else if (newSound.equals("ENTITY_ITEM_PICKUP")) {
                sound = Sound.ENTITY_ITEM_PICKUP;
            } else {
                sound = Sound.valueOf(newSound);
            }
        } else {
            try {
                sound = Sound.valueOf(oldSound);
            } catch (IllegalArgumentException e2) {
                // Sound is missing
            }
        }

        return sound;
    }
}
