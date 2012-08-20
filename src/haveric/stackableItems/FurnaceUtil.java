package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public final class FurnaceUtil {

    private static StackableItems plugin;

    private static final int FUEL_LIST_VERSION = 1;
    private static File defaultFuel;
    private static File customFuel;

    private static List<Material> listOfFuels;
    private static List<Material> furnaceBurnables;

    private FurnaceUtil() { } // Private constructor for utility class

    public static void init(StackableItems si) {
        plugin = si;
        reload();
    }

    public static void reload() {
        furnaceBurnables = new ArrayList<Material>();
        loadRecipes();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File lists = new File(plugin.getDataFolder() + "/lists");
        if (!lists.exists()) {
            lists.mkdir();
        }

        defaultFuel = new File(plugin.getDataFolder() + "/lists/defaultFuels.txt");
        customFuel = new File(plugin.getDataFolder() + "/lists/customFuels.txt");

        createFiles();
    }

    private static void createFiles() {
        if (!defaultFuel.exists()) {
            try {
                defaultFuel.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!customFuel.exists()) {
            try {
                customFuel.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Scanner sc = new Scanner(defaultFuel);

            if (defaultFuel.length() > 0) {
                sc.next();
                int fileVersion = sc.nextInt();
                if (fileVersion < FUEL_LIST_VERSION) {
                    defaultFuel.delete();
                    defaultFuel = new File(plugin.getDataFolder() + "/lists/defaultFuel.txt");
                    writeFuelList(defaultFuel, true);
                }
            } else {
                writeFuelList(defaultFuel, true);
            }
            sc.close();

            Scanner sc2 = new Scanner(defaultFuel);
            listOfFuels = new ArrayList<Material>();
            sc2.next();
            sc2.nextInt();
            while (sc2.hasNextLine()) {
                listOfFuels.add(Material.getMaterial(sc2.nextLine()));
            }

            sc2.close();
        } catch (FileNotFoundException e) {
            plugin.log.warning(String.format("[%s] defaultFuel.txt not found." , plugin.getDescription().getName()));
            e.printStackTrace();
        }

        try {
            Scanner sc3 = new Scanner(customFuel);

            if (customFuel.length() > 0) {
                listOfFuels = new ArrayList<Material>();
                while (sc3.hasNextLine()) {
                    listOfFuels.add(Material.getMaterial(sc3.nextLine()));
                }
            }

            sc3.close();
        } catch (FileNotFoundException e) {
            plugin.log.warning(String.format("[%s] customFuel.txt not found." , plugin.getDescription().getName()));
            e.printStackTrace();
        }
    }

    public static void writeFuelList(File f, boolean vers) {
        try {
            FileWriter fstream = new FileWriter(f);
            PrintWriter out = new PrintWriter(fstream);
            if (vers) {
                out.println("Version: " + FUEL_LIST_VERSION);
            }

            out.println("BLAZE_ROD");
            out.println("BOOKSHELF");
            out.println("CHEST");
            out.println("COAL");
            out.println("FENCE");
            out.println("HUGE_MUSHROOM_1");
            out.println("HUGE_MUSHROOM_2");
            out.println("NOTE_BLOCK");
            out.println("JUKEBOX");
            out.println("LAVA_BUCKET");
            out.println("LOCKED_CHEST");
            out.println("LOG");
            out.println("SAPLING");
            out.println("STEP");
            out.println("STICK");
            out.println("TRAP_DOOR");
            out.println("WOOD");
            out.println("WOOD_AXE");
            out.println("WOOD_HOE");
            out.println("WOOD_PICKAXE");
            out.println("WOOD_PLATE");
            out.println("WOOD_SPADE");
            out.println("WOOD_STAIRS");
            out.println("WOOD_SWORD");
            out.println("WORKBENCH");

            out.close();
            fstream.close();
        } catch (IOException e) {
            plugin.log.warning(String.format("[%s] File %s not found." , plugin.getDescription().getName(), f.getName()));
        }
    }

    public static boolean isFuel(Material mat) {
        return listOfFuels.contains(mat);
    }

    public static void loadRecipes() {
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
