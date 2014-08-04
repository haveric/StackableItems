package haveric.stackableItems.util;

import haveric.stackableItems.Perms;
import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.uuidFetcher.UUIDFetcher;

import java.io.File;
import java.io.FileNotFoundException;
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
        itemsMap = new HashMap<String, Map<String, Integer>>();
        itemGroups = new HashMap<String, ArrayList<String>>();

        try {
            configGroups.load(configGroupsFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("groups.yml missing. Creating a new one");
            Config.saveConfig(configGroups, configGroupsFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        loadItemsFile();

        loadItemGroups();
    }

    private static void loadItemsFile() {
        for (String world : itemsConfig.getKeys(false)) {
            Set<String> categories = itemsConfig.getConfigurationSection(world).getKeys(false);

            for (String category : categories) {
                if (category.equals("default")) {
                    ConfigurationSection itemSection = itemsConfig.getConfigurationSection(world + ".default");
                    Set<String> items = itemSection.getKeys(false);
                    for (String item: items) {
                        Object value = itemSection.get(item);
                        setItemValue(world + ".default", item, value);
                    }

                } else if (category.equals("player")) {
                    ConfigurationSection playerSection = itemsConfig.getConfigurationSection(world + ".player");
                    Set<String> players = playerSection.getKeys(false);

                    for (String player : players) {
                        ConfigurationSection itemSection = itemsConfig.getConfigurationSection(world + ".player." + player);
                        Set<String> items = itemSection.getKeys(false);
                        // UUID already set
                        if (items.contains("original-name")) {
                            for (String item: items) {
                                if (item != "original-name" && item != "updated-name") {
                                    Object value = itemSection.get(item);
                                    setItemValue(world + ".player." + player, item, value);
                                }
                            }
                        // UUID not set
                        } else {
                            UUID uuid;

                            try {
                                uuid = UUIDFetcher.getUUIDOf(player);

                                if (uuid == null) {
                                    itemsConfig.set(world + ".player." + player, null);
                                } else {
                                    playerSection.createSection("" + uuid);

                                    ConfigurationSection uuidSection = itemsConfig.getConfigurationSection(world + ".player." + uuid);
                                    uuidSection.set("original-name", player);

                                    for (String item: items) {
                                        Object value = itemSection.get(item);
                                        uuidSection.set(item, value);
                                        setItemValue(world + ".player." + uuid, item, value);
                                    }
                                    itemsConfig.set(world + ".player." + player, null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (category.equals("group")) {
                    ConfigurationSection groupSection = itemsConfig.getConfigurationSection(world + ".group");
                    Set<String> groups = groupSection.getKeys(false);

                    for (String group : groups) {
                        ConfigurationSection itemSection = itemsConfig.getConfigurationSection(world + ".group." + group);
                        Set<String> items = itemSection.getKeys(false);
                        for (String item: items) {
                            Object value = itemSection.get(item);
                            setItemValue(world + ".group." + group, item, value);
                        }
                    }

                } else if (category.equals("inventory")) {
                    ConfigurationSection inventorySection = itemsConfig.getConfigurationSection(world + ".inventory");
                    Set<String> inventories = inventorySection.getKeys(false);
                    for (String inventory : inventories) {
                        ConfigurationSection itemSection = itemsConfig.getConfigurationSection(world + ".inventory." + inventory);
                        Set<String> items = itemSection.getKeys(false);
                        for (String item: items) {
                            Object value = itemSection.get(item);
                            setItemValue(world + ".inventory." + inventory, item, value);
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
            itemsMap.put(node, new HashMap<String, Integer>());
        }
        Map<String, Integer> itemsNode = itemsMap.get(node);

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
                if (name != originalName) {
                    String updatedName = (String) itemsConfig.get(search + ".updated-name");

                    if (updatedName == null || name != updatedName) {
                        itemsConfig.set(search + ".updated-name", name);
                        Config.saveConfig(itemsConfig, itemsFile);
                    }
                }
            }
        }
    }

    private static void loadItemGroups() {
        List<String> saveList = new ArrayList<String>();
        for (String key : configGroups.getKeys(false)) {
            List<String> items = configGroups.getStringList(key);
            int size = items.size();

            if (size == 0) {
                saveList.add(key);
            } else {
                saveList.add(key);

                for (int i = 0; i < size; i++) {
                    String item = items.get(i).toUpperCase();
                    if (!itemGroups.containsKey(item)) {
                        itemGroups.put(item, new ArrayList<String>());
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

        return max;
    }


    public static int getItemMax(Player player, Material mat, short dur, InventoryType inventoryType) {
        String world = player.getWorld().getName();

        int max = ITEM_DEFAULT;

        // Force air to keep default value
        if (mat != Material.AIR) {
            // Check player
            String uuid = player.getUniqueId().toString();

            max = getMax(world, "player", uuid, mat, dur);

            if (max == ITEM_DEFAULT) {
                max = getMax(allWorlds, "player", uuid, mat, dur);
            }

            // Check groups
            if (max == ITEM_DEFAULT) {
                String groups[] = null;
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
        return max;
    }

    public static int getMax(String world, String type, String item, Material mat, short dur) {
        String itemString = world + "." + type + "." + item;

        if (itemString.endsWith(".")) {
            itemString = itemString.substring(0, itemString.length()-1);
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
                itemString = itemString.substring(0, itemString.length()-1);
            }

            itemsConfig.set(itemString + "." + name, newAmount);
            Config.saveConfig(itemsConfig, itemsFile);
            itemString = itemString.toUpperCase();
            if (!itemsMap.containsKey(itemString)) {
                itemsMap.put(itemString, new HashMap<String, Integer>());
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
        int matId = mat.getId();
        if (itemGroups.containsKey(matName + " " + dur)) {
            groups = itemGroups.get(matName + " " + dur);
        } else if (itemGroups.containsKey(matId + " " + dur)) {
            groups = itemGroups.get(matId + " " + dur);
        } else if (itemGroups.containsKey(matName)) {
            groups = itemGroups.get(matName);
        } else if (itemGroups.containsKey("" + matId)) {
            groups = itemGroups.get("" + matId);
        }

        if (itemsMap.containsKey(itemString)) {
            Map<String, Integer> subMap = itemsMap.get(itemString);

            if (groups != null) {
                int groupSize = groups.size();
                for (int i = 0; i < groupSize; i++) {
                    String key = groups.get(i).toUpperCase();
                    if (subMap.containsKey(key)) {
                        max = subMap.get(key);
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // check for material and durability
                if (subMap.containsKey(matName + " " + dur)) {
                    max = subMap.get(matName + " " + dur);
                // check for item id and durability
                } else if (subMap.containsKey(matId + " " + dur)) {
                    max = subMap.get(matId + " " + dur);
                // material name with no durability
                } else if (subMap.containsKey(matName)) {
                    max = subMap.get(matName);
                // item id with no durability
                } else if (subMap.containsKey("" + matId)) {
                    max = subMap.get("" + matId);
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
        int matId = mat.getId();
        if (itemGroups.containsKey(matName)) {
            groups = itemGroups.get(matName);
        } else if (itemGroups.containsKey("" + matId)) {
            groups = itemGroups.get("" + matId);
        }

        if (itemsMap.containsKey(itemString)) {
            Map<String, Integer> subMap = itemsMap.get(itemString);

            if (groups != null) {
                int groupSize = groups.size();
                for (int i = 0; i < groupSize; i++) {
                    String key = groups.get(i).toUpperCase();
                    if (subMap.containsKey(key)) {
                        max = subMap.get(key);
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // material name with no durability
                if (subMap.containsKey(matName)) {
                    max = subMap.get(matName);
                // item id with no durability
                } else if (subMap.containsKey("" + matId)) {
                    max = subMap.get("" + matId);
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
}
