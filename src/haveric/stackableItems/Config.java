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

    private static String cfgFurnaceAmount = "Furnace_Amount";

    private static String cfgUseStacksFurnace = "Use_Stack_Amounts_In_Furnace";
    private static String cfgUseStacksMerchant = "Use_Stack_Amounts_In_Trading";
    private static String cfgUseStacksCrafting = "Use_Stack_Amounts_In_Crafting";
    private static String cfgUseStacksBrewing = "Use_Stack_Amounts_In_Brewing";
    private static String cfgUseStacksAnvil = "Use_Stack_Amounts_In_Anvil";
    private static String cfgUseStacksBeacon = "Use_Stack_Amounts_In_Beacon";
    private static String cfgUseStacksEnderChest = "Use_Stack_Amounts_In_EnderChest";
    private static String cfgUseStacksHopper = "Use_Stack_Amounts_In_Hopper";
    private static String cfgUseStacksDropper = "Use_Stack_Amounts_In_Dropper";
    private static String cfgUseStacksDispenser = "Use_Stack_Amounts_In_Dispenser";

    private static String cfgDebug = "Debug";

    private static FileConfiguration cfgOptions;
    private static File cfgOptionsFile;

    private static FileConfiguration cfgFurnaces;
    private static File cfgFurnacesFile;

    private static final boolean USE_STACKS_DEFAULT = true;

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
        cfgOptions.addDefault(cfgUseStacksFurnace, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksMerchant, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksCrafting, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksBrewing, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksAnvil, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksBeacon, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksEnderChest, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksHopper, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksDropper, USE_STACKS_DEFAULT);
        cfgOptions.addDefault(cfgUseStacksDispenser, USE_STACKS_DEFAULT);

        cfgOptions.addDefault(cfgDebug, DEBUG_DEFAULT);
        cfgOptions.addDefault(cfgFurnaceAmount, FURNACE_AMOUNT_DEFAULT);

        if (!cfgOptions.isSet(cfgUseStacksFurnace) || !cfgOptions.isSet(cfgUseStacksMerchant) || !cfgOptions.isSet(cfgUseStacksCrafting)
                || !cfgOptions.isSet(cfgUseStacksBrewing) || !cfgOptions.isSet(cfgUseStacksAnvil) || !cfgOptions.isSet(cfgUseStacksBeacon) || !cfgOptions.isSet(cfgUseStacksEnderChest)
                || !cfgOptions.isSet(cfgUseStacksHopper) || !cfgOptions.isSet(cfgUseStacksDropper) || !cfgOptions.isSet(cfgUseStacksDispenser) || !cfgOptions.isSet(cfgDebug) || !cfgOptions.isSet(cfgFurnaceAmount)) {
            cfgOptions.options().copyDefaults(true);
            saveConfig(cfgOptions, cfgOptionsFile);
        }
    }

    /**
     * Saves the configuration to the file.
     */
    /* TODO: Only used for setters, add back in when they are
    private static void saveConfig() {
        try {
            cfgOptions.save(cfgOptionsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

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

    public static boolean isFurnaceUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksFurnace);
    }

    public static boolean isMerchantUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksMerchant);
    }

    public static boolean isCraftingUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksCrafting);
    }

    public static boolean isBrewingUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksBrewing);
    }

    public static boolean isAnvilUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksAnvil);
    }

    public static boolean isBeaconUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksBeacon);
    }

    public static boolean isEnderChestUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksEnderChest);
    }

    public static boolean isHopperUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksHopper);
    }

    public static boolean isDropperUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksDropper);
    }

    public static boolean isDispenserUsingStacks() {
        return cfgOptions.getBoolean(cfgUseStacksDispenser);
    }

    public static boolean isDebugging() {
        return cfgOptions.getBoolean(cfgDebug);
    }

    /* TODO: Implement in game commands to use setters
    public static void setMaxFurnaceAmount(int newAmt) {
        cfgOptions.set(cfgFurnaceAmount, newAmt);
        saveConfig();
    }

    public static void setFurnaceUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksFurnace, isUsing);
        saveConfig();
    }

    public static void setMerchantUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksMerchant, isUsing);
        saveConfig();
    }

    public static void setCraftingUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksCrafting, isUsing);
        saveConfig();
    }

    public static void setBrewingUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksBrewing, isUsing);
        saveConfig();
    }

    public static void setAnvilUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksAnvil, isUsing);
        saveConfig();
    }

    public static void setBeaconUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksBeacon, isUsing);
        saveConfig();
    }

    public static void setEnderChestUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksEnderChest, isUsing);
        saveConfig();
    }

    public static void setHopperUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksHopper, isUsing);
        saveConfig();
    }

    public static void setDropperUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksDropper, isUsing);
        saveConfig();
    }

    public static void setDispenserUsingStacks(boolean isUsing) {
        cfgOptions.set(cfgUseStacksDispenser, isUsing);
        saveConfig();
    }

    public static void setDebugging(boolean isDebugging) {
        cfgOptions.set(cfgDebug, isDebugging);
        saveConfig();
    }
     */
}
