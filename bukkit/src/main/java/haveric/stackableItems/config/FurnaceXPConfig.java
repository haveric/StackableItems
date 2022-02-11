package haveric.stackableItems.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.util.ItemUtil;

public class FurnaceXPConfig {

    private static StackableItems plugin;

    private static FileConfiguration cfgFurnaceXP;
    private static File cfgFurnaceXPFile;
    private static Random random;

    private FurnaceXPConfig() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;
        cfgFurnaceXPFile = new File(plugin.getDataFolder() + File.separator + "furnaceXP.yml");
        cfgFurnaceXP = YamlConfiguration.loadConfiguration(cfgFurnaceXPFile);

        random = new Random();
    }

    public static void reload() {
        try {
            cfgFurnaceXP.load(cfgFurnaceXPFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("furnaceXP.yml not found. Creating a new one");
            Config.saveConfig(cfgFurnaceXP, cfgFurnaceXPFile);
        } catch (IOException | InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setup() {
        cfgFurnaceXP.addDefault(Material.DIAMOND.name(), 1.0);
        cfgFurnaceXP.addDefault(Material.EMERALD.name(), 1.0);
        cfgFurnaceXP.addDefault(Material.GOLD_INGOT.name(), 1.0);
        cfgFurnaceXP.addDefault(Material.GREEN_DYE.name(), 1.0);

        cfgFurnaceXP.addDefault(Material.IRON_INGOT.name(), 0.7);
        cfgFurnaceXP.addDefault(Material.REDSTONE.name(), 0.7);

        cfgFurnaceXP.addDefault(Material.BAKED_POTATO.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_BEEF.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_CHICKEN.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_COD.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_SALMON.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_MUTTON.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_RABBIT.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.COOKED_PORKCHOP.name(), 0.35);
        cfgFurnaceXP.addDefault(Material.TERRACOTTA.name(), 0.35);

        cfgFurnaceXP.addDefault(Material.BRICK.name(), 0.3);

        cfgFurnaceXP.addDefault(Material.LAPIS_LAZULI.name(), 0.2);
        cfgFurnaceXP.addDefault(Material.QUARTZ.name(), 0.2);
        cfgFurnaceXP.addDefault(Material.LIME_DYE.name(), 0.2);

        cfgFurnaceXP.addDefault(Material.CHARCOAL.name(), 0.15);
        cfgFurnaceXP.addDefault(Material.SPONGE.name(), 0.15);

        cfgFurnaceXP.addDefault(Material.COAL.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.STONE.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.NETHER_BRICK.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.GLASS.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.SMOOTH_SANDSTONE.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.SMOOTH_RED_SANDSTONE.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.SMOOTH_STONE.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.SMOOTH_QUARTZ.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.CRACKED_STONE_BRICKS.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.DRIED_KELP.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.BLACK_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.BLUE_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.BROWN_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.CYAN_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.GRAY_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.GREEN_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.LIGHT_BLUE_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.LIGHT_GRAY_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.LIME_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.MAGENTA_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.ORANGE_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.PINK_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.PURPLE_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.RED_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.WHITE_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.YELLOW_GLAZED_TERRACOTTA.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.IRON_NUGGET.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.GOLD_NUGGET.name(), 0.1);
        cfgFurnaceXP.addDefault(Material.POPPED_CHORUS_FRUIT.name(), 0.1);

        if (!cfgFurnaceXP.isSet(Material.DIAMOND.name())
         || !cfgFurnaceXP.isSet(Material.EMERALD.name())
         || !cfgFurnaceXP.isSet(Material.GOLD_INGOT.name())
         || !cfgFurnaceXP.isSet(Material.GREEN_DYE.name())
         || !cfgFurnaceXP.isSet(Material.IRON_INGOT.name())
         || !cfgFurnaceXP.isSet(Material.REDSTONE.name())
         || !cfgFurnaceXP.isSet(Material.BAKED_POTATO.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_BEEF.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_CHICKEN.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_COD.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_SALMON.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_MUTTON.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_RABBIT.name())
         || !cfgFurnaceXP.isSet(Material.COOKED_PORKCHOP.name())
         || !cfgFurnaceXP.isSet(Material.TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.BRICK.name())
         || !cfgFurnaceXP.isSet(Material.LAPIS_LAZULI.name())
         || !cfgFurnaceXP.isSet(Material.QUARTZ.name())
         || !cfgFurnaceXP.isSet(Material.LIME_DYE.name())
         || !cfgFurnaceXP.isSet(Material.CHARCOAL.name())
         || !cfgFurnaceXP.isSet(Material.SPONGE.name())
         || !cfgFurnaceXP.isSet(Material.COAL.name())
         || !cfgFurnaceXP.isSet(Material.STONE.name())
         || !cfgFurnaceXP.isSet(Material.NETHER_BRICK.name())
         || !cfgFurnaceXP.isSet(Material.GLASS.name())
         || !cfgFurnaceXP.isSet(Material.SMOOTH_SANDSTONE.name())
         || !cfgFurnaceXP.isSet(Material.SMOOTH_RED_SANDSTONE.name())
         || !cfgFurnaceXP.isSet(Material.SMOOTH_STONE.name())
         || !cfgFurnaceXP.isSet(Material.SMOOTH_QUARTZ.name())
         || !cfgFurnaceXP.isSet(Material.CRACKED_STONE_BRICKS.name())
         || !cfgFurnaceXP.isSet(Material.DRIED_KELP.name())
         || !cfgFurnaceXP.isSet(Material.BLACK_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.BLUE_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.BROWN_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.CYAN_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.GRAY_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.GREEN_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.LIGHT_BLUE_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.LIGHT_GRAY_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.LIME_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.MAGENTA_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.ORANGE_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.PINK_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.PURPLE_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.RED_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.WHITE_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.YELLOW_GLAZED_TERRACOTTA.name())
         || !cfgFurnaceXP.isSet(Material.IRON_NUGGET.name())
         || !cfgFurnaceXP.isSet(Material.GOLD_NUGGET.name())
         || !cfgFurnaceXP.isSet(Material.POPPED_CHORUS_FRUIT.name()) ) {

            cfgFurnaceXP.options().copyDefaults(true);
            Config.saveConfig(cfgFurnaceXP, cfgFurnaceXPFile);
        }
    }

    private static int getXP(ItemStack item) {
        double xp;
        Material mat = item.getType();
        xp = cfgFurnaceXP.getDouble(mat + " " + ItemUtil.getDurability(item), 0.0);

        if (xp == 0.0) {
            xp = cfgFurnaceXP.getDouble("" + mat, 0.0);
        }
        double totalXp = xp * item.getAmount();
        int intPart = (int) totalXp;
        double fracPart = totalXp - intPart;
        if (Math.random() <= fracPart) {
            intPart++;
        }

        return intPart;
    }

    public static void giveFurnaceXP(Player player, ItemStack item) {
        int xp = getXP(item);
        if (xp > 0) {
            player.giveExp(xp);

            Sound expSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            player.playSound(player.getLocation(), expSound, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
}
