package haveric.stackableItems.util;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.fileWriter.CustomFileWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public final class FurnaceUtil {

    private static StackableItems plugin;

    private static final int FUEL_LIST_VERSION = 4;

    private static List<Material> listOfFuels;
    private static List<Material> furnaceBurnables;

    private FurnaceUtil() { } // Private constructor for utility class

    private static CustomFileWriter fileWriter;

    public static void init(StackableItems si) {
        plugin = si;
        fileWriter = new CustomFileWriter(plugin, "fuel");
        loadDefaultList();
        reload();
    }

    private static void loadDefaultList() {
        listOfFuels = new ArrayList<Material>();
        listOfFuels.add(Material.BLAZE_ROD);
        listOfFuels.add(Material.BOOKSHELF);
        listOfFuels.add(Material.CHEST);
        listOfFuels.add(Material.COAL);
        listOfFuels.add(Material.DAYLIGHT_DETECTOR);
        listOfFuels.add(Material.FENCE);
        listOfFuels.add(Material.FENCE_GATE);
        listOfFuels.add(Material.HUGE_MUSHROOM_1);
        listOfFuels.add(Material.HUGE_MUSHROOM_2);
        listOfFuels.add(Material.NOTE_BLOCK);
        listOfFuels.add(Material.JUKEBOX);
        listOfFuels.add(Material.LAVA_BUCKET);
        listOfFuels.add(Material.LOG);
        listOfFuels.add(Material.SAPLING);
        listOfFuels.add(Material.STICK);
        listOfFuels.add(Material.TRAP_DOOR);
        listOfFuels.add(Material.TRAPPED_CHEST);
        listOfFuels.add(Material.WOOD);
        listOfFuels.add(Material.WOOD_AXE);
        listOfFuels.add(Material.WOOD_HOE);
        listOfFuels.add(Material.WOOD_PICKAXE);
        listOfFuels.add(Material.WOOD_PLATE);
        listOfFuels.add(Material.WOOD_SPADE);
        listOfFuels.add(Material.WOOD_STAIRS);
        listOfFuels.add(Material.WOOD_STEP);
        listOfFuels.add(Material.WOOD_SWORD);
        listOfFuels.add(Material.WORKBENCH);

        try {
            listOfFuels.add(Material.ACACIA_STAIRS);
            listOfFuels.add(Material.DARK_OAK_STAIRS);
            listOfFuels.add(Material.LOG_2);
        } catch (NoSuchFieldError e) {
            plugin.log.warning("1.7 fuel items not found.");
        }

        try {
            listOfFuels.add(Material.BANNER);
        } catch (NoSuchFieldError e) {
            plugin.log.warning("1.8 fuel items not found.");
        }
    }

    public static void reload() {
        loadRecipes();

        fileWriter.reloadFiles(FUEL_LIST_VERSION, listOfFuels);
        listOfFuels = fileWriter.getMatList();
    }

    public static boolean isFuel(Material mat) {
        return listOfFuels.contains(mat);
    }

    public static void loadRecipes() {
        furnaceBurnables = new ArrayList<Material>();

        Iterator<Recipe> iter = plugin.getServer().recipeIterator();

        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof FurnaceRecipe) {
                FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;

                ItemStack item = furnaceRecipe.getInput();
                Material mat = item.getType();

                if (!furnaceBurnables.contains(mat)) {
                    furnaceBurnables.add(mat);
                }
            }
        }
    }

    public static boolean isBurnable(Material mat) {
        return furnaceBurnables.contains(mat);
    }
}
