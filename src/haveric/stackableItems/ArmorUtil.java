package haveric.stackableItems;

import org.bukkit.Material;

public class ArmorUtil {
    
    public static boolean isArmor(Material mat) {
        boolean isArmor = false;

        switch(mat) {
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case GOLD_BOOTS:
            case GOLD_CHESTPLATE:
            case GOLD_HELMET:
            case GOLD_LEGGINGS:
                isArmor = true;
                break;

            default:
                break;
        }

        return isArmor;
    }
}
