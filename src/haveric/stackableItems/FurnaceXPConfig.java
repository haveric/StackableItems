package haveric.stackableItems;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class FurnaceXPConfig {

    private static StackableItems plugin;

    private static FileConfiguration cfgFurnaceXP;
    private static File cfgFurnaceXPFile;

    private FurnaceXPConfig() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;
        cfgFurnaceXPFile = new File(plugin.getDataFolder() + "/furnaceXP.yml");
        cfgFurnaceXP = YamlConfiguration.loadConfiguration(cfgFurnaceXPFile);
    }

    public static void reload() {
        try {
            cfgFurnaceXP.load(cfgFurnaceXPFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setup() {
        cfgFurnaceXP.addDefault("DIAMOND", 1.0);
        cfgFurnaceXP.addDefault("EMERALD", 1.0);
        cfgFurnaceXP.addDefault("GOLD_INGOT", 1.0);

        cfgFurnaceXP.addDefault("IRON_INGOT", 0.7);
        cfgFurnaceXP.addDefault("REDSTONE", 0.7);

        cfgFurnaceXP.addDefault("BAKED_POTATO", 0.35);
        cfgFurnaceXP.addDefault("COOKED_BEEF", 0.35);
        cfgFurnaceXP.addDefault("COOKED_CHICKEN", 0.35);
        cfgFurnaceXP.addDefault("COOKED_FISH", 0.35);
        cfgFurnaceXP.addDefault("GRILLED_PORK", 0.35);

        cfgFurnaceXP.addDefault("CLAY_BRICK", 0.3);

        cfgFurnaceXP.addDefault("INK_SACK 2", 0.2);
        cfgFurnaceXP.addDefault("INK_SACK 4", 0.2);
        cfgFurnaceXP.addDefault("NETHER_QUARTZ", 0.2);

        cfgFurnaceXP.addDefault("COAL 1", 0.15);

        cfgFurnaceXP.addDefault("COAL 0", 0.1);
        cfgFurnaceXP.addDefault("STONE", 0.1);
        cfgFurnaceXP.addDefault("NETHER_BRICK", 0.1);
        cfgFurnaceXP.addDefault("GLASS", 0.1);

        if (!cfgFurnaceXP.isSet("DIAMOND") || !cfgFurnaceXP.isSet("EMERALD") || !cfgFurnaceXP.isSet("GOLD_INGOT") || !cfgFurnaceXP.isSet("IRON_INGOT") || !cfgFurnaceXP.isSet("REDSTONE")
         || !cfgFurnaceXP.isSet("BAKED_POTATO") || !cfgFurnaceXP.isSet("COOKED_BEEF") || !cfgFurnaceXP.isSet("COOKED_CHICKEN") || !cfgFurnaceXP.isSet("COOKED_FISH") || !cfgFurnaceXP.isSet("GRILLED_PORK")
         || !cfgFurnaceXP.isSet("CLAY_BRICK") || !cfgFurnaceXP.isSet("INK_SACK 2") || !cfgFurnaceXP.isSet("INK_SACK 4") || !cfgFurnaceXP.isSet("COAL 1")
         || !cfgFurnaceXP.isSet("COAL 0") || !cfgFurnaceXP.isSet("STONE") || !cfgFurnaceXP.isSet("NETHER_BRICK") || !cfgFurnaceXP.isSet("GLASS")) {

            cfgFurnaceXP.options().copyDefaults(true);
            Config.saveConfig(cfgFurnaceXP, cfgFurnaceXPFile);
        }
    }

    public static int getXP(ItemStack item) {
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
}
