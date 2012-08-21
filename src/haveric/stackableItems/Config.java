package haveric.stackableItems;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Config {

    private static StackableItems plugin;

    private static String cfgVirtualItems = "Virtual_Items";
    private static String cfgFurnaceAmount = "Furnace_Amount";
    private static String cfgFurnaceUseStacks = "Use_Stack_Amounts_In_Furnace";
    private static String cfgMerchantUseStacks = "Use_Stack_Amounts_In_Trading";
    private static String cfgCraftingUseStacks = "Use_Stack_Amounts_In_Crafting";
    private static String cfgBrewingUseStacks = "Use_Stack_Amounts_In_Brewing";

    private static FileConfiguration config;
    private static File configFile;

    private static FileConfiguration configFurnaces;
    private static File configFurnacesFile;

    //private static FileConfiguration configChest;
    //private static File configFileChest;


    // Defaults
    public static final int ITEM_DEFAULT = -1;


    private static final boolean FURNACE_USE_STACKS_DEFAULT = false;
    private static final boolean MERCHANT_USE_STACKS_DEFAULT = false;
    private static final boolean CRAFTING_USE_STACKS_DEFAULT = false;
    private static final boolean BREWING_USE_STACKS_DEFAULT = false;
    private static final boolean VIRTUAL_ITEMS_DEFAULT = false;
    private static final int FURNACE_AMOUNT_DEFAULT = -1;

    private Config() { } // Private constructor for utility class

    /**
     * Initializes the config file
     * @param ss The main class used to
     */
    public static void init(StackableItems si) {
        plugin = si;
        configFile = new File(plugin.getDataFolder() + "/options.yml");
        config = YamlConfiguration.loadConfiguration(configFile);


        configFurnacesFile = new File(plugin.getDataFolder() + "/data/furnaces.yml");
        configFurnaces = YamlConfiguration.loadConfiguration(configFurnacesFile);

        config.options().copyDefaults(true);
    }

    public static void reload() {
        try {
            config.load(configFile);

            configFurnaces.load(configFurnacesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the default variables if they don't exist yet.
     */
    public static void setup() {
        boolean virtualItems = config.getBoolean(cfgVirtualItems, VIRTUAL_ITEMS_DEFAULT);

        boolean furnaceUseStacks = config.getBoolean(cfgFurnaceUseStacks, FURNACE_USE_STACKS_DEFAULT);
        boolean merchantUseStacks = config.getBoolean(cfgMerchantUseStacks, MERCHANT_USE_STACKS_DEFAULT);
        boolean craftingUseStacks = config.getBoolean(cfgCraftingUseStacks, CRAFTING_USE_STACKS_DEFAULT);
        boolean brewingUseStacks = config.getBoolean(cfgCraftingUseStacks, BREWING_USE_STACKS_DEFAULT);

        int furnaceAmt = config.getInt(cfgFurnaceAmount, FURNACE_AMOUNT_DEFAULT);

        // TODO Find a way to not have to set these every time, but only if they don't exist
        config.set(cfgFurnaceUseStacks, furnaceUseStacks);
        config.set(cfgMerchantUseStacks, merchantUseStacks);
        config.set(cfgCraftingUseStacks, craftingUseStacks);
        config.set(cfgCraftingUseStacks, brewingUseStacks);
        config.set(cfgVirtualItems, virtualItems);
        config.set(cfgFurnaceAmount, furnaceAmt);
        saveConfig();

        if (configFurnacesFile.length() == 0) {
            saveConfig(configFurnaces, configFurnacesFile);
        }
    }

    /**
     * Saves the configuration to the file.
     */
    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void saveConfig(FileConfiguration fileConfig, File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isVirtualItemsEnabled() {
        return config.getBoolean(cfgVirtualItems);
    }

    public static int getFurnaceAmount(Furnace furnace) {
        return getFurnaceAmount(furnace.getLocation());
    }

    public static int getFurnaceAmount(Location loc) {
        String world = loc.getWorld().getName();

        return configFurnaces.getInt(world + "." +  loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), ITEM_DEFAULT);
    }

    public static void setFurnaceAmount(Furnace furnace, int newAmt) {
        Location loc = furnace.getLocation();
        setFurnaceAmount(loc, newAmt);
    }

    public static void setFurnaceAmount(Location loc, int newAmt) {
        String world = loc.getWorld().getName();

        configFurnaces.set(world + "." + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), newAmt);

        saveConfig(configFurnaces, configFurnacesFile);
    }

    public static void clearFurnace(Furnace furnace) {
        clearFurnace(furnace.getLocation());
    }

    public static void clearFurnace(Location loc) {
        String world = loc.getWorld().getName();

        configFurnaces.set(world + "." + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), null);

        saveConfig(configFurnaces, configFurnacesFile);
    }

    public static int getMaxFurnaceAmount() {
        return config.getInt(cfgFurnaceAmount, ITEM_DEFAULT);
    }

    public static void setMaxFurnaceAmount(int newAmt) {
        config.set(cfgFurnaceAmount, newAmt);

        saveConfig();
    }

    public static boolean isFurnaceUsingStacks() {
        return config.getBoolean(cfgFurnaceUseStacks);
    }

    public static void setFurnaceUsingStacks(boolean isUsing) {
        config.set(cfgFurnaceUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isMerchantUsingStacks() {
        return config.getBoolean(cfgMerchantUseStacks);
    }

    public static void setMerchantUsingStacks(boolean isUsing) {
        config.set(cfgMerchantUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isCraftingUsingStacks() {
        return config.getBoolean(cfgCraftingUseStacks);
    }

    public static void setCraftingUsingStacks(boolean isUsing) {
        config.set(cfgCraftingUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isBrewingUsingStacks() {
        return config.getBoolean(cfgBrewingUseStacks);
    }

    public static void setBrewingUsingStacks(boolean isUsing) {
        config.set(cfgBrewingUseStacks, isUsing);

        saveConfig();
    }
}
