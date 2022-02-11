package haveric.stackableItems.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemUtil {

    private ItemUtil() { } // Private constructor for utility class

    private static boolean isAxe(Material mat) {
        boolean isAxe = false;

        switch(mat) {
            case WOODEN_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
            case NETHERITE_AXE:
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
            case WOODEN_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case GOLDEN_HOE:
            case DIAMOND_HOE:
            case NETHERITE_HOE:
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
            case WOODEN_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
            case NETHERITE_PICKAXE:
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
            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case IRON_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
            case NETHERITE_SHOVEL:
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
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
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
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case NETHERITE_BOOTS:
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
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
            case ELYTRA:
                isChestplate = true;
                break;
            default:
                break;
        }
        return isChestplate;
    }

    public static boolean isChestplateEnchantable(Material mat) {
        boolean isChestplate = false;

        switch(mat) {
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
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
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case NETHERITE_HELMET:
            case TURTLE_HELMET:
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
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case NETHERITE_LEGGINGS:
                isLeggings = true;
                break;
            default:
                break;
        }
        return isLeggings;
    }

    public static boolean isOffhand(Material mat) {
        return mat == Material.SHIELD;
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

        if (isSword(mat) || mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT) {
            isWeapon = true;
        }

        return isWeapon;
    }

    public static boolean isArmor(Material mat) {
        boolean isArmor = false;

        if (isBoots(mat) || isChestplate(mat) || isHelmet(mat) || isLeggings(mat) || isOffhand(mat)) {
            isArmor = true;
        }

        return isArmor;
    }

    public static boolean isArmorEnchantable(Material mat) {
        boolean isArmor = false;

        if (isBoots(mat) || isChestplateEnchantable(mat) || isHelmet(mat) || isLeggings(mat)) {
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
                case CARROT_ON_A_STICK:
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
        if (isWeapon(mat) || isArmorEnchantable(mat) || isPickaxe(mat) || isShovel(mat) || isAxe(mat)) {
            enchantable = true;
        } else {
            switch(mat) {
                case BOOK:
                case FISHING_ROD:
                case SHEARS:
                case CARROT_ON_A_STICK:
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

    public static boolean isShulkerBox(Material mat) {
        boolean isShulkerBox = false;

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
                isShulkerBox = true;
                break;
            default:
                break;
        }

        return isShulkerBox;
    }

    public static boolean isSameItem(ItemStack one, ItemStack two) {
        return isSameItem(one, two, false);
    }

    public static boolean isSameItem(ItemStack one, ItemStack two, boolean negativeDurAllowed) {
        boolean same = false;

        if (one != null && two != null) {
            boolean sameType = one.getType() == two.getType();
            boolean sameDur = ItemUtil.getDurability(one) == ItemUtil.getDurability(two);
            boolean negativeDur = (ItemUtil.getDurability(one) == Short.MAX_VALUE) || (ItemUtil.getDurability(two) == Short.MAX_VALUE);

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

            if (sameType && (sameDur || (negativeDurAllowed && negativeDur)) && (sameEnchant || noEnchant) && (sameMeta || noMeta)) {
                same = true;
            }
        }
        return same;
    }

    public static int getDurability(ItemMeta itemMetadata) {
        // Max damage/durability can be obtained via Material#getMaxDurability()
        if (itemMetadata instanceof Damageable) {
            Damageable damageableItem = (Damageable)itemMetadata;
            if (!damageableItem.hasDamage()) {
                return -1;
            }
            return damageableItem.getDamage();
        }
        return -1;
    }

    public static int getDurability(ItemStack itemStack) {
        return getDurability(itemStack.getItemMeta());
    }

    public static void setDurability(ItemStack itemStack, int durability) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof Damageable)) {
            throw new IllegalArgumentException("Item type " + itemStack.getType().name() + " cannot have damage set");
        }
        Damageable damageableItem = (Damageable)itemMeta;
        damageableItem.setDamage(durability);
        itemStack.setItemMeta(itemMeta);
    }
}
