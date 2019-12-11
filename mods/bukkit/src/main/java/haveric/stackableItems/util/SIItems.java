package haveric.stackableItems.util;

import haveric.stackableItems.Perms;
import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.uuidFetcher.UUIDFetcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public final class SIItems {

    //           world.player/group  item    num
    private static Map<String, Map<String, Integer>> itemsMap;
    //                 item        groups
    private static Map<String, ArrayList<String>> itemGroups;

    private static StackableItems plugin;

    private static FileConfiguration configGroups;
    private static File configGroupsFile;

    private static FileConfiguration itemsConfig;
    private static File itemsFile;

    private static String cfgMin = "MIN";
    private static String cfgMax = "MAX";
    private static String allWorlds = "allWorlds";

    public static final int ITEM_DEFAULT = -1;
    public static final int ITEM_INFINITE = -2;
    public static final int ITEM_DEFAULT_MAX = 64;
    public static final int ITEM_NEW_MAX = 127;

    private static final String FILE_VERSION = "version";
    private static final String FILE_DEFAULT_GROUPS = "defaultGroups.yml";

    private SIItems() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;

        configGroupsFile = new File(plugin.getDataFolder() + File.separator + "groups.yml");
        configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
        if (configGroupsFile.length() == 0) {
            Config.saveConfig(configGroups, configGroupsFile);
        }

        itemsFile = new File(plugin.getDataFolder() + File.separator + "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        setupItemsFile();

        boolean overwrite = isNewVersion();
        createFile(FILE_DEFAULT_GROUPS, overwrite);

        reload();
    }

    private static void setupItemsFile() {
        itemsConfig.addDefault("allWorlds.default." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.default." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.player.testPlayer." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.player.testPlayer." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.group.testGroup." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.group.testGroup." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.inventory.chest." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.inventory.chest." + cfgMax, ITEM_DEFAULT);

        itemsConfig.addDefault("testWorld.default." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("testWorld.default." + cfgMax, ITEM_DEFAULT);
        itemsConfig.options().copyDefaults(true);
        Config.saveConfig(itemsConfig, itemsFile);
    }

    public static void reload() {
        itemsMap = new HashMap<>();
        itemGroups = new HashMap<>();

        try {
            configGroups.load(configGroupsFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("groups.yml missing. Creating a new one");
            Config.saveConfig(configGroups, configGroupsFile);
        } catch (IOException | InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        loadItemsFile();

        loadItemGroups();
    }

    private static void loadItemsFile() {
        for (String worldToSplit : itemsConfig.getKeys(false)) {
            String[] worlds = worldToSplit.split(",");

            for (String world : worlds) {
                Set<String> categories = itemsConfig.getConfigurationSection(worldToSplit).getKeys(false);

                for (String category : categories) {
                    if (category.equals("default")) {
                        ConfigurationSection itemSection = itemsConfig.getConfigurationSection(worldToSplit + ".default");
                        Set<String> items = itemSection.getKeys(false);

                        for (String item: items) {
                            Object value = itemSection.get(item);
                            setItemValue(world + ".default", item, value);
                        }
                    } else if (category.equals("player")) {
                        ConfigurationSection playerSection = itemsConfig.getConfigurationSection(worldToSplit + ".player");
                        Set<String> players = playerSection.getKeys(false);

                        for (String player : players) {
                            ConfigurationSection itemSection = itemsConfig.getConfigurationSection(worldToSplit + ".player." + player);
                            Set<String> items = itemSection.getKeys(false);
                            // UUID already set
                            if (items.contains("original-name")) {
                                for (String item: items) {
                                    if (!(item.equals("original-name")) && !(item.equals("updated-name"))) {
                                        Object value = itemSection.get(item);
                                        setItemValue(world + ".player." + player, item, value);
                                    }
                                }
                            // UUID not set
                            } else {
                                UUID uuid;

                                try {
                                    uuid = UUIDFetcher.getUUIDOf(player);

                                    if (uuid != null) {
                                        playerSection.createSection("" + uuid);

                                        ConfigurationSection uuidSection = itemsConfig.getConfigurationSection(worldToSplit + ".player." + uuid);
                                        uuidSection.set("original-name", player);

                                        for (String item : items) {
                                            Object value = itemSection.get(item);
                                            uuidSection.set(item, value);
                                            setItemValue(world + ".player." + uuid, item, value);
                                        }
                                    }

                                    itemsConfig.set(world + ".player." + player, null);
                                } catch (Exception e) { }
                            }
                        }
                    } else if (category.equals("group")) {
                        ConfigurationSection groupSection = itemsConfig.getConfigurationSection(worldToSplit + ".group");
                        Set<String> groupsList = groupSection.getKeys(false);

                        for (String groupToSplit : groupsList) {
                            String[] groups = groupToSplit.split(",");

                            for (String group : groups) {
                                ConfigurationSection itemSection = itemsConfig.getConfigurationSection(worldToSplit + ".group." + groupToSplit);
                                Set<String> items = itemSection.getKeys(false);
                                for (String item: items) {
                                    Object value = itemSection.get(item);
                                    setItemValue(world + ".group." + group, item, value);
                                }
                            }
                        }
                    } else if (category.equals("inventory")) {
                        ConfigurationSection inventorySection = itemsConfig.getConfigurationSection(worldToSplit + ".inventory");
                        Set<String> inventoryList = inventorySection.getKeys(false);

                        for (String inventoryToSplit : inventoryList) {
                            String[] inventories = inventoryToSplit.split(",");

                            for (String inventory : inventories) {
                                ConfigurationSection itemSection = itemsConfig.getConfigurationSection(worldToSplit + ".inventory." + inventoryToSplit);
                                Set<String> items = itemSection.getKeys(false);
                                for (String item: items) {
                                    Object value = itemSection.get(item);
                                    setItemValue(world + ".inventory." + inventory, item, value);
                                }
                            }
                        }
                    }
                }
            }
            Config.saveConfig(itemsConfig, itemsFile);
        }
    }

    private static void setItemValue(String node, String item, Object value) {
        node = node.toUpperCase();
        if (!itemsMap.containsKey(node)) {
            itemsMap.put(node, new HashMap<>());
        }
        Map<String, Integer> itemsNode = itemsMap.get(node);

        if (item.equalsIgnoreCase("disabled")) {
            value = 1;
        }

        if (value instanceof String) {
            if (value.equals("unlimited") || value.equals("infinite") || value.equals("infinity")) {
                itemsNode.put(((String) value).toUpperCase(), ITEM_INFINITE);
            }
        } else if (value instanceof Integer) {
            itemsNode.put(item.toUpperCase(), (Integer) value);
        }
    }

    public static void updateUUIDName(String uuid, String name) {
        String search = "player." + uuid;
        for (Map.Entry<String, Map<String, Integer>> entry : itemsMap.entrySet()) {
            String key = entry.getKey();
            if (key.contains(search)) {
                String originalName = (String) itemsConfig.get(search + ".original-name");
                if (!(name.equals(originalName))) {
                    String updatedName = (String) itemsConfig.get(search + ".updated-name");

                    if (!name.equals(updatedName)) {
                        itemsConfig.set(search + ".updated-name", name);
                        Config.saveConfig(itemsConfig, itemsFile);
                    }
                }
            }
        }
    }

    private static void loadItemGroups() {
        List<String> saveList = new ArrayList<>();
        for (String key : configGroups.getKeys(false)) {
            List<String> items = configGroups.getStringList(key);
            int size = items.size();

            if (size == 0) {
                saveList.add(key);
            } else {
                saveList.add(key);

                for (String s : items) {
                    String item = s.toUpperCase();
                    if (!itemGroups.containsKey(item)) {
                        itemGroups.put(item, new ArrayList<>());
                    }
                    itemGroups.get(item).addAll(saveList);
                }
                saveList.clear();
            }
        }
    }

    public static int getInventoryMax(String world, Material mat, short dur, InventoryType inventoryType) {
        int max = ITEM_DEFAULT;

        // Force air to keep default value
        if (mat != Material.AIR) {
            if (isInventoryEnabled(world, inventoryType)) {
                // Check inventory types
                max = getMax(world, "inventory", inventoryType.name(), mat, dur);

                if (max == ITEM_DEFAULT) {
                    max = getMax(allWorlds, "inventory", inventoryType.name(), mat, dur);
                }

                // Check default
                if (max == ITEM_DEFAULT) {
                    max = getMax(world, "default", "", mat, dur);
                }
                if (max == ITEM_DEFAULT) {
                    max = getMax(allWorlds, "default", "", mat, dur);
                }

                // Handle invalid max
                if (max <= ITEM_DEFAULT && max != ITEM_INFINITE) {
                    // Invalid max, count as default
                    max = ITEM_DEFAULT;
                }
            }
        }

        return max;
    }


    public static int getItemMax(Player player, Material mat, short dur, InventoryType inventoryType) {
        String world = player.getWorld().getName();

        int max = ITEM_DEFAULT;

        // Force air to keep default value
        if (mat != Material.AIR) {
            if (isInventoryEnabled(world, inventoryType)) {
                // Check player
                String uuid = player.getUniqueId().toString();

                max = getMax(world, "player", uuid, mat, dur);

                if (max == ITEM_DEFAULT) {
                    max = getMax(allWorlds, "player", uuid, mat, dur);
                }

                // Check groups
                if (max == ITEM_DEFAULT) {
                    String[] groups = null;
                    try {
                        groups = Perms.getPlayerGroups(player);
                    } catch (Exception e) {
                        // No Groups
                        if (Config.isDebugging()) {
                            plugin.log.warning("DEBUG: getItemMax() - No group found.");
                        }
                    }

                    if (groups != null) {
                        for (String group: groups) {
                            if (max == ITEM_DEFAULT) {
                                max = getMax(world, "group", group, mat, dur);

                                if (max == ITEM_DEFAULT) {
                                    max = getMax(allWorlds, "group", group, mat, dur);
                                }
                            }
                        }
                    }
                }

                // Check inventory types
                if (max == ITEM_DEFAULT) {
                    max = getMax(world, "inventory", inventoryType.name(), mat, dur);
                }
                if (max == ITEM_DEFAULT) {
                    max = getMax(allWorlds, "inventory", inventoryType.name(), mat, dur);
                }

                // Check default
                if (max == ITEM_DEFAULT) {
                    max = getMax(world, "default", "", mat, dur);
                }
                if (max == ITEM_DEFAULT) {
                    max = getMax(allWorlds, "default", "", mat, dur);
                }

                // Handle invalid max
                if (max <= ITEM_DEFAULT && max != ITEM_INFINITE) {
                    // Invalid max, count as default
                    max = ITEM_DEFAULT;
                }
            }
        }

        return max;
    }

    public static int getMax(String world, String type, String item, Material mat, short dur) {
        String itemString = world + "." + type + "." + item;

        if (itemString.endsWith(".")) {
            itemString = itemString.substring(0, itemString.length() - 1);
        }
        if (dur == ITEM_DEFAULT) {
            return getMaxFromMap(itemString, mat);
        }

        return getMaxFromMap(itemString, mat, dur);
    }

    public static void setMax(String world, String type, String item, Material mat, short dur, int newAmount) {
        if (item != null) {
            String name;
            String matName = mat.name().toUpperCase();
            if (dur == ITEM_DEFAULT) {
                name = matName;
            } else {
                name = matName + " " + dur;
            }

            String itemString = world + "." + type + "." + item;

            if (itemString.endsWith(".")) {
                itemString = itemString.substring(0, itemString.length() - 1);
            }

            itemsConfig.set(itemString + "." + name, newAmount);
            Config.saveConfig(itemsConfig, itemsFile);
            itemString = itemString.toUpperCase();
            if (!itemsMap.containsKey(itemString)) {
                itemsMap.put(itemString, new HashMap<>());
            }
            Map<String, Integer> itemsCat = itemsMap.get(itemString);
            itemsCat.put(name.toUpperCase(), newAmount);
        }
    }

    private static int getMaxFromMap(String itemString, Material mat, short dur) {
        itemString = itemString.toUpperCase();
        int max = ITEM_DEFAULT;

        List<String> groups = null;
        String matName = mat.name().toUpperCase();
        if (itemGroups.containsKey(matName + " " + dur)) {
            groups = itemGroups.get(matName + " " + dur);
        } else if (itemGroups.containsKey(matName)) {
            groups = itemGroups.get(matName);
        }

        if (itemsMap.containsKey(itemString)) {
            Map<String, Integer> subMap = itemsMap.get(itemString);

            if (groups != null) {
                for (String group : groups) {
                    String key = group.toUpperCase();
                    if (subMap.containsKey(key)) {
                        max = subMap.get(key);
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // check for material and durability
                if (subMap.containsKey(matName + " " + dur)) {
                    max = subMap.get(matName + " " + dur);
                // material name with no durability
                } else if (subMap.containsKey(matName)) {
                    max = subMap.get(matName);
                // no individual item set, use the max and min values
                } else {
                    int defaultMax = mat.getMaxStackSize();
                    if (subMap.containsKey(cfgMin)) {
                        int temp = subMap.get(cfgMin);
                        if (temp > defaultMax && temp > ITEM_DEFAULT) {
                            max = temp;
                        }
                    }
                    if (subMap.containsKey(cfgMax)) {
                        int temp = subMap.get(cfgMax);
                        if (temp < defaultMax && temp > ITEM_DEFAULT) {
                            max = temp;
                        }
                    }
                }
            }

            // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
            if (max > SIItems.ITEM_NEW_MAX) {
                max = SIItems.ITEM_NEW_MAX;
            }
        }

        return max;
    }

    private static int getMaxFromMap(String itemString, Material mat) {
        itemString = itemString.toUpperCase();
        int max = ITEM_DEFAULT;

        List<String> groups = null;

        String matName = mat.name().toUpperCase();
        if (itemGroups.containsKey(matName)) {
            groups = itemGroups.get(matName);
        }

        if (itemsMap.containsKey(itemString)) {
            Map<String, Integer> subMap = itemsMap.get(itemString);

            if (groups != null) {
                for (String group : groups) {
                    String key = group.toUpperCase();
                    if (subMap.containsKey(key)) {
                        max = subMap.get(key);
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // material name with no durability
                if (subMap.containsKey(matName)) {
                    max = subMap.get(matName);
                 // no individual item set, use the max and min values
                } else {
                    int defaultMax = mat.getMaxStackSize();
                    if (subMap.containsKey(cfgMin)) {
                        int temp = subMap.get(cfgMin);
                        if (temp > defaultMax && temp > ITEM_DEFAULT) {
                            max = temp;
                        }
                    }
                    if (subMap.containsKey(cfgMax)) {
                        int temp = subMap.get(cfgMax);
                        if (temp < defaultMax && temp > ITEM_DEFAULT) {
                            max = temp;
                        }
                    }
                }
            }

            // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
            if (max > SIItems.ITEM_NEW_MAX) {
                max = SIItems.ITEM_NEW_MAX;
            }
        }

        return max;
    }

    public static boolean isInventoryEnabled(String worldName, Inventory inventory) {
        return isInventoryEnabled(worldName, inventory.getType());
    }

    public static boolean isInventoryEnabled(String worldName, InventoryType inventoryType) {
        boolean enabled = true;

        String worldDisabled = (worldName + ".inventory." + inventoryType).toUpperCase();
        String allWorldsDisabled = ("allWorlds.inventory." + inventoryType).toUpperCase();

        if (itemsMap.containsKey(worldDisabled)) {
            Map<String, Integer> subMap = itemsMap.get(worldDisabled);
            if (subMap.containsKey("DISABLED")) {
                enabled = false;
            }
        } else if (itemsMap.containsKey(allWorldsDisabled)) {
            Map<String, Integer> subMap = itemsMap.get(allWorldsDisabled);
            if (subMap.containsKey("DISABLED")) {
                enabled = false;
            }
        }

        return enabled;
    }

    private static boolean isNewVersion() {
        boolean newVersion = true;

        try {
            File file = new File(plugin.getDataFolder() + File.separator + FILE_VERSION);
            String currentVersion = plugin.getDescription().getVersion();

            if (file.exists()) {
                BufferedReader b = new BufferedReader(new FileReader(file));
                String version = b.readLine();
                b.close();
                newVersion = (version == null || !version.equals(currentVersion));
            }

            if (newVersion || file.exists()) {
                BufferedWriter b = new BufferedWriter(new FileWriter(file, false));
                b.write(currentVersion);
                b.close();
            }
        } catch (Throwable e) {
            plugin.log.warning("" + e.getStackTrace());
        }

        return newVersion;
    }

    private static boolean fileExists(String file, boolean overwrite) {
        if (overwrite) {
            return false;
        }

        return new File(plugin.getDataFolder() + File.separator + file).exists();
    }

    private static void createFile(String file, boolean overwrite) {
        if (fileExists(file, overwrite)) {
            return;
        }

        plugin.saveResource(file, true);
    }
}
