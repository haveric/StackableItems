package haveric.stackableItems.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemUtil {

    private ItemUtil() { } // Private constructor for utility class

    private static boolean isAxe(Material mat) {
        boolean isAxe = false;

        switch(mat) {
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:
                isAxe = true;
                break;
            default:
                break;
        }
        return isAxe;
    }

    private static boolean isHoe(Material mat) {
        boolean isHoe = false;

        switch(mat) {
            case WOOD_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case GOLD_HOE:
            case DIAMOND_HOE:
                isHoe = true;
                break;
            default:
                break;
        }
        return isHoe;
    }

    private static boolean isPickaxe(Material mat) {
        boolean isPickaxe = false;

        switch(mat) {
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
                isPickaxe = true;
                break;
            default:
                break;
        }
        return isPickaxe;
    }

    private static boolean isShovel(Material mat) {
        boolean isShovel = false;

        switch(mat) {
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE:
                isShovel = true;
                break;
            default:
                break;
        }
        return isShovel;
    }

    private static boolean isSword(Material mat) {
        boolean isSword = false;

        switch(mat) {
            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
                isSword = true;
                break;
            default:
                break;
        }
        return isSword;
    }

    public static boolean isBoots(Material mat) {
        boolean isBoots = false;

        switch(mat) {
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case DIAMOND_BOOTS:
                isBoots = true;
                break;
            default:
                break;
        }
        return isBoots;
    }

    public static boolean isChestplate(Material mat) {
        boolean isChestplate = false;

        switch(mat) {
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
                isChestplate = true;
                break;
            default:
                break;
        }
        return isChestplate;
    }

    public static boolean isHelmet(Material mat) {
        boolean isHelmet = false;

        switch(mat) {
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case DIAMOND_HELMET:
                isHelmet = true;
                break;
            default: break;
        }
        return isHelmet;
    }

    public static boolean isLeggings(Material mat) {
        boolean isLeggings = false;

        switch(mat) {
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case DIAMOND_LEGGINGS:
                isLeggings = true;
                break;
            default:
                break;
        }
        return isLeggings;
    }

    public static boolean isTool(Material mat) {
        boolean isTool = false;

        if (isAxe(mat) || isHoe(mat) || isPickaxe(mat) || isShovel(mat)) {
            isTool = true;
        }

        return isTool;
    }

    public static boolean isWeapon(Material mat) {
        boolean isWeapon = false;

        if (isSword(mat) || mat == Material.BOW) {
            isWeapon = true;
        }

        return isWeapon;
    }

    public static boolean isArmor(Material mat) {
        boolean isArmor = false;

        if (isBoots(mat) || isChestplate(mat) || isHelmet(mat) || isLeggings(mat)) {
            isArmor = true;
        }

        return isArmor;
    }

    public static boolean isRepairable(Material mat) {
        boolean repairable = false;

        if (isTool(mat) || isWeapon(mat) || isArmor(mat)) {
            repairable = true;
        } else {
            switch(mat) {
                case FISHING_ROD:
                case FLINT_AND_STEEL:
                case SHEARS:
                case CARROT_STICK:
                    repairable = true;
                    break;
                default:
                    break;
            }
        }

        return repairable;
    }

    public static boolean isEnchantable(Material mat) {
        boolean enchantable = false;
        if (isWeapon(mat) || isArmor(mat) || isPickaxe(mat) || isShovel(mat) || isAxe(mat)) {
            enchantable = true;
        } else {
            switch(mat) {
                case BOOK:
                case FISHING_ROD:
                    enchantable = true;
                    break;
                default:
                    break;
            }
        }

        return enchantable;
    }


    public static boolean isBrewingIngredient(Material mat) {
        boolean brewingIngredient = false;

        switch(mat) {
            case REDSTONE:
            case NETHER_WARTS:
            case GLOWSTONE_DUST:
            case FERMENTED_SPIDER_EYE:
            case MAGMA_CREAM:
            case SUGAR:
            case SPECKLED_MELON:
            case SPIDER_EYE:
            case GHAST_TEAR:
            case BLAZE_POWDER:
            case SULPHUR:
            case GOLDEN_CARROT:
                brewingIngredient = true;
                break;
            default:
                break;
        }
        return brewingIngredient;
    }

    public static boolean isBeaconFuel(Material mat) {
        boolean beaconFuel = false;

        switch(mat) {
            case DIAMOND:
            case EMERALD:
            case IRON_INGOT:
            case GOLD_INGOT:
                beaconFuel = true;
                break;
            default:
                break;
        }
        return beaconFuel;
    }

    public static boolean isSameItem(ItemStack one, ItemStack two) {
        return isSameItem(one, two, false);
    }

    public static boolean isSameItem(ItemStack one, ItemStack two, boolean negativeDurAllowed) {
        boolean same = false;

        if (one != null && two != null) {
            boolean sameType = one.getType() == two.getType();
            boolean sameDur = one.getDurability() == two.getDurability();
            boolean negativeDur = (one.getDurability() == Short.MAX_VALUE) || (two.getDurability() == Short.MAX_VALUE);

            boolean sameEnchant = false;
            boolean noEnchant = one.getEnchantments() == null && two.getEnchantments() == null;
            if (!noEnchant) {
                sameEnchant = one.getEnchantments().equals(two.getEnchantments());
            }

            boolean sameMeta = false;
            boolean noMeta = one.getItemMeta() == null && two.getItemMeta() == null;

            if (!noMeta) {
                // Handles an empty slot being compared
                if (one.getItemMeta() == null || two.getItemMeta() == null) {
                    sameMeta = false;
                } else {
                    sameMeta = one.getItemMeta().equals(two.getItemMeta());
                }
            }

            if (sameType && (sameDur || (negativeDurAllowed && negativeDur)) && (sameEnchant || noEnchant) && (sameMeta || noMeta)) {
                same = true;
            }
        }
        return same;
    }
}
