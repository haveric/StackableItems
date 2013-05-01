package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class SIItems {

    //                 player    item        num
    private static Map<String, Map<String, Integer>> itemsMap;
    //                 item        groups
    private static Map<String, ArrayList<String>> itemGroups;

    private static StackableItems plugin;

    private static FileConfiguration configItems;
    private static File configItemsFile;

    private static FileConfiguration configGroups;
    private static File configGroupsFile;

    private static FileConfiguration defaultItems;
    private static File defaultItemsFile;

    private static FileConfiguration chestItems;
    private static File chestItemsFile;

    private static String cfgMin = "MIN";
    private static String cfgMax = "MAX";

    public static final int ITEM_DEFAULT = -1;
    public static final int ITEM_INFINITE = -2;
    public static final int ITEM_DEFAULT_MAX = 64;
    public static final int ITEM_NEW_MAX = 127;

    private SIItems() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;

        configGroupsFile = new File(plugin.getDataFolder() + "/groups.yml");
        configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
        if (configGroupsFile.length() == 0) {
            Config.saveConfig(configGroups, configGroupsFile);
        }

        defaultItemsFile = new File(plugin.getDataFolder() + "/defaultItems.yml");
        defaultItems = YamlConfiguration.loadConfiguration(defaultItemsFile);
        setupDefaultItemsFile();

        chestItemsFile = new File(plugin.getDataFolder() + "/chestItems.yml");
        chestItems = YamlConfiguration.loadConfiguration(chestItemsFile);
        setupChestItemsFile();

        reload();
    }

    private static void setupDefaultItemsFile() {
        defaultItems.addDefault(cfgMin, ITEM_DEFAULT);
        defaultItems.addDefault(cfgMax, ITEM_DEFAULT);

        if (!defaultItems.isSet(cfgMin) || !defaultItems.isSet(cfgMax)) {
            defaultItems.options().copyDefaults(true);
            Config.saveConfig(defaultItems, defaultItemsFile);
        }
    }

    private static void setupChestItemsFile() {
        chestItems.addDefault(cfgMin, ITEM_DEFAULT);
        chestItems.addDefault(cfgMax, ITEM_DEFAULT);

        if (!chestItems.isSet(cfgMin) || !chestItems.isSet(cfgMax)) {
            chestItems.options().copyDefaults(true);
            Config.saveConfig(chestItems, chestItemsFile);
        }
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

        try {
            defaultItems.load(defaultItemsFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("defaultItems.yml missing. Creating a new one");
            Config.saveConfig(defaultItems, defaultItemsFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            chestItems.load(chestItemsFile);
        } catch (FileNotFoundException e) {
            plugin.log.warning("chestItems.yml missing. Creating a new one");
            Config.saveConfig(chestItems, chestItemsFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        addItemFiles("defaultItems");
        addItemFiles("chestItems");

        loadGroupItemFiles();

        loadPlayerItemFiles();
        loadItemGroups();
    }

    private static void loadGroupItemFiles() {
        String[] groups = Perms.getGroups();

        if (groups != null) {
            for (String group : groups) {
                addItemFiles(group);
            }
        }
    }

    private static void loadPlayerItemFiles() {
        Player[] players = plugin.getServer().getOnlinePlayers();

        for (Player player : players) {
            addItemFiles(player.getName());
        }
    }

    public static void addItemFiles(String groupOrPlayer) {
        if (groupOrPlayer != null) {
            configItemsFile = new File(plugin.getDataFolder() + "/" + groupOrPlayer + ".yml");
            configItems = YamlConfiguration.loadConfiguration(configItemsFile);

            if (!itemsMap.containsKey(groupOrPlayer)) {
                itemsMap.put(groupOrPlayer, new HashMap<String, Integer>());
            }

            for (String key : configItems.getKeys(false)) {
                Object temp = configItems.get(key);

                if (temp instanceof String) {
                    if (temp.equals("unlimited") || temp.equals("infinite") || temp.equals("infinity")) {
                        itemsMap.get(groupOrPlayer).put(key.toUpperCase(), ITEM_INFINITE);
                    }
                } else if (temp instanceof Integer) {
                    itemsMap.get(groupOrPlayer).put(key.toUpperCase(), configItems.getInt(key));
                }
            }
        }
    }

    public static void removeItemFiles(String groupOrPlayer) {
        if (groupOrPlayer != null && itemsMap != null) {
            if (!itemsMap.get(groupOrPlayer).isEmpty()) {
                itemsMap.get(groupOrPlayer).clear();
            }
            itemsMap.remove(groupOrPlayer);
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

    public static int getItemMax(Player player, Material mat, short dur, boolean isAChest) {
        int max = ITEM_DEFAULT;

        // Force air to keep default value
        if (mat != Material.AIR) {
            if (isAChest) {
                max = getChestMax(mat, dur);
            } else {
                max = getMax(player.getName(), mat, dur);

                if (max == ITEM_DEFAULT && Perms.canStackInGroup(player)) {
                    String group = Perms.getPrimaryGroup(player);
                    if (group != null) {
                        max = getMax(group, mat, dur);
                    }
                }

                if (max == ITEM_DEFAULT) {
                    max = getDefaultMax(mat, dur);
                }

                if (max <= ITEM_DEFAULT && max != ITEM_INFINITE) {
                    // Invalid max, count as default
                    max = ITEM_DEFAULT;
                }
            }
        }
        return max;
    }

    public static int getMax(String playerOrGroup, Material mat, short dur) {
        if (dur == ITEM_DEFAULT) {
            return getMaxFromMap(playerOrGroup, mat);
        }

        return getMaxFromMap(playerOrGroup, mat, dur);
    }

    public static int getDefaultMax(Material mat, short dur) {
        if (dur == ITEM_DEFAULT) {
            return getMaxFromMap("defaultItems", mat);
        }

        return getMaxFromMap("defaultItems", mat, dur);
    }

    public static int getChestMax(Material mat, short dur) {
        if (dur == ITEM_DEFAULT) {
            return getMaxFromMap("chestItems", mat);
        }

        return getMaxFromMap("chestItems", mat, dur);
    }

    public static void setDefaultMax(Material mat, short dur, int newAmount) {
        setMax("defaultItems", mat, dur, newAmount);
    }

    public static void setMax(String playerOrGroup, Material mat, short dur, int newAmount) {
        configItemsFile = new File(plugin.getDataFolder() + "/" + playerOrGroup + ".yml");
        configItems = YamlConfiguration.loadConfiguration(configItemsFile);

        String name;
        if (dur == ITEM_DEFAULT) {
            name = mat.name();
        } else {
            name = mat.name() + " " + dur;
        }

        configItems.set(name, newAmount);
        if (!itemsMap.containsKey(playerOrGroup)) {
            itemsMap.put(playerOrGroup, new HashMap<String, Integer>());
        }
        itemsMap.get(playerOrGroup).put(name, newAmount);

        try {
            configItems.save(configItemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getMaxFromMap(String file, Material mat, short dur) {
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

        if (itemsMap.containsKey(file)) {
            Map<String, Integer> subMap = itemsMap.get(file);

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

    private static int getMaxFromMap(String file, Material mat) {
        int max = ITEM_DEFAULT;

        List<String> groups = null;

        String matName = mat.name().toUpperCase();
        int matId = mat.getId();
        if (itemGroups.containsKey(matName)) {
            groups = itemGroups.get(matName);
        } else if (itemGroups.containsKey("" + matId)) {
            groups = itemGroups.get("" + matId);
        }

        if (itemsMap.containsKey(file)) {
            Map<String, Integer> subMap = itemsMap.get(file);

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
