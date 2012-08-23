package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class SIItems {

    //                     player    item            num
    private static HashMap<String, HashMap<String, Integer>> itemsMap;
    //                     item        groups
    private static HashMap<String, ArrayList<String>> itemGroups;

    private static StackableItems plugin;

    private static FileConfiguration configItems;
    private static File configItemsFile;

    private static FileConfiguration configGroups;
    private static File configGroupsFile;

    private static FileConfiguration defaultItems;
    private static File defaultItemsFile;

    private static String cfgAllItemsMax = "ALL ITEMS MAX";

    public static final int ITEM_DEFAULT = -1;

    private SIItems() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;

        configGroupsFile = new File(plugin.getDataFolder() + "/groups.yml");
        configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
        if (configGroupsFile.length() == 0) {
            Config.saveConfig(configGroups, configGroupsFile);
        }

        defaultItemsFile = new File(plugin.getDataFolder() + "/defaultItems.yml");
        defaultItems = YamlConfiguration.loadConfiguration(configGroupsFile);

        setupDefaultItemsFile();

        reload();
    }

    private static void setupDefaultItemsFile() {
        if (defaultItemsFile.length() == 0) {
            defaultItems.set(cfgAllItemsMax, ITEM_DEFAULT);
            Config.saveConfig(defaultItems, defaultItemsFile);
        }
    }

    public static void reload() {
        itemsMap = new HashMap<String, HashMap<String, Integer>>();
        itemGroups = new HashMap<String, ArrayList<String>>();

        try {
            configGroups.load(configGroupsFile);

            defaultItems.load(defaultItemsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        addItemFiles("defaultItems");
        if (Perms.permEnabled()) {
            loadGroupItemFiles();
        }
        loadPlayerItemFiles();
        loadItemGroups();
    }

    private static void loadGroupItemFiles() {
        String[] groups = Perms.getPerm().getGroups();

        for (String group : groups) {
            addItemFiles(group);
        }
    }

    private static void loadPlayerItemFiles() {
        Player[] players = plugin.getServer().getOnlinePlayers();

        for (Player player : players) {
            addItemFiles(player.getName());
        }
    }

    public static void addItemFiles(String groupOrPlayer) {
        if (groupOrPlayer == null) {
            if (Config.isDebugging()) plugin.log.warning("[StackableItems][DEBUG] Add Item Files: Group or Player is null.");
        } else {
            configItemsFile = new File(plugin.getDataFolder() + "/" + groupOrPlayer + ".yml");
            configItems = YamlConfiguration.loadConfiguration(configItemsFile);
            if (!itemsMap.containsKey(groupOrPlayer)) {
                itemsMap.put(groupOrPlayer, new HashMap<String, Integer>());
            }
            for (String key : configItems.getKeys(false)) {
                itemsMap.get(groupOrPlayer).put(key.toUpperCase(), configItems.getInt(key));
            }
        }
    }

    public static void removeItemFiles(String groupOrPlayer) {
        if (groupOrPlayer == null) {
            if (Config.isDebugging()) plugin.log.warning("[StackableItems][DEBUG] Remove Item Files: Group or Player is null.");
        } else if (itemsMap == null) {
            if (Config.isDebugging()) plugin.log.warning("[StackableItems][DEBUG] Items Map is null. This should never be null.");
        } else {
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

                for (int i = 0; i < items.size(); i++) {
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

    public static int getItemMax(Player player, Material mat, short dur) {
        int max = ITEM_DEFAULT;

        max = getMax(player.getName(), mat, dur);
        if (max == ITEM_DEFAULT && Perms.permEnabled() && Perms.getPerm().has(player, Perms.getStackString())) {
            String group = Perms.getPerm().getPrimaryGroup(player);
            max = getMax(group, mat, dur);
        }
        if (max == ITEM_DEFAULT) {
            max = getDefaultMax(mat, dur);
        }

        if (max <= ITEM_DEFAULT) {
            max = mat.getMaxStackSize();
        }
        return max;
    }

    public static int getMax(String playerOrGroup, Material mat, short dur) {
        if (dur == -1) {
            return getMaxFromMap(playerOrGroup, mat);
        }

        return getMaxFromMap(playerOrGroup, mat, dur);
    }

    public static int getDefaultMax(Material mat, short dur) {
        if (dur == -1) {
            return getMaxFromMap("defaultItems", mat);
        }

        return getMaxFromMap("defaultItems", mat, dur);
    }

    public static void setDefaultMax(Material mat, short dur, int newAmount) {
        setMax("defaultItems", mat, dur, newAmount);
    }

    public static void setMax(String playerOrGroup, Material mat, short dur, int newAmount) {
        configItemsFile = new File(plugin.getDataFolder() + "/" + playerOrGroup + ".yml");
        configItems = YamlConfiguration.loadConfiguration(configItemsFile);

        String name;
        if (dur == -1) {
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
        if (itemGroups.containsKey(mat.name() + " " + dur)) {
            groups = itemGroups.get(mat.name() + " " + dur);
        } else if (itemGroups.containsKey(mat.getId() + " " + dur)) {
            groups = itemGroups.get(mat.getId() + " " + dur);
        } else if (itemGroups.containsKey(mat.name())) {
            groups = itemGroups.get(mat.name());
        } else if (itemGroups.containsKey("" + mat.getId())) {
            groups = itemGroups.get("" + mat.getId());
        }

        if (itemsMap.containsKey(file)) {
            HashMap<String, Integer> subMap = itemsMap.get(file);
            if (groups != null) {
                for (int i = 0; i < groups.size(); i++) {
                    if (subMap.containsKey(groups.get(i).toUpperCase())) {
                        max = subMap.get(groups.get(i).toUpperCase());
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // check for material and durability
                if (subMap.containsKey(mat.name() + " " + dur)) {
                    max = subMap.get(mat.name() + " " + dur);
                // check for item id and durability
                } else if (subMap.containsKey(mat.getId() + " " + dur)) {
                    max = subMap.get(mat.getId() + " " + dur);
                // material name with no durability
                } else if (subMap.containsKey(mat.name())) {
                    max = subMap.get(mat.name());
                // item id with no durability
                } else if (subMap.containsKey("" + mat.getId())) {
                    max = subMap.get("" + mat.getId());
                // no individual item set, use the 'all items' value
                } else if (subMap.containsKey(cfgAllItemsMax)) {
                    max = subMap.get(cfgAllItemsMax);
                }
            }

            // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
            if (max > 127) {
                max = 127;
            }
        }

        return max;
    }

    private static int getMaxFromMap(String file, Material mat) {
        int max = ITEM_DEFAULT;

        List<String> groups = null;

        if (itemGroups.containsKey(mat.name())) {
            groups = itemGroups.get(mat.name());
        } else if (itemGroups.containsKey("" + mat.getId())) {
            groups = itemGroups.get("" + mat.getId());
        }

        if (itemsMap.containsKey(file)) {
            HashMap<String, Integer> subMap = itemsMap.get(file);
            if (groups != null) {
                for (int i = 0; i < groups.size(); i++) {
                    if (subMap.containsKey(groups.get(i).toUpperCase())) {
                        max = subMap.get(groups.get(i).toUpperCase());
                    }
                }
            }

            if (max == ITEM_DEFAULT) {
                // material name with no durability
                if (subMap.containsKey(mat.name())) {
                    max = subMap.get(mat.name());
                // item id with no durability
                } else if (subMap.containsKey("" + mat.getId())) {
                    max = subMap.get("" + mat.getId());
                // no individual item set, use the 'all items' value
                } else if (subMap.containsKey(cfgAllItemsMax)) {
                    max = subMap.get(cfgAllItemsMax);
                }
            }

            // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
            if (max > 127) {
                max = 127;
            }
        }

        return max;
    }
}
