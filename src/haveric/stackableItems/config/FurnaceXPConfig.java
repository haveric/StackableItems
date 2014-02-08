package haveric.stackableItems.config;

import haveric.stackableItems.StackableItems;

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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setup() {
        cfgFurnaceXP.addDefault("DIAMOND", 1.0);
        cfgFurnaceXP.addDefault("EMERALD", 1.0);
        cfgFurnaceXP.addDefault("GOLD_INGOT", 1.0);

        cfgFurnaceXP.addDefault("IRON_INGOT", 0.7);
        cfgFurnaceXP.addDefault("REDSTONE", 0.7);

        cfgFurnaceXP.addDefault("HARD_CLAY", 0.35);
        cfgFurnaceXP.addDefault("BAKED_POTATO", 0.35);
        cfgFurnaceXP.addDefault("COOKED_BEEF", 0.35);
        cfgFurnaceXP.addDefault("COOKED_CHICKEN", 0.35);
        cfgFurnaceXP.addDefault("COOKED_FISH", 0.35);
        cfgFurnaceXP.addDefault("GRILLED_PORK", 0.35);

        cfgFurnaceXP.addDefault("CLAY_BRICK", 0.3);

        cfgFurnaceXP.addDefault("INK_SACK 2", 0.2);
        cfgFurnaceXP.addDefault("INK_SACK 4", 0.2);
        cfgFurnaceXP.addDefault("QUARTZ", 0.2);

        cfgFurnaceXP.addDefault("COAL 1", 0.15);

        cfgFurnaceXP.addDefault("COAL 0", 0.1);
        cfgFurnaceXP.addDefault("STONE", 0.1);
        cfgFurnaceXP.addDefault("NETHER_BRICK", 0.1);
        cfgFurnaceXP.addDefault("GLASS", 0.1);

        if (!cfgFurnaceXP.isSet("DIAMOND") || !cfgFurnaceXP.isSet("EMERALD") || !cfgFurnaceXP.isSet("GOLD_INGOT") || !cfgFurnaceXP.isSet("IRON_INGOT") || !cfgFurnaceXP.isSet("REDSTONE") || !cfgFurnaceXP.isSet("HARD_CLAY")
         || !cfgFurnaceXP.isSet("BAKED_POTATO") || !cfgFurnaceXP.isSet("COOKED_BEEF") || !cfgFurnaceXP.isSet("COOKED_CHICKEN") || !cfgFurnaceXP.isSet("COOKED_FISH") || !cfgFurnaceXP.isSet("GRILLED_PORK")
         || !cfgFurnaceXP.isSet("CLAY_BRICK") || !cfgFurnaceXP.isSet("INK_SACK 2") || !cfgFurnaceXP.isSet("INK_SACK 4") || !cfgFurnaceXP.isSet("QUARTZ") || !cfgFurnaceXP.isSet("COAL 1")
         || !cfgFurnaceXP.isSet("COAL 0") || !cfgFurnaceXP.isSet("STONE") || !cfgFurnaceXP.isSet("NETHER_BRICK") || !cfgFurnaceXP.isSet("GLASS")) {

            cfgFurnaceXP.options().copyDefaults(true);
            Config.saveConfig(cfgFurnaceXP, cfgFurnaceXPFile);
        }
    }

    private static int getXP(ItemStack item) {
        double xp = 0.0;
        Material mat = item.getType();
        xp = cfgFurnaceXP.getDouble(mat + " " + item.getDurability(), 0.0);

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
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
}
