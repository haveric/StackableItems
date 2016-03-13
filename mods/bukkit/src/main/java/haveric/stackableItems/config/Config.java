package haveric.stackableItems.config;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.util.SIItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Config {

    private static StackableItems plugin;

    private static String cfgFurnaceAmount = "Furnace_Amount";

    private static String cfgPreventWastedFlintSteel = "Prevent_Wasted_Flint_and_Steel";

    private static String cfgDebug = "Debug";

    private static String cfgUpdateCheck = "update-check.enabled";
    private static String cfgUpdateFrequency = "update-check.frequency";

    private static FileConfiguration cfgOptions;
    private static File cfgOptionsFile;

    private static FileConfiguration cfgFurnaces;
    private static File cfgFurnacesFile;

    private static final boolean DEBUG_DEFAULT = false;
    private static final int FURNACE_AMOUNT_DEFAULT = -1;

    private static final boolean PREVENT_WASTED_FAS_DEFAULT = true;

    private static final boolean UPDATE_CHECK_ENABLED_DEFAULT = true;
    private static final int UPDATE_CHECK_FREQUENCY_DEFAULT = 6;

    private Config() { } // Private constructor for utility class

    /**
     * Initializes the config file
     * @param ss The main class used to
     */
    public static void init(StackableItems si) {
        plugin = si;
        cfgOptionsFile = new File(plugin.getDataFolder() + File.separator + "options.yml");
        cfgOptions = YamlConfiguration.loadConfiguration(cfgOptionsFile);

        // Create the data folder if it doesn't exist yet.
        File dataFolder = new File(plugin.getDataFolder() + File.separator + "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        cfgFurnacesFile = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "furnaces.yml");
        cfgFurnaces = YamlConfiguration.loadConfiguration(cfgFurnacesFile);
        saveConfig(cfgFurnaces, cfgFurnacesFile);
    }

    public static void reload() {
        try {
            cfgOptions.load(cfgOptionsFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("options.yml not found. Creating a new one");
            saveConfig(cfgOptions, cfgOptionsFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            cfgFurnaces.load(cfgFurnacesFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("data/furnaces.yml not found. Creating a new one");
            saveConfig(cfgFurnaces, cfgFurnacesFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Sets up the default variables if they don't exist yet.
     */
    public static void setup() {
        cfgOptions.addDefault(cfgDebug, DEBUG_DEFAULT);
        cfgOptions.addDefault(cfgFurnaceAmount, FURNACE_AMOUNT_DEFAULT);
        cfgOptions.addDefault(cfgPreventWastedFlintSteel, PREVENT_WASTED_FAS_DEFAULT);
        cfgOptions.addDefault(cfgUpdateCheck, UPDATE_CHECK_ENABLED_DEFAULT);
        cfgOptions.addDefault(cfgUpdateFrequency, UPDATE_CHECK_FREQUENCY_DEFAULT);

        if (!cfgOptions.isSet(cfgDebug)
         || !cfgOptions.isSet(cfgFurnaceAmount)
         || !cfgOptions.isSet(cfgPreventWastedFlintSteel)
         || !cfgOptions.isSet(cfgUpdateCheck)
         || !cfgOptions.isSet(cfgUpdateFrequency)) {
            cfgOptions.options().copyDefaults(true);
            saveConfig(cfgOptions, cfgOptionsFile);
        }
    }

    public static void saveConfig(FileConfiguration fileConfig, File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getFurnaceAmount(Furnace furnace) {
        return getFurnaceAmount(furnace.getLocation());
    }

    public static int getFurnaceAmount(Location loc) {
        String world = loc.getWorld().getName();

        return cfgFurnaces.getInt(world + "." +  loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), SIItems.ITEM_DEFAULT);
    }

    public static void setFurnaceAmount(Furnace furnace, int newAmt) {
        Location loc = furnace.getLocation();
        setFurnaceAmount(loc, newAmt);
    }

    public static void setFurnaceAmount(Location loc, int newAmt) {
        String world = loc.getWorld().getName();

        cfgFurnaces.set(world + "." + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), newAmt);

        saveConfig(cfgFurnaces, cfgFurnacesFile);
    }

    public static void clearFurnace(Furnace furnace) {
        clearFurnace(furnace.getLocation());
    }

    public static void clearFurnace(Location loc) {
        String world = loc.getWorld().getName();

        cfgFurnaces.set(world + "." + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), null);

        saveConfig(cfgFurnaces, cfgFurnacesFile);
    }

    public static int getMaxFurnaceAmount(Material mat) {
        int maxAmount = SIItems.ITEM_DEFAULT;

        // Force air to keep default value
        if (mat != Material.AIR) {
            int maxFurnaceSize = cfgOptions.getInt(cfgFurnaceAmount, SIItems.ITEM_DEFAULT);
            maxAmount = mat.getMaxStackSize();

            if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {
                maxAmount = maxFurnaceSize;
            }
        }

        return maxAmount;
    }

    public static boolean isPreventWastedFASEnabled() {
        return cfgOptions.getBoolean(cfgPreventWastedFlintSteel);
    }

    public static boolean isDebugging() {
        return cfgOptions.getBoolean(cfgDebug);
    }

    public static boolean getUpdateCheckEnabled() {
        return cfgOptions.getBoolean(cfgUpdateCheck, UPDATE_CHECK_ENABLED_DEFAULT);
    }

    public static int getUpdateCheckFrequency() {
        return Math.max(cfgOptions.getInt(cfgUpdateFrequency, UPDATE_CHECK_FREQUENCY_DEFAULT), 0);
    }
}
