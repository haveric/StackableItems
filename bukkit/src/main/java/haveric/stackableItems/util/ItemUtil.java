package haveric.stackableItems.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemUtil {

    private ItemUtil() { } // Private constructor for utility class

    private static boolean isAxe(Material mat) {
        switch(mat) {
            case WOODEN_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
            case NETHERITE_AXE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isHoe(Material mat) {
        switch(mat) {
            case WOODEN_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case GOLDEN_HOE:
            case DIAMOND_HOE:
            case NETHERITE_HOE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isPickaxe(Material mat) {
        switch(mat) {
            case WOODEN_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
            case NETHERITE_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isShovel(Material mat) {
        switch(mat) {
            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case IRON_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
            case NETHERITE_SHOVEL:
                return true;
            default:
                return false;
        }
    }

    private static boolean isSword(Material mat) {
        switch(mat) {
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBoots(Material mat) {
        switch(mat) {
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
            case IRON_BOOTS:
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case NETHERITE_BOOTS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isChestplate(Material mat) {
        switch(mat) {
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
            case ELYTRA:
                return true;
            default:
                return false;
        }
    }

    public static boolean isChestplateEnchantable(Material mat) {
        switch(mat) {
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isHelmet(Material mat) {
        switch(mat) {
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case NETHERITE_HELMET:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLeggings(Material mat) {
        switch(mat) {
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case NETHERITE_LEGGINGS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOffhand(Material mat) {
        return mat == Material.SHIELD;
    }

    public static boolean isTool(Material mat) {
        return isAxe(mat) || isHoe(mat) || isPickaxe(mat) || isShovel(mat);
    }

    public static boolean isWeapon(Material mat) {
        return isSword(mat) || mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT;
    }

    public static boolean isArmor(Material mat) {
        return isBoots(mat) || isChestplate(mat) || isHelmet(mat) || isLeggings(mat) || isOffhand(mat);
    }

    public static boolean isArmorEnchantable(Material mat) {
        return isBoots(mat) || isChestplateEnchantable(mat) || isHelmet(mat) || isLeggings(mat);
    }

    public static boolean isRepairable(Material mat) {
        if (isTool(mat) || isWeapon(mat) || isArmor(mat)) {
            return true;
        } else {
            switch(mat) {
                case FISHING_ROD:
                case FLINT_AND_STEEL:
                case SHEARS:
                case CARROT_ON_A_STICK:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static boolean isEnchantable(Material mat) {
        if (isWeapon(mat) || isArmorEnchantable(mat) || isPickaxe(mat) || isShovel(mat) || isAxe(mat)) {
            return true;
        } else {
            switch(mat) {
                case BOOK:
                case FISHING_ROD:
                case SHEARS:
                case CARROT_ON_A_STICK:
                    return true;
                default:
                    return false;
            }
        }
    }


    public static boolean isBrewingIngredient(Material mat) {
        switch(mat) {
            case BLAZE_POWDER:
            case FERMENTED_SPIDER_EYE:
            case GHAST_TEAR:
            case GLISTERING_MELON_SLICE:
            case GLOWSTONE_DUST:
            case GOLDEN_CARROT:
            case GUNPOWDER:
            case MAGMA_CREAM:
            case NETHER_WART:
            case PHANTOM_MEMBRANE:
            case PUFFERFISH:
            case RABBIT_FOOT:
            case REDSTONE:
            case SPIDER_EYE:
            case SUGAR:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBeaconFuel(Material mat) {
        switch(mat) {
            case DIAMOND:
            case EMERALD:
            case IRON_INGOT:
            case GOLD_INGOT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isShulkerBox(Material mat) {
        switch(mat) {
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSameItem(ItemStack one, ItemStack two) {
        return isSameItem(one, two, false);
    }

    public static boolean isSameItem(ItemStack one, ItemStack two, boolean negativeDurAllowed) {
        if (one != null && two != null) {
            boolean sameType = one.getType() == two.getType();
            boolean sameDur = one.getDurability() == two.getDurability();
            boolean negativeDur = (one.getDurability() == Short.MAX_VALUE) || (two.getDurability() == Short.MAX_VALUE);

            boolean sameEnchant = false;
            boolean noEnchant = one.getEnchantments().isEmpty() && two.getEnchantments().isEmpty();
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

            return sameType 
                && (sameDur || (negativeDurAllowed && negativeDur)) 
                && (sameEnchant || noEnchant) 
                && (sameMeta || noMeta);
        }
        return false;
    }
}
