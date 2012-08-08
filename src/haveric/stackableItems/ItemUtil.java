package haveric.stackableItems;

import org.bukkit.Material;

public class ItemUtil {

    public static boolean isTool(Material mat) {
        boolean isTool = false;

        switch(mat) {
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SPADE:
            case STONE_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SPADE:
            case IRON_SWORD:
            case GOLD_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SPADE:
            case GOLD_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_SWORD:
                isTool = true;
                break;

            default:
                break;
        }

        return isTool;
    }
    
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
    
    public static boolean isRepairable(Material mat){
        boolean repairable = false;
        
        if (isTool(mat) || isArmor(mat)) {
            repairable = true;
        } else {
            switch(mat) {
                case FISHING_ROD:
                case FLINT_AND_STEEL:
                case SHEARS:
                    repairable = true;
                    break;
                    
                default:
                    break;
            }
        }
        
        return repairable;
    }
}
