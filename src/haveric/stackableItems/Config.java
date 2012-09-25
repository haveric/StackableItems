package haveric.stackableItems;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
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
    private static String cfgDebug = "Debug";

    private static FileConfiguration cfgOptions;
    private static File cfgOptionsFile;

    private static FileConfiguration cfgFurnaces;
    private static File cfgFurnacesFile;

    //private static FileConfiguration configChest;
    //private static File configFileChest;


    // Defaults
    public static final int ITEM_DEFAULT = -1;


    private static final boolean FURNACE_USE_STACKS_DEFAULT = false;
    private static final boolean MERCHANT_USE_STACKS_DEFAULT = false;
    private static final boolean CRAFTING_USE_STACKS_DEFAULT = false;
    private static final boolean BREWING_USE_STACKS_DEFAULT = false;
    private static final boolean VIRTUAL_ITEMS_DEFAULT = false;
    private static final boolean DEBUG_DEFAULT = false;
    private static final int FURNACE_AMOUNT_DEFAULT = -1;

    private Config() { } // Private constructor for utility class

    /**
     * Initializes the config file
     * @param ss The main class used to
     */
    public static void init(StackableItems si) {
        plugin = si;
        cfgOptionsFile = new File(plugin.getDataFolder() + "/options.yml");
        cfgOptions = YamlConfiguration.loadConfiguration(cfgOptionsFile);


        cfgFurnacesFile = new File(plugin.getDataFolder() + "/data/furnaces.yml");
        cfgFurnaces = YamlConfiguration.loadConfiguration(cfgFurnacesFile);
    }

    public static void reload() {
        try {
            cfgOptions.load(cfgOptionsFile);

            cfgFurnaces.load(cfgFurnacesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the default variables if they don't exist yet.
     */
    public static void setup() {
        boolean virtualItems = cfgOptions.getBoolean(cfgVirtualItems, VIRTUAL_ITEMS_DEFAULT);

        boolean furnaceUseStacks = cfgOptions.getBoolean(cfgFurnaceUseStacks, FURNACE_USE_STACKS_DEFAULT);
        boolean merchantUseStacks = cfgOptions.getBoolean(cfgMerchantUseStacks, MERCHANT_USE_STACKS_DEFAULT);
        boolean craftingUseStacks = cfgOptions.getBoolean(cfgCraftingUseStacks, CRAFTING_USE_STACKS_DEFAULT);
        boolean brewingUseStacks = cfgOptions.getBoolean(cfgBrewingUseStacks, BREWING_USE_STACKS_DEFAULT);

        boolean debug = cfgOptions.getBoolean(cfgDebug, DEBUG_DEFAULT);

        int furnaceAmt = cfgOptions.getInt(cfgFurnaceAmount, FURNACE_AMOUNT_DEFAULT);

        // TODO Find a way to not have to set these every time, but only if they don't exist
        cfgOptions.set(cfgFurnaceUseStacks, furnaceUseStacks);
        cfgOptions.set(cfgMerchantUseStacks, merchantUseStacks);
        cfgOptions.set(cfgCraftingUseStacks, craftingUseStacks);
        cfgOptions.set(cfgBrewingUseStacks, brewingUseStacks);
        cfgOptions.set(cfgVirtualItems, virtualItems);
        cfgOptions.set(cfgFurnaceAmount, furnaceAmt);
        cfgOptions.set(cfgDebug, debug);
        saveConfig();

        if (cfgFurnacesFile.length() == 0) {
            saveConfig(cfgFurnaces, cfgFurnacesFile);
        }
    }

    /**
     * Saves the configuration to the file.
     */
    private static void saveConfig() {
        try {
            cfgOptions.save(cfgOptionsFile);
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
        return cfgOptions.getBoolean(cfgVirtualItems);
    }

    public static int getFurnaceAmount(Furnace furnace) {
        return getFurnaceAmount(furnace.getLocation());
    }

    public static int getFurnaceAmount(Location loc) {
        String world = loc.getWorld().getName();

        return cfgFurnaces.getInt(world + "." +  loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), ITEM_DEFAULT);
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
        int maxFurnaceSize = cfgOptions.getInt(cfgFurnaceAmount, ITEM_DEFAULT);
        int maxAmount = mat.getMaxStackSize();

        if (maxFurnaceSize > 64 && maxFurnaceSize <= 127) {
            maxAmount = maxFurnaceSize;
        }

        return maxAmount;
    }

    public static void setMaxFurnaceAmount(int newAmt) {
        cfgOptions.set(cfgFurnaceAmount, newAmt);

        saveConfig();
    }

    public static boolean isFurnaceUsingStacks() {
        return cfgOptions.getBoolean(cfgFurnaceUseStacks);
    }

    public static void setFurnaceUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgFurnaceUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isMerchantUsingStacks() {
        return cfgOptions.getBoolean(cfgMerchantUseStacks);
    }

    public static void setMerchantUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgMerchantUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isCraftingUsingStacks() {
        return cfgOptions.getBoolean(cfgCraftingUseStacks);
    }

    public static void setCraftingUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgCraftingUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isBrewingUsingStacks() {
        return cfgOptions.getBoolean(cfgBrewingUseStacks);
    }

    public static void setBrewingUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgBrewingUseStacks, isUsing);

        saveConfig();
    }

    public static boolean isDebugging() {
        return cfgOptions.getBoolean(cfgDebug);
    }

    public static void setDebugging(boolean isDebugging) {
        cfgOptions.set(cfgDebug, isDebugging);

        saveConfig();
    }
}
